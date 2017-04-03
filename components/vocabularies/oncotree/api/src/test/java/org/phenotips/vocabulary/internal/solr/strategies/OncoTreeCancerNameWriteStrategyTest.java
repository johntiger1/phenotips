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
package org.phenotips.vocabulary.internal.solr.strategies;

import org.phenotips.vocabulary.internal.solr.OncoTreeUtils;

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Tests for the {@link OncoTreeCancerNameWriteStrategy} class.
 */
public class OncoTreeCancerNameWriteStrategyTest
{
    private static final String SYMBOL_LABEL = "SYMBOL";

    private static final String NAME_BRACKET_VALUE = "Cancer name (SYMBOL)";

    private static final String NAME_NO_BRACKET_VALUE = "Cancer name";

    private static final String PREFIX = "ONCO:";

    private static final String OLD_ID_LABEL = "OLD";

    private static final String PARENT_NAME_VALUE = "Parent node name";

    private OncoTreePropertyWriteStrategy strategy;

    private SolrInputDocument doc;

    @Before
    public void setUp()
    {
        this.strategy = new OncoTreeCancerNameWriteStrategy();
        this.doc = new SolrInputDocument();
    }

    @Test(expected = Exception.class)
    public void executeThrowsExceptionIfDocIsNull()
    {
        this.strategy.execute(null, OncoTreeUtils.CANCER, NAME_BRACKET_VALUE);
    }

    @Test(expected = Exception.class)
    public void executeThrowsExceptionIfLabelIsNull()
    {
        this.strategy.execute(this.doc, null, NAME_BRACKET_VALUE);
    }

    @Test(expected = Exception.class)
    public void executeThrowsExceptionIfLabelIsEmpty()
    {
        this.strategy.execute(this.doc, StringUtils.EMPTY, NAME_BRACKET_VALUE);
    }

    @Test(expected = Exception.class)
    public void executeThrowsExceptionIfLabelIsBlank()
    {
        this.strategy.execute(this.doc, StringUtils.SPACE, NAME_BRACKET_VALUE);
    }

    @Test
    public void executeDoesNothingIfValueIsBlank()
    {
        final SolrInputDocument spyDoc = spy(this.doc);
        this.strategy.execute(spyDoc, OncoTreeUtils.CANCER, StringUtils.SPACE);
        verifyZeroInteractions(spyDoc);
        this.strategy.execute(spyDoc, OncoTreeUtils.CANCER, StringUtils.EMPTY);
        verifyZeroInteractions(spyDoc);
        this.strategy.execute(spyDoc, OncoTreeUtils.CANCER, null);
        verifyZeroInteractions(spyDoc);
    }

    @Test
    public void executeCancerNameParsedIntoNameAndIdData()
    {
        final SolrInputDocument spyDoc = spy(this.doc);
        this.strategy.execute(spyDoc, OncoTreeUtils.CANCER, NAME_BRACKET_VALUE);
        verify(spyDoc, times(1)).setField(OncoTreeUtils.ID, PREFIX + SYMBOL_LABEL);
        verify(spyDoc, times(1)).setField(OncoTreeUtils.NAME, NAME_NO_BRACKET_VALUE);
        Assert.assertEquals(2, this.doc.size());
        Assert.assertEquals(PREFIX + SYMBOL_LABEL, this.doc.getFieldValue(OncoTreeUtils.ID));
        Assert.assertEquals(NAME_NO_BRACKET_VALUE, this.doc.getFieldValue(OncoTreeUtils.NAME));
    }

    @Test
    public void executeCancerNameHasNoIdDataPreservedAsIs()
    {
        final SolrInputDocument spyDoc = spy(this.doc);
        this.strategy.execute(spyDoc, OncoTreeUtils.CANCER, NAME_NO_BRACKET_VALUE);
        verify(spyDoc, times(1)).setField(OncoTreeUtils.ID, PREFIX + NAME_NO_BRACKET_VALUE);
        verify(spyDoc, times(1)).setField(OncoTreeUtils.NAME, NAME_NO_BRACKET_VALUE);
        Assert.assertEquals(2, this.doc.size());
        Assert.assertEquals(PREFIX + NAME_NO_BRACKET_VALUE, this.doc.getFieldValue(OncoTreeUtils.ID));
        Assert.assertEquals(NAME_NO_BRACKET_VALUE, this.doc.getFieldValue(OncoTreeUtils.NAME));
    }

    @Test
    public void executeParentDataIsUpdated()
    {
        this.doc.setField(OncoTreeUtils.ID, PREFIX + OLD_ID_LABEL);
        this.doc.setField(OncoTreeUtils.NAME, PARENT_NAME_VALUE);
        Assert.assertEquals(2, this.doc.size());
        Assert.assertEquals(PREFIX + OLD_ID_LABEL, this.doc.getFieldValue(OncoTreeUtils.ID));
        Assert.assertEquals(PARENT_NAME_VALUE, this.doc.getFieldValue(OncoTreeUtils.NAME));

        final SolrInputDocument spyDoc = spy(this.doc);
        this.strategy.execute(spyDoc, OncoTreeUtils.CANCER, NAME_BRACKET_VALUE);
        Assert.assertEquals(4, this.doc.size());
        Assert.assertEquals(PREFIX + OLD_ID_LABEL, this.doc.getFieldValue(OncoTreeUtils.IS_A));
        Assert.assertEquals(Collections.singletonList(PREFIX + OLD_ID_LABEL), this.doc.getFieldValues(OncoTreeUtils.TERM_CATEGORY));
        Assert.assertEquals(PREFIX + SYMBOL_LABEL, this.doc.getFieldValue(OncoTreeUtils.ID));
        Assert.assertEquals(NAME_NO_BRACKET_VALUE, this.doc.getFieldValue(OncoTreeUtils.NAME));
    }
}
