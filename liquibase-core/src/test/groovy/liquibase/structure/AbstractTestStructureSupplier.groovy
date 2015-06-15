package liquibase.structure

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.database.ConnectionSupplier
import liquibase.database.Database
import liquibase.database.core.UnsupportedDatabase
import liquibase.servicelocator.Service
import liquibase.snapshot.Snapshot
import liquibase.util.ObjectUtil
import org.springframework.jmx.export.naming.ObjectNamingStrategy

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

        int objectsToCreate = 10;

        def database = ObjectUtil.defaultIfEmpty(scope.database, new UnsupportedDatabase())
        if (database.canStoreObjectName("lower", false, type)) {
            for (int i=1; i<=objectsToCreate; i++) {
                returnList.add(type.getSimpleName().toLowerCase()+i)
            }
        } else {
            for (int i=1; i<=objectsToCreate; i++) {
                returnList.add(type.getSimpleName().toUpperCase()+i)
            }
        }

        return returnList
    }

    List<String> getComplexObjectNames(Class<T> type, Scope scope) {
        List<String> returnList = new ArrayList<>();

        returnList.add("lower" + type.getSimpleName().toLowerCase());
        returnList.add("UPPER" + type.getSimpleName().toUpperCase());
        returnList.add("Mixed" + type.getSimpleName());
        returnList.add("anotherlower" + type.getSimpleName().toLowerCase());
        returnList.add("ANOTHERUPPER" + type.getSimpleName().toUpperCase());
        returnList.add("AnotherMixed" + type.getSimpleName());
        returnList.add("4test_" + type.getSimpleName().toLowerCase());
        returnList.add("4TEST_" + type.getSimpleName().toLowerCase());
        returnList.add("crazy!@#\$%^&*()_+{}[]" + type.getSimpleName());

        return returnList
    }

    List<ObjectName> getObjectNames(Class<T> type, Scope scope) {
        List<ObjectName> returnList = new ArrayList<>();

        def containers = ObjectUtil.defaultIfEmpty(getObjectContainers(type, scope), [null])

        for (ObjectName container : containers) {
            def objectNames;
            if (scope.get(JUnitScope.Attr.objectNameStrategy, JUnitScope.TestObjectNameStrategy.SIMPLE_NAMES) == JUnitScope.TestObjectNameStrategy.COMPLEX_NAMES) {
                objectNames = getComplexObjectNames(type, scope)
            } else {
                objectNames = getSimpleObjectNames(type, scope)
            }

            for (String simpleName : objectNames) {
                returnList.add(new ObjectName(container, simpleName));
            }
            returnList.add(new ObjectName(container, "only_in_" + container.name));
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
