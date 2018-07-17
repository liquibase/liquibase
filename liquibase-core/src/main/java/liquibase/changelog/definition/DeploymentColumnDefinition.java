package liquibase.changelog.definition;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

public class DeploymentColumnDefinition implements ChangeLogColumnDefinition {

    private final String columnName;
    private final int desiredColumnSize;

    public DeploymentColumnDefinition(String columnName, int desiredColumnSize) {
        this.columnName = columnName;
        this.desiredColumnSize = desiredColumnSize;
    }

    public List<SqlStatement> complementChangeLogTable(Database database, Table changeLogTable) throws DatabaseException {
        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        String liquibaseCatalogName = database.getLiquibaseCatalogName();
        String liquibaseSchemaName = database.getLiquibaseSchemaName();
        String databaseChangeLogTableName = database.getDatabaseChangeLogTableName();
        Executor executor = ExecutorService.getInstance().getExecutor(database);


        boolean hasDeploymentIdColumn = changeLogTable.getColumn(columnName) != null;
        if (!hasDeploymentIdColumn) {
            executor.comment("Adding missing databasechangelog." + columnName + " column");
            sqlStatements.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName,
                    databaseChangeLogTableName, columnName, "VARCHAR(" + desiredColumnSize + ")", null));
            if (database instanceof DB2Database) {
                sqlStatements.add(new ReorganizeTableStatement(liquibaseCatalogName,
                        liquibaseSchemaName, databaseChangeLogTableName));
            }
        }

        return sqlStatements;
    }
}