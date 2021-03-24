package liquibase.integrationtest.setup;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.change.Change;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.command.CommandScope;
import liquibase.command.core.DiffCommand;
import liquibase.command.core.GenerateChangeLogCommand;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.integration.commandline.Main;
import liquibase.integrationtest.TestDatabaseConnections;
import liquibase.integrationtest.TestSetup;

import java.util.ArrayList;
import java.util.List;

public class SetupDatabaseStructureGenChangeLog extends TestSetup {

    private final List<SetupDatabaseStructure.Entry> wantedStructure;

    public SetupDatabaseStructureGenChangeLog(List<SetupDatabaseStructure.Entry> wantedStructure) {
        this.wantedStructure = wantedStructure;
    }

    @Override
    public void setup(TestDatabaseConnections.ConnectionStatus connectionStatus) throws Exception {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connectionStatus.connection));

        final ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        changeLogService.init();
        changeLogService.generateDeploymentId();

        changeLogService.reset();

        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
        for (SetupDatabaseStructure.Entry entry : wantedStructure) {
            List<Change> changes = entry.changes;
            changes.forEach(change -> {
                try {
                    executor.execute(change);
                }
                catch (DatabaseException dbe) {
                   throw new RuntimeException(dbe);
                }
            });
        }

        String defaultSchemaName = database.getDefaultSchemaName();
        CatalogAndSchema[] catalogAndSchemas = new CatalogAndSchema[1];
        catalogAndSchemas[0] = new CatalogAndSchema(null, defaultSchemaName);
        CompareControl.SchemaComparison[] comparisons = new CompareControl.SchemaComparison[catalogAndSchemas.length];
        int i = 0;
        for (CatalogAndSchema schema : catalogAndSchemas) {
            comparisons[i++] = new CompareControl.SchemaComparison(schema, schema);
        }
        CompareControl compareControl = new CompareControl(comparisons, (String)null);
        DiffOutputControl diffOutputControl = new DiffOutputControl(
            false, false, false, comparisons);
        String[] command = new String[1];
        command[0] = "generateChangeLog";
        CommandScope commandScope = new CommandScope(command);
        commandScope.addArgumentValue(GenerateChangeLogCommand.CHANGELOG_FILENAME_ARG.getName(), "/tmp/changelog.xml");
        commandScope.addArgumentValue(DiffCommand.TARGET_DATABASE_ARG.getName(), database);
        commandScope.addArgumentValue(DiffCommand.REFERENCE_DATABASE_ARG.getName(), database);
        commandScope.addArgumentValue(DiffCommand.COMPARE_CONTROL_ARG.getName(), compareControl);
        commandScope.addArgumentValue(GenerateChangeLogCommand.DIFF_OUTPUT_CONTROL_ARG.getName(), diffOutputControl);
        commandScope.execute();
    }

    public static class EntryWithChangelog {
        public List<Change> changes = new ArrayList<>();
        public EntryWithChangelog(Change change) {
            changes.add(change);
        }
    }
}
