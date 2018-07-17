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

public class SimpleTextColumnIgnoreDifferentTypeDefinition implements ChangeLogColumnDefinition {

    private final String columnName;
    private final int desiredColumnSize;

    public SimpleTextColumnIgnoreDifferentTypeDefinition(String columnName, int desiredColumnSize) {
        this.columnName = columnName;
        this.desiredColumnSize = desiredColumnSize;
    }

    public List<SqlStatement> complementChangeLogTable(Database database, Table changeLogTable) throws DatabaseException {
        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        String liquibaseCatalogName = database.getLiquibaseCatalogName();
        String liquibaseSchemaName = database.getLiquibaseSchemaName();
        String databaseChangeLogTableName = database.getDatabaseChangeLogTableName();
        String charTypeName = database.getCharTypeName();
        Executor executor = ExecutorService.getInstance().getExecutor(database);

        boolean hasContexts = changeLogTable.getColumn(columnName) != null;
        if (hasContexts) {
            Integer columnSize = changeLogTable.getColumn(columnName).getType().getColumnSize();
            if ((columnSize != null) && (columnSize < desiredColumnSize)) {
                executor.comment("Modifying size of databasechangelog." + columnName + " column");
                sqlStatements.add(new ModifyDataTypeStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, columnName, charTypeName + "(" + desiredColumnSize + ")"));
            }
        } else {
            executor.comment("Adding missing databasechangelog." + columnName + " column");
            sqlStatements.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, columnName, charTypeName + "(" + desiredColumnSize + ")", null));
        }

        return sqlStatements;
    }
}