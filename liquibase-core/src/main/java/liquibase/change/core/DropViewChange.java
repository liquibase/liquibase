package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.DatabaseChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeProperty;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropViewStatement;

/**
 * Drops an existing view.
 */
@DatabaseChange(name="dropView", description = "Drop View", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "view")
public class DropViewChange extends AbstractChange {
    private String catalogName;
    private String schemaName;
    private String viewName;


    @ChangeProperty(mustApplyTo ="view.catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @ChangeProperty(mustApplyTo ="view.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @ChangeProperty(requiredForDatabase = "all", mustApplyTo = "view")
    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
                new DropViewStatement(getCatalogName(), getSchemaName(), getViewName()),
        };
    }

    public String getConfirmationMessage() {
        return "View "+getViewName()+" dropped";
    }

}
