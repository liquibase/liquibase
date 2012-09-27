package liquibase.snapshot;

import liquibase.CatalogAndSchema;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;

public class SnapshotControl {

    private CatalogAndSchema[] schemas;
    private Class[] types;

    public SnapshotControl(CatalogAndSchema... schemas) {
        this.schemas = schemas;
        this.types = new Class[] {
                Table.class,
                View.class
        };
    }

    public CatalogAndSchema[] getSchemas() {
        return schemas;
    }

    public void setTypes(Class<? extends DatabaseObject>... types) {
        this.types = types;
    }
}
