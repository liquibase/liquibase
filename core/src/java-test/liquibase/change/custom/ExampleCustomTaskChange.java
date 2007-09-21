package liquibase.change.custom;

import liquibase.FileOpener;
import liquibase.database.Database;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.CustomChangeException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import java.util.Set;

public class ExampleCustomTaskChange implements CustomTaskChange, CustomTaskRollback {

    private String helloTo;

    @SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
    private FileOpener fileOpener;


    public String getHelloTo() {
        return helloTo;
    }

    public void setHelloTo(String helloTo) {
        this.helloTo = helloTo;
    }

    public void execute(Database database) throws CustomChangeException, UnsupportedChangeException {
        System.out.println("Hello "+getHelloTo());
    }

    public void rollback(Database database) throws CustomChangeException, UnsupportedChangeException, RollbackImpossibleException {
        System.out.println("Goodbye "+getHelloTo());
    }

    public String getConfirmationMessage() {
        return "Said Hello";
    }

    public void setUp() throws SetupException {
        ;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        return null;
    }

    public void setFileOpener(FileOpener fileOpener) {
        this.fileOpener = fileOpener;
    }
}
