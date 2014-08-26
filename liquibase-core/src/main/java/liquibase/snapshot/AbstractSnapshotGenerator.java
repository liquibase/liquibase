package liquibase.snapshot;

import liquibase.ExecutionEnvironment;
import liquibase.statement.Statement;
import liquibase.statement.core.SelectMetaDataStatement;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectCollection;
import liquibase.structure.core.Schema;

public abstract class AbstractSnapshotGenerator<T extends DatabaseObject> implements NewSnapshotGenerator {

    protected String getCatalogName(Schema schema) {
        if (schema == null) {
            return null;
        }
        return schema.getCatalogName();
    }

    protected String getSchemaName(Schema schema) {
        if (schema == null) {
            return null;
        }
        return schema.getName();
    }
}
