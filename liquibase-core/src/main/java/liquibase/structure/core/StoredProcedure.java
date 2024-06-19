package liquibase.structure.core;

public class StoredProcedure extends StoredDatabaseLogic<StoredProcedure> {
    private static final String PROCEDURE_NAME_KEY = "procedureName";
    private static final String ARGS_KEY = "arg";
    private static final String DROP_NAME_KEY = "dropName";

    public StoredProcedure() {
    }

    public StoredProcedure(String catalogName, String schemaName, String procedureName) {
        this.setSchema(new Schema(catalogName, schemaName));
        setName(procedureName);
    }

    public String getArguments() {
        return getAttribute(ARGS_KEY, String.class);
    }

    public StoredProcedure setArguments(String arg) {
        setAttribute(ARGS_KEY, arg);
        return  this;
    }

    public String getProcedureName() {
        return getAttribute(PROCEDURE_NAME_KEY, String.class);
    }

    public StoredProcedure setProcedureName(String functionName) {
        setAttribute(PROCEDURE_NAME_KEY, functionName);
        return  this;
    }

    public String getDropName() {
        return getAttribute(DROP_NAME_KEY, String.class);
    }

    public StoredProcedure setDropName(String dropName) {
        setAttribute(DROP_NAME_KEY, dropName);
        return  this;
    }

}
