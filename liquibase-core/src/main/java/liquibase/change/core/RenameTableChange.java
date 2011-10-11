package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RenameTableStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Renames an existing table.
 */
@ChangeClass(name="renameTable", description = "Rename Table", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class RenameTableChange extends AbstractChange {

    private String schemaName;
    private String oldTableName;

    private String newTableName;

    public RenameTableChange() {
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    @ChangeProperty(requiredForDatabase = "all")
    public String getOldTableName() {
        return oldTableName;
    }

    public void setOldTableName(String oldTableName) {
        this.oldTableName = oldTableName;
    }

    @ChangeProperty(requiredForDatabase = "all")
    public String getNewTableName() {
        return newTableName;
    }

    public void setNewTableName(String newTableName) {
        this.newTableName = newTableName;
    }

    public SqlStatement[] generateStatements(Database database) {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        String schemaName = getSchemaName() == null?database.getDefaultSchemaName():getSchemaName();
        statements.add(new RenameTableStatement(schemaName, getOldTableName(), getNewTableName()));
        if (database instanceof DB2Database) {
            statements.add(new ReorganizeTableStatement(schemaName, getNewTableName()));
        }

        return statements.toArray(new SqlStatement[statements.size()]);
    }

    @Override
    protected Change[] createInverses() {
        RenameTableChange inverse = new RenameTableChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setOldTableName(getNewTableName());
        inverse.setNewTableName(getOldTableName());

        return new Change[]{
                inverse
        };
    }

    public String getConfirmationMessage() {
        return "Table " + oldTableName + " renamed to " + newTableName;
    }
}
