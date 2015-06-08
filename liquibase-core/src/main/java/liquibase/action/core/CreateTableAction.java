package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.datatype.LiquibaseDataType;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.statement.PrimaryKeyConstraint;
import liquibase.statement.UniqueConstraint;
import liquibase.structure.ObjectName;
import liquibase.util.CollectionUtil;

import java.math.BigInteger;
import java.util.*;

public class CreateTableAction extends AbstractAction {

    public ObjectName tableName;
    public String tablespace;
    public String remarks;
    public List<ColumnDefinition> columnDefinitions;
    public String autoIncrementColumnName;
    public BigInteger autoIncrementStartWith;
    public BigInteger autoIncrementBy;
    public String primaryKeyName;
    public List<ForeignKeyDefinition> foreignKeyDefinitions;
    public String primaryKeyTablespace;
    public List<UniqueConstraintDefinition> uniqueConstraintDefinitions;


    public CreateTableAction() {
    }


    public CreateTableAction(ObjectName tableName) {
        this.tableName = tableName;
    }

    public CreateTableAction addColumn(ObjectName columnName, String type) {
        return addColumn(new ColumnDefinition(columnName, type));
    }

    public CreateTableAction addColumn(String columnName, String type) {
        return addColumn(new ColumnDefinition(columnName, type));
    }

    public CreateTableAction addColumn(ColumnDefinition columnDefinition) {
        if (!CollectionUtil.hasValue(columnDefinitions)) {
            this.columnDefinitions = new ArrayList<>();
        }
        columnDefinitions.add(columnDefinition);

        return this;
    }
}
