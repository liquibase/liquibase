package liquibase.change.custom;

import liquibase.FileOpener;
import liquibase.exception.UnsupportedChangeException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.database.sql.SqlStatement;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.StoredProcedureStatement;
import liquibase.database.Database;
import liquibase.database.structure.DatabaseObject;

import java.util.Set;

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

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        StoredProcedureStatement procedureStatement = new StoredProcedureStatement();
        procedureStatement.setProcedureName("testHello");
        return new SqlStatement[]{
                procedureStatement,
        };
    }

    public String getConfirmationMessage() {
        return "Executed " + getProcedureName();
    }

    public void setUp() throws SetupException {
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        return null;
    }


    public void setFileOpener(FileOpener fileOpener) {
        this.fileOpener = fileOpener;
    }
}
