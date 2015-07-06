package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;
import liquibase.util.CollectionUtil;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ForeignKey extends AbstractDatabaseObject {

    public List<ObjectName> primaryKeyColumns = new ArrayList<>();
    public List<ObjectName> foreignKeyColumns = new ArrayList<>();
    public Boolean deferrable;
    public Boolean initiallyDeferred;
    public ForeignKeyConstraintType updateRule;
    public ForeignKeyConstraintType deleteRule;
    public ObjectName backingIndex;

    public ForeignKey() {
    }

    public ForeignKey(ObjectName name) {
        super(name);
    }

    public ForeignKey(ObjectName name, List<ObjectName> foreignKeyColumns, List<ObjectName> primaryKeyColumns) {
        this(name);
        this.foreignKeyColumns = CollectionUtil.createIfNull(foreignKeyColumns);
        this.primaryKeyColumns = CollectionUtil.createIfNull(primaryKeyColumns);
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    @Override
    public Schema getSchema() {
        return null;
    }


    @Override
    public String toString() {
        return getName() + "(" + StringUtils.join(foreignKeyColumns, ", ", new StringUtils.ObjectNameFormatter(Column.class, null)) + " -> " + StringUtils.join(primaryKeyColumns, ", ", new StringUtils.ObjectNameFormatter(Column.class, null)) + ")";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ForeignKey that = (ForeignKey) o;

        String thisString = StringUtils.join(this.foreignKeyColumns, ", ", new StringUtils.ObjectNameFormatter(Column.class, null)) + " -> " + StringUtils.join(this.primaryKeyColumns, ", ", new StringUtils.ObjectNameFormatter(Column.class, null));
        String thatString = StringUtils.join(that.foreignKeyColumns, ", ", new StringUtils.ObjectNameFormatter(Column.class, null)) + " -> " + StringUtils.join(that.primaryKeyColumns, ", ", new StringUtils.ObjectNameFormatter(Column.class, null));

        return thisString.equals(thatString);

    }

    @Override
    public int hashCode() {
        String string = StringUtils.join(this.foreignKeyColumns, ", ", new StringUtils.ObjectNameFormatter(Column.class, null)) + " -> " + StringUtils.join(this.primaryKeyColumns, ", ", new StringUtils.ObjectNameFormatter(Column.class, null));
        return string.hashCode();
    }


    @Override
    public int compareTo(Object other) {
        ForeignKey that = (ForeignKey) other;

        String thisString = StringUtils.join(this.foreignKeyColumns, ", ", new StringUtils.ObjectNameFormatter(Column.class, null)) + " -> " + StringUtils.join(this.primaryKeyColumns, ", ", new StringUtils.ObjectNameFormatter(Column.class, null));
        String thatString = StringUtils.join(that.foreignKeyColumns, ", ", new StringUtils.ObjectNameFormatter(Column.class, null)) + " -> " + StringUtils.join(that.primaryKeyColumns, ", ", new StringUtils.ObjectNameFormatter(Column.class, null));

        return thisString.compareTo(thatString);
    }
}
