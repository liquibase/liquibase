package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;

public class Data extends AbstractDatabaseObject {

    @Override
    public boolean snapshotByDefault() {
        return false;
    }

    public Table getTable() {
        return get("table", Table.class);
    }

    public Data setTable(Table table) {
        set("table", table);

        return this;
    }


    @Override
    public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[] {
                getTable()
        };
    }

    @Override
    public ObjectName getName() {
        Table table = getTable();
        if (table == null) {
            return null;
        }
        return table.getName();
    }

    @Override
    public Schema getSchema() {
        Table table = getTable();
        if (table == null) {
            return null;
        }
        return table.getSchema();
    }
}
