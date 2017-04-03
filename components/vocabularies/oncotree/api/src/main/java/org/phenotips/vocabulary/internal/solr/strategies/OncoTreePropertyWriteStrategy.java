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

import org.apache.solr.common.SolrInputDocument;

/**
 * Defines a strategy for writing a property extracted from the OncoTree data file.
 *
 * @version $Id$
 * @since 1.4
 */
public interface OncoTreePropertyWriteStrategy
{
    /**
     * Writes the {@code label} and {@code value} data into the {@code doc} according to some write strategy.
     *
     * @param doc the {@link SolrInputDocument}
     * @param label the property label
     * @param value the property value
     */
    void execute(@Nonnull SolrInputDocument doc, @Nonnull String label, @Nullable String value);
}
