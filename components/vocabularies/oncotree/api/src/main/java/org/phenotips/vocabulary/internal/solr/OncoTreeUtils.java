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

/**
 * A utility class for the OncoTree vocabulary, containing constants.
 *
 * @version $Id$
 * @since 1.4
 */
public final class OncoTreeUtils
{
    /** The label for the tissue column in the OncoTree data file, as well as the tissue property. */
    public static final String TISSUE = "tissue";

    /** The label for the cancers columns in the OncoTree data file. */
    public static final String CANCER = "cancer";

    /** The label for the cancer group column in the OncoTree data file, as well as the term group property. */
    public static final String TERM_GROUP = "term_group";

    /** The label for the colour column in the OncoTree data file. */
    public static final String COLOUR = "colour";

    /** The label for the nci id column in the OncoTree data file, as well as the nci id property. */
    public static final String NCI_ID = "nci_id";

    /** The label for the umls id column in the OncoTree data file, as well as the umls id property. */
    public static final String UMLS_ID = "umls_id";

    /** The label for the cancer ID property. */
    public static final String ID = "id";

    /** The label for the cancer name property. */
    public static final String NAME = "name";

    /** The label for the direct parent property for a cancer. */
    public static final String IS_A = "is_a";

    /** The label for the ancestors property for a cancer. */
    public static final String TERM_CATEGORY = "term_category";

    /**
     * The private constructor for the class, as the user should not be able to construct instances.
     */
    private OncoTreeUtils() { }
}
