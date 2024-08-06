package liquibase.change.custom;

import liquibase.database.Database;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import lombok.Getter;

public class ExampleCustomSqlChange implements CustomSqlChange, CustomSqlRollback {

    @Getter
    private String schemaName;
    @Getter
    private String tableName;
    @Getter
    private String columnName;
    @Getter
    private String newValue;

    @SuppressWarnings("unused")
    private ResourceAccessor resourceAccessor;


    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
                new RawParameterizedSqlStatement(String.format("UPDATE %s SET %s = %s", database.escapeObjectName(null, schemaName, tableName, Table.class),
                        database.escapeObjectName(columnName, Column.class), newValue))
        };
    }

    @Override
    public SqlStatement[] generateRollbackStatements(Database database) throws RollbackImpossibleException {
        return new SqlStatement[]{
                new RawParameterizedSqlStatement(String.format("UPDATE %s SET %s = NULL", database.escapeObjectName(null, schemaName, tableName, Table.class),
                        database.escapeObjectName(columnName, Column.class)))
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
