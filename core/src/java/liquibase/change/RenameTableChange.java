package liquibase.change;

import liquibase.database.*;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import liquibase.util.SqlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Renames an existing table.
 */
public class RenameTableChange extends AbstractChange {

    private String schemaName;
    private String oldTableName;

    private String newTableName;

    public RenameTableChange() {
        super("renameTable", "Rename Table");
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getOldTableName() {
        return oldTableName;
    }

    public void setOldTableName(String oldTableName) {
        this.oldTableName = oldTableName;
    }

    public String getNewTableName() {
        return newTableName;
    }

    public void setNewTableName(String newTableName) {
        this.newTableName = newTableName;
    }


    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        if (database instanceof MSSQLDatabase) {
            return new SqlStatement[]{new RawSqlStatement("exec sp_rename '" + database.escapeTableName(getSchemaName(), oldTableName) + "', " + database.escapeTableName(null, newTableName))};
        } else if (database instanceof MySQLDatabase) {
            return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + database.escapeTableName(getSchemaName(), oldTableName) + " RENAME " + database.escapeTableName(getSchemaName(), getNewTableName()))};
        } else if (database instanceof PostgresDatabase) {
            return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + database.escapeTableName(getSchemaName(), oldTableName) + " RENAME TO " + database.escapeTableName(null, newTableName))};
        } else if (database instanceof DerbyDatabase) {
            return new SqlStatement[]{new RawSqlStatement("RENAME TABLE " + database.escapeTableName(getSchemaName(), oldTableName) + " TO " + database.escapeTableName(null, newTableName))};
        } else if (database instanceof HsqlDatabase) {
            return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + database.escapeTableName(getSchemaName(), oldTableName) + " RENAME TO " + database.escapeTableName(null, newTableName))};
        } else if (database instanceof OracleDatabase) {
            return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + database.escapeTableName(getSchemaName(), oldTableName) + " RENAME TO " + database.escapeTableName(null, newTableName))};
        } else if (database instanceof DB2Database) {
            return new SqlStatement[]{
                    new RawSqlStatement("RENAME " + database.escapeTableName(getSchemaName(), oldTableName) + " TO " + database.escapeTableName(null, newTableName)), //db2 doesn't allow specifying new schema name
                    new RawSqlStatement("CALL SYSPROC.ADMIN_CMD ('REORG TABLE " + database.escapeTableName(getSchemaName(), getNewTableName()) + "')"),
            };
        } else if (database instanceof CacheDatabase) {
            throw new UnsupportedChangeException("Rename table not currently supported for Cache");
        } else if (database instanceof FirebirdDatabase) {
            throw new UnsupportedChangeException("Rename table not currently supported for Firebird");
        }

        return new SqlStatement[]{new RawSqlStatement("RENAME " + database.escapeTableName(getSchemaName(), getOldTableName()) + " TO " + database.escapeTableName(getSchemaName(), getNewTableName()))};
    }

    protected Change[] createInverses() {
        RenameTableChange inverse = new RenameTableChange();
        inverse.setOldTableName(getNewTableName());
        inverse.setNewTableName(getOldTableName());

        return new Change[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Table " + oldTableName + " renamed to " + newTableName;
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element element = currentChangeLogFileDOM.createElement("renameTable");
        if (getSchemaName() != null) {
            element.setAttribute("schemaName", getSchemaName());
        }

        element.setAttribute("oldTableName", getOldTableName());

        element.setAttribute("newTableName", getNewTableName());

        return element;
    }


    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Table oldTable = new Table(getOldTableName());

        Table newTable = new Table(getNewTableName());

        return new HashSet<DatabaseObject>(Arrays.asList(oldTable, newTable));
    }
}
