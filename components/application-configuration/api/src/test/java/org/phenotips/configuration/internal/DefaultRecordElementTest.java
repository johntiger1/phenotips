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

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.uiextension.UIExtension;
import org.xwiki.uiextension.internal.WikiUIExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the default {@link RecordElement} implementation, {@link DefaultRecordElement}.
 *
 * @version $Id$
 */
public class DefaultRecordElementTest
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final RecordSection recordSection = mock(RecordSection.class);

    private final UIExtension uiExtension = mock(UIExtension.class);

    @Before
    public void setup() throws ComponentLookupException
    {
        MockitoAnnotations.initMocks(this);
    }

    /** Basic tests for {@link RecordElement#getExtension()}. */
    @Test
    public void getExtension()
    {
        RecordElement s = new DefaultRecordElement(this.uiExtension, this.recordSection);
        Assert.assertSame(this.uiExtension, s.getExtension());
    }

    /** Basic test to affirm that passing in a null extension will result in an exception. */
    @Test
    public void nullExtensionThrowsException()
    {
        this.thrown.expect(IllegalArgumentException.class);
        new DefaultRecordElement(null, this.recordSection);
    }

    /** Basic test to affirm that passing in a null record section will result in an exception. */
    @Test
    public void nullSectionThrowsException()
    {
        this.thrown.expect(IllegalArgumentException.class);
        new DefaultRecordElement(this.uiExtension, null);
    }

    /** {@link RecordElement#getName()} returns the title set in the properties. */
    @Test
    public void getName()
    {
        Map<String, String> params = new HashMap<>();
        params.put("title", "Age of onset");
        when(this.uiExtension.getParameters()).thenReturn(params);
        RecordElement s = new DefaultRecordElement(this.uiExtension, this.recordSection);
        Assert.assertEquals("Age of onset", s.getName());
    }

    /** {@link RecordElement#getName()} returns the last part of the ID. */
    @Test
    public void getNameWithMissingTitle()
    {
        when(this.uiExtension.getParameters()).thenReturn(Collections.<String, String>emptyMap());
        when(this.uiExtension.getId()).thenReturn("org.phenotips.patientSheet.field.exam_date");

        RecordElement s = new DefaultRecordElement(this.uiExtension, this.recordSection);
        Assert.assertEquals("Exam date", s.getName());
    }

    /** {@link RecordElement#isEnabled()} returns true when there's no setting in the properties. */
    @Test
    public void isEnabledReturnsTrueForNullSetting()
    {
        Map<String, String> params = new HashMap<>();
        when(this.uiExtension.getParameters()).thenReturn(params);

        RecordElement s = new DefaultRecordElement(this.uiExtension, this.recordSection);
        Assert.assertTrue(s.isEnabled());
    }

    /** {@link RecordElement#isEnabled()} returns true when there's no value set in the properties. */
    @Test
    public void isEnabledReturnsTrueForEmptySetting()
    {
        Map<String, String> params = new HashMap<>();
        when(this.uiExtension.getParameters()).thenReturn(params);

        params.put("enabled", "");
        RecordElement s = new DefaultRecordElement(this.uiExtension, this.recordSection);
        Assert.assertTrue(s.isEnabled());
    }

    /** {@link RecordElement#isEnabled()} returns true when set to "true" in the properties. */
    @Test
    public void isEnabledReturnsTrueForTrueSetting()
    {
        Map<String, String> params = new HashMap<>();
        when(this.uiExtension.getParameters()).thenReturn(params);

        params.put("enabled", "true");
        RecordElement s = new DefaultRecordElement(this.uiExtension, this.recordSection);
        Assert.assertTrue(s.isEnabled());
    }

    /** {@link RecordElement#isEnabled()} returns false only when explicitly disabled in the properties. */
    @Test
    public void isEnabledReturnsFalseForFalseSetting()
    {
        Map<String, String> params = new HashMap<>();
        when(this.uiExtension.getParameters()).thenReturn(params);

        params.put("enabled", "false");
        RecordElement s = new DefaultRecordElement(this.uiExtension, this.recordSection);
        Assert.assertFalse(s.isEnabled());
    }

    /** {@link RecordElement#getDisplayedFields()} returns the fields listed in the extension "fields" property. */
    @Test
    public void getDisplayedFields()
    {
        final DocumentReference documentReference = mock(DocumentReference.class);
        final WikiUIExtension extension = mock(WikiUIExtension.class);
        final Map<String, String> params = new HashMap<>();
        params.put("fields", ",first_name ,, last_name,");
        when(extension.getParameters()).thenReturn(params);
        when(extension.getDocumentReference()).thenReturn(documentReference);
        final DefaultRecordElement element = new DefaultRecordElement(extension, this.recordSection);

        final List<String> result = element.getDisplayedFields();

        Assert.assertEquals(2, result.size());
        Assert.assertEquals("first_name", result.get(0));
        Assert.assertEquals("last_name", result.get(1));
    }

    /** {@link RecordElement#getDisplayedFields()} returns an empty list when there's no "fields" property. */
    @Test
    public void getDisplayedFieldsWithMissingProperty()
    {
        when(this.uiExtension.getParameters()).thenReturn(Collections.<String, String>emptyMap());
        RecordElement s = new DefaultRecordElement(this.uiExtension, this.recordSection);
        List<String> result = s.getDisplayedFields();
        Assert.assertTrue(result.isEmpty());
    }

    /** {@link RecordElement#getContainingSection()} returns the passed section. */
    @Test
    public void getContainingSection()
    {
        RecordElement s = new DefaultRecordElement(this.uiExtension, this.recordSection);
        Assert.assertSame(this.recordSection, s.getContainingSection());
    }

    /** {@link RecordElement#toString()} returns the title set in the properties. */
    @Test
    public void toStringTest()
    {
        Map<String, String> params = new HashMap<>();
        params.put("title", "Age of onset");
        when(this.uiExtension.getParameters()).thenReturn(params);
        RecordElement s = new DefaultRecordElement(this.uiExtension, this.recordSection);
        Assert.assertEquals("Age of onset", s.toString());
    }
}
