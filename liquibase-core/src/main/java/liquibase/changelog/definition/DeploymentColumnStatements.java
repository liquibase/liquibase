package liquibase.changelog.definition;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

public class DeploymentColumnStatements implements ChangeLogTableChangesProvider {

    private final ChangeLogColumnDefinition columnDefinition;

    public DeploymentColumnStatements(ChangeLogColumnDefinition columnDefinition) {
        this.columnDefinition = columnDefinition;
    }

    public List<SqlStatement> createSqlStatements(Database database, Table changeLogTable) throws DatabaseException {
        List<SqlStatement> sqlStatements = new ArrayList<>();
        String liquibaseCatalogName = database.getLiquibaseCatalogName();
        String liquibaseSchemaName = database.getLiquibaseSchemaName();
        String databaseChangeLogTableName = database.getDatabaseChangeLogTableName();
        Executor executor = ExecutorService.getInstance().getExecutor(database);

        String columnName = columnDefinition.getColumnName();
        LiquibaseDataType dataType = columnDefinition.getDataType();

        boolean hasDeploymentIdColumn = changeLogTable.getColumn(columnName) != null;
        if (!hasDeploymentIdColumn) {
            executor.comment("Adding missing databasechangelog." + columnName + " column");
            sqlStatements.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName,
                    databaseChangeLogTableName, columnName, dataType.toDatabaseDataType(database).toSql(), null));
            if (database instanceof DB2Database) {
                sqlStatements.add(new ReorganizeTableStatement(liquibaseCatalogName,
                        liquibaseSchemaName, databaseChangeLogTableName));
            }
        }

        return sqlStatements;
    }
}