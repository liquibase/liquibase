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

public class SimpleTextColumnChangesProvider implements ChangeLogTableChangesProvider {

    private final ChangeLogColumnDefinition columnDefinition;

    public SimpleTextColumnChangesProvider(ChangeLogColumnDefinition columnDefinition) {
        this.columnDefinition = columnDefinition;
    }

    public List<SqlStatement> createSqlStatements(Database database, Table changeLogTable) throws DatabaseException {
        List<SqlStatement> statements = new ArrayList<>();
        String liquibaseCatalogName = database.getLiquibaseCatalogName();
        String liquibaseSchemaName = database.getLiquibaseSchemaName();
        String databaseChangeLogTableName = database.getDatabaseChangeLogTableName();
        Executor executor = ExecutorService.getInstance().getExecutor(database);

        String columnType = columnDefinition.getDataType().toDatabaseDataType(database).getType();
        String columnName = columnDefinition.getColumnName();

        boolean columnExists = changeLogTable.getColumn(columnName) != null;
        if (!columnExists) {
            executor.comment("Adding missing databasechangelog." + columnName + " column");
            statements.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, columnName, columnType, null));
        }

        return statements;
    }
}