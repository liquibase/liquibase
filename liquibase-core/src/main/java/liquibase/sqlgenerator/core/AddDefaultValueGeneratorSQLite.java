package liquibase.sqlgenerator.core;

import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.BooleanType;
import liquibase.datatype.core.CharType;
import liquibase.datatype.core.DateType;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddDefaultValueStatement;
import liquibase.structure.core.Index;

import java.util.Date;

/**
 * Workaround for Adding default value for SQLite
 */
public class AddDefaultValueGeneratorSQLite extends AddDefaultValueGenerator {


    @Override
    public ValidationErrors validate(AddDefaultValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return super.validate(statement, database, sqlGeneratorChain);
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddDefaultValueStatement statement, Database database) {
        return database instanceof SQLiteDatabase;
    }

    @Override
    public boolean generateStatementsIsVolatile(Database database) {
        // need metadata for copying the table
        return true;
    }

    @Override
    public Sql[] generateSql(final AddDefaultValueStatement statement, final Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Workaround implemented by replacing a table with a new one (duplicate)
        // with default value set on the specified column
        final SQLiteDatabase.AlterTableVisitor alterTableVisitor = new SQLiteDatabase.AlterTableVisitor() {
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
                // update the column to set Default value while copying
                if (column.getName().equals(statement.getColumnName())) {
                    if (statement.getDefaultValueConstraintName() != null) {
                        column.setDefaultValueConstraintName(statement.getDefaultValueConstraintName());
                    }

                    // could be of string, numeric, boolean and date
                    Object defaultValue = statement.getDefaultValue();

                    LiquibaseDataType dataType = DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType(), database);
                    if (dataType instanceof BooleanType) {
                        // validation is done before hand so the defaultValue is instance of dataType
                        if (defaultValue instanceof Boolean) {
                            column.setDefaultValueBoolean((Boolean) defaultValue);
                        } else {
                            column.setDefaultValueBoolean(defaultValue.toString());
                        }
                    } else if (dataType instanceof CharType) {
                        column.setDefaultValue(defaultValue.toString());
                    } else if (dataType instanceof DateType) {
                        if (defaultValue instanceof Date) {
                            column.setDefaultValueDate((Date) defaultValue);
                        } else {
                            column.setDefaultValueDate(defaultValue.toString());
                        }
                    } else {
                        // fallback??
                        column.setDefaultValue(defaultValue.toString());
                    }


                }
                return true;
            }

            @Override
            public boolean createThisIndex(Index index) {
                return true;
            }
        };
        Sql[] generatedSqls = SQLiteDatabase.getAlterTableSqls(database, alterTableVisitor, statement.getCatalogName(),
                statement.getSchemaName(), statement.getTableName());

        return generatedSqls;
    }
}
