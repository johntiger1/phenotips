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

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.test.mockito.MockitoComponentMockingRule;
import org.xwiki.uiextension.UIExtension;
import org.xwiki.uiextension.UIExtensionFilter;
import org.xwiki.uiextension.UIExtensionManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link GlobalFamilyConfigurationModule}.
 *
 * @version $Id $
 */
public class GlobalFamilyConfigurationModuleTest
{
    private static final String PATIENT_LABEL = "patient";

    private static final String FAMILY_LABEL = "family";

    private static final String TITLE_LABEL = "title";

    private static final int MODULE_PRIORITY = 0;

    private static final String SORT_PARAMETER_NAME = "order";

    private static final String ENABLED_LABEL = "enabled";

    private static final String TRUE_LABEL = "true";

    private static final String EXPANDED_BY_DEFAULT_LABEL = "expanded_by_default";

    private static final String SECTION_A_LABEL = "sectionA";

    private static final String SECTION_B_LABEL = "sectionB";

    private static final String SECTION_C_LABEL = "sectionC";

    @Rule
    public final MockitoComponentMockingRule<RecordConfigurationModule> mocker =
        new MockitoComponentMockingRule<RecordConfigurationModule>(GlobalFamilyConfigurationModule.class);

    private RecordConfigurationModule component;

    @Before
    public void setUp() throws ComponentLookupException
    {
        this.component = this.mocker.getComponentUnderTest();
        final UIExtensionManager uixManager = this.mocker.getInstance(UIExtensionManager.class);
        final UIExtensionFilter orderFilter = this.mocker.getInstance(UIExtensionFilter.class, "sortByParameter");

        final UIExtension extensionA = mock(UIExtension.class);
        final UIExtension extensionB = mock(UIExtension.class);
        final UIExtension extensionC = mock(UIExtension.class);

        final List<UIExtension> extensions = Arrays.asList(extensionA, extensionB, extensionC);
        final Map<String, String> param = new HashMap<>();
        param.put(ENABLED_LABEL, TRUE_LABEL);
        param.put(EXPANDED_BY_DEFAULT_LABEL, TRUE_LABEL);

        when(uixManager.get("phenotips.familyRecord.content")).thenReturn(extensions);
        when(orderFilter.filter(extensions, SORT_PARAMETER_NAME)).thenReturn(extensions);

        final Map<String, String> paramA = new HashMap<>(param);
        paramA.put(TITLE_LABEL, SECTION_A_LABEL);
        when(extensionA.getParameters()).thenReturn(paramA);

        final Map<String, String> paramB = new HashMap<>(param);
        paramB.put(TITLE_LABEL, SECTION_B_LABEL);
        when(extensionB.getParameters()).thenReturn(paramB);

        final Map<String, String> paramC = new HashMap<>(param);
        paramC.put(TITLE_LABEL, SECTION_C_LABEL);
        when(extensionC.getParameters()).thenReturn(paramC);
    }

    /**
     * Tests that {@link GlobalFamilyConfigurationModule#process(RecordConfiguration)} returns configuration containing
     * all the sections.
     */
    @Test
    public void processTestAllSectionsAddedToConfig()
    {
        final RecordConfiguration config = this.component.process(null);
        final List<String> sectionNames = Arrays.asList(SECTION_A_LABEL, SECTION_B_LABEL, SECTION_C_LABEL);
        final List<RecordSection> sections = config.getAllSections();
        Assert.assertEquals(3, sections.size());
        Assert.assertTrue(sectionNames.contains(sections.get(0).getName()));
        Assert.assertTrue(sectionNames.contains(sections.get(1).getName()));
        Assert.assertTrue(sectionNames.contains(sections.get(2).getName()));
    }

    /**
     * Tests that {@link GlobalFamilyConfigurationModule#getPriority()} returns correct priority for module.
     */
    @Test
    public void getPriority()
    {
        final int priority = this.component.getPriority();
        Assert.assertEquals(MODULE_PRIORITY, priority);
    }

    /**
     * Tests that {@link GlobalFamilyConfigurationModule#getSupportedRecordTypes()} returns correct types.
     */
    @Test
    public void getSupportedRecordTypes()
    {
        final String[] supportedRecordTypes = this.component.getSupportedRecordTypes();
        Assert.assertArrayEquals(new String[] { FAMILY_LABEL }, supportedRecordTypes);
    }

    /**
     * Tests that {@link GlobalFamilyConfigurationModule#supportsRecordType(java.lang.String)} returns true if record is
     * of type "family".
     */
    @Test
    public void supportsRecordTypeReturnsTrueIfPatient()
    {
        final boolean supportsRecordType = this.component.supportsRecordType(FAMILY_LABEL);
        Assert.assertTrue(supportsRecordType);

    }

    /**
     * Tests that {@link GlobalFamilyConfigurationModule#supportsRecordType(java.lang.String)} returns false if record
     * is not of type "family".
     */
    @Test
    public void supportsRecordTypeReturnsFalseIfNotPatient()
    {
        final boolean supportsFamily = this.component.supportsRecordType(PATIENT_LABEL);
        final boolean supportsRandom = this.component.supportsRecordType("asdfasdf");
        Assert.assertFalse(supportsFamily);
        Assert.assertFalse(supportsRandom);
    }


    /**
     * Tests that {@link GlobalFamilyConfigurationModule#supportsRecordType(java.lang.String)} returns false if record
     * type is null or empty string.
     */
    @Test
    public void supportsRecordTypeReturnsFalseIfNullOrEmpty()
    {
        final boolean supportsNull = this.component.supportsRecordType(null);
        final boolean supportsEmpty = this.component.supportsRecordType("");
        Assert.assertFalse(supportsNull);
        Assert.assertFalse(supportsEmpty);
    }
}
