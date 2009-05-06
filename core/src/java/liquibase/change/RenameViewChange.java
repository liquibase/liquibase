package liquibase.change;

import liquibase.database.Database;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.statement.RenameViewStatement;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtils;

/**
 * Renames an existing view.
 */
public class RenameViewChange extends AbstractChange {
    private String schemaName;
    private String oldViewName;
    private String newViewName;

    public RenameViewChange() {
        super("renameView", "Rename View");
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getOldViewName() {
        return oldViewName;
    }

    public void setOldViewName(String oldViewName) {
        this.oldViewName = oldViewName;
    }

    public String getNewViewName() {
        return newViewName;
    }

    public void setNewViewName(String newViewName) {
        this.newViewName = newViewName;
    }

    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{new RenameViewStatement(getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), getOldViewName(), getNewViewName())};
    }

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
