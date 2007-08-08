package liquibase.migrator.change;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.DerbyDatabase;
import liquibase.database.HsqlDatabase;
import liquibase.database.MSSQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.migrator.exception.UnsupportedChangeException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Extracts data from an existing column to create a lookup table.
 * A foreign key is created between the old column and the new lookup table.
 */
public class AddLookupTableChange extends AbstractChange {

    private String existingTableName;
    private String existingColumnName;
    private String newTableName;
    private String newColumnName;
    private String newColumnDataType;
    private String constraintName;

    public AddLookupTableChange() {
        super("addLookupTable", "Add Lookup Table");
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

    protected Change[] createInverses() {
        DropForeignKeyConstraintChange dropFK = new DropForeignKeyConstraintChange();
        dropFK.setBaseTableName(getExistingTableName());
        dropFK.setConstraintName(getFinalConstraintName());

        DropTableChange dropTable = new DropTableChange();
        dropTable.setTableName(getNewTableName());

        return new Change[]{
                dropFK,
                dropTable,
        };
    }

    public String[] generateStatements(Database database) throws UnsupportedChangeException {
        if (database instanceof DerbyDatabase) {
            throw new UnsupportedChangeException("Add Lookup Table currently not supported in Derby");
        } else if (database instanceof HsqlDatabase) {
                throw new UnsupportedChangeException("Add Lookup Table currently not supported in HSQLDB");
        }

        List<String> statements = new ArrayList<String>();

        String[] createTablesSQL = { "CREATE TABLE " + getNewTableName() + " AS SELECT DISTINCT " + getExistingColumnName() + " AS " + getNewColumnName() + " FROM " + getExistingTableName() + " WHERE " + getExistingColumnName() + " IS NOT NULL"};
        if (database instanceof MSSQLDatabase) {
            createTablesSQL = new String[] {"SELECT DISTINCT " + getExistingColumnName() + " AS " + getNewColumnName() + " INTO " + getNewTableName() + " FROM " + getExistingTableName() + " WHERE " + getExistingColumnName() + " IS NOT NULL", };
        } else if (database instanceof DB2Database) {
           createTablesSQL = new String[] {
                   "CREATE TABLE " + getNewTableName() + " AS (SELECT " + getExistingColumnName() + " AS " + getNewColumnName() + " FROM " + getExistingTableName() + ") WITH NO DATA",
                   "INSERT INTO "+getNewTableName()+" SELECT DISTINCT " + getExistingColumnName() + " FROM " + getExistingTableName() + " WHERE " + getExistingColumnName() + " IS NOT NULL",
           };
        }

        statements.addAll(Arrays.asList(createTablesSQL));

        if (!(database instanceof OracleDatabase)) {
            AddNotNullConstraintChange addNotNullChange = new AddNotNullConstraintChange();
            addNotNullChange.setTableName(getNewTableName());
            addNotNullChange.setColumnName(getNewColumnName());
            addNotNullChange.setColumnDataType(getNewColumnDataType());
            statements.addAll(Arrays.asList(addNotNullChange.generateStatements(database)));
        }

        if (database instanceof DB2Database) {
            statements.add("CALL SYSPROC.ADMIN_CMD ('REORG TABLE "+getNewTableName()+"')");
        }

        AddPrimaryKeyChange addPKChange = new AddPrimaryKeyChange();
        addPKChange.setTableName(getNewTableName());
        addPKChange.setColumnNames(getNewColumnName());
        statements.addAll(Arrays.asList(addPKChange.generateStatements(database)));

        if (database instanceof DB2Database) {
            statements.add("CALL SYSPROC.ADMIN_CMD ('REORG TABLE "+getNewTableName()+"')");
        }
        
        AddForeignKeyConstraintChange addFKChange = new AddForeignKeyConstraintChange();
        addFKChange.setBaseTableName(getExistingTableName());
        addFKChange.setBaseColumnNames(getExistingColumnName());
        addFKChange.setReferencedTableName(getNewTableName());
        addFKChange.setReferencedColumnNames(getNewColumnName());

        addFKChange.setConstraintName(getFinalConstraintName());
        statements.addAll(Arrays.asList(addFKChange.generateStatements(database)));

        return statements.toArray(new String[statements.size()]);
    }

    public String getConfirmationMessage() {
        return "Lookup table added";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement(getTagName());
        node.setAttribute("existingTableName", getExistingTableName());
        node.setAttribute("existingColumnName", getExistingColumnName());
        node.setAttribute("newTableName", getNewTableName());
        node.setAttribute("newColumnName", getNewColumnName());
        node.setAttribute("constraintName", getConstraintName());

        return node;
    }
}
