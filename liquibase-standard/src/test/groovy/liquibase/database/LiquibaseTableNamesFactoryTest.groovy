package liquibase.database

import liquibase.Scope
import liquibase.servicelocator.ServiceLocator
import spock.lang.Specification

class LiquibaseTableNamesFactoryTest extends Specification {

    def "generators with the same getOrder() all contribute (no silent drop)"() {
        given:
        def g1 = tableNames(100, ["T1"])
        def g2 = tableNames(100, ["T2"])
        def locator = locatorReturning(LiquibaseTableNames, [g1, g2])

        when: "the factory is built with a service locator that returns two same-order generators"
        def names = Scope.child([(Scope.Attr.serviceLocator.name()): locator], {
            def ctor = LiquibaseTableNamesFactory.getDeclaredConstructor()
            ctor.setAccessible(true)
            def factory = ctor.newInstance()
            factory.getLiquibaseTableNames(null)
        } as Scope.ScopedRunnerWithReturn)

        then: "both generators' table names are present (a comparator-keyed TreeSet would have dropped one)"
        names.toSet() == ["T1", "T2"].toSet()
    }

    private static LiquibaseTableNames tableNames(int order, List<String> names) {
        return new LiquibaseTableNames() {
            @Override List<String> getLiquibaseGeneratedTableNames(Database database) { names }
            @Override void destroy(Database database) {}
            @Override int getOrder() { order }
        }
    }

    private static ServiceLocator locatorReturning(Class type, List instances) {
        return new ServiceLocator() {
            @Override int getPriority() { 0 }
            @Override List findInstances(Class interfaceType) { interfaceType == type ? instances : [] }
        }
    }
}