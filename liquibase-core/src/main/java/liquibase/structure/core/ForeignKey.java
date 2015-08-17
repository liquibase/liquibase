package liquibase.structure.core;

import liquibase.AbstractExtensibleObject;
import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;
import liquibase.util.CollectionUtil;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ForeignKey extends AbstractDatabaseObject {

    public List<ForeignKeyColumnCheck> columnChecks = new ArrayList<>();
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
        for (int i=0; i<CollectionUtil.createIfNull(foreignKeyColumns).size(); i++) {
            this.columnChecks.add(new ForeignKeyColumnCheck(foreignKeyColumns.get(i), primaryKeyColumns.get(i), i));
        }
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
        return getName() + "(" + StringUtils.join(columnChecks, ", ", new ForeignKeyColumnCheckStringUtilsFormatter())+")";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ForeignKey that = (ForeignKey) o;

        String thisString = StringUtils.join(this.columnChecks, ", ", new ForeignKeyColumnCheckStringUtilsFormatter());
        String thatString = StringUtils.join(that.columnChecks, ", ", new ForeignKeyColumnCheckStringUtilsFormatter());

        return thisString.equals(thatString);

    }

    @Override
    public int hashCode() {
        String string = StringUtils.join(this.columnChecks, ", ", new ForeignKeyColumnCheckStringUtilsFormatter());
        return string.hashCode();
    }


    @Override
    public int compareTo(Object other) {
        ForeignKey that = (ForeignKey) other;

        String thisString = StringUtils.join(this.columnChecks, ", ", new ForeignKeyColumnCheckStringUtilsFormatter());
        String thatString = StringUtils.join(that.columnChecks, ", ", new ForeignKeyColumnCheckStringUtilsFormatter());

        return thisString.compareTo(thatString);
    }

    public static class ForeignKeyColumnCheck extends AbstractExtensibleObject {
        public ObjectName baseColumn;
        public ObjectName referencedColumn;

        public Integer position;

        public ForeignKeyColumnCheck() {
        }

        public ForeignKeyColumnCheck(ObjectName baseColumn, ObjectName referencedColumn) {
            this(baseColumn, referencedColumn, null);
        }

        public ForeignKeyColumnCheck(ObjectName baseColumn, ObjectName referencedColumn, Integer position) {
            this.baseColumn = baseColumn;
            this.referencedColumn = referencedColumn;
            this.position = position;
        }
    }

    private static class ForeignKeyColumnCheckStringUtilsFormatter implements StringUtils.StringUtilsFormatter<ForeignKeyColumnCheck> {

        @Override
        public String toString(ForeignKeyColumnCheck obj) {
            return obj.baseColumn + "->" +obj.referencedColumn;
        }
    }
}
