package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;

@Getter
public class CreateViewStatement extends AbstractSqlStatement {

    private final String catalogName;
    private final String schemaName;
    private final String viewName;
    private final String selectQuery;
    private final boolean replaceIfExists;
    /**
     * -- GETTER --
     *  Returns the property "Does the statement contain a full CREATE [OR REPLACE] VIEW ... AS..." command (true),
     *  or just the view definition (SELECT ... FROM data_sources...) (false)?
     *
     * @return true if a complete CREATE ... VIEW statement is included, false if not.
     */
    private boolean fullDefinition;

    public CreateViewStatement(String catalogName, String schemaName, String viewName, String selectQuery, boolean replaceIfExists) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.viewName = viewName;
        this.selectQuery = selectQuery;
        this.replaceIfExists = replaceIfExists;
    }

    /**
     * Sets the property "Does the statement contain a full CREATE [OR REPLACE] VIEW ... AS..." command (true),
     * or just the view definition (SELECT ... FROM data_sources...) (false)?
     *
     * @param fullDefinition true if a CREATE ... VIEW statement is included, false if not.
     * @return the same, altered object
     */
    public CreateViewStatement setFullDefinition(boolean fullDefinition) {
        this.fullDefinition = fullDefinition;
        return this;
    }
}
