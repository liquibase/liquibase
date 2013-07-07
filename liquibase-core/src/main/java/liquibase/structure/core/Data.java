package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;

public class Data extends AbstractDatabaseObject {

    public Table getTable() {
        return getAttribute("table", Table.class);
    }

    public Data setTable(Table table) {
        setAttribute("table", table);

        return this;
    }


    public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[] {
                getTable()
        };
    }

    public String getName() {
        Table table = getTable();
        if (table == null) {
            return null;
        }
        return table.getName();
    }

    public Data setName(String name) {
        Table table = getTable();
        if (table == null) {
            setTable(new Table().setName(name));
        } else {
            table.setName(name);
        }

        return this;
    }

    public Schema getSchema() {
        Table table = getTable();
        if (table == null) {
            return null;
        }
        return table.getSchema();
    }
}
