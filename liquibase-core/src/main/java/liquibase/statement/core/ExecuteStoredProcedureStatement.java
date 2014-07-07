package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExecuteStoredProcedureStatement extends AbstractStatement {

    public static final String PROCEDURE_NAME = "procedureName";
    public static final String PARAMETERS = "parameters";

    public ExecuteStoredProcedureStatement() {
    }

    public ExecuteStoredProcedureStatement(String procedureName) {
        setProcedureName(procedureName);
    }

    @Override
    protected void init() {
        setAttribute(PARAMETERS, new ArrayList());
    }

    public String getProcedureName() {
        return getAttribute(PROCEDURE_NAME, String.class);
    }

    public ExecuteStoredProcedureStatement setProcedureName(String procedureName) {
        return (ExecuteStoredProcedureStatement) setAttribute(PROCEDURE_NAME, procedureName);
    }


    /**
     * Return parameters to stored procedure.
     * Returned list is not modifiable, to add to the parameter list use {@link #addParameter(String)}
     */
    public List<String> getParameters() {
        return Collections.unmodifiableList(getAttribute(PARAMETERS, List.class));
    }

    public ExecuteStoredProcedureStatement addParameter(String param) {
        getAttribute(PARAMETERS, List.class).add(param);
        return this;
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
