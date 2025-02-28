package liquibase.command.core;

import liquibase.command.*;
import liquibase.command.core.helpers.ReferenceDatabaseOutputWriterCommandStep;
import liquibase.command.providers.ReferenceDatabase;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotControl;

import java.io.Writer;
import java.util.Arrays;
import java.util.List;

public class SnapshotReferenceCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"snapshotReference"};
    public static final CommandArgumentDefinition<SnapshotControl> SNAPSHOT_CONTROL_ARG;

    public static final CommandArgumentDefinition<String> SNAPSHOT_FORMAT_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        SNAPSHOT_FORMAT_ARG = builder.argument("snapshotFormat", String.class)
                .description("Output format to use (JSON or YAML)").build();
        SNAPSHOT_CONTROL_ARG = builder.argument("snapshotControl", SnapshotControl.class).hidden().build();
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(ReferenceDatabase.class, ReferenceDatabaseOutputWriterCommandStep.ReferenceDatabaseWriter.class);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Capture the current state of the reference database");
    }

    protected SnapshotControl createSnapshotControl(CommandScope commandScope, Database database) {
        return new SnapshotControl(database);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope snapshotReferenceCommand = resultsBuilder.getCommandScope();
        Database referenceDatabase = (Database) snapshotReferenceCommand.getDependency(ReferenceDatabase.class);
        Writer outputWriter = ((Writer) snapshotReferenceCommand.getDependency(ReferenceDatabaseOutputWriterCommandStep.ReferenceDatabaseWriter.class));

        SnapshotControl snapshotControl = createSnapshotControl(snapshotReferenceCommand, referenceDatabase);
        CommandScope snapshotCommand = new CommandScope(InternalSnapshotCommandStep.COMMAND_NAME);
        snapshotCommand
                .addArgumentValue(InternalSnapshotCommandStep.DATABASE_ARG, referenceDatabase)
                .addArgumentValue(InternalSnapshotCommandStep.SNAPSHOT_CONTROL_ARG, snapshotControl)
                .addArgumentValue(InternalSnapshotCommandStep.SERIALIZER_FORMAT_ARG, snapshotCommand.getArgumentValue(SNAPSHOT_FORMAT_ARG));

        outputWriter.write(InternalSnapshotCommandStep.printSnapshot(snapshotCommand, snapshotCommand.execute()));
    }
}
