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
package org.phenotips.configuration.internal;

import org.phenotips.components.ComponentManagerRegistry;
import org.phenotips.configuration.RecordConfiguration;
import org.phenotips.configuration.RecordElement;
import org.phenotips.configuration.RecordSection;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.ClassPropertyReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Provider;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Default (global) implementation of the {@link RecordConfiguration} role.
 *
 * @version $Id$
 * @since 1.0M9
 */
public class DefaultRecordConfiguration implements RecordConfiguration
{
    /** The location where preferences are stored. */
    private static final EntityReference PREFERENCES_LOCATION = new EntityReference("WebHome", EntityType.DOCUMENT,
        new EntityReference("data", EntityType.SPACE));

    /** The name of the class storing default phenotype mapping. */
    private static final String PHENOTYPE_MAPPING_CLASSNAME = "PhenoTips.PhenotypeMapping";

    /** The phenotype mapping label. */
    private static final String PHENOTYPE_MAPPING_LABEL = "phenotypeMapping";

    /** Date of birth format label. */
    private static final String DOB_FORMAT_LABEL = "dateOfBirthFormat";

    /** "current" label. */
    private static final String CURRENT_LABEL = "current";

    /** Default format for dates. */
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /** Provides access to the current request context. */
    private Provider<XWikiContext> xcontextProvider;

    /** Logging helper object. */
    private Logger logger = LoggerFactory.getLogger(DefaultRecordConfiguration.class);

    /** List of all Record Sections. */
    private List<RecordSection> sections;

    /**
     * The default constructor that takes an {@code xcontextProvider XWiki context provider object}.
     *
     * @param xcontextProvider an {@link Provider<XWikiContext>} object
     */
    public DefaultRecordConfiguration(final Provider<XWikiContext> xcontextProvider)
    {
        this.xcontextProvider = xcontextProvider;
    }

    @Override
    public List<RecordSection> getAllSections()
    {
        return CollectionUtils.isNotEmpty(this.sections)
            ? Collections.unmodifiableList(this.sections)
            : Collections.<RecordSection>emptyList();
    }

    @Override
    public List<RecordSection> getEnabledSections()
    {
        List<RecordSection> result = new LinkedList<>();
        for (RecordSection section : getAllSections()) {
            if (section.isEnabled()) {
                result.add(section);
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public void setSections(List<RecordSection> sections)
    {
        this.sections = sections;
    }

    @Override
    public List<ClassPropertyReference> getEnabledFields()
    {
        final List<ClassPropertyReference> enabledFields = new LinkedList<>();
        for (final RecordSection section : getEnabledSections()) {
            for (final RecordElement element : section.getEnabledElements()) {
                enabledFields.addAll(element.getFields());
            }
        }
        return Collections.unmodifiableList(enabledFields);
    }

    @Override
    public List<String> getEnabledFieldNames()
    {
        final List<String> enabledFieldNames = new LinkedList<>();
        for (final ClassPropertyReference field : getEnabledFields()) {
            enabledFieldNames.add(field.getName());
        }
        return Collections.unmodifiableList(enabledFieldNames);
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<String> getEnabledNonIdentifiableFieldNames()
    {
        List<String> result = new LinkedList<>();
        for (RecordSection section : getEnabledSections()) {
            for (RecordElement element : section.getEnabledElements()) {
                if (!element.containsPrivateIdentifiableInformation()) {
                    result.addAll(element.getDisplayedFields());
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<ClassPropertyReference> getAllFields()
    {
        final List<ClassPropertyReference> fields = new LinkedList<>();
        for (final RecordSection section : getAllSections()) {
            for (final RecordElement element : section.getAllElements()) {
                fields.addAll(element.getFields());
            }
        }
        return Collections.unmodifiableList(fields);
    }

    @Override
    public List<String> getAllFieldNames()
    {
        final List<String> fieldNames = new LinkedList<>();
        for (final ClassPropertyReference field : getAllFields()) {
            fieldNames.add(field.getName());
        }
        return Collections.unmodifiableList(fieldNames);
    }

    @Override
    public DocumentReference getPhenotypeMapping()
    {
        BaseObject settings = getGlobalConfigurationObject();
        if (settings == null) {
            return null;
        }
        final String mapping = StringUtils.defaultIfBlank(settings.getStringValue(PHENOTYPE_MAPPING_LABEL),
            PHENOTYPE_MAPPING_CLASSNAME);
        try {
            DocumentReferenceResolver<String> resolver = ComponentManagerRegistry.getContextComponentManager()
                .getInstance(DocumentReferenceResolver.TYPE_STRING, CURRENT_LABEL);
            return resolver.resolve(mapping);
        } catch (ComponentLookupException e) {
            // Shouldn't happen, base components must be available
            this.logger.error("Failed to look up component when getting phenotype mapping: {}.", e.getMessage());
        }
        return null;
    }

    @Override
    public String getISODateFormat()
    {
        return DATE_FORMAT;
    }

    @Override
    public String getDateOfBirthFormat()
    {
        String result = getISODateFormat();
        BaseObject settings = getGlobalConfigurationObject();
        if (settings != null) {
            return StringUtils.defaultIfBlank(settings.getStringValue(DOB_FORMAT_LABEL), result);
        }
        return result;
    }

    @Override
    public String toString()
    {
        return StringUtils.join(getEnabledSections(), ", ");
    }

    private BaseObject getGlobalConfigurationObject()
    {
        try {
            XWikiContext context = this.xcontextProvider.get();
            return context.getWiki().getDocument(PREFERENCES_LOCATION, context).getXObject(GLOBAL_PREFERENCES_CLASS);
        } catch (XWikiException ex) {
            this.logger.warn("Failed to read preferences: {}", ex.getMessage());
        }
        return null;
    }
}
