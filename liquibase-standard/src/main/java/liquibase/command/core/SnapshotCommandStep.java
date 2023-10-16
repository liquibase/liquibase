package liquibase.command.core;

import liquibase.CatalogAndSchema;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.ObjectChangeFilter;
import liquibase.diff.output.StandardObjectChangeFilter;
import liquibase.serializer.SnapshotSerializerFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.DatabaseObjectFactory;
import liquibase.util.StringUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

public class SnapshotCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"snapshot"};

    public static final CommandArgumentDefinition<String> SCHEMAS_ARG;
    public static final CommandArgumentDefinition<String> SNAPSHOT_FORMAT_ARG;
    public static final CommandArgumentDefinition<SnapshotControl> SNAPSHOT_CONTROL_ARG;
    public static final CommandArgumentDefinition<String> SNAPSHOT_TYPES_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        SCHEMAS_ARG = builder.argument("schemas", String.class).description("The schemas to snapshot").build();
        SNAPSHOT_FORMAT_ARG = builder.argument("snapshotFormat", String.class)
                .description("Output format to use (JSON, YAML, or TXT)").build();
        SNAPSHOT_CONTROL_ARG = builder.argument("snapshotControl", SnapshotControl.class).hidden().build();
        SNAPSHOT_TYPES_ARG = builder.argument("snapshotTypes", String.class)
                .description("Types of objects to snapshot: " + getStandardTypes()).build();
    }

    private Map<String, Object> snapshotMetadata;

    private static String getStandardTypes() {
        Set<Class<? extends DatabaseObject>> standardTypes = DatabaseObjectFactory.getInstance().getStandardTypes();
        StringBuilder builder = new StringBuilder();
        standardTypes.forEach(type -> {
            String[] parts= type.getName().split("\\.");
            String name = parts[parts.length-1];
            if (builder.length() == 0) {
                builder.append(name);
            } else {
                builder.append(", ");
                builder.append(name);
            }
        });
        return builder.toString();
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Collections.singletonList(Database.class);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Capture the current state of the database");
    }

    private CatalogAndSchema[] parseSchemas(Database database, String... schemas) {
        if ((schemas == null) || (schemas.length == 0) || (schemas[0] == null)) {
            return null;
        }

        schemas = StringUtil.join(schemas, ",").split("\\s*,\\s*");
        List<CatalogAndSchema> finalList = new ArrayList<>();
        for (String schema : schemas) {
            finalList.add(new CatalogAndSchema(null, schema).customize(database));
        }

        return finalList.toArray(new CatalogAndSchema[0]);
    }

    public Map<String, Object> getSnapshotMetadata() {
        return snapshotMetadata;
    }

    public void setSnapshotMetadata(Map<String, Object> snapshotMetadata) {
        this.snapshotMetadata = snapshotMetadata;
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        Database database = (Database) commandScope.getDependency(Database.class);

        CatalogAndSchema[] schemas = parseSchemas(database, commandScope.getArgumentValue(SCHEMAS_ARG));

        InternalSnapshotCommandStep.logUnsupportedDatabase(database, this.getClass());
        SnapshotControl snapshotControl;
        if (commandScope.getArgumentValue(SNAPSHOT_CONTROL_ARG) == null) {
            String typesString = commandScope.getArgumentValue(SNAPSHOT_TYPES_ARG);
            if (typesString != null) {
                Class<? extends DatabaseObject>[] types = DiffCommandStep.parseSnapshotTypes(typesString);
                snapshotControl = new SnapshotControl(database, types);
            } else {
                snapshotControl = new SnapshotControl(database);
            }
        } else {
            snapshotControl = commandScope.getArgumentValue(SnapshotCommandStep.SNAPSHOT_CONTROL_ARG);
        }

        if (schemas == null) {
            schemas = new CatalogAndSchema[]{database.getDefaultSchema()};
        }

        ObjectQuotingStrategy originalQuotingStrategy = database.getObjectQuotingStrategy();

        database.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);

        try {
            DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(schemas, database, snapshotControl);

            snapshot.setMetadata(this.getSnapshotMetadata());

            resultsBuilder.addResult("snapshot", snapshot);
            resultsBuilder.addResult("statusCode", 0);

            OutputStream outputStream = resultsBuilder.getOutputStream();
            if (outputStream != null) {
                String result = printSnapshot(commandScope, snapshot);
                Writer outputWriter = getOutputWriter(outputStream);
                outputWriter.write(result);
                outputWriter.flush();
            }
        } finally {
            //
            // Reset the quoting strategy
            //
            database.setObjectQuotingStrategy(originalQuotingStrategy);
        }
    }

    private Writer getOutputWriter(final OutputStream outputStream) throws IOException {
        String charsetName = GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue();

        return new OutputStreamWriter(outputStream, charsetName);
    }

    private String printSnapshot(CommandScope commandScope, DatabaseSnapshot snapshot) {
        String format = commandScope.getArgumentValue(SNAPSHOT_FORMAT_ARG);
        if (format == null) {
            format = "txt";
        }

        return SnapshotSerializerFactory.getInstance()
                                        .getSerializer(format.toLowerCase(Locale.US))
                                        .serialize(snapshot, true);
    }

}
