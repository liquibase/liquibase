package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectReference;

import java.util.*;

public class Index extends AbstractDatabaseObject {

    public List<IndexedColumn> columns = new ArrayList<>();
    public String tablespace;
    public Boolean unique;
    public Boolean clustered;

    public Index() {
    }

    public Index(String name) {
        super(name);
    }

    public Index(ObjectReference nameAndContainer) {
        super(nameAndContainer);
    }

    public Index(ObjectReference container, String name) {
        super(container, name);
    }

    public Index(String indexName, String... columns) {
        super(indexName);
        if (columns != null && columns.length > 0) {
            for (String column : columns) {
                this.columns.add(new IndexedColumn(column));
            }
        }
    }


//    @Override
//    public int compareTo(Object other) {
//        Index o = (Index) other;
//        int returnValue = this.columns.get(0).name.container.compareTo(o.columns.get(0).name.container);
//
//        if (returnValue == 0) {
//            ObjectReference thisName = ObjectUtil.defaultIfEmpty(this.getName(), new ObjectReference());
//            ObjectReference oName = ObjectUtil.defaultIfEmpty(o.getName(), new ObjectReference());
//            returnValue = thisName.compareTo(oName);
//        }
//
//        //We should not have two indexes that have the same name and tablename
//        /*if (returnValue == 0) {
//        	returnValue = this.getColumnName().compareTo(o.getColumnName());
//        }*/
//
//
//        return returnValue;
//    }

//    @Override
//    public int hashCode() {
//        return toString().hashCode();
//    }

//    @Override
//    public boolean equals(Object obj) {
//        if (obj == null) {
//            return false;
//        }
//
//        if (!(obj instanceof Index)) {
//            return false;
//        }
//
//        return this.compareTo(obj) == 0;
//    }

    public static class IndexReference extends ObjectReference {
        public ObjectReference table;
    }

    public static class IndexedColumn extends AbstractDatabaseObject {
        public Boolean computed;
        public Boolean descending;

        public IndexedColumn() {
        }

        public IndexedColumn(String name) {
            super(name);
        }

        public IndexedColumn(ObjectReference nameAndContainer) {
            super(nameAndContainer);
        }

        public IndexedColumn(ObjectReference container, String name) {
            super(container, name);
        }

        public String toString(boolean includeRelation) {
            if (includeRelation) {
                return toString();
            } else {
                return name  + (descending != null && descending ? " DESC" : "");
            }
        }

        @Override
        public String toString() {
            return name + (descending != null && descending ? " DESC" : "");
        }

    }
}