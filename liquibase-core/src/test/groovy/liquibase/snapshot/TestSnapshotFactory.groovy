package liquibase.snapshot

import liquibase.Scope
import liquibase.database.ConnectionSupplier
import liquibase.servicelocator.ServiceLocator
import liquibase.structure.DatabaseObject
import liquibase.structure.TestStructureSupplierFactory
import liquibase.structure.core.Column
import liquibase.structure.core.Table;

public class TestSnapshotFactory {

    Snapshot createSnapshot(ConnectionSupplier conn, Scope scope) {
        Snapshot snapshot = new Snapshot(scope)
        def supplierFactory = scope.getSingleton(TestStructureSupplierFactory)

        def types = [Table, Column] //TODO: ensure correct sorting for scope.getSingleton(ServiceLocator).findClasses(DatabaseObject)

        for (def type : types) {
            def supplier = supplierFactory.getTestStructureSupplier(type, conn, scope)
            if (supplier != null) {
                for (DatabaseObject obj : supplier.getTestObjects(type, snapshot, conn, scope)) {
                    snapshot.add(obj)
                }
            }
        }

        snapshot.relate(scope)

        return snapshot;
    }

}
