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
package org.phenotips.vocabularies.rest.internal;

import org.phenotips.rest.Autolinker;
import org.phenotips.vocabularies.rest.CategoryResource;
import org.phenotips.vocabularies.rest.CategoryTermSuggestionsResource;
import org.phenotips.vocabularies.rest.DomainObjectFactory;
import org.phenotips.vocabularies.rest.VocabularyResource;
import org.phenotips.vocabularies.rest.VocabularyTermSuggestionsResource;
import org.phenotips.vocabularies.rest.model.Category;
import org.phenotips.vocabularies.rest.model.VocabularyTermSummary;
import org.phenotips.vocabulary.Vocabulary;
import org.phenotips.vocabulary.VocabularyTerm;

import org.xwiki.component.annotation.Component;
import org.xwiki.security.authorization.Right;
import org.xwiki.stability.Unstable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import javax.ws.rs.core.UriInfo;

import org.json.JSONObject;

/**
 * @version $Id$
 * @since 1.3M1
 */
@Unstable
@Component
@Singleton
public class DefaultDomainObjectFactory implements DomainObjectFactory
{
    private static final String CATEGORY_LABEL = "category";

    private static final String VOCABULARY_ID_LABEL = "vocabulary-id";

    @Override
    public org.phenotips.vocabularies.rest.model.Vocabulary createVocabularyRepresentation(Vocabulary vocabulary)
    {
        org.phenotips.vocabularies.rest.model.Vocabulary result =
            new org.phenotips.vocabularies.rest.model.Vocabulary();
        result
            .withIdentifier(vocabulary.getIdentifier())
            .withName(vocabulary.getName())
            .withAliases(vocabulary.getAliases())
            .withSize(vocabulary.size())
            .withVersion(vocabulary.getVersion());
        try {
            result.withDefaultSourceLocation(vocabulary.getDefaultSourceLocation());
        } catch (UnsupportedOperationException e) {
            // Don't do anything and leave source empty
        }
        return result;
    }

    @Override
    public VocabularyTermSummary createVocabularyTermRepresentation(VocabularyTerm term)
    {
        VocabularyTermSummary rep = new VocabularyTermSummary();
        rep.withId(term.getId());
        rep.withName(term.getName());
        JSONObject jsonObject = term.toJSON();
        String symbolKey = "symbol";
        if (jsonObject != null && jsonObject.opt(symbolKey) != null) {
            rep.withSymbol(jsonObject.get(symbolKey).toString());
        }
        rep.withDescription(term.getDescription());
        return rep;
    }

    @Override
    public Category createCategoryRepresentation(@Nonnull final String category)
    {
        final Category categoryRep = new Category();
        categoryRep.withCategory(category);
        return categoryRep;
    }

    @Override
    public List<Category> createCategoriesList(
        @Nonnull final Collection<String> categoryIds,
        @Nonnull final Autolinker autolinker,
        @Nonnull final UriInfo uriInfo,
        final boolean userIsAdmin)
    {
        final List<Category> reps = new ArrayList<>();
        for (final String categoryId : categoryIds) {
            final Category category = createCategoryRepresentation(categoryId);
            category.withLinks(autolinker.forSecondaryResource(CategoryResource.class, uriInfo)
                .withActionableResources(CategoryTermSuggestionsResource.class)
                .withExtraParameters(CATEGORY_LABEL, categoryId)
                .withGrantedRight(userIsAdmin ? Right.ADMIN : Right.VIEW)
                .build());
            reps.add(category);
        }
        return reps;
    }

    @Override
    public List<org.phenotips.vocabularies.rest.model.Vocabulary> createVocabulariesList(
        @Nonnull final Set<Vocabulary> vocabularies,
        @Nonnull final Autolinker autolinker,
        @Nonnull final UriInfo uriInfo,
        final boolean userIsAdmin)
    {
        final List<org.phenotips.vocabularies.rest.model.Vocabulary> reps = new ArrayList<>();
        for (final Vocabulary vocabulary : vocabularies) {
            final org.phenotips.vocabularies.rest.model.Vocabulary rep = createVocabularyRepresentation(vocabulary);
            rep.withLinks(autolinker.forSecondaryResource(VocabularyResource.class, uriInfo)
                .withActionableResources(VocabularyTermSuggestionsResource.class)
                .withExtraParameters(VOCABULARY_ID_LABEL, vocabulary.getIdentifier())
                .withGrantedRight(userIsAdmin ? Right.ADMIN : Right.VIEW)
                .build());
            reps.add(rep);
        }
        return reps;
    }

}
