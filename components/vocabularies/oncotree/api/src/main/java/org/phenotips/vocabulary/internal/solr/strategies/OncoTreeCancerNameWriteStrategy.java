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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of a {@link OncoTreePropertyWriteStrategy} for the cancer name property.
 *
 * @version $Id$
 * @since 1.4
 */
public class OncoTreeCancerNameWriteStrategy implements OncoTreePropertyWriteStrategy
{
    private static final String ID_PREFIX = "ONCO:";

    private static final String OPEN_BRACKET = "(";

    private static final String CLOSED_BRACKET = ")";

    private Logger logger = LoggerFactory.getLogger(OncoTreeCancerNameWriteStrategy.class);

    @Override
    public void execute(@Nonnull final SolrInputDocument doc, @Nonnull final String label, @Nullable final String value)
    {
        Validate.notNull(doc, "The SolrInputDocument must not be null.");
        Validate.notBlank(label, "The property label must not be blank.");
        if (StringUtils.isNotBlank(value)) {
            updateParentNode(doc);
            updateCancerId(doc, value);
            updateCancerName(doc, value);
        }
    }

    /**
     * Updates the name of the cancer.
     *
     * @param doc the {@link SolrInputDocument}
     * @param name the name of the cancer, that will be processed
     */
    private void updateCancerName(@Nonnull final SolrInputDocument doc, @Nonnull final String name)
    {
        final String updatedName = StringUtils.substringBefore(name, OPEN_BRACKET).trim();
        doc.setField(OncoTreeUtils.NAME, updatedName);
    }

    /**
     * Updates the cancer identifier.
     *
     * @param doc the {@link SolrInputDocument}
     * @param name the name of the cancer, from which the ID will be extracted
     */
    private void updateCancerId(@Nonnull final SolrInputDocument doc, @Nonnull final String name)
    {
        final String id = StringUtils.substringBetween(name, OPEN_BRACKET, CLOSED_BRACKET);
        if (StringUtils.isNotBlank(id)) {
            doc.setField(OncoTreeUtils.ID, ID_PREFIX + id);
        } else {
            this.logger.warn("No identifier could be extracted from the provided cancer name: {}", name);
            doc.setField(OncoTreeUtils.ID, ID_PREFIX + name);
        }
    }

    /**
     * Updates the ancestor data for the cancer.
     *
     * @param doc the {@link SolrInputDocument}
     */
    private void updateParentNode(@Nonnull final SolrInputDocument doc)
    {
        final String prevId = (String) doc.getFieldValue(OncoTreeUtils.ID);
        if (StringUtils.isNotBlank(prevId)) {
            doc.setField(OncoTreeUtils.IS_A, prevId);
            doc.addField(OncoTreeUtils.TERM_CATEGORY, prevId);
        }
    }
}
