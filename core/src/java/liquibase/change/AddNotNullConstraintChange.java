package liquibase.change;

import liquibase.database.*;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import liquibase.util.SqlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Adds a not-null constraint to an existing column.
 */
public class AddNotNullConstraintChange extends AbstractChange {
    private String schemaName;
    private String tableName;
    private String columnName;
    private String defaultNullValue;
    private String columnDataType;


    public AddNotNullConstraintChange() {
        super("addNotNullConstraint", "Add Not-Null Constraint");
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
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

    public String getDefaultNullValue() {
        return defaultNullValue;
    }

    public void setDefaultNullValue(String defaultNullValue) {
        this.defaultNullValue = defaultNullValue;
    }

    public String getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }


    private SqlStatement generateUpdateStatement(Database database) {
        return new RawSqlStatement("UPDATE " + database.escapeTableName(getSchemaName(), tableName) + " SET " + columnName + "='" + defaultNullValue + "' WHERE " + columnName + " IS NULL");
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
            return generateDerbyStatements((DerbyDatabase) database);
        } else if (database instanceof H2Database) {
            return generateH2Statements((H2Database) database);
        } else if (database instanceof CacheDatabase) {
            return generateCacheStatements((CacheDatabase) database);
        } else if (database instanceof FirebirdDatabase) {
            throw new UnsupportedChangeException("LiquiBase does not currently support adding null constraints in Firebird");
        }

        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement(database));
        }

        statements.add(new RawSqlStatement("ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER COLUMN  " + getColumnName() + " SET NOT NULL "));

        if (database instanceof DB2Database) {
            statements.add(new RawSqlStatement("CALL SYSPROC.ADMIN_CMD ('REORG TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + "')"));
        }

        return statements.toArray(new SqlStatement[statements.size()]);

    }

    private SqlStatement[] generateCacheStatements(CacheDatabase database) {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement(database));
        }
        statements.add(new RawSqlStatement("COMMIT"));
        statements.add(new RawSqlStatement("ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER COLUMN " + getColumnName() + " NOT NULL"));

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    private SqlStatement[] generateSybaseStatements(SybaseDatabase database) {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement(database));
        }
        statements.add(new RawSqlStatement("COMMIT"));
        statements.add(new RawSqlStatement("ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " MODIFY " + getColumnName() + " NOT NULL"));

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    private SqlStatement[] generateMSSQLStatements(MSSQLDatabase database) {
        if (columnDataType == null) {
            throw new RuntimeException("columnDataType is required to add not null constraints with MS-SQL");
        }

        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement(database));
        }
        statements.add(new RawSqlStatement("ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER COLUMN " + getColumnName() + " " + columnDataType + " " + " NOT NULL"));

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    private SqlStatement[] generateMySQLStatements(MySQLDatabase database) {
        if (columnDataType == null) {
            throw new RuntimeException("columnDataType is required to add not null constraints with MySQL");
        }

        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement(database));
        }
        statements.add(new RawSqlStatement("ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " MODIFY " + getColumnName() + " " + columnDataType + " NOT NULL"));

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    private SqlStatement[] generateOracleStatements(OracleDatabase database) {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement(database));
        }
        statements.add(new RawSqlStatement("ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " MODIFY " + getColumnName() + " NOT NULL"));

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    public SqlStatement[] generateDerbyStatements(DerbyDatabase database) {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement(database));
        }
        statements.add(new RawSqlStatement("ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER COLUMN  " + getColumnName() + " NOT NULL"));

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    public SqlStatement[] generateH2Statements(H2Database database) {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        if (defaultNullValue != null) {
            statements.add(generateUpdateStatement(database));
        }
        statements.add(new RawSqlStatement("ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER COLUMN  " + getColumnName() + " " + getColumnDataType() + " NOT NULL"));

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    protected Change[] createInverses() {
        DropNotNullConstraintChange inverse = new DropNotNullConstraintChange();
        inverse.setColumnName(getColumnName());
        inverse.setTableName(getTableName());
        inverse.setColumnDataType(getColumnDataType());

        return new Change[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Null constraint has been added to " + getTableName() + "." + getColumnName();
    }


    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("addNotNullConstraint");
        if (getSchemaName() != null) {
            element.setAttribute("schemaName", getSchemaName());
        }

        element.setAttribute("tableName", getTableName());
        element.setAttribute("columnName", getColumnName());
        element.setAttribute("defaultNullValue", getDefaultNullValue());
        return element;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {

        Table table = new Table(getTableName());

        Column column = new Column();
        column.setTable(table);
        column.setName(getColumnName());


        return new HashSet<DatabaseObject>(Arrays.asList(table, column));

    }
}
