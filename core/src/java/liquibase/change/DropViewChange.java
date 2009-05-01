package liquibase.change;

import liquibase.database.Database;
import liquibase.database.statement.DropViewStatement;
import liquibase.database.statement.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.View;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Drops an existing view.
 */
public class DropViewChange extends AbstractChange {
    private String schemaName;
    private String viewName;

    public DropViewChange() {
        super("dropView", "Drop View");
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public void validate(Database database) throws InvalidChangeDefinitionException {
        if (StringUtils.trimToNull(viewName) == null) {
            throw new InvalidChangeDefinitionException("viewName is required", this);
        }

    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        return new SqlStatement[]{
                new DropViewStatement(getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), getViewName()),
        };
    }

    public String getConfirmationMessage() {
        return "View "+getViewName()+" dropped";
    }

}
