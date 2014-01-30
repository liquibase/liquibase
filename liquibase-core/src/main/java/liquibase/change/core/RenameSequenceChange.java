package liquibase.change.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.change.AbstractChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RenameSequenceStatement;

/**
 * Renames an existing table.
 */
@DatabaseChange(name="renameSequence", description = "Renames an existing sequence", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "sequence")
public class RenameSequenceChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String oldSequenceName;

    private String newSequenceName;

    public RenameSequenceChange() {
    }

    @DatabaseChangeProperty(mustEqualExisting ="sequence.catalog", since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="sequence.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "sequence", description = "Name of the sequence to rename")
    public String getOldSequenceName() {
        return oldSequenceName;
    }

    public void setOldSequenceName(String oldSequenceName) {
        this.oldSequenceName = oldSequenceName;
    }
    
    @DatabaseChangeProperty(description = "New name for the sequence")
    public String getNewSequenceName() {
        return newSequenceName;
    }

    public void setNewSequenceName(String newSequenceName) {
        this.newSequenceName = newSequenceName;
    }
    
    @Override
    public SqlStatement[] generateStatements(Database database) {
        List<SqlStatement> statements = new ArrayList<SqlStatement>();
        statements.add(new RenameSequenceStatement(getCatalogName(), getSchemaName(), getOldSequenceName(), getNewSequenceName()));
        return statements.toArray(new SqlStatement[statements.size()]);
    }

    @Override
    protected Change[] createInverses() {
        RenameSequenceChange inverse = new RenameSequenceChange();
        inverse.setSchemaName(getSchemaName());
        inverse.setOldSequenceName(getNewSequenceName());
        inverse.setNewSequenceName(getOldSequenceName());

        return new Change[]{
                inverse
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Sequence " + oldSequenceName + " renamed to " + newSequenceName;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
