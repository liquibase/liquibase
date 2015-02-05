package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.datatype.LiquibaseDataType;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.statement.PrimaryKeyConstraint;
import liquibase.statement.UniqueConstraint;
import liquibase.structure.ObjectName;

import java.util.*;

public class CreateTableAction extends AbstractAction {

    public static enum Attr {
        tableName,
        tablespace,
        remarks,
        columnDefinitions,
        autoIncrementColumnName,
        autoIncrementStartWith,
        autoIncrementBy,
        primaryKeyName,
        foreignKeyDefinitions,
        primaryKeyTablespace, uniqueConstraintDefinitions
    }


    public CreateTableAction() {
    }


    public CreateTableAction(ObjectName tableName) {
        set(Attr.tableName, tableName);
    }

    public CreateTableAction addColumn(ColumnDefinition columnDefinition) {
        if (!has(Attr.columnDefinitions)) {
            set(Attr.columnDefinitions, new ArrayList<ColumnDefinition>());
        }
        get(Attr.columnDefinitions, List.class).add(columnDefinition);

        return this;
    }
}
