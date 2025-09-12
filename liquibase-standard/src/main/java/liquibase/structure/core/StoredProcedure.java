package liquibase.structure.core;

import liquibase.structure.DatabaseObject;
import org.apache.commons.lang3.StringUtils;

public class StoredProcedure extends StoredDatabaseLogic<StoredProcedure> {

    public StoredProcedure() {
    }

    public StoredProcedure(String catalogName, String schemaName, String procedureName) {
        this.setSchema(new Schema(catalogName, schemaName));
        setName(procedureName);
    }

    public String getArguments() {
        return getAttribute("arg", String.class);
    }

    public StoredProcedure setArguments(String arg) {
        setAttribute("arg", arg);
        return this;
    }

    public String getProcedureName() {
        return getAttribute("procedureName", String.class);
    }

    public StoredProcedure setProcedureName(String procedureName) {
        setAttribute("procedureName", procedureName);
        return this;
    }

    /**
     * For databases (like Snowflake) that support procedure overloading {@link #getName()} will contain id (name with input arguments)
     * so actual name should be fetched from {@link #getProcedureName()}
     */
    public static String getProcedureName(DatabaseObject dbObject) {
        if (dbObject instanceof StoredProcedure procedure) {
            return StringUtils.isNotEmpty(procedure.getProcedureName()) ? procedure.getProcedureName() : procedure.getName();
        }
        throw new IllegalArgumentException("Expected Procedure, got: " + (dbObject == null ? "null" : dbObject.getClass().getName()));
    }

}
