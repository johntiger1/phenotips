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
package org.phenotips.vocabularies.rest;

import org.phenotips.rest.Autolinker;
import org.phenotips.vocabularies.rest.model.Category;
import org.phenotips.vocabularies.rest.model.VocabularyTermSummary;
import org.phenotips.vocabulary.Vocabulary;
import org.phenotips.vocabulary.VocabularyTerm;

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.UriInfo;

/**
 * Factory for converting internal java objects into their REST representations.
 *
 * @version $Id$
 * @since 1.3M1
 */
@Unstable("New API introduced in 1.3")
@Role
public interface DomainObjectFactory
{
    /**
     * Converts a {@link Vocabulary} into its REST representation,
     * {@link org.phenotips.vocabularies.rest.model.Vocabulary}.
     *
     * @param vocabulary the vocabulary to be converted
     * @return the REST representation without links
     */
    org.phenotips.vocabularies.rest.model.Vocabulary createVocabularyRepresentation(Vocabulary vocabulary);

    /**
     * Converts a {@link VocabularyTerm} into a summary representation,
     * {@link org.phenotips.vocabularies.rest.model.VocabularyTermSummary}.
     *
     * @param term the term to be converted
     * @return a REST representation summarizing the term information
     */
    VocabularyTermSummary createVocabularyTermRepresentation(VocabularyTerm term);

    /**
     * Converts a category name and a list of associated {@link Vocabulary} identifiers into a {@link Category category}
     * REST representation.
     *
     * @param category the name of the category, as string
     * @return a REST representation of the {@link Category category}
     */
    Category createCategoryRepresentation(String category);

    /**
     * Creates a list of {@link Category} objects for a collection of {@code categoryIds}.
     *
     * @param categoryIds a list of category identifiers
     * @param autolinker an {@link Autolinker} object for generation fo links
     * @param uriInfo the {@link UriInfo} object
     * @param userIsAdmin true iff user has admin rights, false otherwise
     * @return a list of {@link Category} objects that the {@code vocabulary} belongs to
     */
    List<Category> createCategoriesList(Collection<String> categoryIds, Autolinker autolinker, UriInfo uriInfo,
        boolean userIsAdmin);

    /**
     * Creates a list of {@link org.phenotips.vocabularies.rest.model.Vocabulary} object representations of
     * {@code vocabularies}.
     *
     * @param vocabularies a set of {@link Vocabulary} objects
     * @param autolinker an {@link Autolinker} object for generation fo links
     * @param uriInfo the {@link UriInfo} object
     * @param userIsAdmin true iff user has admin rights, false otherwise
     * @return a list of {@link org.phenotips.vocabularies.rest.model.Vocabulary} objects
     */
    List<org.phenotips.vocabularies.rest.model.Vocabulary> createVocabulariesList(Set<Vocabulary> vocabularies,
        Autolinker autolinker, UriInfo uriInfo, boolean userIsAdmin);
}
