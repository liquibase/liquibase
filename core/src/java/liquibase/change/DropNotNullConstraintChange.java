package liquibase.change;

import liquibase.database.*;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Drops a not-null constraint from an existing column.
 */
public class DropNotNullConstraintChange extends AbstractChange {
    private String tableName;
    private String columnName;
    private String columnDataType;


    public DropNotNullConstraintChange() {
        super("dropNotNullConstraint", "Drop Not-Null Constraint");
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        if (database instanceof SybaseDatabase) {
            return generateSybaseStatements((SybaseDatabase) database);
        } else if (database instanceof MSSQLDatabase) {
            return generateMSSQLStatements((MSSQLDatabase) database);
        } else if (database instanceof MySQLDatabase) {
            return generateMySQLStatements((MySQLDatabase) database);
        } else if (database instanceof OracleDatabase) {
            return generateOracleStatements((OracleDatabase) database);
        } else if (database instanceof DerbyDatabase) {
            return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + escapeTableName(tableName, database) + " ALTER COLUMN " + columnName + " NULL")};
        } else if (database instanceof HsqlDatabase) {
            return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + escapeTableName(tableName, database) + " ALTER COLUMN " + columnName + " NULL")};
        } else if (database instanceof CacheDatabase) {
            return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + escapeTableName(tableName, database) + " ALTER COLUMN " + columnName + " NULL")};
        }

        return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + escapeTableName(tableName, database) + " ALTER COLUMN " + columnName + " DROP NOT NULL")};
    }

    private SqlStatement[] generateSybaseStatements(SybaseDatabase database) {
        return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + escapeTableName(tableName, database) + " MODIFY " + columnName + " NULL")};
    }

    private SqlStatement[] generateMSSQLStatements(MSSQLDatabase database) {
        if (columnDataType == null) {
            throw new RuntimeException("columnDataType is required to drop not null constraints with MS-SQL");
        }

        return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + escapeTableName(tableName, database) + " ALTER COLUMN " + columnName + " " + columnDataType + " NULL")};
    }

    private SqlStatement[] generateOracleStatements(OracleDatabase database) {
        return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + escapeTableName(tableName, database) + " MODIFY " + columnName + " NULL")};
    }

    private SqlStatement[] generateMySQLStatements(MySQLDatabase database) {
        if (columnDataType == null) {
            throw new RuntimeException("columnDataType is required to drop not null constraints with MySQL");
        }

        return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + escapeTableName(tableName, database) + " MODIFY " + columnName + " " + columnDataType + " DEFAULT NULL")};
    }

    protected Change[] createInverses() {
        AddNotNullConstraintChange inverse = new AddNotNullConstraintChange();
        inverse.setColumnName(getColumnName());
        inverse.setTableName(getTableName());
        inverse.setColumnDataType(getColumnDataType());

        return new Change[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Null constraint dropped from " + getTableName() + "." + getColumnName();
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("dropNotNullConstraint");
        element.setAttribute("tableName", getTableName());
        element.setAttribute("columnName", getColumnName());
        return element;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {

        Table table = new Table();
        table.setName(tableName);

        Column column = new Column();
        column.setTable(table);
        column.setName(columnName);


        return new HashSet<DatabaseObject>(Arrays.asList(table, column));
    }
}
