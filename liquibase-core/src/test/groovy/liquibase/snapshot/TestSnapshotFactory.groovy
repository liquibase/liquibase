package liquibase.snapshot

import liquibase.Scope
import liquibase.snapshot.transformer.SnapshotTransformer
import liquibase.structure.DatabaseObject
import liquibase.structure.ObjectName
import liquibase.structure.TestStructureSupplierFactory
import liquibase.structure.core.Catalog
import liquibase.structure.core.Column
import liquibase.structure.core.Schema
import liquibase.structure.core.Table;

public class TestSnapshotFactory {

    Snapshot createSnapshot(Scope scope) {
        return createSnapshot(null, scope)
    }

    Snapshot createSnapshot(SnapshotTransformer transformer, Scope scope) {
        Snapshot snapshot = new Snapshot(scope)
        def supplierFactory = scope.getSingleton(TestStructureSupplierFactory)

        def types = [Table, Column] //TODO: ensure correct sorting for scope.getSingleton(ServiceLocator).findClasses(DatabaseObject)

        for (def type : types) {
            def structureSupplier = supplierFactory.getTestStructureSupplier(type, scope)
            if (structureSupplier != null) {
                for (DatabaseObject obj : structureSupplier.getTestObjects(type, snapshot, scope)) {
                    if (transformer != null) {
                        obj = transformer.transform(obj, scope)
                    }
                    if (obj != null) {
                        snapshot.add(obj)
                    }
                }
            }
        }

        Set<ObjectName> schemas = new HashSet<>();
        for (Table table : snapshot.get(Table)) {
            schemas.add(table.name.container);
        }


        Set<ObjectName> catalogs = new HashSet<>();
        for (ObjectName schema : schemas) {
            snapshot.add(new Schema(schema))
            if (schema.container != null) {
                catalogs.add(schema.container);
            }
        }

        for (ObjectName catalog : catalogs) {
            snapshot.add(new Catalog(catalog))
        }

        return snapshot;
    }

}
