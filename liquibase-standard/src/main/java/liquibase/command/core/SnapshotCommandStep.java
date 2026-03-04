package liquibase.command.core;

import liquibase.CatalogAndSchema;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.serializer.SnapshotSerializerFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.util.ExceptionUtil;
import lombok.Getter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class SnapshotCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"snapshot"};

    public static final CommandArgumentDefinition<String> SCHEMAS_ARG;
    public static final CommandArgumentDefinition<String> SNAPSHOT_FORMAT_ARG;
    public static final CommandArgumentDefinition<SnapshotControl> SNAPSHOT_CONTROL_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        SCHEMAS_ARG = builder.argument("schemas", String.class).description("The schemas to snapshot").build();
        SNAPSHOT_FORMAT_ARG = builder.argument("snapshotFormat", String.class)
                .description("Output format to use (JSON, YAML, or TXT)").build();
        SNAPSHOT_CONTROL_ARG = builder.argument("snapshotControl", SnapshotControl.class).hidden().build();
    }

    private Map<String, Object> snapshotMetadata;

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

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        Database database = (Database) commandScope.getDependency(Database.class);

        CatalogAndSchema[] schemas = InternalSnapshotCommandStep.parseSchemas(database, commandScope.getArgumentValue(SCHEMAS_ARG));

        InternalSnapshotCommandStep.logUnsupportedDatabase(database, this.getClass());
        SnapshotControl snapshotControl;
        snapshotControl = createSnapshotControl(commandScope, database);

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

    protected SnapshotControl createSnapshotControl(CommandScope commandScope, Database database) {
        SnapshotControl snapshotControl;
        if (commandScope.getArgumentValue(SNAPSHOT_CONTROL_ARG) == null) {
            snapshotControl = new SnapshotControl(database);
        } else {
            snapshotControl = commandScope.getArgumentValue(SnapshotCommandStep.SNAPSHOT_CONTROL_ARG);
        }
        return snapshotControl;
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

        AtomicReference<String> serializedSnapshotString = new AtomicReference<>();
        String finalFormat = format;
        try {
            Scope.child(DatabaseSnapshot.SNAPSHOT_SCOPE_KEY, snapshot, () -> {
                serializedSnapshotString.set(SnapshotSerializerFactory.getInstance()
                        .getSerializer(finalFormat.toLowerCase(Locale.US))
                        .serialize(snapshot, true));
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return serializedSnapshotString.get();
    }

}
