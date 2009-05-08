package liquibase.change;

import liquibase.database.Database;
import liquibase.statement.DropViewStatement;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtils;

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

    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
                new DropViewStatement(getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), getViewName()),
        };
    }

    public String getConfirmationMessage() {
        return "View "+getViewName()+" dropped";
    }

}
