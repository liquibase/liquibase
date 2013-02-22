package liquibase.change.custom;

import liquibase.database.Database;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;
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

    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
                new RawSqlStatement("update "+database.escapeObjectName(tableName, Table.class)
                        +" set "+database.escapeObjectName(columnName, Column.class)+" = "+newValue)
        };
    }

    public SqlStatement[] generateRollbackStatements(Database database) throws RollbackImpossibleException {
        return new SqlStatement[]{
                new RawSqlStatement("update "+database.correctObjectName(tableName, Table.class)
                        +" set "+database.escapeObjectName(columnName, Column.class)+" = null")
        };
    }

    public String getConfirmationMessage() {
        return "Custom class updated "+tableName+"."+columnName;
    }

    public void setUp() throws SetupException {
    }

    public void setFileOpener(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
    }

    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
    
}
