package liquibase.change.custom;

import liquibase.database.Database;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.FileOpener;
import liquibase.statement.RawSqlStatement;
import liquibase.statement.SqlStatement;

public class ExampleCustomSqlChange implements CustomSqlChange, CustomSqlRollback {

    private String tableName;
    private String columnName;
    private String newValue;

    @SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
    private FileOpener fileOpener;


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
                new RawSqlStatement("update "+tableName+" set "+columnName+" = "+newValue)
        };
    }

    public SqlStatement[] generateRollbackStatements(Database database) throws UnsupportedChangeException, RollbackImpossibleException {
        return new SqlStatement[]{
                new RawSqlStatement("update "+tableName+" set "+columnName+" = null")
        };
    }

    public String getConfirmationMessage() {
        return "Custom class updated "+tableName+"."+columnName;
    }

    public void setUp() throws SetupException {
    }

    public void setFileOpener(FileOpener fileOpener) {
        this.fileOpener = fileOpener;
    }

    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
    
}
