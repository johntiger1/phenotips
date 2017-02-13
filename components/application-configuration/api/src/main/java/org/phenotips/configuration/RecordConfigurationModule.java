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
package org.phenotips.configuration;

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

/**
 * Modular record configuration service which provides or alters the record configuration according to a specific
 * purpose. This can be implemented by different components, each one with a different priority. When a specific record
 * configuration is requested, each of the available implementations will be queried by the
 * {@link RecordConfigurationManager} in ascending order of their priority, starting with an empty configuration that
 * can be altered by each module, and the final record configuration is considered the active configuration to be
 * displayed.
 *
 * @version $Id$
 * @since 1.3M3
 */
@Unstable
@Role
public interface RecordConfigurationModule
{
    /**
     * Configure how the record appears.
     *
     * @param config the previous configuration
     * @return an updated configuration
     */
    RecordConfiguration process(RecordConfiguration config);

    /**
     * The priority of this implementation. Implementations with a lower priority will be queried before implementations
     * with a higher priority. The base implementation has a priority of 0, and returns the global configuration. It is
     * recommended that the returned values be in the [0..100] range.
     *
     * @return a positive number
     */
    int getPriority();

    /**
     * Get the record types supported by this module.
     *
     * @return an array of record type identifiers, such as {@code patient} or {@code family}
     */
    String[] getSupportedRecordTypes();

    /**
     * Returns true iff the implementation of the {@link RecordConfigurationModule} supports the desired
     * {@code recordType type of record}, as specified by {@link #getSupportedRecordTypes()}.
     *
     * @param recordType the string representing the type of record
     * @return true iff the specific implementation of {@link RecordConfigurationModule} supports {@code recordType},
     *         false otherwise
     */
    boolean supportsRecordType(String recordType);
}
