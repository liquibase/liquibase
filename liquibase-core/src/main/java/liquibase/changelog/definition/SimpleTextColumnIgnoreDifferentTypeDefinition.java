package liquibase.changelog.definition;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.ModifyDataTypeStatement;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

public class SimpleTextColumnIgnoreDifferentTypeDefinition implements AlterChangeLogTableSqlStatementProvider {

    private final ChangeLogColumnDefinition columnDefinition;

    public SimpleTextColumnIgnoreDifferentTypeDefinition(ChangeLogColumnDefinition columnDefinition) {
        this.columnDefinition = columnDefinition;
    }

    public List<SqlStatement> createSqlStatements(Database database, Table changeLogTable) throws DatabaseException {
        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        String liquibaseCatalogName = database.getLiquibaseCatalogName();
        String liquibaseSchemaName = database.getLiquibaseSchemaName();
        String databaseChangeLogTableName = database.getDatabaseChangeLogTableName();
        Executor executor = ExecutorService.getInstance().getExecutor(database);

        String columnName = columnDefinition.getColumnName();
        Integer desiredColumnSize = (Integer) columnDefinition.getDataType().getParameters()[0];
        String charType = columnDefinition.getDataType().toDatabaseDataType(database).getType();

        boolean hasContexts = changeLogTable.getColumn(columnName) != null;

        if (hasContexts) {
            Integer columnSize = changeLogTable.getColumn(columnName).getType().getColumnSize();
            if ((columnSize != null) && (columnSize < desiredColumnSize)) {
                executor.comment("Modifying size of databasechangelog." + columnName + " column");
                sqlStatements.add(new ModifyDataTypeStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, columnName, charType));
            }
        } else {
            executor.comment("Adding missing databasechangelog." + columnName + " column");
            sqlStatements.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, columnName, charType, null));
        }

        return sqlStatements;
    }
}