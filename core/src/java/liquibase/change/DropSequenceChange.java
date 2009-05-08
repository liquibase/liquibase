package liquibase.change;

import liquibase.database.Database;
import liquibase.statement.DropSequenceStatement;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtils;

/**
 * Drops an existing sequence.
 */
public class DropSequenceChange extends AbstractChange {

    private String schemaName;
    private String sequenceName;

    public DropSequenceChange() {
        super("dropSequence", "Drop Sequence");
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{new DropSequenceStatement(getSchemaName() == null?database.getDefaultSchemaName():getSchemaName(), getSequenceName())};
    }

    public String getConfirmationMessage() {
        return "Sequence " + getSequenceName() + " dropped";
    }
}
