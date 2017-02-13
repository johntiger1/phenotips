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

import org.phenotips.configuration.RecordConfiguration;
import org.phenotips.configuration.RecordConfigurationModule;
import org.phenotips.configuration.RecordSection;
import org.phenotips.configuration.internal.DefaultRecordConfiguration;
import org.phenotips.configuration.internal.DefaultRecordSection;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.mockito.MockitoComponentMockingRule;
import org.xwiki.text.StringUtils;
import org.xwiki.uiextension.UIExtension;
import org.xwiki.uiextension.UIExtensionFilter;
import org.xwiki.uiextension.UIExtensionManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link StudyRecordConfigurationModule}.
 *
 * @version $Id $
 */
public class StudyRecordConfigurationModuleTest
{
    private static final String PATIENT_LABEL = "patient";

    private static final String FAMILY_LABEL = "family";

    private static final String SECTIONS_LABEL = "sections";

    private static final String FIELDS_LABEL = "fields";

    private static final int MODULE_PRIORITY = 50;

    private static final String ENABLED_LABEL = "enabled";

    private static final String TRUE_LABEL = "true";

    private static final String EXPANDED_BY_DEFAULT_LABEL = "expanded_by_default";

    private static final String SECTION_A_LABEL = "phenotips.sectionA";

    private static final String SECTION_B_LABEL = "phenotips.sectionB";

    private static final String FIELD_A_LABEL = "phenotips.fieldA";

    private static final String ORDER_LABEL = "order";

    @Rule
    public final MockitoComponentMockingRule<RecordConfigurationModule> mocker =
        new MockitoComponentMockingRule<RecordConfigurationModule>(StudyRecordConfigurationModule.class);

    private DocumentAccessBridge dab;

    private RecordConfigurationModule component;

    private DefaultRecordConfiguration config;

    private BaseObject baseObject;

    @Before
    public void setUp() throws ComponentLookupException, XWikiException
    {
        this.component = this.mocker.getComponentUnderTest();
        this.dab = this.mocker.getInstance(DocumentAccessBridge.class);

        final Provider<XWikiContext> xcontextProvider = this.mocker.getInstance(XWikiContext.TYPE_PROVIDER);
        final XWikiContext context = mock(XWikiContext.class);
        final XWiki wiki = mock(XWiki.class);
        final XWikiDocument xWikiDocument = mock(XWikiDocument.class);

        this.config = mock(DefaultRecordConfiguration.class);
        this.baseObject = mock(BaseObject.class);
        final DocumentReference documentReference = mock(DocumentReference.class);

        when(this.dab.getCurrentDocumentReference()).thenReturn(documentReference);
        when(this.dab.getProperty(any(DocumentReference.class), any(DocumentReference.class), anyString()))
            .thenReturn("abc");

        when(xcontextProvider.get()).thenReturn(context);
        when(context.getWiki()).thenReturn(wiki);
        when(wiki.getDocument(any(DocumentReference.class), any(XWikiContext.class)))
            .thenReturn(xWikiDocument);
        when(xWikiDocument.isNew()).thenReturn(false);
        when(xWikiDocument.getXObject(RecordConfiguration.CUSTOM_PREFERENCES_CLASS)).thenReturn(this.baseObject);
    }

    /**
     * Tests that {@link StudyRecordConfigurationModule#process(RecordConfiguration)} returns null if passed
     * configuration parameter is null.
     */
    @Test
    public void processConfigIsNullReturnsNull()
    {
        final RecordConfiguration config = this.component.process(null);
        Assert.assertNull(config);
    }

    /**
     * Tests that {@link StudyRecordConfigurationModule#process(RecordConfiguration)} returns previous configuration if
     * current document reference is null.
     */
    @Test
    public void processWithNullCurrentDocumentReferenceReturnsPreviousConfigUnchanged()
    {
        when(this.dab.getCurrentDocumentReference()).thenReturn(null);
        final RecordConfiguration configuration = this.component.process(this.config);
        verifyZeroInteractions(this.config);
        Assert.assertEquals(this.config, configuration);
    }

    /**
     * Tests that {@link StudyRecordConfigurationModule#process(RecordConfiguration)} returns previous configuration
     * if the study binding class reference string is blank.
     */
    @Test
    public void processWithBlankStudyBindingClassConfigReturnsPreviousConfigUnchanged()
    {
        when(this.dab.getProperty(any(DocumentReference.class), any(DocumentReference.class), anyString()))
            .thenReturn(StringUtils.EMPTY);
        final RecordConfiguration configuration = this.component.process(this.config);
        verifyZeroInteractions(this.config);
        Assert.assertEquals(this.config, configuration);
    }

    /**
     * Tests that {@link StudyRecordConfigurationModule#process(RecordConfiguration)} returns configuration with
     * all sections disabled if study binding class has empty field and/or section overrides.
     */
    @Test
    public void processNoSectionsEnabledByStudy()
    {
        final UIExtension uiExtension = mock(UIExtension.class);
        final UIExtensionManager uiExtensionManager = mock(UIExtensionManager.class);
        final UIExtensionFilter uiExtensionFilter = mock(UIExtensionFilter.class);

        final Map<String, String> param = new HashMap<>();
        param.put(ENABLED_LABEL, TRUE_LABEL);
        param.put(EXPANDED_BY_DEFAULT_LABEL, TRUE_LABEL);
        when(uiExtension.getParameters()).thenReturn(param);

        final RecordSection sectionA = new DefaultRecordSection(uiExtension, uiExtensionManager, uiExtensionFilter);
        final RecordSection sectionB = new DefaultRecordSection(uiExtension, uiExtensionManager, uiExtensionFilter);
        sectionB.setEnabled(false);

        when(this.config.getAllSections()).thenReturn(Arrays.asList(sectionA, sectionB));
        when(this.config.getEnabledSections()).thenCallRealMethod();
        Assert.assertEquals(1, this.config.getEnabledSections().size());
        Assert.assertTrue(sectionA.isEnabled());
        Assert.assertFalse(sectionB.isEnabled());

        when(this.baseObject.getListValue(SECTIONS_LABEL)).thenReturn(Collections.emptyList());
        when(this.baseObject.getListValue(FIELDS_LABEL)).thenReturn(Collections.emptyList());

        final RecordConfiguration configuration = this.component.process(this.config);
        Assert.assertEquals(0, configuration.getEnabledSections().size());
        Assert.assertEquals(0, this.config.getEnabledSections().size());
        Assert.assertFalse(sectionA.isEnabled());
        Assert.assertFalse(sectionB.isEnabled());
    }

    /**
     * Tests that {@link StudyRecordConfigurationModule#process(RecordConfiguration)} returns configuration with
     * only the specified fields and sections enabled.
     */
    @Test
    public void processSectionsEnabledByStudy() {
        final UIExtension uiExtension1 = mock(UIExtension.class);
        final UIExtension uiExtension2 = mock(UIExtension.class);
        final UIExtensionManager uiExtensionManager = mock(UIExtensionManager.class);
        final UIExtensionFilter uiExtensionFilter = mock(UIExtensionFilter.class);

        final Map<String, String> param = new HashMap<>();
        param.put(ENABLED_LABEL, TRUE_LABEL);
        param.put(EXPANDED_BY_DEFAULT_LABEL, TRUE_LABEL);
        when(uiExtension1.getParameters()).thenReturn(param);
        when(uiExtension2.getParameters()).thenReturn(param);

        when(uiExtension1.getId()).thenReturn(SECTION_A_LABEL);
        final RecordSection sectionA = new DefaultRecordSection(uiExtension1, uiExtensionManager, uiExtensionFilter);

        when(uiExtension2.getId()).thenReturn(SECTION_B_LABEL);
        final RecordSection sectionB = new DefaultRecordSection(uiExtension2, uiExtensionManager, uiExtensionFilter);

        final UIExtension fieldExtension = mock(UIExtension.class);
        final List<UIExtension> fields = Collections.singletonList(fieldExtension);
        when(fieldExtension.getParameters()).thenReturn(new HashMap<String, String>());
        when(fieldExtension.getId()).thenReturn(FIELD_A_LABEL);
        when(uiExtensionManager.get(SECTION_A_LABEL)).thenReturn(fields);
        when(uiExtensionFilter.filter(fields, ORDER_LABEL)).thenReturn(fields);

        when(this.config.getAllSections()).thenReturn(Arrays.asList(sectionA, sectionB));
        when(this.config.getEnabledSections()).thenCallRealMethod();
        Assert.assertEquals(2, this.config.getEnabledSections().size());
        Assert.assertEquals(1, this.config.getEnabledSections().get(0).getEnabledElements().size());
        Assert.assertEquals(0, this.config.getEnabledSections().get(1).getEnabledElements().size());
        Assert.assertTrue(sectionA.isEnabled());
        Assert.assertTrue(sectionB.isEnabled());

        when(this.baseObject.getListValue(SECTIONS_LABEL)).thenReturn(Collections.singletonList(SECTION_A_LABEL));
        when(this.baseObject.getListValue(FIELDS_LABEL)).thenReturn(Collections.singletonList(FIELD_A_LABEL));

        final RecordConfiguration configuration = this.component.process(this.config);
        Assert.assertEquals(1, configuration.getEnabledSections().size());
        Assert.assertEquals(1, configuration.getEnabledSections().get(0).getEnabledElements().size());
        Assert.assertTrue(configuration.getEnabledSections().get(0).getEnabledElements().get(0).isEnabled());
        configuration.getEnabledSections().get(0).getEnabledElements().get(0).getName();
        Assert.assertEquals("FieldA", configuration.getEnabledSections().get(0).getEnabledElements().get(0).getName());

        Assert.assertTrue(sectionA.isEnabled());
        Assert.assertFalse(sectionB.isEnabled());
    }

    /**
     * Tests that {@link StudyRecordConfigurationModule#getPriority()} returns correct priority for module.
     */
    @Test
    public void getPriority()
    {
        final int priority = this.component.getPriority();
        Assert.assertEquals(MODULE_PRIORITY, priority);
    }

    /**
     * Tests that {@link StudyRecordConfigurationModule#getSupportedRecordTypes()} returns correct types.
     */
    @Test
    public void getSupportedRecordTypes()
    {
        final String[] supportedRecordTypes = this.component.getSupportedRecordTypes();
        Assert.assertArrayEquals(new String[] { PATIENT_LABEL }, supportedRecordTypes);
    }

    /**
     * Tests that {@link StudyRecordConfigurationModule#supportsRecordType(java.lang.String)} returns true if record is
     * of type "patient".
     */
    @Test
    public void supportsRecordTypeReturnsTrueIfPatient()
    {
        final boolean supportsRecordType = this.component.supportsRecordType(PATIENT_LABEL);
        Assert.assertTrue(supportsRecordType);
    }

    /**
     * Tests that {@link StudyRecordConfigurationModule#supportsRecordType(java.lang.String)} returns false if record
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
     * Tests that {@link StudyRecordConfigurationModule#supportsRecordType(java.lang.String)} returns false if record
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
