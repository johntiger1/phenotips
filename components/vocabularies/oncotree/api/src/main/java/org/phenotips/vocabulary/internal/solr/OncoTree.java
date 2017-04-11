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

import org.phenotips.vocabulary.VocabularyTerm;

import org.xwiki.component.annotation.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.DisMaxParams;
import org.apache.solr.common.params.SpellingParams;

import com.google.common.annotations.VisibleForTesting;

/**
 * Provides access to the OncoTree vocabulary. The vocabulary prefix is {@code ONCO}.
 *
 * @version $Id$
 * @since 1.4
 */
@Component
@Named("onco")
@Singleton
public class OncoTree extends AbstractCSVSolrVocabulary
{
    /** The default location of the OncoTree data file. */
    private static final String SOURCE = "http://oncotree.mskcc.org/oncotree/api/tumor_types.txt";

    private static final String CANCER = "cancer";

    private static final String TISSUE = "tissue";

    private static final String TERM_GROUP = "term_group";

    private static final String COLOUR = "colour";

    private static final String NCI_ID = "nci_id";

    private static final String UMLS_ID = "umls_id";

    private static final String IS_A = "is_a";

    private static final String TERM_CATEGORY = "term_category";

    private static final String ID = "id";

    private static final String NAME = "name";

    private static final String SEPARATOR = ":";

    /** Column labels for the tab-separated cancers file. */
    private static final String[] COLUMNS = {
        TISSUE,
        CANCER + "1",
        CANCER + "2",
        CANCER + "3",
        CANCER + "4",
        TERM_GROUP,
        COLOUR,
        NCI_ID,
        UMLS_ID
    };

    @Override
    protected int getSolrDocsPerBatch()
    {
        return 15000;
    }

    @Override
    protected Collection<SolrInputDocument> load(@Nonnull final URL url)
    {
        final Collection<SolrInputDocument> data = new ArrayList<>();
        try (final BufferedReader in = new BufferedReader(
            new InputStreamReader(getInputStream(url), StandardCharsets.UTF_8))) {
            final CSVFormat parser = setupCSVParser();
            // Process each csv record row.
            for (final CSVRecord row : parser.parse(in)) {
                processDataRow(row, data);
            }
        } catch (final IOException e) {
            this.logger.error("Failed to load vocabulary source: {}", e.getMessage());
        }
        return data;
    }

    /**
     * Gets an input stream from the provided {@code url}.
     *
     * @param url the {@link URL} for the cancers data
     * @return an {@link InputStream} with the data
     * @throws IOException if a connection cannot be opened
     */
    @VisibleForTesting
    InputStream getInputStream(@Nonnull final URL url) throws IOException
    {
        return url.openConnection().getInputStream();
    }

    /**
     * Processes a CSV {@code row}, and adds the relevant fields to a collection of {@code data}.
     *
     * @param row a {@link CSVRecord} containing data for one path (from root to leaf) of the OncoTree
     * @param data a collection of {@link SolrInputDocument} objects where data from {@code row} will be written
     */
    private void processDataRow(@Nonnull final CSVRecord row, @Nonnull final Collection<SolrInputDocument> data)
    {
        final SolrInputDocument doc = new SolrInputDocument();
        for (int i = 0; i < row.size(); i++) {
            final String value = row.get(i);
            if (StringUtils.isNotBlank(value)) {
                if (i == 0) {
                    processTissue(doc, COLUMNS[i], value);
                } else if (i < 5) {
                    processName(doc, value);
                } else {
                    doc.addField(COLUMNS[i], value);
                }
            }
        }
        data.add(doc);
    }

    /**
     * Processes the {@code value cancer name value}, and writes the extracted identifier and name information into
     * {@code doc}.
     *
     * @param doc the {@link SolrInputDocument} into which data is written
     * @param value the provided raw cancer name
     */
    private void processName(@Nonnull final SolrInputDocument doc, @Nonnull final String value)
    {
        updateParents(doc);
        updateCancerId(doc, value);
        updateCancerName(doc, value);
    }

    /**
     * Tries to extract the name of the cancer from the provided raw {@code value name} string, and writes it into
     * {@code doc}.
     *
     * @param doc the {@link SolrInputDocument} into which data is written
     * @param value the provided raw cancer name
     */
    private void updateCancerName(@Nonnull final SolrInputDocument doc, @Nonnull final String value)
    {
        final String name = StringUtils.substringBefore(value, "(").trim();
        doc.setField(NAME, name);
    }

    /**
     * Tries to extract the cancer identifier from the provided raw {@code value name} string, and writes it into
     * {@code doc}. If no ID can be extracted, the raw value is written as is.
     *
     * @param doc the {@link SolrInputDocument} into which data is written
     * @param value the provided raw cancer name
     */
    private void updateCancerId(@Nonnull final SolrInputDocument doc, @Nonnull final String value)
    {
        final String id = StringUtils.substringBetween(value, "(", ")").trim();
        if (StringUtils.isNotBlank(id)) {
            doc.setField(ID, getTermPrefix() + SEPARATOR + id);
        } else {
            this.logger.warn("No identifier could be extracted from the provided cancer name: {}", value);
            doc.setField(ID, getTermPrefix() + SEPARATOR + value);
        }
    }

    /**
     * Updates the parents of the cancer that is currently being processed.
     *
     * @param doc the {@link SolrInputDocument} into which data is written
     */
    private void updateParents(@Nonnull final SolrInputDocument doc)
    {
        final String prevId = (String) doc.getFieldValue(ID);
        if (StringUtils.isNotBlank(prevId)) {
            doc.setField(IS_A, prevId);
            doc.addField(TERM_CATEGORY, prevId);
        }
    }

    /**
     * Processes the type of cancer tissue.
     *
     * @param doc the {@link SolrInputDocument} into which data is written
     * @param name the tissue property name
     * @param value the provided tissue property value
     */
    private void processTissue(
        @Nonnull final SolrInputDocument doc,
        @Nonnull final String name,
        @Nonnull final String value)
    {
        final String tissueName = StringUtils.substringBefore(value, "(").trim();
        doc.addField(name, tissueName);
    }

    /**
     * Sets up the CSV parser with tab-delimited format, and provided {@link #COLUMNS column names}.
     *
     * @return a {@link CSVRecord parser}
     */
    private CSVFormat setupCSVParser()
    {
        return CSVFormat.TDF.withHeader(COLUMNS).withSkipHeaderRecord();
    }

    /**
     * Returns the prefix for the vocabulary terms belonging to the OncoTree vocabulary.
     *
     * @return a prefix for the OncoTree vocabulary terms
     */
    private String getTermPrefix()
    {
        return "ONCO";
    }

    @Override
    protected String getCoreName()
    {
        return getIdentifier();
    }

    @Override
    public String getIdentifier()
    {
        return "onco";
    }

    @Override
    public String getName()
    {
        return "OncoTree";
    }

    @Override
    public Set<String> getAliases()
    {
        final Set<String> aliases = new HashSet<>();
        aliases.add(getName());
        aliases.add(getIdentifier());
        aliases.add(getTermPrefix());
        return Collections.unmodifiableSet(aliases);
    }

    @Override
    public String getDefaultSourceLocation()
    {
        return SOURCE;
    }

    @Override
    public String getWebsite()
    {
        return "http://oncotree.mskcc.org/oncotree/";
    }

    @Override
    public String getCitation()
    {
        // FIXME: Citation needs to be added. How do they want to be credited?
        return "";
    }

    @Override
    public List<VocabularyTerm> search(@Nullable final String input, final int maxResults, @Nullable final String sort,
        @Nullable final String customFilter)
    {
        return StringUtils.isBlank(input)
            ? Collections.<VocabularyTerm>emptyList()
            : searchMatches(input, maxResults, sort, customFilter);
    }

    /**
     * Searches the Solr index for matches to the input string.
     *
     * @param input string to match
     * @param maxResults the maximum number of results
     * @param sort the optional sort parameter
     * @param customFilter custom filter for results
     * @return a list of matching {@link VocabularyTerm} objects; empty if no suitable matches found
     */
    private List<VocabularyTerm> searchMatches(@Nonnull final String input, final int maxResults,
        @Nullable final String sort, @Nullable final String customFilter)
    {
        final SolrQuery query = new SolrQuery();
        addGlobalQueryParam(query);
        addFieldQueryParam(query);
        final List<SolrDocument> searchResults = search(addDynamicQueryParam(input, maxResults, sort, customFilter,
            query));
        final List<VocabularyTerm> results = new LinkedList<>();
        for (final SolrDocument doc : searchResults) {
            results.add(new SolrVocabularyTerm(doc, this));
        }
        return Collections.unmodifiableList(results);
    }

    /**
     * Adds dynamic solr query parameters to {@code query}, based on the received {@code rawQuery raw query string},
     * {@code rows the maximum number of results to return}, {@code sort the sorting order}, and {@code customFilter a
     * custom filter}.
     *
     * @param rawQuery unprocessed query string
     * @param rows the maximum number of search items to return
     * @param sort the optional sort parameter
     * @param customFilter custom filter for the results
     * @param query a {@link SolrQuery solr query} object
     * @return the updated {@link SolrQuery solr query} object
     */
    private SolrQuery addDynamicQueryParam(@Nonnull final String rawQuery, final Integer rows,
        @Nullable final String sort, @Nullable final String customFilter, @Nonnull SolrQuery query)
    {
        final String queryString = rawQuery.trim();
        final String escapedQuery = ClientUtils.escapeQueryChars(queryString);
        if (StringUtils.isNotBlank(customFilter)) {
            query.setFilterQueries(customFilter);
        }
        query.setQuery(escapedQuery);
        query.set(SpellingParams.SPELLCHECK_Q, queryString);
        final String lastWord = StringUtils.defaultIfBlank(StringUtils.substringAfterLast(escapedQuery, " "),
            escapedQuery) + "*";
        query.set(DisMaxParams.BQ,
            String.format("nameSpell:%1$s^20 text:%1$s^1 textSpell:%1$s^2", lastWord));
        query.setRows(rows);
        if (StringUtils.isNotBlank(sort)) {
            for (final String sortItem : sort.split("\\s*,\\s*")) {
                query.addSort(StringUtils.substringBefore(sortItem, " "),
                    sortItem.endsWith(" desc") || sortItem.startsWith("-")
                        ? SolrQuery.ORDER.desc
                        : SolrQuery.ORDER.asc);
            }
        }
        return query;
    }

    /**
     * Given a {@code query} object, adds global query parameters.
     *
     * @param query a {@link SolrQuery solr query} object
     */
    private void addGlobalQueryParam(@Nonnull final SolrQuery query)
    {
        // Add global query parameters.
        query.set("spellcheck", Boolean.toString(true));
        query.set(SpellingParams.SPELLCHECK_COLLATE, Boolean.toString(true));
        query.set(SpellingParams.SPELLCHECK_COUNT, "100");
        query.set(SpellingParams.SPELLCHECK_MAX_COLLATION_TRIES, "3");
        query.set("lowercaseOperators", Boolean.toString(false));
        query.set("defType", "edismax");
    }

    /**
     * Given a {@code query} object, adds field query parameters.
     *
     * @param query a {@link SolrQuery solr query} object
     */
    private void addFieldQueryParam(@Nonnull final SolrQuery query)
    {
        query.set(DisMaxParams.PF, "name^20 nameSpell^36 nameExact^100 namePrefix^30 text^3 textSpell^5");
        query.set(DisMaxParams.QF, "id^100 name^10 nameSpell^18 nameStub^5 text^1 textSpell^2 textStub^0.5");
    }
}
