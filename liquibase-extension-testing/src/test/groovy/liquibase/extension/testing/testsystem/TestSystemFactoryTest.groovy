package liquibase.extension.testing.testsystem

import liquibase.Scope
import spock.lang.Specification
import spock.lang.Unroll

class TestSystemFactoryTest extends Specification {

    def getTestSystem() {
        when:
        def factory = Scope.currentScope.getSingleton(TestSystemFactory)

        then:
        factory.getTestSystem("mysql").is(factory.getTestSystem("mysql"))
        factory.getTestSystem("mysql:x").is(factory.getTestSystem("mysql:x"))
        !factory.getTestSystem("mysql").is(factory.getTestSystem("mysql:x"))
        factory.getTestSystem("mysql?version=1.2").is(factory.getTestSystem("mysql?version=1.2"))

        factory.getTestSystem("mysql").toString() == "mysql"
        factory.getTestSystem("mysql:x").toString() == "mysql:x"
    }

    @Unroll
    def "can construct all defined types: #name"() {
        expect:
        assert Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem(name).getDefinition().getName() == name

        where:
        name << Scope.currentScope.getSingleton(TestSystemFactory).getTestSystemNames()

    }

    def "getTestSystemNames"() {
        expect:
        Scope.currentScope.getSingleton(TestSystemFactory).getTestSystemNames().contains("mysql")
    }
}
