package liquibase.change.custom;

import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.FileOpener;
import liquibase.statement.SqlStatement;
import liquibase.statement.StoredProcedureStatement;

public class ExampleCustomProcCallChange implements CustomSqlChange {

    private String procedureName;

    @SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
    private FileOpener fileOpener;


    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    public SqlStatement[] generateStatements(Database database) throws CustomChangeException {
        StoredProcedureStatement procedureStatement = new StoredProcedureStatement("testHello");
        return new SqlStatement[]{
                procedureStatement,
        };
    }

    public String getConfirmationMessage() {
        return "Executed " + getProcedureName();
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
