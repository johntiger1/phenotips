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

import java.util.Arrays;
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
 * Tests for the {@link DefaultOncoTreePropertyWriteStrategy} class.
 */
public class DefaultOncoTreePropertyWriteStrategyTest
{
    private static final String PROPERTY_LABEL = "property";

    private static final String PROPERTY_VALUE = "Some property value";

    private static final String SECOND_PROPERTY_VALUE = "Second property value";

    private OncoTreePropertyWriteStrategy strategy;

    private SolrInputDocument doc;

    @Before
    public void setUp()
    {
        this.strategy = new DefaultOncoTreePropertyWriteStrategy();
        this.doc = new SolrInputDocument();
    }

    @Test(expected = Exception.class)
    public void executeThrowsExceptionIfDocIsNull()
    {
        this.strategy.execute(null, PROPERTY_LABEL, PROPERTY_VALUE);
    }

    @Test(expected = Exception.class)
    public void executeThrowsExceptionIfLabelIsNull()
    {
        this.strategy.execute(this.doc, null, PROPERTY_VALUE);
    }

    @Test(expected = Exception.class)
    public void executeThrowsExceptionIfLabelIsEmpty()
    {
        this.strategy.execute(this.doc, StringUtils.EMPTY, PROPERTY_VALUE);
    }

    @Test(expected = Exception.class)
    public void executeThrowsExceptionIfLabelIsBlank()
    {
        this.strategy.execute(this.doc, StringUtils.SPACE, PROPERTY_VALUE);
    }

    @Test
    public void executeDoesNothingIfValueIsBlank()
    {
        final SolrInputDocument spyDoc = spy(this.doc);
        this.strategy.execute(spyDoc, PROPERTY_LABEL, StringUtils.SPACE);
        verifyZeroInteractions(spyDoc);
        this.strategy.execute(spyDoc, PROPERTY_LABEL, StringUtils.EMPTY);
        verifyZeroInteractions(spyDoc);
        this.strategy.execute(spyDoc, PROPERTY_LABEL, null);
        verifyZeroInteractions(spyDoc);
    }

    @Test
    public void executeWritesValueAsIs()
    {
        final SolrInputDocument spyDoc = spy(this.doc);
        this.strategy.execute(spyDoc, PROPERTY_LABEL, PROPERTY_VALUE);
        verify(spyDoc, times(1)).addField(PROPERTY_LABEL, PROPERTY_VALUE);
        Assert.assertEquals(1, this.doc.size());
        Assert.assertEquals(PROPERTY_VALUE, this.doc.getFieldValue(PROPERTY_LABEL));
    }

    @Test
    public void executeAddsMoreValuesToSameProperty()
    {
        this.doc.addField(OncoTreeUtils.TERM_CATEGORY, PROPERTY_VALUE);
        Assert.assertEquals(1, this.doc.size());
        Assert.assertEquals(Collections.singletonList(PROPERTY_VALUE), this.doc.getFieldValues(OncoTreeUtils.TERM_CATEGORY));

        final SolrInputDocument spyDoc = spy(this.doc);
        this.strategy.execute(spyDoc, OncoTreeUtils.TERM_CATEGORY, SECOND_PROPERTY_VALUE);
        verify(spyDoc, times(1)).addField(OncoTreeUtils.TERM_CATEGORY, SECOND_PROPERTY_VALUE);
        Assert.assertEquals(1, this.doc.size());
        Assert.assertEquals(Arrays.asList(PROPERTY_VALUE, SECOND_PROPERTY_VALUE),
            this.doc.getFieldValues(OncoTreeUtils.TERM_CATEGORY));
    }
}
