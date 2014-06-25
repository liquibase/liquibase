package liquibase.snapshot;

import liquibase.ExecutionEnvironment;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.MetaDataQueryStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectCollection;
import liquibase.structure.core.Schema;

public abstract class AbstractSnapshotGenerator<T extends DatabaseObject> {

    public SqlStatement generateLookupStatement(T example, ExecutionEnvironment env, StatementLogicChain chain) {
        return new MetaDataQueryStatement(example);
    }


    public abstract SqlStatement[] generateAddToStatements(DatabaseObject example, ExecutionEnvironment env, StatementLogicChain chain);

    public abstract void addTo(DatabaseObject object, DatabaseObjectCollection collection, ExecutionEnvironment env, StatementLogicChain chain);


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
