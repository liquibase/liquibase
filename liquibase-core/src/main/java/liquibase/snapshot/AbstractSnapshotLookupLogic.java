package liquibase.snapshot;

import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;

public abstract class AbstractSnapshotLookupLogic<T extends DatabaseObject> implements SnapshotLookupLogic {

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
