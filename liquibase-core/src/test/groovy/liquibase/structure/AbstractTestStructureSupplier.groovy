package liquibase.structure

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.database.ConnectionSupplier
import liquibase.servicelocator.Service
import liquibase.snapshot.Snapshot
import liquibase.util.ObjectUtil

abstract class AbstractTestStructureSupplier<T extends DatabaseObject> implements Service {

    protected abstract Class<T> getTypeCreates();

    int getPriority(Class<? extends DatabaseObject> type, Scope scope) {
        if (!type.isAssignableFrom(getTypeCreates())) {
            return PRIORITY_NOT_APPLICABLE;
        }
        return PRIORITY_DEFAULT
    }

    abstract List<T> getTestObjects(Class<T> type, Snapshot snapshot, Scope scope);

    List<String> getSimpleObjectNames(Class<T> type, Scope scope) {
        List<String> returnList = new ArrayList<>();

        returnList.add("test" + type.getSimpleName().toLowerCase());
        returnList.add("TEST" + type.getSimpleName().toUpperCase());
        returnList.add("Test" + type.getSimpleName());
        returnList.add("anothertest" + type.getSimpleName().toLowerCase());
        returnList.add("ANOTHERTEST" + type.getSimpleName().toUpperCase());
        returnList.add("AnotherTest" + type.getSimpleName());
        returnList.add("4test_" + type.getSimpleName().toLowerCase());
        returnList.add("4TEST_" + type.getSimpleName().toLowerCase());
        returnList.add("crazy!@#\$%^&*()_+{}[]" + type.getSimpleName());

        return returnList
    }

    List<ObjectName> getObjectNames(Class<T> type, Scope scope) {
        List<ObjectName> returnList = new ArrayList<>();

        def containers = ObjectUtil.defaultIfEmpty(getObjectContainers(type, scope), [null])

        for (ObjectName container : containers) {
            for (String simpleName : getSimpleObjectNames(type, scope)) {
                returnList.add(new ObjectName(container, simpleName));
            }
            returnList.add(new ObjectName(container, "only_" + container.name));
        }

        if (!scope.database.isCaseSensitive(type)) {
            returnList = returnList.findAll { it.name.matches("[^a-z]+")}
        }

        return returnList;
    }

    protected List<ObjectName> getObjectContainers(Class<T> objectType, Scope scope) {
        return scope.get(JUnitScope.Attr.connectionSupplier, ConnectionSupplier).getAllContainers()
    }

    abstract Set<Class<? extends DatabaseObject>> requires(Scope scope);
}
