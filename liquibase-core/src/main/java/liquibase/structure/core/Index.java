package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtils;

import java.util.*;

public class Index extends AbstractDatabaseObject {

    public List<Index.IndexedColumn> columns = new ArrayList<>();
    public String tablespace;
    public Boolean unique;
    public Boolean clustered;

    public Index() {
    }

    public Index(ObjectName indexName, String... columns) {
        super(indexName);
//        if (columns != null && columns.length > 0) {
//            for (String column : columns) {
//                this.columns.add(new IndexedColumn())
//            }
//        }
    }


    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    @Override
    public Schema getSchema() {
       return null;
    }

    @Override
    public int compareTo(Object other) {
        Index o = (Index) other;
        int returnValue = this.columns.get(0).name.container.compareTo(o.columns.get(0).name.container);

        if (returnValue == 0) {
            ObjectName thisName = ObjectUtil.defaultIfEmpty(this.getName(), new ObjectName());
            ObjectName oName = ObjectUtil.defaultIfEmpty(o.getName(), new ObjectName());
            returnValue = thisName.compareTo(oName);
        }

        //We should not have two indexes that have the same name and tablename
        /*if (returnValue == 0) {
        	returnValue = this.getColumnName().compareTo(o.getColumnName());
        }*/


        return returnValue;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Index)) {
            return false;
        }

        return this.compareTo(obj) == 0;
    }

    public static class IndexedColumn extends AbstractDatabaseObject {
        public Boolean computed;
        public Boolean descending;

        public IndexedColumn() {
        }

        public IndexedColumn(ObjectName name) {
            super(name);
        }

        @Override
        public DatabaseObject[] getContainingObjects() {
            return null;
        }

        @Override
        public Schema getSchema() {
            return null;
        }

        public String toString(boolean includeRelation) {
            if (includeRelation) {
                return toString();
            } else {
                return getName().toShortString()  + (descending != null && descending ? " DESC" : "");
            }
        }

        @Override
        public String toString() {
            return getName().toString() + (descending != null && descending ? " DESC" : "");
        }

    }
}