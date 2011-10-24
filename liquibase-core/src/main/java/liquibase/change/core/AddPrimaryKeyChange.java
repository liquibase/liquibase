package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SQLiteDatabase.AlterTableVisitor;
import liquibase.database.structure.Index;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddPrimaryKeyStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a primary key out of an existing column or set of columns.
 */
@ChangeClass(name="addPrimaryKey", description = "Add Primary Key", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
public class AddPrimaryKeyChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private String tablespace;
    private String columnNames;
    private String constraintName;

    @ChangeProperty(requiredForDatabase = "all", mustApplyTo = "column.table")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @ChangeProperty(mustApplyTo ="column.table.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    @ChangeProperty(requiredForDatabase = "all", mustApplyTo = "column")
    public String getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }


    public String getTablespace() {
        return tablespace;
    }

    public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }

    public SqlStatement[] generateStatements(Database database) {


        String schemaName = getSchemaName() == null ? database.getDefaultSchemaName() : getSchemaName();

        AddPrimaryKeyStatement statement = new AddPrimaryKeyStatement(schemaName, getTableName(), getColumnNames(), getConstraintName());
        statement.setTablespace(getTablespace());

        if (database instanceof DB2Database) {
            return new SqlStatement[]{
                    statement,
                    new ReorganizeTableStatement(schemaName, getTableName())
            };
//todo        } else if (database instanceof SQLiteDatabase) {
//            // return special statements for SQLite databases
//            return generateStatementsForSQLiteDatabase(database);
        }

        return new SqlStatement[]{
                statement
        };
    }

    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database) {
        // SQLite does not support this ALTER TABLE operation until now.
        // or more information: http://www.sqlite.org/omitted.html
        // This is a small work around...

        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        // define alter table logic
        AlterTableVisitor rename_alter_visitor = new AlterTableVisitor() {
            public ColumnConfig[] getColumnsToAdd() {
                return new ColumnConfig[0];
            }

            public boolean copyThisColumn(ColumnConfig column) {
                return true;
            }

            public boolean createThisColumn(ColumnConfig column) {
                String[] split_columns = getColumnNames().split("[ ]*,[ ]*");
                for (String split_column : split_columns) {
                    if (column.getName().equals(split_column)) {
                        column.getConstraints().setPrimaryKey(true);
                    }
                }
                return true;
            }

            public boolean createThisIndex(Index index) {
                return true;
            }
        };

        try {
            // alter table
            statements.addAll(SQLiteDatabase.getAlterTableStatements(
                    rename_alter_visitor,
                    database, getSchemaName(), getTableName()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    @Override
    protected Change[] createInverses() {
        DropPrimaryKeyChange inverse = new DropPrimaryKeyChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setConstraintName(getConstraintName());

        return new Change[]{
                inverse,
        };
    }

    public String getConfirmationMessage() {
        return "Primary key added to " + getTableName() + " (" + getColumnNames() + ")";
    }
}
