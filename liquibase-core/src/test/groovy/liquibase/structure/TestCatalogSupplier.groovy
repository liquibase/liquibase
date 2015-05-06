package liquibase.structure

import liquibase.Scope
import liquibase.database.ConnectionSupplier
import liquibase.snapshot.Snapshot
import liquibase.structure.core.Catalog
import liquibase.structure.core.Schema

public class TestCatalogSupplier extends DefaultTestStructureSupplier {

    @Override
    protected Class getTypeCreates() {
        return Catalog
    }

    @Override
    List<? extends DatabaseObject> getTestObjects(Class type, Snapshot snapshot, ConnectionSupplier conn, Scope scope) {
        def returnList = []
        def seenNames = [] as Set
        for (ObjectName schemaName : conn.getAllContainers()) {
            if (schemaName.getContainer() != null && !seenNames.contains(schemaName.getContainer().getName())) {
                returnList.add(new Catalog(schemaName.getContainer().getName()))
            }

        }

        return returnList
    }

    @Override
    Set<Class<? extends DatabaseObject>> requires(ConnectionSupplier conn, Scope scope) {
        return null
    }
}
