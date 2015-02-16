package liquibase.structure

import liquibase.Scope
import liquibase.database.ConnectionSupplier
import liquibase.snapshot.Snapshot

public class DefaultTestStructureSupplier extends AbstractTestStructureSupplier {

    @Override
    protected Class getTypeCreates() {
        return DatabaseObject.class;
    }

    @Override
    public List<? extends DatabaseObject> getTestObjects(Class type, Snapshot snapshot, ConnectionSupplier conn, Scope scope) {
        List<? extends DatabaseObject> returnList = new ArrayList<>();

        for (def name : getObjectNames(type, conn, scope)) {
            returnList.add(type.newInstance().set(DatabaseObject.Attr.name, name))
        }

        return returnList;
    }

    @Override
    Set<Class<? extends DatabaseObject>> requires(ConnectionSupplier conn, Scope scope) {
        return null
    }
}
