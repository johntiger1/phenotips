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
package org.phenotips.studies.internal;

import org.phenotips.Constants;
import org.phenotips.configuration.RecordConfiguration;
import org.phenotips.configuration.RecordConfigurationModule;
import org.phenotips.configuration.RecordElement;
import org.phenotips.configuration.RecordSection;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Implementation of {@link RecordConfiguration} that takes into account a {@link CustomConfiguration custom
 * configuration}. Its {@link #getPriority() priority} is {@code 50}.
 *
 * The current implementation, does not allow {@link StudyRecordConfigurationModule} to add any sections or fields. It
 * only permits the module to disable sections and/or fields for an existing configuration.
 *
 * @version $Id$
 * @since 1.3RC1
 */
@Named("Study")
public class StudyRecordConfigurationModule implements RecordConfigurationModule
{
    /**
     * Reference to the xclass which allows to bind a specific form customization to a patient record.
     */
    public static final EntityReference STUDY_BINDING_CLASS_REFERENCE = new EntityReference("StudyBindingClass",
        EntityType.DOCUMENT, Constants.CODE_SPACE_REFERENCE);

    private static final String STUDY_REFERENCE_PROPERTY_LABEL = "studyReference";

    private static final String PATIENT_TYPE_LABEL = "patient";

    private static final String[] SUPPORTED_RECORD_TYPES = new String[] { PATIENT_TYPE_LABEL };

    /** Provides access to the data. */
    @Inject
    private DocumentAccessBridge dab;

    /** Completes xclass references with the current wiki. */
    @Inject
    @Named("current")
    private DocumentReferenceResolver<EntityReference> resolver;

    /** Provides access to the current request context. */
    @Inject
    private Provider<XWikiContext> xcontextProvider;

    /** Parses serialized document references into proper references. */
    @Inject
    @Named("current")
    private DocumentReferenceResolver<String> referenceParser;

    /** Logging helper. */
    @Inject
    private Logger logger;

    @Override
    public RecordConfiguration process(RecordConfiguration config)
    {
        final CustomConfiguration configObj = (config == null) ? null : getBoundConfiguration();

        // If no study record configuration is provided, then return the default configuration, unchanged.
        if (configObj == null) {
            return config;
        }
        // Get the field overrides.
        final List<String> sectionOverrides = configObj.getSectionsOverride();
        // Get the field overrides.
        final List<String> fieldOverrides = configObj.getFieldsOverride();

        // If no overrides for enabled sections or fields are specified, then everything should be disabled.
        if (CollectionUtils.isEmpty(sectionOverrides) || CollectionUtils.isEmpty(fieldOverrides)) {
            disableAllSections(config);
            return config;
        }
        // Otherwise, update the configuration.
        updateStudyConfiguration(config, new LinkedHashSet<>(sectionOverrides), new LinkedHashSet<>(fieldOverrides));
        return config;
    }

    /**
     * Given the old {@code config form configuration}, the list of {@code sectionOverrides enabled sections} and
     * {@code fieldOverrides enabled fields}, update the {@code config configuration} with the new section settings.
     *
     * @param config the {@link RecordConfiguration configuration} that needs to be updated
     * @param sectionOverrides the list of identifiers for enabled sections
     * @param fieldOverrides the list of identifiers for enabled fields
     */
    private void updateStudyConfiguration(final RecordConfiguration config, final Set<String> sectionOverrides,
        final Set<String> fieldOverrides)
    {
        for (final RecordSection section : config.getEnabledSections()) {
            // If section ID is not in sectionOverrides, it's not enabled. Disable all elements for section.
            final boolean disableAll = !sectionOverrides.contains(section.getExtension().getId());
            configureSectionFields(section, fieldOverrides, disableAll);
        }
    }

    /**
     * Given the {@code section} that needs to be configured, the {@code fieldOverrides enabled fields} and a
     * {@code disableAll flag specifying if all fields should be disabled}, configures the {@code section} elements.
     *
     * @param section the {@link RecordSection record section} that needs to be configured
     * @param fieldOverrides the list of identifiers for enabled fields
     * @param disableAll a flag; true iff all fields should be disabled, false otherwise
     */
    private void configureSectionFields(final RecordSection section, final Set<String> fieldOverrides,
        final boolean disableAll)
    {
        if (disableAll) {
            disableAllFields(section);
        } else {
            configureFields(section, fieldOverrides);
        }
    }

    /**
     * Given the {@code section} that needs to be configured, and the {@code fieldOverrides enabled fields}, configures
     * the {@code section} elements.
     *
     * @param section the {@link RecordSection record section} that needs to be configured
     * @param fieldOverrides the list of identifiers for enabled fields
     */
    private void configureFields(final RecordSection section, final Set<String> fieldOverrides)
    {
        final List<RecordElement> elements = section.getEnabledElements();
        for (final RecordElement element : elements) {
            if (!fieldOverrides.contains(element.getExtension().getId())) {
                element.setEnabled(false);
            }
        }
    }

    /**
     * Disables all the {@link RecordConfiguration#getEnabledSections() enabled section} for the {@code config
     * configuration}.
     *
     * @param config the {@link RecordConfiguration} object currently being configured
     */
    private void disableAllSections(final RecordConfiguration config)
    {
        final List<RecordSection> enabledSections = config.getEnabledSections();
        for (final RecordSection section : enabledSections) {
            section.setEnabled(false);
        }
    }

    /**
     * Disables all the {@link RecordSection#getAllElements()} elements} for {@code section}.
     *
     * @param section the {@link RecordSection} object currently being configured
     */
    private void disableAllFields(final RecordSection section)
    {
        final List<RecordElement> enabledElements = section.getEnabledElements();
        for (final RecordElement element : enabledElements) {
            element.setEnabled(false);
        }
        // If no elements enabled for section, the section should be disabled as well.
        section.setEnabled(false);
    }

    @Override
    public int getPriority()
    {
        return 50;
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

    /**
     * If the current document is a patient record, and it has a valid specific study binding specified, then return
     * that configuration.
     *
     * @return a form configuration, if one is bound to the current document, or {@code null} otherwise
     */
    private CustomConfiguration getBoundConfiguration()
    {
        if (this.dab.getCurrentDocumentReference() == null) {
            // Non-interactive requests, use the default configuration
            return null;
        }
        String boundConfig = (String) this.dab.getProperty(this.dab.getCurrentDocumentReference(),
            this.resolver.resolve(STUDY_BINDING_CLASS_REFERENCE), STUDY_REFERENCE_PROPERTY_LABEL);
        if (StringUtils.isNotBlank(boundConfig)) {
            try {
                XWikiContext context = this.xcontextProvider.get();
                XWikiDocument doc = context.getWiki().getDocument(this.referenceParser.resolve(boundConfig), context);
                if (doc == null || doc.isNew()) {
                    // Inaccessible or deleted document, use default
                    // configuration
                    return null;
                }
                return new CustomConfiguration(doc.getXObject(RecordConfiguration.CUSTOM_PREFERENCES_CLASS));
            } catch (Exception ex) {
                this.logger.warn("Failed to read the bound configuration [{}] for [{}]: {}", boundConfig,
                    this.dab.getCurrentDocumentReference(), ex.getMessage());
            }
        }
        return null;
    }
}
