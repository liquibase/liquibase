package liquibase.change.core;

import liquibase.action.ActionStatus;
import liquibase.change.*;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropSequenceStatement;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.Sequence;

/**
 * Drops an existing sequence.
 */
@DatabaseChange(name="dropSequence", description = "Drop an existing sequence", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "sequence")
public class DropSequenceChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String sequenceName;

    @DatabaseChangeProperty(mustEqualExisting = "sequence.catalog", since = "3.0")
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

    @DatabaseChangeProperty(mustEqualExisting = "sequence", description = "Name of the sequence to drop")
    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{new DropSequenceStatement(getCatalogName(), getSchemaName(), getSequenceName())};
    }

    @Override
    public ActionStatus checkStatus(Database database) {
        try {
            return new ActionStatus().assertApplied(!SnapshotGeneratorFactory.getInstance().has(new Sequence(new ObjectReference(getCatalogName(), getSchemaName(), getSequenceName())), database), "Sequence exists");
        } catch (Exception e) {
            return new ActionStatus().unknown(e);
        }
    }


    @Override
    public String getConfirmationMessage() {
        return "Sequence " + getSequenceName() + " dropped";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
