package liquibase.sqlgenerator.core;

import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddColumnStatement;
import liquibase.structure.core.Index;

import java.util.Set;

/**
 * Workaround for adding column on existing table for SQLite.
 *
 */
public class AddColumnGeneratorSQLite extends AddColumnGenerator {

    @Override
    public ValidationErrors validate(AddColumnStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = super.validate(statement, database, sqlGeneratorChain);
        validationErrors.checkRequiredField("tableName", statement);
        validationErrors.checkRequiredField("columnName", statement);
        return validationErrors;
    }

    @Override
    public boolean generateStatementsIsVolatile(Database database) {
        // need metadata for copying the table
        return true;
    }

    @Override
    public boolean supports(AddColumnStatement statement, Database database) {
        return database instanceof SQLiteDatabase;
    }

    @Override
    public Sql[] generateSql(final AddColumnStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Workaround implemented by replacing a table with a new one (duplicate)
        // with a new column added
        Sql[] generatedSqls;
        SQLiteDatabase.AlterTableVisitor alterTableVisitor = new SQLiteDatabase.AlterTableVisitor() {
            @Override
            public ColumnConfig[] getColumnsToAdd() {
                ColumnConfig[] columnConfigs = new ColumnConfig[1];
                ColumnConfig newColumn = new ColumnConfig();
                newColumn.setName(statement.getColumnName());
                newColumn.setType(statement.getColumnType());
                newColumn.setAutoIncrement(statement.isAutoIncrement());
                ConstraintsConfig constraintsConfig = new ConstraintsConfig();
                if (statement.isPrimaryKey()) {
                    constraintsConfig.setPrimaryKey(true);
                }
                if (statement.isNullable()) {
                    constraintsConfig.setNullable(true);
                }
                if (statement.isUnique()) {
                    constraintsConfig.setUnique(true);
                }
                newColumn.setConstraints(constraintsConfig);
                columnConfigs[0] = newColumn;
                return columnConfigs;
            }

            @Override
            public boolean copyThisColumn(ColumnConfig column) {
                return !column.getName().equals(statement.getColumnName());
            }

            @Override
            public boolean createThisColumn(ColumnConfig column) {
                return true;
            }

            @Override
            public boolean createThisIndex(Index index) {
                return true;
            }
        };
        generatedSqls = SQLiteDatabase.getAlterTableSqls(database, alterTableVisitor, statement.getCatalogName(),
                statement.getSchemaName(), statement.getTableName());

        return generatedSqls;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }
}
