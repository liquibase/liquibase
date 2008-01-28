package liquibase.database.structure;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class UniqueConstraint {
    private String name;
    private Table table;
    private Set<Column> columns = new HashSet<Column>();

    public UniqueConstraint(String name, Table table, Column... columns) {
        this.name = name;
        this.table = table;
        this.columns.addAll(Arrays.asList(columns));
    }

    public String getName() {
        return name;
    }

    public Table getTable() {
        return table;
    }

    public Set<Column> getColumns() {
        return columns;
    }
}
