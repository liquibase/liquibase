package liquibase.changelog.definition;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddColumnStatement;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

public class SimpleTextColumnDefinition implements ChangeLogColumnDefinition {

    private final String columnName;
    private final int desiredColumnSize;

    public SimpleTextColumnDefinition(String columnName, int desiredColumnSize) {
        this.columnName = columnName;
        this.desiredColumnSize = desiredColumnSize;
    }

    public List<SqlStatement> complementChangeLogTable(Database database, Table changeLogTable) throws DatabaseException {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        String liquibaseCatalogName = database.getLiquibaseCatalogName();
        String liquibaseSchemaName = database.getLiquibaseSchemaName();
        String databaseChangeLogTableName = database.getDatabaseChangeLogTableName();
        String charTypeName = database.getCharTypeName();
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        String columnType = charTypeName + "(" + desiredColumnSize + ")";

        boolean columnExists = changeLogTable.getColumn(columnName) != null;
        if (!columnExists) {
            executor.comment("Adding missing databasechangelog." + columnName + " column");
            statements.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, columnName, columnType, null));
        }

        return statements;
    }
}