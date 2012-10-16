package liquibase.snapshot;

import liquibase.CatalogAndSchema;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SnapshotControl {

    private CatalogAndSchema[] schemas;
    private Set<Class<? extends DatabaseObject>> types;

    public SnapshotControl() {
        this(new CatalogAndSchema[]{CatalogAndSchema.DEFAULT}, null);
    }

    public SnapshotControl(CatalogAndSchema schema, Class<? extends DatabaseObject>... types) {
        this(new CatalogAndSchema[]{schema}, types);
    }

    public SnapshotControl(CatalogAndSchema[] schemas, Class<? extends DatabaseObject>[] types) {
        this.schemas = schemas;
        if (types == null || types.length == 0) {
            this.types = getDefaultTypes();
        } else {
            this.types = new HashSet<Class<? extends DatabaseObject>>(Arrays.asList(types));
        }
    }

    protected Set<Class<? extends DatabaseObject>> getDefaultTypes() {
        HashSet<Class<? extends DatabaseObject>> set = new HashSet<Class<? extends DatabaseObject>>();
        set.add(Table.class);
        set.add(View.class);
        set.add(Column.class);

        return set;
    }

    public CatalogAndSchema[] getSchemas() {
        return schemas;
    }

    public Set<Class<? extends DatabaseObject>> getTypesToInclude() {
        return types;
    }

    public boolean shouldSnapshot(Class<? extends DatabaseObject> type) {
        return types.contains(type);
    }
}
