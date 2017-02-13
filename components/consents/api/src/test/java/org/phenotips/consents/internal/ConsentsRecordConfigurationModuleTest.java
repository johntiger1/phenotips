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
package org.phenotips.consents.internal;

import org.phenotips.configuration.RecordConfiguration;
import org.phenotips.configuration.RecordConfigurationModule;
import org.phenotips.configuration.RecordElement;
import org.phenotips.configuration.RecordSection;
import org.phenotips.configuration.internal.DefaultRecordConfiguration;
import org.phenotips.configuration.internal.DefaultRecordSection;
import org.phenotips.data.Patient;
import org.phenotips.data.PatientRepository;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.mockito.MockitoComponentMockingRule;
import org.xwiki.uiextension.UIExtension;
import org.xwiki.uiextension.UIExtensionFilter;
import org.xwiki.uiextension.UIExtensionManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * Tests for the {@link ConsentsRecordConfigurationModule}.
 *
 * @version $Id $
 */
public class ConsentsRecordConfigurationModuleTest
{
    private static final int MODULE_PRIORITY = 90;

    private static final String PATIENT_LABEL = "patient";

    private static final String FAMILY_LABEL = "family";

    private static final String ENABLED_LABEL = "enabled";

    private static final String TRUE_LABEL = "true";

    private static final String EXPANDED_BY_DEFAULT_LABEL = "expanded_by_default";

    private static final String SECTION_A_LABEL = "phenotips.sectionA";

    private static final String SECTION_B_LABEL = "phenotips.sectionB";

    private static final String SECTION_C_LABEL = "phenotips.sectionC";

    private static final String FIELD_A_LABEL = "phenotips.fieldA";

    private static final String FIELD_B_LABEL = "phenotips.fieldB";

    private static final String FIELD_C_LABEL = "phenotips.fieldC";

    private static final String ORDER_LABEL = "order";

    @Rule
    public final MockitoComponentMockingRule<RecordConfigurationModule> mocker =
        new MockitoComponentMockingRule<RecordConfigurationModule>(ConsentsRecordConfigurationModule.class);

    private ConsentAuthorizer consentAuthorizer;

    private RecordConfigurationModule component;

    private DefaultRecordConfiguration config;

    private PatientRepository patients;

    private Patient patient;

    @Before
    public void setUp() throws ComponentLookupException
    {
        final DocumentAccessBridge dab = this.mocker.getInstance(DocumentAccessBridge.class);

        this.component = this.mocker.getComponentUnderTest();
        this.patients = this.mocker.getInstance(PatientRepository.class);
        this.consentAuthorizer = this.mocker.getInstance(ConsentAuthorizer.class);
        this.config = mock(DefaultRecordConfiguration.class);
        this.patient = mock(Patient.class);

        when(dab.getCurrentDocumentReference()).thenReturn(mock(DocumentReference.class));
        when(this.patients.get(anyString())).thenReturn(this.patient);
    }

    /**
     * Tests that {@link ConsentsRecordConfigurationModule#process(RecordConfiguration)} returns null if passed
     * configuration parameter is null.
     */
    @Test
    public void processConfigIsNullReturnsNull()
    {
        final RecordConfiguration config = this.component.process(null);
        Assert.assertNull(config);
    }

    /**
     * Tests that {@link ConsentsRecordConfigurationModule#process(RecordConfiguration)} returns the previous
     * configuration, unchanged, if the patient is null.
     */
    @Test
    public void processPatientIsNullReturnsPreviousConfig()
    {
        when(this.patients.get(anyString())).thenReturn(null);
        final RecordConfiguration configuration = this.component.process(this.config);
        verifyZeroInteractions(configuration);
        verifyZeroInteractions(this.config);
    }

    /**
     * Tests that {@link ConsentsRecordConfigurationModule#process(RecordConfiguration)} returns the previous
     * configuration, unchanged, if the previous configuration has no enabled sections.
     */
    @Test
    public void processConfigHasNoEnabledSections()
    {
        when(this.patients.get(anyString())).thenReturn(this.patient);
        when(this.config.getEnabledSections()).thenReturn(Collections.<RecordSection>emptyList());

        final RecordConfiguration configuration = this.component.process(this.config);
        verify(configuration, times(1)).getEnabledSections();
        verifyNoMoreInteractions(configuration);
    }

    /**
     * Tests that {@link ConsentsRecordConfigurationModule#process(RecordConfiguration)} returns a modified
     * configuration, containing only the consented sections and elements.
     */
    @Test
    public void processOnlyConsentedSectionsRemain()
    {
        final UIExtension uiExtensionA = mock(UIExtension.class);
        final UIExtension uiExtensionB = mock(UIExtension.class);
        final UIExtension uiExtensionC = mock(UIExtension.class);
        final UIExtensionManager uixManager = mock(UIExtensionManager.class);
        final UIExtensionFilter orderFilter = mock(UIExtensionFilter.class);

        final Map<String, String> param = new HashMap<>();
        param.put(ENABLED_LABEL, TRUE_LABEL);
        param.put(EXPANDED_BY_DEFAULT_LABEL, TRUE_LABEL);
        when(uiExtensionA.getParameters()).thenReturn(param);
        when(uiExtensionB.getParameters()).thenReturn(param);
        when(uiExtensionC.getParameters()).thenReturn(param);
        when(uiExtensionA.getId()).thenReturn(SECTION_A_LABEL);
        when(uiExtensionB.getId()).thenReturn(SECTION_B_LABEL);
        when(uiExtensionC.getId()).thenReturn(SECTION_C_LABEL);

        final UIExtension fieldExtensionA = mock(UIExtension.class);
        final List<UIExtension> fieldsA = Collections.singletonList(fieldExtensionA);
        when(fieldExtensionA.getParameters()).thenReturn(Collections.<String, String>emptyMap());
        when(fieldExtensionA.getId()).thenReturn(FIELD_A_LABEL);
        when(uixManager.get(SECTION_A_LABEL)).thenReturn(fieldsA);
        when(orderFilter.filter(fieldsA, ORDER_LABEL)).thenReturn(fieldsA);

        final UIExtension fieldExtensionB = mock(UIExtension.class);
        final List<UIExtension> fieldsB = Collections.singletonList(fieldExtensionB);
        when(fieldExtensionB.getId()).thenReturn(FIELD_B_LABEL);
        when(uixManager.get(SECTION_B_LABEL)).thenReturn(fieldsB);
        when(orderFilter.filter(fieldsB, ORDER_LABEL)).thenReturn(fieldsB);

        final UIExtension fieldExtensionC = mock(UIExtension.class);
        final List<UIExtension> fieldsC = Collections.singletonList(fieldExtensionC);
        when(fieldExtensionC.getId()).thenReturn(FIELD_C_LABEL);
        when(uixManager.get(SECTION_C_LABEL)).thenReturn(fieldsC);
        when(orderFilter.filter(fieldsC, ORDER_LABEL)).thenReturn(fieldsC);

        final RecordSection sectionA = new DefaultRecordSection(uiExtensionA, uixManager, orderFilter);
        final RecordSection sectionB = new DefaultRecordSection(uiExtensionB, uixManager, orderFilter);
        final RecordSection sectionC = new DefaultRecordSection(uiExtensionC, uixManager, orderFilter);

        when(this.config.getAllSections()).thenReturn(Arrays.asList(sectionA, sectionB, sectionC));
        when(this.config.getEnabledSections()).thenCallRealMethod();
        Assert.assertEquals(3, this.config.getEnabledSections().size());
        Assert.assertEquals(1, this.config.getEnabledSections().get(0).getEnabledElements().size());
        Assert.assertEquals(1, this.config.getEnabledSections().get(1).getEnabledElements().size());
        Assert.assertEquals(1, this.config.getEnabledSections().get(2).getEnabledElements().size());
        Assert.assertTrue(sectionA.isEnabled());
        Assert.assertTrue(sectionA.getAllElements().get(0).isEnabled());
        Assert.assertTrue(sectionB.isEnabled());
        Assert.assertTrue(sectionB.getAllElements().get(0).isEnabled());
        Assert.assertTrue(sectionC.isEnabled());
        Assert.assertTrue(sectionC.getAllElements().get(0).isEnabled());

        when(this.consentAuthorizer.filterForm(sectionA.getEnabledElements(), this.patient))
            .thenReturn(Collections.<RecordElement>emptyList());
        when(this.consentAuthorizer.filterForm(sectionB.getEnabledElements(), this.patient))
            .thenReturn(sectionB.getEnabledElements());
        when(this.consentAuthorizer.filterForm(sectionC.getEnabledElements(), this.patient))
            .thenReturn(Collections.<RecordElement>emptyList());

        // Call the method being tested.
        final RecordConfiguration configuration = this.component.process(this.config);

        Assert.assertEquals(1, configuration.getEnabledSections().size());
        Assert.assertEquals(1, configuration.getEnabledSections().get(0).getEnabledElements().size());
        Assert.assertEquals(0, sectionA.getEnabledElements().size());
        Assert.assertEquals(1, sectionA.getAllElements().size());
        Assert.assertFalse(sectionA.getAllElements().get(0).isEnabled());
        Assert.assertEquals(1, sectionB.getEnabledElements().size());
        Assert.assertEquals(1, sectionB.getAllElements().size());
        Assert.assertTrue(sectionB.getAllElements().get(0).isEnabled());
        Assert.assertEquals(0, sectionC.getEnabledElements().size());
        Assert.assertEquals(1, sectionC.getAllElements().size());
        Assert.assertFalse(sectionC.getAllElements().get(0).isEnabled());

        Assert.assertFalse(sectionA.isEnabled());
        Assert.assertTrue(sectionB.isEnabled());
        Assert.assertFalse(sectionC.isEnabled());
    }

    /**
     * Tests that {@link ConsentsRecordConfigurationModule#getPriority()} returns correct priority for module.
     */
    @Test
    public void getPriority()
    {
        final int priority = this.component.getPriority();
        Assert.assertEquals(MODULE_PRIORITY, priority);
    }

    /**
     * Tests that {@link ConsentsRecordConfigurationModule#getSupportedRecordTypes()} returns correct types.
     */
    @Test
    public void getSupportedRecordTypes()
    {
        final String[] supportedRecordTypes = this.component.getSupportedRecordTypes();
        Assert.assertArrayEquals(new String[] { PATIENT_LABEL }, supportedRecordTypes);
    }

    /**
     * Tests that {@link ConsentsRecordConfigurationModule#supportsRecordType(java.lang.String)} returns true if record
     * is of type "patient".
     */
    @Test
    public void supportsRecordTypeReturnsTrueIfPatient()
    {
        final boolean supportsRecordType = this.component.supportsRecordType(PATIENT_LABEL);
        Assert.assertTrue(supportsRecordType);
    }

    /**
     * Tests that {@link ConsentsRecordConfigurationModule#supportsRecordType(java.lang.String)} returns false if record
     * is not of type "patient".
     */
    @Test
    public void supportsRecordTypeReturnsFalseIfNotPatient()
    {
        final boolean supportsFamily = this.component.supportsRecordType(FAMILY_LABEL);
        final boolean supportsRandom = this.component.supportsRecordType("asdfasdf");
        Assert.assertFalse(supportsFamily);
        Assert.assertFalse(supportsRandom);
    }

    /**
     * Tests that {@link ConsentsRecordConfigurationModule#supportsRecordType(java.lang.String)} returns false if record
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
