/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bloomberg.presto.accumulo.metadata;

import bloomberg.presto.accumulo.conf.AccumuloConfig;
import com.facebook.presto.spi.SchemaTableName;
import com.facebook.presto.spi.type.Type;
import com.facebook.presto.type.TypeDeserializer;
import com.facebook.presto.type.TypeRegistry;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.airlift.json.ObjectMapperProvider;

import java.io.IOException;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Abstract class for managing Accumulo metadata. Extendible and configurable, users can create
 * their own means for managing the metadata for the Accumulo connector. Implementors of this class
 * are expected to persist schema names, table names, and table definitions so they can be retrieved
 * throughput the lifetime of the Presto installation. Metadata must also be cleaned up on demand!
 * <br>
 * <br>
 * TODO Make this configurable like the comment sends
 */
public abstract class AccumuloMetadataManager
{
    protected final AccumuloConfig config;
    protected final ObjectMapper mapper;

    /**
     * Super class for the Accumulo metadata manager.
     *
     * @param config
     *            Connector configuration for Accumulo
     */
    public AccumuloMetadataManager(AccumuloConfig config)
    {
        this.config = requireNonNull(config, "config is null");

        // Create JSON deserializer for the AccumuloTable
        ObjectMapperProvider objectMapperProvider = new ObjectMapperProvider();
        objectMapperProvider.setJsonDeserializers(ImmutableMap.<Class<?>, JsonDeserializer<?>>of(
                Type.class, new TypeDeserializer(new TypeRegistry())));
        mapper = objectMapperProvider.get();
    }

    /**
     * Gets the default implementation of an AccumuloMetadataManager,
     * {@link ZooKeeperMetadataManager}
     *
     * @param config
     *            Connector config for Accumulo
     * @return Default implementation
     */
    public static AccumuloMetadataManager getDefault(AccumuloConfig config)
    {
        return new ZooKeeperMetadataManager(config);
    }

    /**
     * Gets all schema names based on the metadata.
     *
     * @return Set of all schema names
     */
    public abstract Set<String> getSchemaNames();

    /**
     * Gets all table names that have the given schema. The returns table names should not contain
     * the schema.
     *
     * @param schema
     *            Schema name
     * @return Set of all table names with the given schema
     */
    public abstract Set<String> getTableNames(String schema);

    /**
     * Gets the {@link AccumuloTable} object for the given schema and table
     *
     * @param table
     *            Schema and table name
     * @return The AccumuloTable object, or null if does not exist.
     */
    public abstract AccumuloTable getTable(SchemaTableName table);

    /**
     * Creates and store table metadata for the given table
     *
     * @param table
     *            Table to create the metadata for
     */
    public abstract void createTableMetadata(AccumuloTable table);

    /**
     * Destroy the metadata for the given table
     *
     * @param stName
     *            Schema and table name
     */
    public abstract void deleteTableMetadata(SchemaTableName stName);

    /**
     * Converts the given byte array to an {@link AccumuloTable}.
     *
     * @param data
     *            byte array of a serialized AccumuloTable
     * @return AccumuloTable
     * @throws JsonParseException
     *             If a parsing exception occurs
     * @throws JsonMappingException
     *             If a mapping exception occurs
     * @throws IOException
     *             If an IOException occurs
     */
    protected AccumuloTable toAccumuloTable(byte[] data)
            throws JsonParseException, JsonMappingException, IOException
    {
        return mapper.readValue(new String(data), AccumuloTable.class);
    }

    /**
     * Converts the given {@link AccumuloTable} to a byte array.
     *
     * @param t
     *            AccumuloTable
     * @return The byte array of the serialized object
     * @throws JsonParseException
     *             If a parsing exception occurs
     * @throws JsonMappingException
     *             If a mapping exception occurs
     * @throws IOException
     *             If an IOException occurs
     */
    protected byte[] toJsonBytes(AccumuloTable t)
            throws JsonParseException, JsonMappingException, IOException
    {
        return mapper.writeValueAsBytes(t);
    }
}
