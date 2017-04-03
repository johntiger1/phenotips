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

import org.phenotips.vocabulary.internal.solr.strategies.DefaultOncoTreePropertyWriteStrategy;
import org.phenotips.vocabulary.internal.solr.strategies.OncoTreeCancerNameWriteStrategy;
import org.phenotips.vocabulary.internal.solr.strategies.OncoTreePropertyWriteStrategy;
import org.phenotips.vocabulary.internal.solr.strategies.OncoTreeTissueWriteStrategy;

import org.xwiki.text.StringUtils;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link OncoTreePropertyWriteStrategyPicker} class.
 */
public class OncoTreePropertyWriteStrategyPickerTest
{
    private static final String CANCER_32_LABEL = "cancer32";

    private static final String RANDOM_LABEL = "rand";

    private OncoTreePropertyWriteStrategyPicker picker;

    @Before
    public void setUp()
    {
        this.picker = new OncoTreePropertyWriteStrategyPicker();
    }

    @Test
    public void getWithTissueLabelPicksTissueStrategy()
    {
        Collection<OncoTreePropertyWriteStrategy> strategies = this.picker.get(OncoTreeUtils.TISSUE);
        Assert.assertEquals(1, strategies.size());
        Assert.assertTrue(OncoTreeTissueWriteStrategy.class.isInstance(strategies.iterator().next()));
    }

    @Test
    public void getWithSomeCancerLabelPicksCancerNameStrategy()
    {
        Collection<OncoTreePropertyWriteStrategy> strategies = this.picker.get(CANCER_32_LABEL);
        Assert.assertEquals(1, strategies.size());
        Assert.assertTrue(OncoTreeCancerNameWriteStrategy.class.isInstance(strategies.iterator().next()));
    }

    @Test
    public void getWithUnknownLabelPicksDefaultStrategy()
    {
        Collection<OncoTreePropertyWriteStrategy> strategies = this.picker.get(RANDOM_LABEL);
        Assert.assertEquals(1, strategies.size());
        Assert.assertTrue(DefaultOncoTreePropertyWriteStrategy.class.isInstance(strategies.iterator().next()));
    }

    @Test(expected = Exception.class)
    public void getWithNullLabelThrowsException()
    {
        this.picker.get(null);
    }

    @Test(expected = Exception.class)
    public void getWithBlankLabelThrowsException()
    {
        this.picker.get(StringUtils.EMPTY);
    }
}
