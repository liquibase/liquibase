package liquibase.structure

import liquibase.Scope
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
    List<? extends DatabaseObject> getTestObjects(Class type, Snapshot snapshot, Scope scope) {
        def returnList = []
        for (Table table : snapshot.get(Table)) {
            for (Column baseColumn : super.getTestObjects(type, snapshot, scope)) {
                Column column = new Column(new ObjectName(table.name, baseColumn.name.name));
                column.type = new DataType("int")
                returnList.add(column)
            }
        }

        return returnList
    }

    @Override
    Set<Class<? extends DatabaseObject>> requires(Scope scope) {
        return [Table] as Set
    }

    @Override
    protected List<ObjectName> getObjectContainers(Scope scope) {
        return [new ObjectName()];
    }

}
