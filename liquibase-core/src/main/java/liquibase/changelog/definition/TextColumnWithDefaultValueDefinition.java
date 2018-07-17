package liquibase.changelog.definition;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.SetNullableStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

public class TextColumnWithDefaultValueDefinition implements ChangeLogColumnDefinition {

    private final String columnName;
    private final Integer columnSize;
    private final String defaultValue;

    public TextColumnWithDefaultValueDefinition(String columnName, String defaultValue, Integer columnSize) {
        this.columnName = columnName;
        this.defaultValue = defaultValue;
        this.columnSize = columnSize;
    }

    public List<SqlStatement> complementChangeLogTable(Database database, Table changeLogTable) throws DatabaseException {
        List<SqlStatement> sqlStatements = new ArrayList<SqlStatement>();
        String liquibaseCatalogName = database.getLiquibaseCatalogName();
        String liquibaseSchemaName = database.getLiquibaseSchemaName();
        String databaseChangeLogTableName = database.getDatabaseChangeLogTableName();
        Executor executor = ExecutorService.getInstance().getExecutor(database);

        String columnDataType = database.getCharTypeName() + "(" + columnSize + ")";

        boolean columnExists = changeLogTable.getColumn(columnName) != null;
        if (!columnExists) {
            executor.comment("Adding missing databasechangelog." + columnName + " column");
            sqlStatements.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, columnName, columnDataType, null));
            sqlStatements.add(new UpdateStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName).addNewColumnValue(columnName, defaultValue));
            sqlStatements.add(new SetNullableStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, columnName, columnDataType, false));
        }

        return sqlStatements;
    }
}