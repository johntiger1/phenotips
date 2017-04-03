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
import java.util.Collections;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.Validate;

/**
 * A {@link OncoTreePropertyWriteStrategy} picker for {@link OncoTree} field data.
 *
 * @version $Id$
 * @since 1.4
 */
class OncoTreePropertyWriteStrategyPicker
{
    /**
     * Returns a collection of {@link OncoTreePropertyWriteStrategy} objects for provided field {@code label}.
     *
     * @param label the label of the field
     */
    Collection<OncoTreePropertyWriteStrategy> get(@Nonnull final String label)
    {
        Validate.notBlank(label, "The property label must not be blank.");
        if (StringUtils.contains(label, OncoTreeUtils.CANCER)) {
            return Collections.<OncoTreePropertyWriteStrategy>singletonList(new OncoTreeCancerNameWriteStrategy());
        }
        if (OncoTreeUtils.TISSUE.equals(label)) {
            return Collections.<OncoTreePropertyWriteStrategy>singletonList(new OncoTreeTissueWriteStrategy());
        }
        return Collections.<OncoTreePropertyWriteStrategy>singletonList(new DefaultOncoTreePropertyWriteStrategy());
    }
}
