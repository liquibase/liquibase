package liquibase.structure;

import liquibase.Scope;
import liquibase.database.ConnectionSupplier;
import liquibase.snapshot.Snapshot;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

import java.util.List;
import java.util.Set;

public class TestSchemaSupplier extends DefaultTestStructureSupplier {

    @Override
    protected Class getTypeCreates() {
        return Schema
    }

    @Override
    List<? extends DatabaseObject> getTestObjects(Class type, Snapshot snapshot, ConnectionSupplier conn, Scope scope) {
        def returnList = []
        for (ObjectName schemaName : conn.getAllContainers()) {
            returnList.add(new Schema(schemaName))
        }

        return returnList
    }

    @Override
    Set<Class<? extends DatabaseObject>> requires(ConnectionSupplier conn, Scope scope) {
        return null
    }
}
