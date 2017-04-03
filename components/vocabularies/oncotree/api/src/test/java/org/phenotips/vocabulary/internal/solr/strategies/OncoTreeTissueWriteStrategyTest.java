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

import org.phenotips.vocabulary.internal.solr.OncoTree;
import org.phenotips.vocabulary.internal.solr.OncoTreeUtils;

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
 * Tests for the {@link OncoTreeTissueWriteStrategy} class.
 */
public class OncoTreeTissueWriteStrategyTest
{
    private static final String TISSUE_BRACKET_VALUE = " Tissue name (ABC)";

    private static final String TISSUE_NO_BRACKET_VALUE = "Tissue name";

    private OncoTreePropertyWriteStrategy strategy;

    private SolrInputDocument doc;

    @Before
    public void setUp()
    {
        this.strategy = new OncoTreeTissueWriteStrategy();
        this.doc = new SolrInputDocument();
    }

    @Test(expected = Exception.class)
    public void executeThrowsExceptionIfDocIsNull()
    {
        this.strategy.execute(null, OncoTreeUtils.TISSUE, TISSUE_BRACKET_VALUE);
    }

    @Test(expected = Exception.class)
    public void executeThrowsExceptionIfLabelIsNull()
    {
        this.strategy.execute(this.doc, null, TISSUE_BRACKET_VALUE);
    }

    @Test(expected = Exception.class)
    public void executeThrowsExceptionIfLabelIsEmpty()
    {
        this.strategy.execute(this.doc, StringUtils.EMPTY, TISSUE_BRACKET_VALUE);
    }

    @Test(expected = Exception.class)
    public void executeThrowsExceptionIfLabelIsBlank()
    {
        this.strategy.execute(this.doc, StringUtils.SPACE, TISSUE_BRACKET_VALUE);
    }

    @Test
    public void executeDoesNothingIfValueIsBlank()
    {
        final SolrInputDocument spyDoc = spy(this.doc);
        this.strategy.execute(spyDoc, OncoTreeUtils.TISSUE, StringUtils.SPACE);
        verifyZeroInteractions(spyDoc);
        this.strategy.execute(spyDoc, OncoTreeUtils.TISSUE, StringUtils.EMPTY);
        verifyZeroInteractions(spyDoc);
        this.strategy.execute(spyDoc, OncoTreeUtils.TISSUE, null);
        verifyZeroInteractions(spyDoc);
    }

    @Test
    public void executeDataInBracketsRemovedIfPresent()
    {
        final SolrInputDocument spyDoc = spy(this.doc);
        this.strategy.execute(spyDoc, OncoTreeUtils.TISSUE, TISSUE_BRACKET_VALUE);
        verify(spyDoc, times(1)).addField(OncoTreeUtils.TISSUE, TISSUE_NO_BRACKET_VALUE);
        Assert.assertEquals(1, this.doc.size());
        Assert.assertEquals(TISSUE_NO_BRACKET_VALUE, this.doc.getFieldValue(OncoTreeUtils.TISSUE));
    }

    @Test
    public void executeWhenNoDataInBracketsWriteUnmodified()
    {
        final SolrInputDocument spyDoc = spy(this.doc);
        this.strategy.execute(spyDoc, OncoTreeUtils.TISSUE, TISSUE_NO_BRACKET_VALUE);
        verify(spyDoc, times(1)).addField(OncoTreeUtils.TISSUE, TISSUE_NO_BRACKET_VALUE);
        Assert.assertEquals(1, this.doc.size());
        Assert.assertEquals(TISSUE_NO_BRACKET_VALUE, this.doc.getFieldValue(OncoTreeUtils.TISSUE));
    }
}
