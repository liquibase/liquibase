package liquibase.changelog.definition;

import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.ModifyDataTypeStatement;
import liquibase.structure.core.DataType;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

public class ShortTextColumnDefinition implements AlterChangeLogTableSqlStatementProvider {

    private final ChangeLogColumnDefinition columnDefinition;

    public ShortTextColumnDefinition(ChangeLogColumnDefinition columnDefinition) {
        this.columnDefinition = columnDefinition;
    }

    public List<SqlStatement> createSqlStatements(Database database, Table changeLogTable) throws DatabaseException {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        String liquibaseCatalogName = database.getLiquibaseCatalogName();
        String liquibaseSchemaName = database.getLiquibaseSchemaName();
        String databaseChangeLogTableName = database.getDatabaseChangeLogTableName();
        Executor executor = ExecutorService.getInstance().getExecutor(database);

        String columnName = columnDefinition.getColumnName();
        String columnType = columnDefinition.getDataType().toDatabaseDataType(database).getType();
        Integer desiredColumnSize = (Integer)columnDefinition.getDataType().getParameters()[0];

        boolean columnExists = changeLogTable.getColumn(columnName) != null;
        if (!columnExists) {
            executor.comment("Adding missing databasechangelog." + columnName + " column");
            statements.add(new AddColumnStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, columnName, columnType, null));
        } else {
            boolean liquibaseColumnNotRightSize = false;
            if (!(database instanceof SQLiteDatabase)) {
                DataType type = changeLogTable.getColumn(columnName).getType();
                if (type.getTypeName().toLowerCase().startsWith("varchar")) {
                    Integer columnSize = type.getColumnSize();
                    liquibaseColumnNotRightSize = (columnSize != null) && (columnSize < desiredColumnSize);
                }
            }
            if (liquibaseColumnNotRightSize) {
                executor.comment("Modifying size of databasechangelog." + columnName + " column");

                statements.add(new ModifyDataTypeStatement(liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, columnName, columnType));
            }
        }

        return statements;
    }
}