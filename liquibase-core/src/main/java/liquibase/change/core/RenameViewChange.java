package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.Change;
import liquibase.change.ChangeClass;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeProperty;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RenameViewStatement;

/**
 * Renames an existing view.
 */
@ChangeClass(name="renameView", description = "Rename View", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "view")
public class RenameViewChange extends AbstractChange {
    private String catalogName;
    private String schemaName;
    private String oldViewName;
    private String newViewName;

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
    public String getOldViewName() {
        return oldViewName;
    }

    public void setOldViewName(String oldViewName) {
        this.oldViewName = oldViewName;
    }

    @ChangeProperty(requiredForDatabase = "all")
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
