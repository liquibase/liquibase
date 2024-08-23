package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RenameSequenceStatement;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Renames an existing table.
 */
@DatabaseChange(name = "renameSequence", description = "Renames an existing sequence", priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "sequence")
@Setter
public class RenameSequenceChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String oldSequenceName;

    private String newSequenceName;

    public RenameSequenceChange() {
    }

    @DatabaseChangeProperty(mustEqualExisting = "sequence.catalog", since = "3.0", description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "sequence.schema", description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "sequence", description = "Name of the existing sequence to rename")
    public String getOldSequenceName() {
        return oldSequenceName;
    }

    @DatabaseChangeProperty(description = "New name for the sequence")
    public String getNewSequenceName() {
        return newSequenceName;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        List<SqlStatement> statements = new ArrayList<>();
        statements.add(new RenameSequenceStatement(getCatalogName(), getSchemaName(), getOldSequenceName(), getNewSequenceName()));
        return statements.toArray(SqlStatement.EMPTY_SQL_STATEMENT);
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
