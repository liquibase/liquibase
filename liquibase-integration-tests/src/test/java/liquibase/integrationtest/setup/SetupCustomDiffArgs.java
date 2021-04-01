package liquibase.integrationtest.setup;


import liquibase.CatalogAndSchema;
import liquibase.command.CommandScope;
import liquibase.command.core.DiffCommandStep;
import liquibase.command.core.GenerateChangeLogCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.integrationtest.CustomTestSetup;
import liquibase.integrationtest.TestDatabaseConnections;

public class SetupCustomDiffArgs extends CustomTestSetup {

    public SetupCustomDiffArgs() {
        super();
    }

    @Override
    public void customSetup(TestDatabaseConnections.ConnectionStatus connectionStatus, CommandScope commandScope) throws Exception {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connectionStatus.connection));
        commandScope.addArgumentValue(GenerateChangeLogCommandStep.REFERENCE_DATABASE_ARG, database);
        CatalogAndSchema[] schemas = new CatalogAndSchema[1];
        schemas[0] = new CatalogAndSchema(null, null);
        CompareControl.ComputedSchemas computedSchemas = CompareControl.computeSchemas(
            null,
            null,
            null,
            null, null,
            null, null,
            database);
        CompareControl.SchemaComparison[] comparisons = new CompareControl.SchemaComparison[schemas.length];
        int i = 0;
        for (CatalogAndSchema schema : schemas) {
            comparisons[i++] = new CompareControl.SchemaComparison(schema, schema);
        }
        DiffOutputControl diffOutputControl = new DiffOutputControl(
            false, false, false, comparisons);
        CompareControl compareControl = new CompareControl(comparisons, (String)null);
        commandScope.addArgumentValue(GenerateChangeLogCommandStep.COMPARE_CONTROL_ARG, compareControl);
        commandScope.addArgumentValue(GenerateChangeLogCommandStep.DIFF_OUTPUT_CONTROL_ARG, diffOutputControl);
    }
}
