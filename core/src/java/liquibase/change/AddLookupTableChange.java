package liquibase.change;

import liquibase.database.*;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.ReorganizeTableStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.ForeignKey;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Extracts data from an existing column to create a lookup table.
 * A foreign key is created between the old column and the new lookup table.
 */
public class AddLookupTableChange extends AbstractChange {

    private String existingTableSchemaName;
    private String existingTableName;
    private String existingColumnName;

    private String newTableSchemaName;
    private String newTableName;
    private String newColumnName;
    private String newColumnDataType;
    private String constraintName;

    public AddLookupTableChange() {
        super("addLookupTable", "Add Lookup Table");
    }

    public String getExistingTableSchemaName() {
        return existingTableSchemaName;
    }

    public void setExistingTableSchemaName(String existingTableSchemaName) {
        this.existingTableSchemaName = existingTableSchemaName;
    }

    public String getExistingTableName() {
        return existingTableName;
    }

    public void setExistingTableName(String existingTableName) {
        this.existingTableName = existingTableName;
    }

    public String getExistingColumnName() {
        return existingColumnName;
    }

    public void setExistingColumnName(String existingColumnName) {
        this.existingColumnName = existingColumnName;
    }


    public String getNewTableSchemaName() {
        return newTableSchemaName;
    }

    public void setNewTableSchemaName(String newTableSchemaName) {
        this.newTableSchemaName = newTableSchemaName;
    }

    public String getNewTableName() {
        return newTableName;
    }

    public void setNewTableName(String newTableName) {
        this.newTableName = newTableName;
    }

    public String getNewColumnName() {
        return newColumnName;
    }

    public void setNewColumnName(String newColumnName) {
        this.newColumnName = newColumnName;
    }

    public String getNewColumnDataType() {
        return newColumnDataType;
    }

    public void setNewColumnDataType(String newColumnDataType) {
        this.newColumnDataType = newColumnDataType;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public String getFinalConstraintName() {
        if (constraintName == null) {
            return ("FK_" + getExistingTableName() + "_" + getNewTableName()).toUpperCase();
        } else {
            return constraintName;
        }
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    public void validate(Database database) throws InvalidChangeDefinitionException {
        if (StringUtils.trimToNull(existingTableName) == null) {
            throw new InvalidChangeDefinitionException("existingTableName is required", this);
        }
        if (StringUtils.trimToNull(existingColumnName) == null) {
            throw new InvalidChangeDefinitionException("existingColumnName is required", this);
        }
        if (StringUtils.trimToNull(newTableName) == null) {
            throw new InvalidChangeDefinitionException("newTableName is required", this);
        }
        if (StringUtils.trimToNull(newColumnName) == null) {
            throw new InvalidChangeDefinitionException("newColumnName is required", this);
        }
        if (StringUtils.trimToNull(newColumnDataType) == null) {
            throw new InvalidChangeDefinitionException("newColumnDataType is required", this);
        }
    }

    protected Change[] createInverses() {
        DropForeignKeyConstraintChange dropFK = new DropForeignKeyConstraintChange();
        dropFK.setBaseTableSchemaName(getExistingTableSchemaName());
        dropFK.setBaseTableName(getExistingTableName());
        dropFK.setConstraintName(getFinalConstraintName());

        DropTableChange dropTable = new DropTableChange();
        dropTable.setSchemaName(getNewTableSchemaName());
        dropTable.setTableName(getNewTableName());

        return new Change[]{
                dropFK,
                dropTable,
        };
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        if (database instanceof DerbyDatabase) {
            throw new UnsupportedChangeException("Add Lookup Table currently not supported in Derby");
        } else if (database instanceof HsqlDatabase) {
            throw new UnsupportedChangeException("Add Lookup Table currently not supported in HSQLDB");
        } else if (database instanceof CacheDatabase) {
            throw new UnsupportedChangeException("Add Lookup Table not currently supported for Cache");
        } else if (database instanceof FirebirdDatabase) {
            throw new UnsupportedChangeException("Add Lookup Table not currently supported for Firebird");
        }

        List<SqlStatement> statements = new ArrayList<SqlStatement>();

        String newTableSchemaName = getNewTableSchemaName() == null?database.getDefaultSchemaName():getNewTableSchemaName();
        String existingTableSchemaName = getExistingTableSchemaName() == null?database.getDefaultSchemaName():getExistingTableSchemaName();

        SqlStatement[] createTablesSQL = {new RawSqlStatement("CREATE TABLE " + database.escapeTableName(newTableSchemaName, getNewTableName()) + " AS SELECT DISTINCT " + getExistingColumnName() + " AS " + getNewColumnName() + " FROM " + database.escapeTableName(existingTableSchemaName, getExistingTableName()) + " WHERE " + getExistingColumnName() + " IS NOT NULL")};
        if (database instanceof MSSQLDatabase) {
            createTablesSQL = new SqlStatement[]{new RawSqlStatement("SELECT DISTINCT " + getExistingColumnName() + " AS " + getNewColumnName() + " INTO " + database.escapeTableName(newTableSchemaName, getNewTableName()) + " FROM " + database.escapeTableName(existingTableSchemaName, getExistingTableName()) + " WHERE " + getExistingColumnName() + " IS NOT NULL"),};
        } else if (database instanceof DB2Database) {
            createTablesSQL = new SqlStatement[]{
                    new RawSqlStatement("CREATE TABLE " + database.escapeTableName(newTableSchemaName, getNewTableName()) + " AS (SELECT " + getExistingColumnName() + " AS " + getNewColumnName() + " FROM " + database.escapeTableName(existingTableSchemaName, getExistingTableName()) + ") WITH NO DATA"),
                    new RawSqlStatement("INSERT INTO " + database.escapeTableName(newTableSchemaName, getNewTableName()) + " SELECT DISTINCT " + getExistingColumnName() + " FROM " + database.escapeTableName(existingTableSchemaName, getExistingTableName()) + " WHERE " + getExistingColumnName() + " IS NOT NULL"),
            };
        }

        statements.addAll(Arrays.asList(createTablesSQL));

        if (!(database instanceof OracleDatabase)) {
            AddNotNullConstraintChange addNotNullChange = new AddNotNullConstraintChange();
            addNotNullChange.setSchemaName(newTableSchemaName);
            addNotNullChange.setTableName(getNewTableName());
            addNotNullChange.setColumnName(getNewColumnName());
            addNotNullChange.setColumnDataType(getNewColumnDataType());
            statements.addAll(Arrays.asList(addNotNullChange.generateStatements(database)));
        }

        if (database instanceof DB2Database) {
            statements.add(new ReorganizeTableStatement(newTableSchemaName, getNewTableName()));
        }

        AddPrimaryKeyChange addPKChange = new AddPrimaryKeyChange();
        addPKChange.setSchemaName(newTableSchemaName);
        addPKChange.setTableName(getNewTableName());
        addPKChange.setColumnNames(getNewColumnName());
        statements.addAll(Arrays.asList(addPKChange.generateStatements(database)));

        if (database instanceof DB2Database) {
            statements.add(new ReorganizeTableStatement(newTableSchemaName, getNewTableName()));
        }

        AddForeignKeyConstraintChange addFKChange = new AddForeignKeyConstraintChange();
        addFKChange.setBaseTableSchemaName(existingTableSchemaName);
        addFKChange.setBaseTableName(getExistingTableName());
        addFKChange.setBaseColumnNames(getExistingColumnName());
        addFKChange.setReferencedTableSchemaName(newTableSchemaName);
        addFKChange.setReferencedTableName(getNewTableName());
        addFKChange.setReferencedColumnNames(getNewColumnName());

        addFKChange.setConstraintName(getFinalConstraintName());
        statements.addAll(Arrays.asList(addFKChange.generateStatements(database)));

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    public String getConfirmationMessage() {
        return "Lookup table added for "+getExistingTableName()+"."+getExistingColumnName();
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement(getTagName());
        if (getExistingTableSchemaName() != null) {
            node.setAttribute("newTableSchemaName", getExistingTableSchemaName());
        }
        node.setAttribute("existingTableName", getExistingTableName());
        node.setAttribute("existingColumnName", getExistingColumnName());

        if (getNewTableSchemaName() != null) {
            node.setAttribute("newTableSchemaName", getNewTableSchemaName());
        }

        node.setAttribute("newTableName", getNewTableName());
        node.setAttribute("newColumnName", getNewColumnName());
        node.setAttribute("constraintName", getConstraintName());

        return node;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Set<DatabaseObject> returnSet = new HashSet<DatabaseObject>();

        Table existingTable = new Table(getExistingTableName());
        returnSet.add(existingTable);

        Column existingColumn = new Column();
        existingColumn.setTable(existingTable);
        existingColumn.setName(getExistingColumnName());
        returnSet.add(existingColumn);

        Table newTable = new Table(getNewTableName());
        returnSet.add(newTable);

        Column newColumn = new Column();
        newColumn.setTable(existingTable);
        newColumn.setName(getNewColumnName());
        returnSet.add(newColumn);

        ForeignKey fk = new ForeignKey();
        fk.setForeignKeyTable(existingTable);
        fk.setForeignKeyColumns(existingColumn.getName());
        fk.setPrimaryKeyTable(newTable);
        fk.setPrimaryKeyColumns(newColumn.getName());
        returnSet.add(fk);

        return returnSet;

    }
}
