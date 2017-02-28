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
package org.phenotips.data.internal.controller;

import org.phenotips.components.ComponentManagerRegistry;
import org.phenotips.data.IndexedPatientData;
import org.phenotips.data.Patient;
import org.phenotips.data.PatientData;
import org.phenotips.data.PatientDataController;
import org.phenotips.data.SimpleValuePatientData;
import org.phenotips.data.internal.PhenoTipsGene;
import org.phenotips.vocabulary.VocabularyManager;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Provider;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseStringProperty;
import com.xpn.xwiki.objects.StringListProperty;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.StaticListClass;
import com.xpn.xwiki.web.Utils;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for the {@link GeneListController} Component, only the overridden methods from {@link AbstractComplexController}
 * are tested here.
 */
public class GeneListControllerTest
{
    private static final String GENES_STRING = "genes";

    private static final String CONTROLLER_NAME = GENES_STRING;

    private static final String GENES_ENABLING_FIELD_NAME = GENES_STRING;

    private static final String GENE_KEY = "gene";

    private static final String GENE_VALUE = "GENE";

    private static final String STATUS_KEY = "status";

    private static final String STRATEGY_KEY = "strategy";

    private static final String COMMENTS_KEY = "comments";

    private static final String JSON_GENE_ID = "id";

    private static final String JSON_GENE_SYMBOL = GENE_KEY;

    private static final String JSON_STATUS_KEY = STATUS_KEY;

    private static final String JSON_STRATEGY_KEY = STRATEGY_KEY;

    private static final String JSON_COMMENTS_KEY = COMMENTS_KEY;

    private static final String JSON_OLD_REJECTED_GENE_KEY = "rejectedGenes";

    private static final String JSON_OLD_SOLVED_GENE_KEY = "solved";

    @Rule
    public MockitoComponentMockingRule<PatientDataController<List<PhenoTipsGene>>> mocker =
        new MockitoComponentMockingRule<PatientDataController<List<PhenoTipsGene>>>(GeneListController.class);

    private DocumentAccessBridge documentAccessBridge;

    @Mock
    private Patient patient;

    @Mock
    private XWikiDocument doc;

    @Mock
    private ComponentManager cm;

    @Mock
    private Provider<ComponentManager> mockProvider;

    @Mock
    private VocabularyManager vm;

    @Mock
    private XWiki xwiki;

    @Mock
    private Provider<XWikiContext> provider;

    @Mock
    private XWikiContext xWikiContext;

    private List<BaseObject> geneXWikiObjects;

    private static List<String> STATUS_VALUES = Arrays.asList("candidate", "rejected", "solved");

    private static List<String> STRATEGY_VALUES = Arrays.asList("sequencing", "deletion", "familial_mutation",
        "common_mutations");

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        Utils.setComponentManager(this.cm);
        ReflectionUtils.setFieldValue(new ComponentManagerRegistry(), "cmProvider", this.mockProvider);
        when(this.mockProvider.get()).thenReturn(this.cm);
        when(this.cm.getInstance(VocabularyManager.class)).thenReturn(this.vm);

        when(this.cm.getInstance(XWikiContext.TYPE_PROVIDER)).thenReturn(this.provider);
        XWikiContext context = mock(XWikiContext.class);
        when(this.provider.get()).thenReturn(context);
        XWiki x = mock(XWiki.class);
        when(context.getWiki()).thenReturn(x);

        DocumentReference geneDocRef = new DocumentReference("wiki", "PhenoTips", "GeneClass");
        XWikiDocument geneDoc = new XWikiDocument(geneDocRef);
        when(Matchers.any(PhenoTipsGene.class).getGeneDoc()).thenReturn(geneDoc);
        geneDoc.setNew(false);
        XWikiDocument spy = Mockito.spy(geneDoc);
        BaseClass c = mock(BaseClass.class);
        when(spy.getXClass()).thenReturn(c);
        StaticListClass lc1 = mock(StaticListClass.class);
        StaticListClass lc2 = mock(StaticListClass.class);
        when(c.get(STATUS_KEY)).thenReturn(lc1);
        when(c.get(STRATEGY_KEY)).thenReturn(lc1);
        when(lc1.getList(context)).thenReturn(STATUS_VALUES);
        when(lc2.getList(context)).thenReturn(STRATEGY_VALUES);

        this.documentAccessBridge = this.mocker.getInstance(DocumentAccessBridge.class);

        DocumentReference patientDocument = new DocumentReference("wiki", "patient", "00000001");
        doReturn(patientDocument).when(this.patient).getDocument();
        doReturn(this.doc).when(this.documentAccessBridge).getDocument(patientDocument);
        this.geneXWikiObjects = new LinkedList<>();
        doReturn(this.geneXWikiObjects).when(this.doc).getXObjects(any(EntityReference.class));
    }

    @Test
    public void checkGetName() throws ComponentLookupException
    {
        Assert.assertEquals(CONTROLLER_NAME, this.mocker.getComponentUnderTest().getName());
    }

    @Test
    public void loadWorks() throws Exception
    {
        for (int i = 0; i < 3; ++i) {
            BaseObject gene = mock(BaseObject.class);
            this.geneXWikiObjects.add(gene);

            BaseStringProperty geneString = mock(BaseStringProperty.class);
            doReturn("gene" + i).when(geneString).getValue();
            doReturn(geneString).when(gene).getField(GENE_KEY);

            BaseStringProperty statusString = mock(BaseStringProperty.class);
            doReturn("status" + i).when(statusString).getValue();
            doReturn(statusString).when(gene).getField(STATUS_KEY);

            BaseStringProperty commentString = mock(BaseStringProperty.class);
            doReturn("comment" + i).when(commentString).getValue();
            doReturn(commentString).when(gene).getField(COMMENTS_KEY);

            StringListProperty strategyString = mock(StringListProperty.class);
            doReturn("strategy" + i).when(strategyString).getTextValue();
            doReturn(Arrays.asList("strategy" + i)).when(strategyString).getList();
            doReturn(strategyString).when(gene).getField(STRATEGY_KEY);

            doReturn(Arrays.asList(geneString, statusString, commentString, strategyString)).when(gene).getFieldList();
        }

        PatientData<List<PhenoTipsGene>> result = this.mocker.getComponentUnderTest().load(this.patient);

        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.getValue().size());
        for (int i = 0; i < 3; ++i) {
            PhenoTipsGene item = result.getValue().get(i);
            Assert.assertEquals("gene" + i, item.getName());
            Assert.assertEquals(null, item.getStatus());
            Assert.assertEquals("comment" + i, item.getComment());
            Assert.assertEquals("strategy" + i, item.getStrategy());
        }
    }

    @Test
    public void loadCatchesExceptionFromDocumentAccess() throws Exception
    {
        Exception exception = new Exception();
        doThrow(exception).when(this.documentAccessBridge).getDocument(any(DocumentReference.class));

        PatientData<List<PhenoTipsGene>> result = this.mocker.getComponentUnderTest().load(this.patient);

        Assert.assertNull(result);
        verify(this.mocker.getMockedLogger()).error("Could not find requested document or some unforeseen "
            + "error has occurred during controller loading ", exception.getMessage());
    }

    @Test
    public void loadReturnsNullWhenPatientDoesNotHaveGeneClass() throws ComponentLookupException
    {
        doReturn(null).when(this.doc).getXObjects(any(EntityReference.class));

        PatientData<List<PhenoTipsGene>> result = this.mocker.getComponentUnderTest().load(this.patient);

        Assert.assertNull(result);
    }

    @Test
    public void loadReturnsNullWhenGeneIsEmpty() throws ComponentLookupException
    {
        doReturn(new LinkedList<BaseObject>()).when(this.doc).getXObjects(any(EntityReference.class));

        PatientData<List<PhenoTipsGene>> result = this.mocker.getComponentUnderTest().load(this.patient);

        Assert.assertNull(result);
    }

    @Test
    public void loadIgnoresNullFields() throws ComponentLookupException
    {
        BaseObject obj = mock(BaseObject.class);
        doReturn(null).when(obj).getField(anyString());
        this.geneXWikiObjects.add(obj);

        PatientData<List<PhenoTipsGene>> result = this.mocker.getComponentUnderTest().load(this.patient);

        Assert.assertNull(result);
    }

    @Test
    public void loadIgnoresNullGenes() throws ComponentLookupException
    {
        // Deleted objects appear as nulls in XWikiObjects list
        this.geneXWikiObjects.add(null);
        addGeneFields(GENE_KEY, new String[] { "SRCAP" });
        PatientData<List<PhenoTipsGene>> result = this.mocker.getComponentUnderTest().load(this.patient);

        Assert.assertEquals(1, result.getValue().size());
    }

    @Test
    public void checkLoadParsingOfGeneKey() throws ComponentLookupException
    {
        String[] genes = new String[] { "A", "<!'>;", "two words" };
        addGeneFields(GENE_KEY, genes);

        PatientData<List<PhenoTipsGene>> result = this.mocker.getComponentUnderTest().load(this.patient);

        Assert.assertNotNull(result);
        for (int i = 0; i < genes.length; i++) {
            PhenoTipsGene gene = result.getValue().get(i);
            Assert.assertEquals(genes[i], gene.getName());
        }
    }

    @Test
    public void writeJSONReturnsWhenGetDataReturnsNotNull() throws ComponentLookupException
    {
        doReturn(null).when(this.patient).getData(CONTROLLER_NAME);
        JSONObject json = new JSONObject();
        Collection<String> selectedFields = new LinkedList<>();
        selectedFields.add(GENES_ENABLING_FIELD_NAME);

        this.mocker.getComponentUnderTest().writeJSON(this.patient, json, selectedFields);

        Assert.assertTrue(json.has(CONTROLLER_NAME));
        verify(this.patient).getData(CONTROLLER_NAME);
    }

    @Test
    public void writeJSONReturnsWhenDataIsEmpty() throws ComponentLookupException
    {
        List<PhenoTipsGene> internalList = new LinkedList<>();
        PatientData<List<PhenoTipsGene>> patientData = new SimpleValuePatientData<>(CONTROLLER_NAME, internalList);
        doReturn(patientData).when(this.patient).getData(CONTROLLER_NAME);
        JSONObject json = new JSONObject();
        Collection<String> selectedFields = new LinkedList<>();
        selectedFields.add(GENES_ENABLING_FIELD_NAME);

        this.mocker.getComponentUnderTest().writeJSON(this.patient, json, selectedFields);

        Assert.assertTrue(json.has(CONTROLLER_NAME));
        verify(this.patient).getData(CONTROLLER_NAME);
    }

    /*
     * Tests that the passed JSON will not be affected by writeJSON in this controller if selected fields is not null,
     * and does not contain GeneListController.GENES_ENABLING_FIELD_NAME
     */
    @Test
    public void writeJSONReturnsWhenSelectedFieldsDoesNotContainGeneEnabler() throws ComponentLookupException
    {
        List<PhenoTipsGene> internalList = new LinkedList<>();
        PatientData<List<PhenoTipsGene>> patientData = new SimpleValuePatientData<>(CONTROLLER_NAME, internalList);
        doReturn(patientData).when(this.patient).getData(CONTROLLER_NAME);
        JSONObject json = new JSONObject();
        Collection<String> selectedFields = new LinkedList<>();
        // selectedFields could contain any number of random strings; it should not affect the behavior in this case
        selectedFields.add("some_string");

        this.mocker.getComponentUnderTest().writeJSON(this.patient, json, selectedFields);

        Assert.assertFalse(json.has(CONTROLLER_NAME));
    }

    @Test(expected = IllegalArgumentException.class)
    public void writeJSONIgnoresItemsWhenGeneIsBlank() throws ComponentLookupException
    {
        new PhenoTipsGene("", null, null, null, null);
    }

    @Test
    public void writeJSONAddsContainerWithAllValuesWhenSelectedFieldsNull() throws ComponentLookupException
    {
        List<PhenoTipsGene> internalList = new LinkedList<>();

        PhenoTipsGene item = new PhenoTipsGene(null, "geneName", "", "", null);
        internalList.add(item);

        PatientData<List<PhenoTipsGene>> patientData = new SimpleValuePatientData<>(CONTROLLER_NAME, internalList);
        doReturn(patientData).when(this.patient).getData(CONTROLLER_NAME);
        JSONObject json = new JSONObject();

        this.mocker.getComponentUnderTest().writeJSON(this.patient, json, null);

        Assert.assertNotNull(json.get(CONTROLLER_NAME));
        Assert.assertTrue(json.get(CONTROLLER_NAME) instanceof JSONArray);
        Assert.assertEquals("geneName", json.getJSONArray(CONTROLLER_NAME).getJSONObject(0).get(JSON_GENE_ID));
        Assert.assertEquals("geneName", json.getJSONArray(CONTROLLER_NAME).getJSONObject(0).get(JSON_GENE_SYMBOL));
    }

    @Test
    public void writeJSONWorksCorrectly() throws ComponentLookupException
    {
        List<PhenoTipsGene> internalList = new LinkedList<>();

        PhenoTipsGene item = new PhenoTipsGene(null, GENE_VALUE, "Status", "Strategy", "Comment");
        internalList.add(item);

        PatientData<List<PhenoTipsGene>> patientData = new SimpleValuePatientData<>(CONTROLLER_NAME, internalList);
        doReturn(patientData).when(this.patient).getData(CONTROLLER_NAME);
        JSONObject json = new JSONObject();
        Collection<String> selectedFields = new LinkedList<>();
        selectedFields.add(GENES_ENABLING_FIELD_NAME);

        this.mocker.getComponentUnderTest().writeJSON(this.patient, json, selectedFields);

        Assert.assertNotNull(json.get(CONTROLLER_NAME));
        Assert.assertTrue(json.get(CONTROLLER_NAME) instanceof JSONArray);
        JSONObject result = json.getJSONArray(CONTROLLER_NAME).getJSONObject(0);
        Assert.assertEquals(GENE_VALUE, result.get(JSON_GENE_SYMBOL));
        Assert.assertEquals(GENE_VALUE, result.get(JSON_GENE_ID));
        Assert.assertEquals(null, result.opt(JSON_STATUS_KEY));
        String[] strategyArray = { "strategy" };
        Assert.assertEquals(new JSONArray(strategyArray).get(0), ((JSONArray) result.get(JSON_STRATEGY_KEY)).get(0));
        Assert.assertEquals("Comment", result.get(JSON_COMMENTS_KEY));
        // id, gene, strategy, comment
        Assert.assertEquals(4, result.length());
    }

    @Test
    public void readWithNullJsonDoesNothing() throws ComponentLookupException
    {
        Assert.assertNull(this.mocker.getComponentUnderTest().readJSON(null));
    }

    @Test
    public void readWithNoDataDoesNothing() throws ComponentLookupException
    {
        Assert.assertNull(this.mocker.getComponentUnderTest().readJSON(new JSONObject()));
    }

    @Test
    public void readWithWrongDataDoesNothing() throws ComponentLookupException
    {
        JSONObject json = new JSONObject();
        json.put(CONTROLLER_NAME, "No");
        PatientData<List<PhenoTipsGene>> result = this.mocker.getComponentUnderTest().readJSON(json);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.getValue().size());
    }

    @Test
    public void readWithEmptyDataDoesNothing() throws ComponentLookupException
    {
        JSONObject json = new JSONObject();
        json.put(CONTROLLER_NAME, new JSONArray());
        PatientData<List<PhenoTipsGene>> result = this.mocker.getComponentUnderTest().readJSON(json);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.getValue().size());
    }

    @Test
    public void readWorksCorrectly() throws ComponentLookupException
    {
        JSONArray data = new JSONArray();
        JSONObject item = new JSONObject();
        item.put(JSON_GENE_SYMBOL, "GENE1");
        item.put(JSON_COMMENTS_KEY, "Notes1");
        data.put(item);
        item = new JSONObject();
        item.put(JSON_GENE_SYMBOL, "GENE2");
        item.put(JSON_STATUS_KEY, "rejected");
        data.put(item);
        item = new JSONObject();
        item.put(JSON_GENE_ID, "ENSG00000123456");
        data.put(item);
        item = new JSONObject();
        item.put(JSON_GENE_ID, "ENSG00000098765");
        item.put(JSON_STATUS_KEY, "incorrect_status");
        data.put(item);
        JSONObject json = new JSONObject();
        json.put(CONTROLLER_NAME, data);
        PatientData<List<PhenoTipsGene>> result = this.mocker.getComponentUnderTest().readJSON(json);
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.getValue().size());
        PhenoTipsGene gene = result.getValue().get(0);
        Assert.assertEquals("GENE1", gene.getName());
        Assert.assertEquals("Notes1", gene.getComment());
        Assert.assertNull(gene.getStrategy());
        gene = result.getValue().get(1);
        Assert.assertEquals("GENE2", gene.getName());
        Assert.assertEquals("rejected", gene.getStatus());
        Assert.assertNull(gene.getComment());
        Assert.assertNull(gene.getStrategy());
        gene = result.getValue().get(2);
        Assert.assertEquals("ENSG00000123456", gene.getId());
        Assert.assertNull(gene.getStatus());
        Assert.assertNull(gene.getComment());
        Assert.assertNull(gene.getStrategy());
        gene = result.getValue().get(3);
        Assert.assertEquals("ENSG00000098765", gene.getId());
        // any incorrect status should be replaced with "candidate"
        Assert.assertNull(gene.getStatus());
    }

    @Test
    public void readParsedsOldJSONCorrectly() throws ComponentLookupException
    {
        JSONArray data = new JSONArray();
        JSONObject item = new JSONObject();
        item.put(JSON_GENE_SYMBOL, "GENE_1");
        item.put(JSON_STATUS_KEY, "candidate");
        data.put(item);
        item = new JSONObject();
        // this gene is duplicated 2 times - in candidate and in rejected sections. Should become rejected
        item.put(JSON_GENE_SYMBOL, "GENE_TO_BECOME_REJECTED");
        item.put(JSON_STATUS_KEY, "candidate");
        data.put(item);
        item = new JSONObject();
        // this gene is duplicated 3 times - in candidate, rejected and solved sections. Should become solved
        item.put(JSON_GENE_SYMBOL, "GENE_TO_BECOME_SOLVED");
        item.put(JSON_STATUS_KEY, "candidate");
        data.put(item);
        JSONObject json = new JSONObject();
        json.put(CONTROLLER_NAME, data);
        data = new JSONArray();
        item = new JSONObject();
        item.put(JSON_GENE_SYMBOL, "GENE_TO_BECOME_REJECTED");
        item.put(JSON_STATUS_KEY, "rejected");
        data.put(item);
        item = new JSONObject();
        item.put(JSON_GENE_SYMBOL, "GENE_TO_BECOME_SOLVED");
        item.put(JSON_STATUS_KEY, "rejected");
        data.put(item);
        json.put(JSON_OLD_REJECTED_GENE_KEY, data);
        item = new JSONObject();
        item.put(JSON_GENE_SYMBOL, "GENE_TO_BECOME_SOLVED");
        json.put(JSON_OLD_SOLVED_GENE_KEY, item);

        PatientData<List<PhenoTipsGene>> result = this.mocker.getComponentUnderTest().readJSON(json);
        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.getValue().size());
        PhenoTipsGene gene = result.getValue().get(0);
        Assert.assertEquals("GENE_1", gene.getName());
        Assert.assertEquals("candidate", gene.getStatus());
        gene = result.getValue().get(1);
        Assert.assertEquals("GENE_TO_BECOME_REJECTED", gene.getName());
        Assert.assertEquals("rejected", gene.getStatus());
        gene = result.getValue().get(2);
        Assert.assertEquals("GENE_TO_BECOME_SOLVED", gene.getName());
        Assert.assertEquals("solved", gene.getStatus());
    }

    @Test
    public void saveWithNoDataDoesNothing() throws ComponentLookupException
    {
        this.mocker.getComponentUnderTest().save(this.patient, this.doc);
        Mockito.verifyZeroInteractions(this.doc);
    }

    @Test
    public void saveWithEmptyDataClearsGenes() throws ComponentLookupException
    {
        when(this.patient.getData(CONTROLLER_NAME))
            .thenReturn(new IndexedPatientData<>(CONTROLLER_NAME, Collections.emptyList()));
        Provider<XWikiContext> xcontextProvider = this.mocker.getInstance(XWikiContext.TYPE_PROVIDER);
        XWikiContext context = xcontextProvider.get();
        when(context.getWiki()).thenReturn(mock(XWiki.class));
        this.mocker.getComponentUnderTest().save(this.patient, this.doc);
        verify(this.doc).removeXObjects(GeneListController.GENE_CLASS_REFERENCE);

        Mockito.verifyNoMoreInteractions(this.doc);
    }

    @Test
    public void saveUpdatesGenes() throws ComponentLookupException, XWikiException
    {
        List<PhenoTipsGene> data = new LinkedList<>();
        PhenoTipsGene item = new PhenoTipsGene(null, "GENE1", null, null, "Notes1");
        data.add(item);
        item = new PhenoTipsGene(null, "GENE2", null, null, null);
        data.add(item);
        when(this.patient.<List<PhenoTipsGene>>getData(CONTROLLER_NAME))
            .thenReturn(new SimpleValuePatientData<>(CONTROLLER_NAME, data));

        Provider<XWikiContext> xcontextProvider = this.mocker.getInstance(XWikiContext.TYPE_PROVIDER);
        XWikiContext context = xcontextProvider.get();
        when(context.getWiki()).thenReturn(mock(XWiki.class));

        BaseObject o1 = mock(BaseObject.class);
        BaseObject o2 = mock(BaseObject.class);
        when(this.doc.newXObject(GeneListController.GENE_CLASS_REFERENCE, context)).thenReturn(o1, o2);

        this.mocker.getComponentUnderTest().save(this.patient, this.doc);

        verify(this.doc).removeXObjects(GeneListController.GENE_CLASS_REFERENCE);
        verify(o1).set(GENE_KEY, "GENE1", context);
        verify(o1).set(COMMENTS_KEY, "Notes1", context);
        verify(o2).set(GENE_KEY, "GENE2", context);
        verify(o2, Mockito.never()).set(eq(COMMENTS_KEY), anyString(), eq(context));
    }

    // ----------------------------------------Private methods----------------------------------------

    private void addGeneFields(String key, String[] fieldValues)
    {
        BaseObject obj;
        BaseStringProperty property;
        for (String value : fieldValues) {
            obj = mock(BaseObject.class);
            property = mock(BaseStringProperty.class);
            List<String> list = new ArrayList<>();
            list.add(value);
            doReturn(value).when(property).getValue();
            doReturn(property).when(obj).getField(key);
            doReturn(list).when(obj).getFieldList();
            this.geneXWikiObjects.add(obj);
        }
    }
}
