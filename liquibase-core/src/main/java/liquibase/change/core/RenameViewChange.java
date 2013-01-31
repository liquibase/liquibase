package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RenameViewStatement;
import liquibase.structure.core.View;

/**
 * Renames an existing view.
 */
@DatabaseChange(name="renameView", description = "Rename View", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = View.class)
public class RenameViewChange extends AbstractChange {
    private String catalogName;
    private String schemaName;
    private String oldViewName;
    private String newViewName;

    @DatabaseChangeProperty(mustApplyTo ="view.catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustApplyTo ="view.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all", mustApplyTo = "view")
    public String getOldViewName() {
        return oldViewName;
    }

    public void setOldViewName(String oldViewName) {
        this.oldViewName = oldViewName;
    }

    @DatabaseChangeProperty(requiredForDatabase = "all")
    public String getNewViewName() {
        return newViewName;
    }

    public void setNewViewName(String newViewName) {
        this.newViewName = newViewName;
    }

    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{new RenameViewStatement(getCatalogName(), getSchemaName(), getOldViewName(), getNewViewName())};
    }

    @Override
    protected Change[] createInverses() {
        RenameViewChange inverse = new RenameViewChange();
        inverse.setOldViewName(getNewViewName());
        inverse.setNewViewName(getOldViewName());

        return new Change[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "View " + oldViewName + " renamed to " + newViewName;
    }

}
