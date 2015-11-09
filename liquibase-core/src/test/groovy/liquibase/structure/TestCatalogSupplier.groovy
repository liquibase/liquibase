package liquibase.structure

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.database.ConnectionSupplier
import liquibase.snapshot.Snapshot
import liquibase.structure.core.Catalog

public class TestCatalogSupplier extends DefaultTestStructureSupplier {

    @Override
    protected Class getTypeCreates() {
        return Catalog
    }

    @Override
    List<? extends DatabaseObject> getTestObjects(Class type, Snapshot snapshot, Scope scope) {
        def returnList = []
        def seenNames = [] as Set
        for (ObjectReference schemaName : scope.get(JUnitScope.Attr.connectionSupplier, ConnectionSupplier).getAllContainers()) {
            if (schemaName.container != null && !seenNames.contains(schemaName.container.name)) {
                returnList.add(new Catalog(schemaName.container.name))
            }

        }

        return returnList
    }

    @Override
    Set<Class<? extends DatabaseObject>> requires(Scope scope) {
        return null
    }
}
