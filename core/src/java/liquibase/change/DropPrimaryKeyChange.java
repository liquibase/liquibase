package liquibase.change;

import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;
import liquibase.database.PostgresDatabase;
import liquibase.database.FirebirdDatabase;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import liquibase.util.SqlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * Removes an existing primary key.
 */
public class DropPrimaryKeyChange extends AbstractChange {
    private String tableName;
    private String constraintName;

    public DropPrimaryKeyChange() {
        super("dropPrimaryKey", "Drop Primary Key");
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        if (database instanceof MSSQLDatabase) {
            return generateMSSQLStatements((MSSQLDatabase) database);
        } else if (database instanceof PostgresDatabase) {
            return generatePostgresStatements((PostgresDatabase) database);
        } else if (database instanceof FirebirdDatabase) {
            return new SqlStatement[]{
                    new RawSqlStatement("ALTER TABLE " + SqlUtil.escapeTableName(getTableName(),database) + " DROP CONSTRAINT "+getConstraintName()),
            };
        }

        return new SqlStatement[]{
                new RawSqlStatement("ALTER TABLE " + SqlUtil.escapeTableName(getTableName(),database) + " DROP PRIMARY KEY"),
        };
    }

    private SqlStatement[] generateMSSQLStatements(MSSQLDatabase database) throws UnsupportedChangeException {
        if (getConstraintName() == null) {
            throw new UnsupportedChangeException("MS-SQL requires a constraint name to drop the primary key");
        }
        return new SqlStatement[]{
                new RawSqlStatement("ALTER TABLE " + SqlUtil.escapeTableName(getTableName(), database) + " DROP CONSTRAINT " + getConstraintName()),
        };
    }

    private SqlStatement[] generatePostgresStatements(PostgresDatabase database) throws UnsupportedChangeException {
        if (getConstraintName() == null) {
            throw new UnsupportedChangeException("PostgreSQL requires a constraint name to drop the primary key");
        }
        return new SqlStatement[]{
                new RawSqlStatement("ALTER TABLE " + SqlUtil.escapeTableName(getTableName(), database) + " DROP CONSTRAINT " + getConstraintName()),
        };
    }

    public String getConfirmationMessage() {
        return "Primary key dropped from "+getTableName();
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement(getTagName());
        node.setAttribute("tableName", getTableName());
        return node;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {

        Set<DatabaseObject> dbObjects = new HashSet<DatabaseObject>();

        Table table = new Table();
        table.setName(tableName);
        dbObjects.add(table);

        return dbObjects;

    }

}
