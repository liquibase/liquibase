package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;

public class Data extends AbstractDatabaseObject {

    @Override
    public boolean snapshotByDefault() {
        return false;
    }

    public Table getTable() {
        return getAttribute("table", Table.class);
    }

    public Data setTable(Table table) {
        setAttribute("table", table);

        return this;
    }


    @Override
    public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[] {
                getTable()
        };
    }

    @Override
    public String getName() {
        Table table = getTable();
        if (table == null) {
            return null;
        }
        return table.getName();
    }

    @Override
    public Data setName(String name) {
        Table table = getTable();
        if (table == null) {
            setTable(new Table().setName(name));
        } else {
            table.setName(name);
        }

        return this;
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
