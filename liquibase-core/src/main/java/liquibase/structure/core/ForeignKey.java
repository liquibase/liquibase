package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ForeignKey extends AbstractDatabaseObject{

    public ForeignKey() {
        setForeignKeyColumns(new ArrayList<Column>());
        setPrimaryKeyColumns(new ArrayList<Column>());
    }

    public ForeignKey(String name) {
        this();
        setName(name);
    }

    public ForeignKey(String name, String foreignKeyCatalog, String foreignKeySchema, String foreignKeyTable, Column... baseTableColumns) {
        this(name);

        if (foreignKeyTable != null) {
            setForeignKeyTable(new Table(foreignKeyCatalog, foreignKeySchema, foreignKeyTable));
        }
        if ((baseTableColumns != null) && (baseTableColumns.length > 0) && (baseTableColumns[0] != null)) {
            setForeignKeyColumns(new ArrayList<>(Arrays.asList(baseTableColumns)));
        }

    }

    @Override
    public DatabaseObject[] getContainingObjects() {

        List<Column> objects = new ArrayList<>();
        if (getPrimaryKeyColumns() != null) {
            for (Column column : getPrimaryKeyColumns()) {
                objects.add(column);
            }
        }

        if (getForeignKeyColumns() != null) {
            for (Column column : getForeignKeyColumns()) {
                objects.add(column);
            }
        }

        return objects.toArray(new DatabaseObject[objects.size()]);
    }

    @Override
    public Schema getSchema() {
        if (getForeignKeyTable() == null) {
            return null;
        }

        return getForeignKeyTable().getSchema();
    }


    public Table getPrimaryKeyTable() {
        return getAttribute("primaryKeyTable", Table.class);
    }

    public ForeignKey setPrimaryKeyTable(Table primaryKeyTable) {
        this.setAttribute("primaryKeyTable",primaryKeyTable);
        return this;
    }

    public List<Column> getPrimaryKeyColumns() {
        return getAttribute("primaryKeyColumns", List.class);
    }

    public ForeignKey setPrimaryKeyColumns(List<Column> primaryKeyColumns) {
        this.setAttribute("primaryKeyColumns", primaryKeyColumns);
        for (Column column : getPrimaryKeyColumns()) {
            if (column.getAttribute("relation", Object.class) == null) {
                column.setRelation(getPrimaryKeyTable());
            }
        }
        return this;
    }

    public ForeignKey addPrimaryKeyColumn(Column primaryKeyColumn) {
        this.getAttribute("primaryKeyColumns", List.class).add(primaryKeyColumn);
        if (primaryKeyColumn.getAttribute("relation", Object.class) == null) {
            primaryKeyColumn.setRelation(getPrimaryKeyTable());
        }

        return this;
    }

    public Table getForeignKeyTable() {
        return getAttribute("foreignKeyTable", Table.class);
    }

    public ForeignKey setForeignKeyTable(Table foreignKeyTable) {
        this.setAttribute("foreignKeyTable", foreignKeyTable);
        return this;
    }

    public List<Column> getForeignKeyColumns() {
        return getAttribute("foreignKeyColumns", List.class);
    }

    public ForeignKey setForeignKeyColumns(List<Column> foreignKeyColumns) {
        this.setAttribute("foreignKeyColumns", foreignKeyColumns);

        for (Column column : getForeignKeyColumns()) {
            if (column.getAttribute("relation", Object.class) == null) {
                column.setRelation(getForeignKeyTable());
            }
        }

        return this;
    }

    public ForeignKey addForeignKeyColumn(Column foreignKeyColumn) {
        if (foreignKeyColumn.getAttribute("relation", Object.class) == null) {
            foreignKeyColumn.setRelation(getForeignKeyTable());
        }
        getAttribute("foreignKeyColumns", List.class).add(foreignKeyColumn);

        return this;
    }

    @Override
    public String getName() {
        return getAttribute("name", String.class);
    }

    @Override
    public ForeignKey setName(String name) {
        this.setAttribute("name", name);
        return this;
    }


    @Override
    public String toString() {
        StringUtils.StringUtilsFormatter<Column> columnFormatter = new StringUtils.StringUtilsFormatter<Column>() {
            @Override
            public String toString(Column obj) {
                return obj.getName();
            }
        };
        return getName() + "(" + getForeignKeyTable() + "[" + StringUtils.join(getForeignKeyColumns(), ", ", columnFormatter) + "] -> " + getPrimaryKeyTable() + "[" + StringUtils.join(getPrimaryKeyColumns(), ", ", columnFormatter) + "])";
    }

    public boolean isDeferrable() {

        return getAttribute("deferrable", false);
    }

    public ForeignKey setDeferrable(boolean deferrable) {
        this.setAttribute("deferrable", deferrable);
        return this;
    }


    public boolean isInitiallyDeferred() {
        return getAttribute("initiallyDeferred", false);
    }

    public ForeignKey setInitiallyDeferred(boolean initiallyDeferred) {
        this.setAttribute("initiallyDeferred", initiallyDeferred);
        return this;
    }

    /**
     * In Oracle PL/SQL, the VALIDATE keyword defines whether a foreign key constraint on a column in a table
     * should be checked if it refers to a valid row or not.
     * @return true if ENABLE VALIDATE (this is the default), or false if ENABLE NOVALIDATE.
     */
    public boolean shouldValidate() {
        return getAttribute("validate", true);
    }

    /**
     * @param shouldValidate - if shouldValidate is set to FALSE then the constraint will be created
     * with the 'ENABLE NOVALIDATE' mode. This means the constraint would be created, but that no
     * check will be done to ensure old data has valid foreign keys - only new data would be checked
     * to see if it complies with the constraint logic. The default state for foreign keys is to
     * have 'ENABLE VALIDATE' set.
     */
    public ForeignKey setShouldValidate(boolean shouldValidate) {
        this.setAttribute("validate", shouldValidate);
        return this;
    }

    public ForeignKeyConstraintType getUpdateRule() {
        return getAttribute("updateRule", ForeignKeyConstraintType.class);
    }

    public ForeignKey setUpdateRule(ForeignKeyConstraintType rule) {
        this.setAttribute("updateRule", rule);
        return this;
    }

    public ForeignKeyConstraintType getDeleteRule() {
        return getAttribute("deleteRule", ForeignKeyConstraintType.class);
    }

    public ForeignKey setDeleteRule(ForeignKeyConstraintType rule) {
        this.setAttribute("deleteRule", rule);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;

        ForeignKey that = (ForeignKey) o;

        if (this.getSchema() != null && that.getSchema() != null) {
            boolean schemasEqual = StringUtils.trimToEmpty(this.getSchema().getName()).equalsIgnoreCase(StringUtils.trimToEmpty(that.getSchema().getName()));
            if (!schemasEqual) {
                return false;
            }
        }


        if (getForeignKeyColumns() == null) {
            return this.getName().equalsIgnoreCase(that.getName());
        }

        StringUtils.StringUtilsFormatter formatter = new StringUtils.StringUtilsFormatter<Column>() {
            @Override
            public String toString(Column obj) {
                return obj.toString(false);
            }
        };

        return (StringUtils.join(getForeignKeyColumns(), ",", formatter).equalsIgnoreCase(StringUtils.join(that
            .getForeignKeyColumns(), ",", formatter)) && ((getForeignKeyTable() != null) && (that.getForeignKeyTable
            () != null) && getForeignKeyTable().equals(that.getForeignKeyTable())) && (StringUtils.join
            (getPrimaryKeyColumns(), ",", formatter).equalsIgnoreCase(StringUtils.join(that.getPrimaryKeyColumns(),
            ",", formatter))) && ((getPrimaryKeyTable() != null) && (that.getPrimaryKeyTable() != null) &&
            getPrimaryKeyTable().equals(that.getPrimaryKeyTable())));
    }

    @Override
    public int hashCode() {
        StringUtils.StringUtilsFormatter formatter = new StringUtils.StringUtilsFormatter<Column>() {
            @Override
            public String toString(Column obj) {
                return obj.toString(false);
            }
        };

        int result = 0;
        if (getPrimaryKeyTable() != null) {
            result = getPrimaryKeyTable().hashCode();
        }
        if (getPrimaryKeyColumns() != null) {
            result = (31 * result) + StringUtils.join(getPrimaryKeyColumns(), ",", formatter).toUpperCase().hashCode();
        }

        if (getForeignKeyTable() != null) {
            result = (31 * result) + getForeignKeyTable().hashCode();
        }

        if (getForeignKeyColumns() != null) {
            result = (31 * result) + StringUtils.join(getForeignKeyColumns(), ",", formatter).toUpperCase().hashCode();
        }

        return result;
    }


    @Override
    public int compareTo(Object other) {
        StringUtils.StringUtilsFormatter formatter = new StringUtils.StringUtilsFormatter<Column>() {
            @Override
            public String toString(Column obj) {
                return obj.toString(false);
            }
        };

        ForeignKey o = (ForeignKey) other;
        int returnValue = 0;
        if ((this.getForeignKeyTable() != null) && (o.getForeignKeyTable() != null)) {
            returnValue = this.getForeignKeyTable().compareTo(o.getForeignKeyTable());
        }
        if ((returnValue == 0) && (this.getForeignKeyColumns() != null) && (o.getForeignKeyColumns() != null)) {
            returnValue = StringUtils.join(this.getForeignKeyColumns(), ",", formatter).compareToIgnoreCase(StringUtils.join(o.getForeignKeyColumns(), ",", formatter));
        }
        if ((returnValue == 0) && (this.getName() != null) && (o.getName() != null)) {
            returnValue = this.getName().compareToIgnoreCase(o.getName());
        }
        if ((returnValue == 0) && (this.getPrimaryKeyTable() != null) && (o.getPrimaryKeyTable() != null)) {
            returnValue = this.getPrimaryKeyTable().compareTo(o.getPrimaryKeyTable());
        }

        if ((returnValue == 0) && (this.getPrimaryKeyColumns() != null) && (o.getPrimaryKeyColumns() != null)) {
            returnValue = StringUtils.join(this.getPrimaryKeyColumns(), ",", formatter).compareToIgnoreCase(StringUtils.join(o.getPrimaryKeyColumns(), ",", formatter));
        }
        if ((returnValue == 0) && (this.getUpdateRule() != null) && (o.getUpdateRule() != null))
            returnValue = this.getUpdateRule().compareTo(o.getUpdateRule());
        if ((returnValue == 0) && (this.getDeleteRule() != null) && (o.getDeleteRule() != null))
            returnValue = this.getDeleteRule().compareTo(o.getDeleteRule());
        return returnValue;
    }

    private String toDisplayString(List<String> columnsNames) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String columnName : columnsNames) {
            i++;
            sb.append(columnName);
            if (i < columnsNames.size()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public Index getBackingIndex() {
        return getAttribute("backingIndex", Index.class);
    }

    public ForeignKey setBackingIndex(Index backingIndex) {
        this.setAttribute("backingIndex", backingIndex);
        return this;
    }
}
