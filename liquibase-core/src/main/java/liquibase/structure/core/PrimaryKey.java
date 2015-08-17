package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;
import liquibase.util.CollectionUtil;
import liquibase.util.StringUtils;
import liquibase.util.Validate;

import java.util.ArrayList;
import java.util.List;

public class PrimaryKey extends AbstractDatabaseObject {

    public List<PrimaryKeyColumnName> columns = new ArrayList<>();
    public String tablespace;
    public ObjectName backingIndex;
    public Boolean clustered;

    public PrimaryKey() {
    }

    public PrimaryKey(ObjectName pkName, String... columns) {
        super(pkName);
        ObjectName tableName = pkName.container;

        for (String columnName : columns) {
            this.columns.add(new PrimaryKeyColumnName(tableName, columnName));
        }
    }

    @Override
    public Schema getSchema() {
        return null;
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    public ObjectName getTableName() {
        if (name == null) {
            return null;
        }
        return name.container;
    }

    @Override
    public int compareTo(Object other) {
        PrimaryKey that = (PrimaryKey) other;

        if (that == null) {
            return -1;
        }

        ObjectName thisTableName = getTableName();
        ObjectName thatTableName = that.getTableName();


        if (thisTableName != null && thatTableName != null) {
            return thisTableName.compareTo(thatTableName);
        } else {
            if (this.getSimpleName() == null) {
                if (that.getSimpleName() == null) {
                    return 0;
                } else {
                    return 1;
                }
            } else {
                return this.getSimpleName().compareTo(that.getSimpleName());
            }
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrimaryKey that = (PrimaryKey) o;

        ObjectName thisTableName = getTableName();
        ObjectName thatTableName = that.getTableName();

        if (thisTableName != null && thatTableName != null) {
            return thisTableName.equals(thatTableName);
        } else {
            if (this.getSimpleName() == null) {
                return that.getSimpleName() == null;
            } else {
                return this.getSimpleName().equals(that.getSimpleName());
            }
        }
    }

    @Override
    public int hashCode() {
        int result;
        if (name == null) {
            return 0;
        }

        ObjectName tableName = getTableName();
        if (tableName == null) {
            return 0;
        } else {
            return tableName.hashCode();
        }
    }

    @Override
    public String toString() {
        return getName() + "(" + StringUtils.join(this.columns, ",", new StringUtils.ToStringFormatter()) + ")";
    }

    public boolean containsColumn(Column column) {
        for (PrimaryKeyColumnName name : CollectionUtil.createIfNull(columns)) {
            if (name.equals(column.name)) {
                return true;
            }
        }
        return false;
    }

    public static class PrimaryKeyColumnName extends ObjectName {

        public Boolean descending;
        public Integer position;

        public PrimaryKeyColumnName(ObjectName container, String name) {
            super(container, name);
        }

        public PrimaryKeyColumnName(String... names) {
            super(names);
        }
    }
}