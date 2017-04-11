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
package org.phenotips.vocabulary.internal.solr;

import org.phenotips.vocabulary.SolrVocabularyResourceManager;
import org.phenotips.vocabulary.Vocabulary;
import org.phenotips.vocabulary.VocabularyTerm;

import org.xwiki.cache.Cache;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.test.mockito.MockitoComponentMockingRule;
import org.xwiki.text.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OncoTree} class.
 */
public class OncoTreeTest
{
    private static final String ONCO_LOWER = "onco";

    private static final String PREFIX = "ONCO:";

    private static final String BLUE_LABEL = "青い";

    private static final String PURPLE_LABEL = "Purple";

    private static final String ESOPHAGUS_STOMACH_LABEL = "Esophagus/Stomach";

    private static final String THYMUS_LABEL = "Thymus";

    private static final String THYMIC_TUMOR_LABEL = "Thymic Tumor";

    private static final String EGC_LABEL = "EGC";

    private static final String STAD_LABEL = "STAD";

    private static final String TET_LABEL = "TET";

    private static final String THYC_LABEL = "THYC";

    private static final String TNET_LABEL = "TNET";

    private static final String EGC_NAME_LABEL = "Esophagogastric Adenocarcinoma";

    private static final String TNET_NAME_LABEL = "Thymic Neuroendocrine Tumor";

    private static final String ACA_LABEL = "ACA";

    private static final String ADRENAL_GLAND_LABEL = "Adrenal Gland";

    private static final String ESOPHAGOGASTRIC_CANCER_LABEL = "Esophagogastric Cancer";

    private static final String DSTAD_LABEL = "DSTAD";

    private static final String STAD_NAME_LABEL = "Stomach Adenocarcinoma";

    private static final String THYC_NAME_LABEL = "Thymic Carcinoma";

    private static final String ACA_NAME_LABEL = "Adrenocortical Adenoma";

    private static final String ADRENOCORTICAL_CARCINOMA_LABEL = "Adrenocortical Carcinoma";

    private static final String DSTAD_NAME_LABEL = "Diffuse Type Stomach Adenocarcinoma";

    private static final String TET_NAME_LABEL = "Thymic Epithelial Tumor";

    private static final String ONCOTREE_LABEL = "OncoTree";

    private static final String ONCO_UPPER = "ONCO";

    private static final String TISSUE = "tissue";

    private static final String ID = "id";

    private static final String NAME = "name";

    private static final String TERM_GROUP = "term_group";

    private static final String COLOUR = "colour";

    private static final String NCI_ID = "nci_id";

    private static final String UMLS_ID = "umls_id";

    private static final String IS_A = "is_a";

    private static final String TERM_CATEGORY = "term_category";

    @Rule
    public MockitoComponentMockingRule<Vocabulary> mocker = new MockitoComponentMockingRule<Vocabulary>(OncoTree.class);

    @Mock
    private Cache<VocabularyTerm> cache;

    @Mock
    private SolrClient server;

    private int ontologyServiceResult;

    private OncoTree component;

    private Logger logger;

    private URL url;

    @Before
    public void setUp() throws ComponentLookupException, IOException
    {
        MockitoAnnotations.initMocks(this);
        SolrVocabularyResourceManager externalServicesAccess =
            this.mocker.getInstance(SolrVocabularyResourceManager.class);
        when(externalServicesAccess.getTermCache(ONCO_LOWER)).thenReturn(this.cache);
        when(externalServicesAccess.getSolrConnection(ONCO_LOWER)).thenReturn(this.server);
        final OncoTree oncoTree = (OncoTree) this.mocker.getComponentUnderTest();
        this.logger = this.mocker.getMockedLogger();
        this.component = spy(oncoTree);
        this.ontologyServiceResult =
            this.component.reindex(this.getClass().getResource("/test.rdf").toString());
        this.url = new URL(this.component.getDefaultSourceLocation());
        final InputStream inputStream = getInputStream();
        doReturn(inputStream).when(this.component).getInputStream(any(URL.class));
    }

    @Test
    public void reindexOncoTree() throws IOException, SolrServerException
    {
        Mockito.verify(this.server).deleteByQuery("*:*");
        Mockito.verify(this.server, Mockito.atLeast(1)).commit();
        Mockito.verify(this.server, Mockito.atLeast(1)).add(
            Matchers.anyCollectionOf(SolrInputDocument.class));
        Mockito.verify(this.cache, Mockito.atLeast(1)).removeAll();
        Mockito.verifyNoMoreInteractions(this.cache, this.server);
        Assert.assertTrue(this.ontologyServiceResult == 0);
    }

    @Test
    public void getVersionForOncoTree() throws Exception
    {
        final QueryResponse response = mock(QueryResponse.class);
        when(this.server.query(any(SolrQuery.class))).thenReturn(response);
        final SolrDocumentList results = mock(SolrDocumentList.class);
        when(response.getResults()).thenReturn(results);
        when(results.isEmpty()).thenReturn(false);
        final SolrDocument versionDoc = mock(SolrDocument.class);
        when(results.get(0)).thenReturn(versionDoc);
        when(versionDoc.getFieldValue("version")).thenReturn("2014:01:01");
        Assert.assertEquals("2014:01:01", this.component.getVersion());
    }

    @Test
    public void getSolrDocsPerBatchForOncoTree()
    {
        Assert.assertEquals(15000, this.component.getSolrDocsPerBatch());
    }

    @Test
    public void loadReturnsEmptyDataWhenExceptionThrownWhenObtainingInputStream() throws IOException
    {
        doThrow(IOException.class).when(this.component).getInputStream(any(URL.class));
        final Collection<SolrInputDocument> data = this.component.load(this.url);
        verify(this.logger).error("Failed to load vocabulary source: {}", (Object) null);
        Assert.assertTrue(data.isEmpty());
    }

    @Test
    public void loadReturnsCorrectDataWhenReadingFromSourceWithNonEnglishChars() throws IOException
    {
        final Collection<SolrInputDocument> data = this.component.load(this.url);
        final Iterator<SolrInputDocument> iterator = data.iterator();
        Assert.assertEquals(7, data.size());

        SolrInputDocument datum = iterator.next();
        Assert.assertEquals(7, datum.size());
        Assert.assertEquals(ADRENAL_GLAND_LABEL, datum.getFieldValue(TISSUE));
        Assert.assertEquals(PREFIX + ACA_LABEL, datum.getFieldValue(ID));
        Assert.assertEquals(ACA_NAME_LABEL, datum.getFieldValue(NAME));
        Assert.assertEquals(ADRENOCORTICAL_CARCINOMA_LABEL, datum.getFieldValue(TERM_GROUP));
        Assert.assertEquals(PURPLE_LABEL, datum.getFieldValue(COLOUR));
        Assert.assertEquals("C9003", datum.getFieldValue(NCI_ID));
        Assert.assertEquals("C0206667", datum.getFieldValue(UMLS_ID));

        datum = iterator.next();
        Assert.assertEquals(7, datum.size());
        Assert.assertEquals(ESOPHAGUS_STOMACH_LABEL, datum.getFieldValue(TISSUE));
        Assert.assertEquals(PREFIX + EGC_LABEL, datum.getFieldValue(ID));
        Assert.assertEquals(EGC_NAME_LABEL, datum.getFieldValue(NAME));
        Assert.assertEquals(ESOPHAGOGASTRIC_CANCER_LABEL, datum.getFieldValue(TERM_GROUP));
        Assert.assertEquals(BLUE_LABEL, datum.getFieldValue(COLOUR));
        Assert.assertEquals("C9296", datum.getFieldValue(NCI_ID));
        Assert.assertEquals("C1332166", datum.getFieldValue(UMLS_ID));

        datum = iterator.next();
        Assert.assertEquals(9, datum.size());
        Assert.assertEquals(ESOPHAGUS_STOMACH_LABEL, datum.getFieldValue(TISSUE));
        Assert.assertEquals(PREFIX + STAD_LABEL, datum.getFieldValue(ID));
        Assert.assertEquals(STAD_NAME_LABEL, datum.getFieldValue(NAME));
        Assert.assertEquals(ESOPHAGOGASTRIC_CANCER_LABEL, datum.getFieldValue(TERM_GROUP));
        Assert.assertEquals(BLUE_LABEL, datum.getFieldValue(COLOUR));
        Assert.assertEquals("C4004", datum.getFieldValue(NCI_ID));
        Assert.assertEquals("C0278701", datum.getFieldValue(UMLS_ID));
        Assert.assertEquals(PREFIX + EGC_LABEL, datum.getFieldValue(IS_A));
        Assert.assertEquals(Collections.singletonList(PREFIX + EGC_LABEL),
            datum.getFieldValues(TERM_CATEGORY));

        datum = iterator.next();
        Assert.assertEquals(9, datum.size());
        Assert.assertEquals(ESOPHAGUS_STOMACH_LABEL, datum.getFieldValue(TISSUE));
        Assert.assertEquals(PREFIX + DSTAD_LABEL, datum.getFieldValue(ID));
        Assert.assertEquals(DSTAD_NAME_LABEL, datum.getFieldValue(NAME));
        Assert.assertEquals(ESOPHAGOGASTRIC_CANCER_LABEL, datum.getFieldValue(TERM_GROUP));
        Assert.assertEquals(BLUE_LABEL, datum.getFieldValue(COLOUR));
        Assert.assertEquals("C9159", datum.getFieldValue(NCI_ID));
        Assert.assertEquals("C0279635", datum.getFieldValue(UMLS_ID));
        Assert.assertEquals(PREFIX + STAD_LABEL, datum.getFieldValue(IS_A));
        Assert.assertEquals(Arrays.asList(PREFIX + EGC_LABEL, PREFIX + STAD_LABEL),
            datum.getFieldValues(TERM_CATEGORY));

        datum = iterator.next();
        Assert.assertEquals(7, datum.size());
        Assert.assertEquals(THYMUS_LABEL, datum.getFieldValue(TISSUE));
        Assert.assertEquals(PREFIX + TET_LABEL, datum.getFieldValue(ID));
        Assert.assertEquals(TET_NAME_LABEL, datum.getFieldValue(NAME));
        Assert.assertEquals(THYMIC_TUMOR_LABEL, datum.getFieldValue(TERM_GROUP));
        Assert.assertEquals(PURPLE_LABEL, datum.getFieldValue(COLOUR));
        Assert.assertEquals("C6450", datum.getFieldValue(NCI_ID));
        Assert.assertEquals("C1266101", datum.getFieldValue(UMLS_ID));

        datum = iterator.next();
        Assert.assertEquals(9, datum.size());
        Assert.assertEquals(THYMUS_LABEL, datum.getFieldValue(TISSUE));
        Assert.assertEquals(PREFIX + THYC_LABEL, datum.getFieldValue(ID));
        Assert.assertEquals(THYC_NAME_LABEL, datum.getFieldValue(NAME));
        Assert.assertEquals(THYMIC_TUMOR_LABEL, datum.getFieldValue(TERM_GROUP));
        Assert.assertEquals(PURPLE_LABEL, datum.getFieldValue(COLOUR));
        Assert.assertEquals("C7569", datum.getFieldValue(NCI_ID));
        Assert.assertEquals("C0205969", datum.getFieldValue(UMLS_ID));
        Assert.assertEquals(PREFIX + TET_LABEL, datum.getFieldValue(IS_A));
        Assert.assertEquals(Collections.singletonList(PREFIX + TET_LABEL),
            datum.getFieldValues(TERM_CATEGORY));

        datum = iterator.next();
        Assert.assertEquals(6, datum.size());
        Assert.assertEquals(THYMUS_LABEL, datum.getFieldValue(TISSUE));
        Assert.assertEquals(PREFIX + TNET_LABEL, datum.getFieldValue(ID));
        Assert.assertEquals(TNET_NAME_LABEL, datum.getFieldValue(NAME));
        Assert.assertEquals(THYMIC_TUMOR_LABEL, datum.getFieldValue(TERM_GROUP));
        Assert.assertEquals(PURPLE_LABEL, datum.getFieldValue(COLOUR));
        Assert.assertEquals("CL511204", datum.getFieldValue(UMLS_ID));
    }

    @Test
    public void getCoreName()
    {
        Assert.assertEquals(ONCO_LOWER, this.component.getCoreName());
    }

    @Test
    public void getIdentifier()
    {
        Assert.assertEquals(ONCO_LOWER, this.component.getIdentifier());
    }

    @Test
    public void getName()
    {
        Assert.assertEquals(ONCOTREE_LABEL, this.component.getName());
    }

    @Test
    public void getAliases()
    {
        final Set<String> aliases = new HashSet<>();
        aliases.add(ONCOTREE_LABEL);
        aliases.add(ONCO_LOWER);
        aliases.add(ONCO_UPPER);
        Assert.assertEquals(aliases, this.component.getAliases());
    }

    @Test
    public void getDefaultSourceLocation()
    {
        Assert.assertEquals("http://oncotree.mskcc.org/oncotree/api/tumor_types.txt", this.component.getDefaultSourceLocation());
    }

    @Test
    public void getWebsite()
    {
        Assert.assertEquals("http://oncotree.mskcc.org/oncotree/", this.component.getWebsite());
    }

    @Test
    public void getCitation()
    {
        // FIXME: update this.
        Assert.assertEquals(StringUtils.EMPTY, this.component.getCitation());
    }

    /**
     * Gets an input stream from a file.
     *
     * @return an {@link InputStream}
     * @throws FileNotFoundException if the file cannot be found
     */
    private InputStream getInputStream() throws FileNotFoundException
    {
        final File file = new File("src/test/resources/test.rdf");
        return new FileInputStream(file);
    }
}
