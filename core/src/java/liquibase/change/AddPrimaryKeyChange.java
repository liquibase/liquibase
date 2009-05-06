package liquibase.change;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.database.SQLiteDatabase.AlterTableVisitor;
import liquibase.database.structure.Index;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.JDBCException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.statement.AddPrimaryKeyStatement;
import liquibase.statement.ReorganizeTableStatement;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a primary key out of an existing column or set of columns.
 */
public class AddPrimaryKeyChange extends AbstractChange {

    private String schemaName;
    private String tableName;
    private String tablespace;
    private String columnNames;
    private String constraintName;

    public AddPrimaryKeyChange() {
        super("addPrimaryKey", "Add Primary Key");
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

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

    public void validate(Database database) throws InvalidChangeDefinitionException {
        if (StringUtils.trimToNull(tableName) == null) {
            throw new InvalidChangeDefinitionException("tableName is required", this);
        }
        if (StringUtils.trimToNull(columnNames) == null) {
            throw new InvalidChangeDefinitionException("columnNames is required", this);
        }

    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {


        String schemaName = getSchemaName() == null ? database.getDefaultSchemaName() : getSchemaName();

        AddPrimaryKeyStatement statement = new AddPrimaryKeyStatement(schemaName, getTableName(), getColumnNames(), getConstraintName());
        statement.setTablespace(getTablespace());

        if (database instanceof DB2Database) {
            return new SqlStatement[]{
                    statement,
                    new ReorganizeTableStatement(schemaName, getTableName())
            };
        } else if (database instanceof SQLiteDatabase) {
            // return special statements for SQLite databases
            return generateStatementsForSQLiteDatabase(database);
        }

        return new SqlStatement[]{
                statement
        };
    }

    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database)
            throws UnsupportedChangeException {
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
        } catch (JDBCException e) {
            e.printStackTrace();
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

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
