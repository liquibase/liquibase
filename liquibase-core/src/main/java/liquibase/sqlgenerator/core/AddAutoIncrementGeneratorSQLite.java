package liquibase.sqlgenerator.core;

import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddAutoIncrementStatement;
import liquibase.structure.core.Index;

/**
 * This class provides a workaround for adding auto increment for SQLite
 * since SQLite does not support it
 */
public class AddAutoIncrementGeneratorSQLite extends AddAutoIncrementGenerator {

    @Override
    public ValidationErrors validate(AddAutoIncrementStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return super.validate(statement, database, sqlGeneratorChain);
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddAutoIncrementStatement statement, Database database) {
        return database instanceof SQLiteDatabase;
    }

    @Override
    public boolean generateStatementsIsVolatile(Database database) {
        // need metadata for copying the table
        return true;
    }

    @Override
    public Sql[] generateSql(final AddAutoIncrementStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Workaround implemented by replacing a table with a new one (duplicate)
        // with auto increment set true on the specified column
        // https://sqlite.org/autoinc.html
        // For Auto increment to work, the column has to be either primary or have rowid specified on insert statements
        // Since adding rowid on inserts is out of scope here, we will try to use primary key for the column
        Sql[] generatedSqls;
        SQLiteDatabase.AlterTableVisitor alterTableVisitor = new SQLiteDatabase.AlterTableVisitor() {
            @Override
            public ColumnConfig[] getColumnsToAdd() {
                return new ColumnConfig[0];
            }

            @Override
            public boolean copyThisColumn(ColumnConfig column) {
                return true;
            }

            @Override
            public boolean createThisColumn(ColumnConfig column) {
                // update the column to set autoincrement while copying
                setPrimaryKeyAndAutoIncrement(column, statement);
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

    private void setPrimaryKeyAndAutoIncrement(ColumnConfig column, AddAutoIncrementStatement statement) {
        if (column.getName().equals(statement.getColumnName())) {
            column.setAutoIncrement(true);
            ConstraintsConfig constraints = column.getConstraints();
            if (constraints == null) {
                constraints = new ConstraintsConfig();
                column.setConstraints(constraints);
            }
            constraints.setPrimaryKey(true);
        }
    }
}
