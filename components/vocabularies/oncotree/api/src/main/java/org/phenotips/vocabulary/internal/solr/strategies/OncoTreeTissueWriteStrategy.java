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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.solr.common.SolrInputDocument;

/**
 * An implementation of a {@link OncoTreePropertyWriteStrategy} for the tissue name property.
 *
 * @version $Id$
 * @since 1.4
 */
public class OncoTreeTissueWriteStrategy implements OncoTreePropertyWriteStrategy
{
    @Override
    public void execute(@Nonnull final SolrInputDocument doc, @Nonnull final String label, @Nullable final String value)
    {
        Validate.notNull(doc, "The SolrInputDocument must not be null.");
        Validate.notBlank(label, "The property label must not be blank.");
        if (StringUtils.isNotBlank(value)) {
            final String tissueName = StringUtils.substringBefore(value, "(").trim();
            doc.addField(label, tissueName);
        }
    }
}
