package liquibase.snapshot

import liquibase.Scope
import liquibase.snapshot.transformer.SnapshotTransformer
import liquibase.structure.DatabaseObject
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

        def types = [Catalog, Schema, Table, Column] //TODO: ensure correct sorting for scope.getSingleton(ServiceLocator).findClasses(DatabaseObject)

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

        snapshot.relate(scope)

        return snapshot;
    }

}
