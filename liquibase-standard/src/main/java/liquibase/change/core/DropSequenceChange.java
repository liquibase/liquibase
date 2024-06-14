package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropSequenceStatement;
import liquibase.structure.core.Sequence;
import lombok.Setter;

/**
 * Drops an existing sequence.
 */
@DatabaseChange(name = "dropSequence", description = "Drop an existing sequence", priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "sequence")
@Setter
public class DropSequenceChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String sequenceName;

    @DatabaseChangeProperty(mustEqualExisting = "sequence.catalog", since = "3.0", description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="sequence.schema", description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "sequence", description = "Name of the sequence to drop")
    public String getSequenceName() {
        return sequenceName;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        DropSequenceStatement statement = new DropSequenceStatement(getCatalogName(), getSchemaName(), getSequenceName());
        return new SqlStatement[]{statement};
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            return new ChangeStatus().assertComplete(!SnapshotGeneratorFactory.getInstance().has(new Sequence(getCatalogName(), getSchemaName(), getSequenceName()), database), "Sequence exists");
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
