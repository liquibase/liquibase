package liquibase.change.core;

import liquibase.change.*;
import  liquibase.ExecutionEnvironment;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropSequenceStatement;
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
    public SqlStatement[] generateStatements(ExecutionEnvironment env) {
        return new SqlStatement[]{new DropSequenceStatement(getCatalogName(), getSchemaName(), getSequenceName())};
    }

    @Override
    public ChangeStatus checkStatus(ExecutionEnvironment env) {
        try {
            return new ChangeStatus().assertComplete(!SnapshotGeneratorFactory.getInstance().has(new Sequence(getCatalogName(), getSchemaName(), getSequenceName()), env.getTargetDatabase()), "Sequence exists");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
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
