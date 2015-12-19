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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import bloomberg.presto.accumulo.AccumuloColumnHandle;
import bloomberg.presto.accumulo.PrestoType;

public class ZooKeeperMetadataCreator extends Configured implements Tool {

    private final char ZOOKEEPERS_OPT = 'z';
    private final char METADATA_ROOT_OPT = 'r';
    private final char NAMESPACE_OPT = 'n';
    private final char TABLE_OPT = 't';
    private final char COLUMN_FAMILY_OPT = 'f';
    private final char COLUMN_QUALIFIER_OPT = 'q';
    private final char PRESTO_COLUMN_NAME_OPT = 'c';
    private final char PRESTO_TYPE_OPT = 'p';
    private final char DESCRIPTION_OPT = 'd';
    private final char HELP_OPT = 'h';
    private String zooKeepers, metadataRoot, namespace, table, columnFamily,
            columnQualifier, prestoColumn, prestoType, comment = null;
    private boolean force;
    private CuratorFramework client = null;

    @Override
    public int run(String[] args) throws Exception {
        parseOpts(args);
        createMetadata();
        return 0;
    }

    public void createMetadata() throws Exception {
        if (client == null) {
            initCurator();
        }
        String tablePath = String.format("/%s/%s", this.getNamespace(),
                this.getTable());
        String colPath = String.format("%s/%s", tablePath,
                this.getPrestoColumn());

        // Ordinal is number of children + 1 (for row ID)
        int ordinal = 1;
        if (client.checkExists().forPath(tablePath) != null) {
            ordinal += client.getChildren().forPath(tablePath).size();
        }

        AccumuloColumnHandle col = new AccumuloColumnHandle("accumulo",
                this.getPrestoColumn(), this.getColumnFamily(),
                this.getColumnQualifier(),
                PrestoType.fromString(this.getPrestoType()).spiType(), ordinal,
                this.getComment());

        ObjectMapper mapper = new ObjectMapper();
        byte[] data = mapper.writeValueAsBytes(col);

        if (force && client.checkExists().forPath(colPath) != null) {
            client.delete().forPath(colPath);
        }

        client.create().creatingParentsIfNeeded().forPath(colPath, data);
    }

    public String getColumnFamily() {
        return columnFamily;
    }

    public void setColumnFamily(String columnFamily) {
        this.columnFamily = columnFamily;
    }

    public String getColumnQualifier() {
        return columnQualifier;
    }

    public void setColumnQualifier(String columnQualifier) {
        this.columnQualifier = columnQualifier;
    }

    public String getComment() {
        return comment == null ? String.format("Accumulo column %s:%s",
                columnFamily, columnQualifier) : comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean getForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public String getMetadataRoot() {
        return metadataRoot;
    }

    public void setMetadataRoot(String metadataRoot) {
        this.metadataRoot = metadataRoot;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getPrestoColumn() {
        return prestoColumn;
    }

    public void setPrestoColumn(String prestoColumn) {
        this.prestoColumn = prestoColumn;
    }

    public String getPrestoType() {
        return prestoType;
    }

    public void setPrestoType(String prestoType) {
        this.prestoType = prestoType;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getZooKeepers() {
        return zooKeepers;
    }

    public void setZooKeepers(String zooKeepers) {
        this.zooKeepers = zooKeepers;
    }

    @SuppressWarnings("static-access")
    private void parseOpts(String[] args) {
        Options opts = new Options();
        opts.addOption(
                OptionBuilder.withLongOpt("zookeepers")
                        .withDescription(
                                "Comma-delimited list of ZooKeeper servers")
                .hasArg().isRequired().create(ZOOKEEPERS_OPT));

        opts.addOption(OptionBuilder.withLongOpt("metadata-root")
                .withDescription(
                        "ZooKeeper root path for metadata.  Default /presto-accumulo")
                .hasArg().create(METADATA_ROOT_OPT));

        opts.addOption(OptionBuilder.withLongOpt("accumulo-namespace")
                .withDescription(
                        "Namespace of the Accumulo table.  Default 'default'")
                .hasArg().create(NAMESPACE_OPT));

        opts.addOption(OptionBuilder.withLongOpt("accumulo-table")
                .withDescription("Name of the accumulo table").hasArg()
                .isRequired().create(TABLE_OPT));

        opts.addOption(OptionBuilder.withLongOpt("accumulo-column-family")
                .withDescription("Name of the Accumulo column family").hasArg()
                .isRequired().create(COLUMN_FAMILY_OPT));

        opts.addOption(
                OptionBuilder.withLongOpt("accumulo-column-qualifier")
                        .withDescription(
                                "Name of the Accumulo column qualifier")
                .hasArg().isRequired().create(COLUMN_QUALIFIER_OPT));

        opts.addOption(OptionBuilder.withLongOpt("presto-column-name")
                .withDescription("Name of the presto column to create").hasArg()
                .isRequired().create(PRESTO_COLUMN_NAME_OPT));

        opts.addOption(OptionBuilder.withLongOpt("presto-column-type")
                .withDescription("Presto type of the column").hasArg()
                .isRequired().create(PRESTO_TYPE_OPT));

        opts.addOption(
                OptionBuilder.withLongOpt("description")
                        .withDescription(
                                "Comment for the column, default is none")
                .hasArg().create(DESCRIPTION_OPT));

        opts.addOption(OptionBuilder.withLongOpt("force")
                .withDescription(
                        "Force operation, i.e. delete existing ZK node if exists")
                .create());

        opts.addOption(OptionBuilder.withLongOpt("help")
                .withDescription("Print this dialog").create(HELP_OPT));

        CommandLineParser parser = new GnuParser();

        CommandLine cli = null;
        try {
            cli = parser.parse(opts, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            HelpFormatter help = new HelpFormatter();
            help.printHelp("java -jar <jarfile> [opts]", opts);
            System.exit(1);
        }

        if (cli.hasOption(HELP_OPT)) {
            HelpFormatter help = new HelpFormatter();
            help.printHelp("java -jar <jarfile> [opts]", opts);
            System.exit(0);
        }

        this.setColumnFamily(cli.getOptionValue(COLUMN_FAMILY_OPT));
        this.setColumnQualifier(cli.getOptionValue(COLUMN_QUALIFIER_OPT));
        this.setComment(cli.getOptionValue(DESCRIPTION_OPT));
        this.setForce(cli.hasOption("--force"));
        this.setMetadataRoot(
                cli.getOptionValue(METADATA_ROOT_OPT, "/presto-accumulo"));
        this.setNamespace(cli.getOptionValue(NAMESPACE_OPT, "default"));
        this.setPrestoColumn(
                cli.getOptionValue(PRESTO_COLUMN_NAME_OPT).toLowerCase());
        this.setPrestoType(cli.getOptionValue(PRESTO_TYPE_OPT));
        this.setTable(cli.getOptionValue(TABLE_OPT));
        this.setZooKeepers(cli.getOptionValue(ZOOKEEPERS_OPT));
    }

    private void initCurator() {
        CuratorFramework checkRoot = CuratorFrameworkFactory.newClient(
                this.getZooKeepers(), new ExponentialBackoffRetry(1000, 3));
        checkRoot.start();

        try {
            if (checkRoot.checkExists()
                    .forPath(this.getMetadataRoot()) == null) {
                boolean created = false;
                BufferedReader rdr = new BufferedReader(
                        new InputStreamReader(System.in));
                do {
                    System.out.println(String.format(
                            "ZK metadata root %s does not exist, create it? (y/n)",
                            this.getMetadataRoot()));

                    String line = rdr.readLine();
                    if (line.toLowerCase().equals("y")) {
                        checkRoot.create().creatingParentsIfNeeded()
                                .forPath(this.getMetadataRoot());
                        created = true;
                    } else if (line.toLowerCase().equals("n")) {
                        System.exit(0);
                    } else {
                        System.out.println("Please enter 'y' or 'n'");
                    }
                } while (!created);
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error connecting to ZooKeeper for fetching metadata", e);
        }
        checkRoot.close();

        client = CuratorFrameworkFactory.newClient(
                this.getZooKeepers() + this.getMetadataRoot(),
                new ExponentialBackoffRetry(1000, 3));
        client.start();

    }

    public static void main(String[] args) throws Exception {
        System.exit(ToolRunner.run(new ZooKeeperMetadataCreator(), args));
    }
}
