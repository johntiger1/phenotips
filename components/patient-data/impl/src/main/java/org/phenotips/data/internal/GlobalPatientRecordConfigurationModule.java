/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 */
package org.phenotips.data.internal;

import org.phenotips.configuration.RecordConfiguration;
import org.phenotips.configuration.RecordConfigurationModule;
import org.phenotips.configuration.RecordSection;
import org.phenotips.configuration.internal.DefaultRecordConfiguration;
import org.phenotips.configuration.internal.DefaultRecordSection;

import org.xwiki.uiextension.UIExtension;
import org.xwiki.uiextension.UIExtensionFilter;
import org.xwiki.uiextension.UIExtensionManager;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.lang3.ArrayUtils;

import com.xpn.xwiki.XWikiContext;

/**
 * Default (global) implementation of the {@link RecordConfiguration} role for patient records. Its
 * {@link #getPriority() priority} is {@code 0}.
 *
 * @version $Id$
 * @since 1.3RC1
 */
public class GlobalPatientRecordConfigurationModule implements RecordConfigurationModule
{
    private static final String PATIENT_TYPE_LABEL = "patient";

    private static final String[] SUPPORTED_RECORD_TYPES = new String[] { PATIENT_TYPE_LABEL };

    /** The name of the UIX parameter used for specifying the order of fields and sections. */
    private static final String SORT_PARAMETER_NAME = "order";

    /** Lists the patient form sections and fields. */
    @Inject
    protected UIExtensionManager uixManager;

    /** Sorts fields by their declared order. */
    @Inject
    @Named("sortByParameter")
    protected UIExtensionFilter orderFilter;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Override
    public RecordConfiguration process(RecordConfiguration config)
    {
        List<RecordSection> result = new LinkedList<>();
        RecordConfiguration updatedConfig = new DefaultRecordConfiguration(this.xcontextProvider);
        List<UIExtension> sections = this.uixManager.get("org.phenotips.patientSheet.content");
        sections = this.orderFilter.filter(sections, SORT_PARAMETER_NAME);
        for (UIExtension sectionExtension : sections) {
            RecordSection section = new DefaultRecordSection(sectionExtension, this.uixManager, this.orderFilter);
            result.add(section);
        }
        updatedConfig.setSections(result);
        return updatedConfig;
    }

    @Override
    public int getPriority()
    {
        return 0;
    }

    @Override
    public String[] getSupportedRecordTypes()
    {
        return SUPPORTED_RECORD_TYPES;
    }

    @Override
    public boolean supportsRecordType(String recordType)
    {
        return ArrayUtils.contains(getSupportedRecordTypes(), recordType);
    }
}
