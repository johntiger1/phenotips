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

import org.phenotips.configuration.RecordElement;
import org.phenotips.configuration.RecordSection;

import org.xwiki.model.reference.ClassPropertyReference;
import org.xwiki.uiextension.UIExtension;
import org.xwiki.uiextension.internal.WikiUIExtension;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Default (global) implementation for {@link RecordElement}.
 *
 * @version $Id$
 * @since 1.3RC1
 */
public class DefaultRecordElement implements RecordElement
{
    private static final String CONTAINS_PII_LABEL = "contains_PII";

    private static final String TRUE_LABEL = "true";

    private static final String FALSE_LABEL = "false";

    private static final String ENABLED_LABEL = "enabled";

    private static final String TITLE_LABEL = "title";

    private static final String FIELDS_LABEL = "fields";

    /** @see #getExtension() */
    private final UIExtension extension;

    /** @see #getContainingSection() */
    private final RecordSection section;

    /** @see #getFields() */
    private final List<ClassPropertyReference> fields;

    /** @see #getName(). */
    private final String name;

    /** @see #isEnabled(). */
    private boolean enabled;

    /**
     * Simple constructor, taking a UI {@code extension} and the parent record {@code section} as parameters.
     *
     * @param extension the {@link UIExtension UI extension} object defining this record element
     * @param section the parent {@link RecordSection section} containing this element
     * @throws IllegalArgumentException if {@code extension} or {@code section} are null
     */
    DefaultRecordElement(UIExtension extension, RecordSection section)
    {
        if (extension == null || section == null) {
            throw new IllegalArgumentException("DefaultRecordElement constructor parameters must not be null");
        }
        this.extension = extension;
        this.section = section;
        this.enabled = !StringUtils.equals(FALSE_LABEL, this.extension.getParameters().get(ENABLED_LABEL));
        this.fields = constructFields();
        this.name = constructName();
    }

    @Override
    public UIExtension getExtension()
    {
        return this.extension;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public boolean isEnabled()
    {
        return this.enabled;
    }

    @Override
    public boolean containsPrivateIdentifiableInformation()
    {
        return StringUtils.equals(TRUE_LABEL, this.extension.getParameters().get(CONTAINS_PII_LABEL));
    }

    @Override
    public List<ClassPropertyReference> getFields()
    {
        return this.fields;
    }

    @Override
    public List<String> getDisplayedFields()
    {
        final List<String> fieldNames = new LinkedList<>();
        for (final ClassPropertyReference field : getFields()) {
            fieldNames.add(field.getName());
        }
        return Collections.unmodifiableList(fieldNames);
    }

    @Override
    public RecordSection getContainingSection()
    {
        return this.section;
    }

    @Override
    public String toString()
    {
        return getName();
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * Constructs an unmodifiable list of {@link ClassPropertyReference field objects} for the element.
     *
     * @return an unmodifiable list of {@link ClassPropertyReference field objects}
     */
    private List<ClassPropertyReference> constructFields()
    {
        final String fieldsStr = this.extension.getParameters().get(FIELDS_LABEL);
        final List<ClassPropertyReference> elementFields = new LinkedList<>();
        if (StringUtils.isNotBlank(fieldsStr)) {
            final String[] fieldNames = StringUtils.split(fieldsStr, ",");
            for (final String fieldName : fieldNames) {
                final ClassPropertyReference field = new ClassPropertyReference(StringUtils.trim(fieldName),
                    ((WikiUIExtension) this.extension).getDocumentReference());
                elementFields.add(field);
            }
        }
        return Collections.unmodifiableList(elementFields);
    }

    /**
     * Returns the name of the section as string.
     *
     * @return the name of the section as string
     */
    private String constructName()
    {
        String result = this.extension.getParameters().get(TITLE_LABEL);
        if (StringUtils.isBlank(result)) {
            result = StringUtils.capitalize(StringUtils.replaceChars(
                StringUtils.substringAfterLast(this.extension.getId(), "."), "_-", "  "));
        }
        return result;
    }
}
