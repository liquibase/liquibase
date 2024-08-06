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
import lombok.Setter;

public class ExampleCustomSqlChange implements CustomSqlChange, CustomSqlRollback {

    @Setter
    @Getter
    private String schemaName;
    @Setter
    @Getter
    private String tableName;
    @Setter
    @Getter
    private String columnName;
    @Setter
    @Getter
    private String newValue;

    @SuppressWarnings("unused")
    private ResourceAccessor resourceAccessor;


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
