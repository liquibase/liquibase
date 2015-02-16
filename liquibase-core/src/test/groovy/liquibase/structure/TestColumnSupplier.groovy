package liquibase.structure

import liquibase.Scope
import liquibase.database.ConnectionSupplier
import liquibase.snapshot.Snapshot
import liquibase.structure.core.Column
import liquibase.structure.core.DataType
import liquibase.structure.core.Table

class TestColumnSupplier extends DefaultTestStructureSupplier{

    @Override
    protected Class getTypeCreates() {
        return Column
    }

    @Override
    List<? extends DatabaseObject> getTestObjects(Class type, Snapshot snapshot, ConnectionSupplier conn, Scope scope) {
        def returnList = []
        for (Table table : snapshot.get(Table)) {
            for (Column column : super.getTestObjects(type, snapshot, conn, scope)) {
                returnList.add(column.setRelation(new Table(table.getName())).setType(new DataType("int")))
            }
        }

        return returnList
    }

    @Override
    Set<Class<? extends DatabaseObject>> requires(ConnectionSupplier conn, Scope scope) {
        return [Table] as Set
    }
}
