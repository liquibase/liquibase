package liquibase.snapshot;

import liquibase.RuntimeEnvironment;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.FetchObjectsStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectCollection;
import liquibase.structure.core.Schema;

public abstract class AbstractSnapshotGenerator<T extends DatabaseObject> {

    public SqlStatement generateLookupStatement(T example, RuntimeEnvironment runtimeEnvironment, ActionGeneratorChain chain) {
        return new FetchObjectsStatement(example);
    }


    public abstract SqlStatement[] generateAddToStatements(DatabaseObject example, RuntimeEnvironment runtimeEnvironment, ActionGeneratorChain chain);

    public abstract void addTo(DatabaseObject object, DatabaseObjectCollection collection, RuntimeEnvironment runtimeEnvironment, ActionGeneratorChain chain);


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
