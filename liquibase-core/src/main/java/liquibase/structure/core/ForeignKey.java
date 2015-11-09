package liquibase.structure.core;

import liquibase.AbstractExtensibleObject;
import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.ObjectReference;
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
    public ObjectReference backingIndex;

    public ForeignKey() {
    }

    public ForeignKey(String name) {
        super(name);
    }

    public ForeignKey(ObjectReference nameAndContainer) {
        super(nameAndContainer);
    }

    public ForeignKey(ObjectReference container, String name) {
        super(container, name);
    }

    public ForeignKey(String name, List<ObjectReference> foreignKeyColumns, List<ObjectReference> primaryKeyColumns) {
        this(name);
        for (int i=0; i<CollectionUtil.createIfNull(foreignKeyColumns).size(); i++) {
            this.columnChecks.add(new ForeignKeyColumnCheck(foreignKeyColumns.get(i), primaryKeyColumns.get(i), i));
        }
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
        public ObjectReference baseColumn;
        public ObjectReference referencedColumn;

        public Integer position;

        public ForeignKeyColumnCheck() {
        }

        public ForeignKeyColumnCheck(ObjectReference baseColumn, ObjectReference referencedColumn) {
            this(baseColumn, referencedColumn, null);
        }

        public ForeignKeyColumnCheck(ObjectReference baseColumn, ObjectReference referencedColumn, Integer position) {
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

    public static class ForeignKeyReference extends ObjectReference {
        public ObjectReference table;
    }
}
