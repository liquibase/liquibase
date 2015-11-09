package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.*;
import liquibase.util.CollectionUtil;

import java.util.*;

public class CreateTableAction extends AbstractAction {

    public Table table;
    public List<Column> columns = new ArrayList<>();
    public PrimaryKey primaryKey;
    public List<ForeignKey> foreignKeys;
    public List<UniqueConstraint> uniqueConstraints;


    public CreateTableAction() {
    }


    public CreateTableAction(Table table) {
        this.table = table;
    }

    public CreateTableAction addColumn(ObjectReference columnName, String type) {
        return addColumn(new Column(columnName, type));
    }

    public CreateTableAction addColumn(String columnName, String type) {
        return addColumn(new Column(new ObjectReference(table.name, columnName), type));
    }

    public CreateTableAction addColumn(Column column) {
        if (!CollectionUtil.hasValue(columns)) {
            this.columns = new ArrayList<>();
        }
        columns.add(column);

        return this;
    }
}
