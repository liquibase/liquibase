package liquibase.structure

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.database.ConnectionSupplier
import liquibase.snapshot.Snapshot
import liquibase.structure.core.Schema

public class TestSchemaSupplier extends DefaultTestStructureSupplier {

    @Override
    protected Class getTypeCreates() {
        return Schema
    }

    @Override
    List<? extends DatabaseObject> getTestObjects(Class type, Snapshot snapshot, Scope scope) {
        def returnList = []
        for (ObjectName schemaName : scope.get(JUnitScope.Attr.connectionSupplier, ConnectionSupplier).getAllContainers()) {
            returnList.add(new Schema(schemaName))
        }

        return returnList
    }

    @Override
    Set<Class<? extends DatabaseObject>> requires(Scope scope) {
        return null
    }
}
