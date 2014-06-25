package liquibase.change.custom;

import liquibase.database.Database;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.Statement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

public class ExampleCustomSqlChange implements CustomSqlChange, CustomSqlRollback {

    private String tableName;
    private String columnName;
    private String newValue;

    @SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
    private ResourceAccessor resourceAccessor;


    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    @Override
    public Statement[] generateStatements(Database database) {
        return new Statement[]{
                new RawSqlStatement("update "+database.escapeObjectName(tableName, Table.class)
                        +" set "+database.escapeObjectName(columnName, Column.class)+" = "+newValue)
        };
    }

    @Override
    public Statement[] generateRollbackStatements(Database database) throws RollbackImpossibleException {
        return new Statement[]{
                new RawSqlStatement("update "+database.correctObjectName(tableName, Table.class)
                        +" set "+database.escapeObjectName(columnName, Column.class)+" = null")
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Custom class updated "+tableName+"."+columnName;
    }

    @Override
    public void setUp() throws SetupException {
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
    
}
