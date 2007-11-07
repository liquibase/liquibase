package liquibase.change;

import liquibase.database.Database;
import liquibase.database.sql.AddForeignKeyConstraintStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.ForeignKey;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * Adds a foreign key constraint to an existing column.
 */
public class AddForeignKeyConstraintChange extends AbstractChange {
    private String baseTableSchemaName;
    private String baseTableName;
    private String baseColumnNames;

    private String referencedTableSchemaName;
    private String referencedTableName;
    private String referencedColumnNames;

    private String constraintName;

    private Boolean deferrable;
    private Boolean initiallyDeferred;

    private Boolean deleteCascade;

    public AddForeignKeyConstraintChange() {
        super("addForeignKeyConstraint", "Add Foreign Key Constraint");
    }

    public String getBaseTableSchemaName() {
        return baseTableSchemaName;
    }

    public void setBaseTableSchemaName(String baseTableSchemaName) {
        this.baseTableSchemaName = baseTableSchemaName;
    }

    public String getBaseTableName() {
        return baseTableName;
    }

    public void setBaseTableName(String baseTableName) {
        this.baseTableName = baseTableName;
    }

    public String getBaseColumnNames() {
        return baseColumnNames;
    }

    public void setBaseColumnNames(String baseColumnNames) {
        this.baseColumnNames = baseColumnNames;
    }

    public String getReferencedTableSchemaName() {
        return referencedTableSchemaName;
    }

    public void setReferencedTableSchemaName(String referencedTableSchemaName) {
        this.referencedTableSchemaName = referencedTableSchemaName;
    }

    public String getReferencedTableName() {
        return referencedTableName;
    }

    public void setReferencedTableName(String referencedTableName) {
        this.referencedTableName = referencedTableName;
    }

    public String getReferencedColumnNames() {
        return referencedColumnNames;
    }

    public void setReferencedColumnNames(String referencedColumnNames) {
        this.referencedColumnNames = referencedColumnNames;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    public Boolean getDeferrable() {
        return deferrable;
    }

    public void setDeferrable(Boolean deferrable) {
        this.deferrable = deferrable;
    }

    public Boolean getInitiallyDeferred() {
        return initiallyDeferred;
    }

    public void setInitiallyDeferred(Boolean initiallyDeferred) {
        this.initiallyDeferred = initiallyDeferred;
    }


    public Boolean getDeleteCascade() {
        return deleteCascade;
    }

    public void setDeleteCascade(Boolean deleteCascade) {
        this.deleteCascade = deleteCascade;
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        boolean deferrable = false;
        if (getDeferrable() != null) {
            deferrable = getDeferrable();
        }

        boolean initiallyDeferred = false;
        if (getInitiallyDeferred() != null) {
            initiallyDeferred = getInitiallyDeferred();
        }

        boolean deleteCascade = false;
        if (getDeleteCascade() != null) {
            deleteCascade = getDeleteCascade();
        }
        
        return new SqlStatement[]{
                new AddForeignKeyConstraintStatement(getConstraintName(),
                        getBaseTableSchemaName(),
                        getBaseTableName(),
                        getBaseColumnNames(),
                        getReferencedTableSchemaName(),
                        getReferencedTableName(),
                        getReferencedColumnNames())
                .setDeferrable(deferrable)
                .setInitiallyDeferred(initiallyDeferred)
                .setDeleteCascade(deleteCascade)
        };
    }

    protected Change[] createInverses() {
        DropForeignKeyConstraintChange inverse = new DropForeignKeyConstraintChange();
        inverse.setBaseTableSchemaName(getBaseTableSchemaName());
        inverse.setBaseTableName(getBaseTableName());
        inverse.setConstraintName(getConstraintName());

        return new Change[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Foreign key contraint added to " + getBaseTableName() + " (" + getBaseColumnNames() + ")";
    }

    public Element createNode(Document currentChangeLogFileDOM) {
        Element node = currentChangeLogFileDOM.createElement(getTagName());

        if (getBaseTableSchemaName() != null) {
            node.setAttribute("baseTableSchemaName", getBaseTableSchemaName());
        }

        node.setAttribute("baseTableName", getBaseTableName());
        node.setAttribute("baseColumnNames", getBaseColumnNames());
        node.setAttribute("constraintName", getConstraintName());

        if (getReferencedTableSchemaName() != null) {
            node.setAttribute("referencedTableSchemaName", getReferencedTableSchemaName());
        }
        node.setAttribute("referencedTableName", getReferencedTableName());
        node.setAttribute("referencedColumnNames", getReferencedColumnNames());

        if (getDeferrable() != null) {
            node.setAttribute("deferrable", getDeferrable().toString());
        }

        if (getInitiallyDeferred() != null) {
            node.setAttribute("initiallyDeferred", getInitiallyDeferred().toString());
        }

        if (getDeleteCascade() != null) {
            node.setAttribute("deleteCascade", getDeleteCascade().toString());
        }
        return node;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        Set<DatabaseObject> returnSet = new HashSet<DatabaseObject>();

        Table baseTable = new Table(getBaseTableName());
        returnSet.add(baseTable);

        for (String columnName : getBaseColumnNames().split(",")) {
            Column baseColumn = new Column();
            baseColumn.setTable(baseTable);
            baseColumn.setName(columnName.trim());

            returnSet.add(baseColumn);
        }

        Table referencedTable = new Table(getReferencedTableName());
        returnSet.add(referencedTable);

        for (String columnName : getReferencedColumnNames().split(",")) {
            Column referencedColumn = new Column();
            referencedColumn.setTable(baseTable);
            referencedColumn.setName(columnName.trim());

            returnSet.add(referencedColumn);
        }

        ForeignKey fk = new ForeignKey();
        fk.setName(constraintName);
        fk.setForeignKeyTable(baseTable);
        fk.setForeignKeyColumn(baseColumnNames);
        fk.setPrimaryKeyTable(referencedTable);
        fk.setPrimaryKeyColumn(referencedColumnNames);
        returnSet.add(fk);

        return returnSet;

    }

}
