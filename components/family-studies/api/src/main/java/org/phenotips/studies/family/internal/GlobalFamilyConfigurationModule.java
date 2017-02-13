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
package org.phenotips.studies.family.internal;

import org.phenotips.configuration.RecordConfiguration;
import org.phenotips.configuration.RecordConfigurationModule;
import org.phenotips.configuration.RecordSection;
import org.phenotips.configuration.internal.DefaultRecordConfiguration;
import org.phenotips.configuration.internal.DefaultRecordSection;

import org.xwiki.uiextension.UIExtension;
import org.xwiki.uiextension.UIExtensionFilter;
import org.xwiki.uiextension.UIExtensionManager;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.lang3.ArrayUtils;

import com.xpn.xwiki.XWikiContext;

/**
 * Default (global) implementation of the {@link RecordConfiguration} role for family records. This module has the
 * top {@link #getPriority() priority}, that is {@code 0}.
 *
 * @version $Id $
 * @since 1.3RC1
 */
@Named("Family")
public class GlobalFamilyConfigurationModule implements RecordConfigurationModule
{
    private static final String FAMILY_TYPE_LABEL = "family";

    private static final String[] SUPPORTED_RECORD_TYPES = new String[] { FAMILY_TYPE_LABEL };

    /** The name of the UIX parameter used for specifying the order of fields and sections. */
    private static final String SORT_PARAMETER_NAME = "order";

    /** Lists the patient form sections and fields. */
    @Inject
    private UIExtensionManager uixManager;

    /** Sorts fields by their declared order. */
    @Inject
    @Named("sortByParameter")
    private UIExtensionFilter orderFilter;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Override
    public RecordConfiguration process(@Nullable final RecordConfiguration config)
    {
        // The "config" parameter is ignored, since this is the base configuration.
        final List<UIExtension> sectionUIExtensions = getOrderedSectionUIExtensions();
        final List<RecordSection> recordSections = new LinkedList<>();
        for (final UIExtension uiExtension : sectionUIExtensions) {
            final RecordSection section = new DefaultRecordSection(uiExtension, this.uixManager, this.orderFilter);
            recordSections.add(section);
        }

        final RecordConfiguration updatedConfig = new DefaultRecordConfiguration(this.xcontextProvider);
        updatedConfig.setSections(Collections.unmodifiableList(recordSections));
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
    public boolean supportsRecordType(final String recordType)
    {
        return ArrayUtils.contains(getSupportedRecordTypes(), recordType);
    }

    /**
     * Returns all the {@link UIExtension} sections for the default family sheet, and sorts them in preferred order.
     *
     * @return a list of sorted {@link UIExtension family sheet section objects}.
     */
    private List<UIExtension> getOrderedSectionUIExtensions()
    {
        final List<UIExtension> sections = this.uixManager.get("phenotips.familyRecord.content");
        return this.orderFilter.filter(sections, SORT_PARAMETER_NAME);
    }
}
