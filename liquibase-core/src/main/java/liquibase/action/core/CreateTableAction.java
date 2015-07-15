package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.UniqueConstraint;
import liquibase.util.CollectionUtil;

import java.math.BigInteger;
import java.util.*;

public class CreateTableAction extends AbstractAction {

    public ObjectName tableName;
    public String tablespace;
    public String remarks;
    public List<Column> columns = new ArrayList<>();
    public String autoIncrementColumnName;
    public BigInteger autoIncrementStartWith;
    public BigInteger autoIncrementBy;
    public String primaryKeyName;
    public List<ForeignKey> foreignKeys;
    public String primaryKeyTablespace;
    public List<UniqueConstraint> uniqueConstraintDefinitions;


    public CreateTableAction() {
    }


    public CreateTableAction(ObjectName tableName) {
        this.tableName = tableName;
    }

    public CreateTableAction addColumn(ObjectName columnName, String type) {
        return addColumn(new Column(columnName, type));
    }

    public CreateTableAction addColumn(String columnName, String type) {
        return addColumn(new Column(new ObjectName(tableName, columnName), type));
    }

    public CreateTableAction addColumn(Column column) {
        if (!CollectionUtil.hasValue(columns)) {
            this.columns = new ArrayList<>();
        }
        columns.add(column);

        return this;
    }
}
