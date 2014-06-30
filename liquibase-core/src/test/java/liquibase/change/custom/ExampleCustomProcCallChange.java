package liquibase.change.custom;

import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.Statement;
import liquibase.statement.core.StoredProcedureStatement;

public class ExampleCustomProcCallChange implements CustomSqlChange {

    private String procedureName;

    @SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
    private ResourceAccessor resourceAccessor;


    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    @Override
    public Statement[] generateStatements(Database database) throws CustomChangeException {
        StoredProcedureStatement procedureStatement = new StoredProcedureStatement("testHello");
        return new Statement[]{
                procedureStatement,
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Executed " + getProcedureName();
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
