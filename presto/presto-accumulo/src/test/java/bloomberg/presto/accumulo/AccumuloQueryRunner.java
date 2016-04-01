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
package bloomberg.presto.accumulo;

import bloomberg.presto.accumulo.conf.AccumuloConfig;
import com.facebook.presto.Session;
import com.facebook.presto.metadata.QualifiedObjectName;
import com.facebook.presto.testing.QueryRunner;
import com.facebook.presto.tests.DistributedQueryRunner;
import com.facebook.presto.tpch.TpchPlugin;
import com.google.common.collect.ImmutableMap;
import io.airlift.log.Logger;
import io.airlift.log.Logging;
import io.airlift.tpch.TpchColumn;
import io.airlift.tpch.TpchTable;
import org.intellij.lang.annotations.Language;

import java.util.Map;

import static com.facebook.presto.testing.TestingSession.testSessionBuilder;
import static com.facebook.presto.tpch.TpchMetadata.TINY_SCHEMA_NAME;
import static io.airlift.units.Duration.nanosSince;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;

public final class AccumuloQueryRunner
{
    private static final Logger log = Logger.get(AccumuloQueryRunner.class);

    private AccumuloQueryRunner()
    {}

    public static DistributedQueryRunner createAccumuloQueryRunner(
            Map<String, String> extraProperties, boolean loadTpch)
            throws Exception
    {
        DistributedQueryRunner queryRunner =
                new DistributedQueryRunner(createSession(), 2, extraProperties);

        queryRunner.installPlugin(new TpchPlugin());
        queryRunner.createCatalog("tpch", "tpch");

        queryRunner.installPlugin(new AccumuloPlugin());
        Map<String, String> accumuloProperties =
                ImmutableMap.<String, String>builder()
                        .put(AccumuloConfig.INSTANCE, "default")
                        .put(AccumuloConfig.ZOOKEEPERS, "localhost:2181")
                        .put(AccumuloConfig.USERNAME, "root")
                        .put(AccumuloConfig.PASSWORD, "secret")
                        .put(AccumuloConfig.ZOOKEEPER_METADATA_ROOT, "/presto-accumulo-test")
                        .build();

        queryRunner.createCatalog("accumulo", "accumulo", accumuloProperties);

        if (loadTpch) {
            copyTpchTables(queryRunner, "tpch", TINY_SCHEMA_NAME, createSession(), TpchTable.getTables());
        }

        return queryRunner;
    }

    private static void copyTpchTables(
            QueryRunner queryRunner,
            String sourceCatalog,
            String sourceSchema,
            Session session,
            Iterable<TpchTable<?>> tables)
            throws Exception
    {
        log.info("Loading data from %s.%s...", sourceCatalog, sourceSchema);
        long startTime = System.nanoTime();
        for (TpchTable<?> table : tables) {
            StringBuilder bldr = new StringBuilder("column_mapping = '");

            for (TpchColumn tch : table.getColumns()) {
                bldr.append(tch.getColumnName()).append(':').append("cf").append(':').append(tch.getColumnName()).append(',');
            }
            bldr.deleteCharAt(bldr.length() - 1);
            bldr.append("'");

            copyTable(queryRunner, sourceCatalog, session, sourceSchema, table, bldr.toString());
        }
        log.info("Loading from %s.%s complete in %s", sourceCatalog, sourceSchema, nanosSince(startTime).toString(SECONDS));
    }

    public static void dropTpchTables(QueryRunner queryRunner, Session session)
    {
        for (TpchTable<?> table : TpchTable.getTables()) {
            @Language("SQL")
            String sql = format("DROP TABLE IF EXISTS %s", table.getTableName());
            log.info("%s", sql);
            queryRunner.execute(session, sql);
        }
    }

    private static void copyTable(QueryRunner queryRunner, String catalog, Session session,
            String schema, TpchTable<?> table, String properties)
    {
        QualifiedObjectName source = new QualifiedObjectName(catalog, schema, table.getTableName());
        String target = table.getTableName();
        String with = properties.isEmpty() ? "" : format(" WITH (%s)", properties);

        @Language("SQL")
        String sql = format("CREATE TABLE %s%s AS SELECT * FROM %s", target, with, source);

        log.info("Running import for %s", target, sql);
        log.info("%s", sql);
        long start = System.nanoTime();
        long rows = queryRunner.execute(session, sql).getUpdateCount().getAsLong();
        log.info("Imported %s rows for %s in %s", rows, target, nanosSince(start));
    }

    public static Session createSession()
    {
        return testSessionBuilder().setCatalog("accumulo").setSchema("tpch").build();
    }

    public static void main(String[] args)
            throws Exception
    {
        Logging.initialize();
        Map<String, String> properties = ImmutableMap.of("http-server.http.port", "8080");
        DistributedQueryRunner queryRunner = createAccumuloQueryRunner(properties, false);
        Thread.sleep(10);
        Logger log = Logger.get(AccumuloQueryRunner.class);
        log.info("======== SERVER STARTED ========");
        log.info("\n====\n%s\n====", queryRunner.getCoordinator().getBaseUrl());
    }
}