package liquibase.change;

import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.database.sql.AddForeignKeyConstraintStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.ForeignKey;
import liquibase.database.structure.Table;
import liquibase.exception.UnsupportedChangeException;
import liquibase.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;
import java.sql.DatabaseMetaData;

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

    private Integer updateRule;
    private Integer deleteRule;

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

//    public Boolean getDeleteCascade() {
//        return deleteCascade;
//    }

    public void setDeleteCascade(Boolean deleteCascade) {
        if (deleteCascade != null && deleteCascade) {
            setOnDelete("CASCADE");
        }
    }

    public void setUpdateRule(Integer rule) {
        this.updateRule = rule;
    }

    public Integer getUpdateRule() {
        return this.updateRule;
    }

    public void setDeleteRule(Integer rule) {
        this.deleteRule = rule;
    }

    public Integer getDeleteRule() {
        return this.deleteRule;
    }

    public void setOnDelete(String onDelete) {
        if (onDelete != null && onDelete.equalsIgnoreCase("CASCADE")) {
            setDeleteRule(DatabaseMetaData.importedKeyCascade);
        } else if (onDelete != null && onDelete.equalsIgnoreCase("SET NULL")) {
            setDeleteRule(DatabaseMetaData.importedKeySetNull);
        } else if (onDelete != null && onDelete.equalsIgnoreCase("SET DEFAULT")) {
            setDeleteRule(DatabaseMetaData.importedKeySetDefault);
        } else if (onDelete != null && onDelete.equalsIgnoreCase("RESTRICT")) {
            setDeleteRule(DatabaseMetaData.importedKeyRestrict);
        } else if (onDelete == null || onDelete.equalsIgnoreCase("NO ACTION")){
            setDeleteRule(DatabaseMetaData.importedKeyNoAction);
        } else {
            throw new RuntimeException("Unknown onDelete action: "+onDelete);
        }
    }

    public void setOnUpdate(String onUpdate) {
        if (onUpdate != null && onUpdate.equalsIgnoreCase("CASCADE")) {
            setUpdateRule(DatabaseMetaData.importedKeyCascade);
        } else  if (onUpdate != null && onUpdate.equalsIgnoreCase("SET NULL")) {
            setUpdateRule(DatabaseMetaData.importedKeySetNull);
        } else if (onUpdate != null && onUpdate.equalsIgnoreCase("SET DEFAULT")) {
            setUpdateRule(DatabaseMetaData.importedKeySetDefault);
        } else if (onUpdate != null && onUpdate.equalsIgnoreCase("RESTRICT")) {
            setUpdateRule(DatabaseMetaData.importedKeyRestrict);
        } else if (onUpdate == null || onUpdate.equalsIgnoreCase("NO ACTION")) {
            setUpdateRule(DatabaseMetaData.importedKeyNoAction);
        } else {
            throw new RuntimeException("Unknown onUpdate action: "+onUpdate);
        }
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {

        if (database instanceof SQLiteDatabase) {
            // return special statements for SQLite databases
            return generateStatementsForSQLiteDatabase(database);
        }

        boolean deferrable = false;
        if (getDeferrable() != null) {
            deferrable = getDeferrable();
        }

        boolean initiallyDeferred = false;
        if (getInitiallyDeferred() != null) {
            initiallyDeferred = getInitiallyDeferred();
        }

        return new SqlStatement[]{
                new AddForeignKeyConstraintStatement(getConstraintName(),
                        getBaseTableSchemaName() == null ? database.getDefaultSchemaName() : getBaseTableSchemaName(),
                        getBaseTableName(),
                        getBaseColumnNames(),
                        getReferencedTableSchemaName() == null ? database.getDefaultSchemaName() : getReferencedTableSchemaName(),
                        getReferencedTableName(),
                        getReferencedColumnNames())
                        .setDeferrable(deferrable)
                        .setInitiallyDeferred(initiallyDeferred)
                        .setUpdateRule(updateRule)
                        .setDeleteRule(deleteRule)
        };
    }

    private SqlStatement[] generateStatementsForSQLiteDatabase(Database database)
            throws UnsupportedChangeException {
        // SQLite does not support foreign keys until now.
        // See for more information: http://www.sqlite.org/omitted.html
        // Therefore this is an empty operation...
        return new SqlStatement[]{};
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

//        if (getDeleteCascade() != null) {
//            node.setAttribute("deleteCascade", getDeleteCascade().toString());
//        }

        if (getUpdateRule() != null) {
            switch (getUpdateRule()) {
                case DatabaseMetaData.importedKeyCascade:
                    node.setAttribute("onUpdate", "CASCADE");
                    break;
                case DatabaseMetaData.importedKeySetNull:
                    node.setAttribute("onUpdate", "SET NULL");
                    break;
                case DatabaseMetaData.importedKeySetDefault:
                    node.setAttribute("onUpdate", "SET DEFAULT");
                    break;
                case DatabaseMetaData.importedKeyRestrict:
                    node.setAttribute("onUpdate", "RESTRICT");
                    break;
                default:
                    //don't set anything
//                    node.setAttribute("onUpdate", "NO ACTION");
                    break;
            }
        }
        if (getDeleteRule() != null) {
            switch (getDeleteRule()) {
                case DatabaseMetaData.importedKeyCascade:
                    node.setAttribute("onDelete", "CASCADE");
                    break;
                case DatabaseMetaData.importedKeySetNull:
                    node.setAttribute("onDelete", "SET NULL");
                    break;
                case DatabaseMetaData.importedKeySetDefault:
                    node.setAttribute("onDelete", "SET DEFAULT");
                    break;
                case DatabaseMetaData.importedKeyRestrict:
                    node.setAttribute("onDelete", "RESTRICT");
                    break;
                default:
                    //don't set anything
//                    node.setAttribute("onDelete", "NO ACTION");
                    break;
            }
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
        fk.setForeignKeyColumns(baseColumnNames);
        fk.setPrimaryKeyTable(referencedTable);
        fk.setPrimaryKeyColumns(referencedColumnNames);
        fk.setDeleteRule(this.deleteRule);
        fk.setUpdateRule(this.updateRule);
        returnSet.add(fk);

        return returnSet;

    }

}
