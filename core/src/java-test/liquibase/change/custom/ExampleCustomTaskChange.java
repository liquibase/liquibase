package liquibase.change.custom;

import liquibase.FileOpener;
import liquibase.database.Database;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.*;

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

    public void setFileOpener(FileOpener fileOpener) {
        this.fileOpener = fileOpener;
    }

    public void validate(Database database) throws InvalidChangeDefinitionException {

    }
}
