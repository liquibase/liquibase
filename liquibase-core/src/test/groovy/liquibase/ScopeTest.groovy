package liquibase

import spock.lang.Specification

class ScopeTest extends Specification {

    def "getCurrentScope() creates root scope"() {
        expect:
        Scope.getCurrentScope().describe() == "scope(database=null)"
    }

    def "Nesting Scopes works"() {
        expect:
        Scope.currentScope.describe() == "scope(database=null)"

        assert Scope.currentScope.get("test1", String) == null
        assert Scope.currentScope.get("test2", String) == null
        assert Scope.currentScope.get("test3", String) == null

        Scope.child([test1: "Level 1 A", test2: "Level 1 B"], {
            assert Scope.currentScope.get("test1", String) == "Level 1 A"
            assert Scope.currentScope.get("test2", String) == "Level 1 B"
            assert Scope.currentScope.get("test3", String) == null

            Scope.child(["test1": "Level 2 A", "test3": "Level 2 C"], {
                assert Scope.currentScope.get("test1", String) == "Level 2 A"
                assert Scope.currentScope.get("test2", String) == "Level 1 B"
                assert Scope.currentScope.get("test3", String) == "Level 2 C"
            } as Scope.ScopedRunner)

            assert Scope.currentScope.get("test1", String) == "Level 1 A"
            assert Scope.currentScope.get("test2", String) == "Level 1 B"
            assert Scope.currentScope.get("test3", String) == null

            Scope.child(["test1": "Level 2 X", "test3": "Level 2 Y"], {
                assert Scope.currentScope.get("test1", String) == "Level 2 X"
                assert Scope.currentScope.get("test2", String) == "Level 1 B"
                assert Scope.currentScope.get("test3", String) == "Level 2 Y"

                Scope.child(["test1": "Level 3 D", "test2": "Level 3 E"], {
                    assert Scope.currentScope.get("test1", String) == "Level 3 D"
                    assert Scope.currentScope.get("test2", String) == "Level 3 E"
                    assert Scope.currentScope.get("test3", String) == "Level 2 Y"
                } as Scope.ScopedRunner)
            } as Scope.ScopedRunner)
        } as Scope.ScopedRunner)
    }

    def "start and end works"() {
        expect:
        Scope.currentScope.describe() == "scope(database=null)"

        assert Scope.currentScope.get("test1", String) == null
        assert Scope.currentScope.get("test2", String) == null
        assert Scope.currentScope.get("test3", String) == null

        def scope1 = Scope.enter(null, [test1: "Level 1 A", test2: "Level 1 B"])
        assert Scope.currentScope.get("test1", String) == "Level 1 A"
        assert Scope.currentScope.get("test2", String) == "Level 1 B"
        assert Scope.currentScope.get("test3", String) == null

        def scope2 = Scope.enter(null, ["test1": "Level 2 A", "test3": "Level 2 C"])
        assert Scope.currentScope.get("test1", String) == "Level 2 A"
        assert Scope.currentScope.get("test2", String) == "Level 1 B"
        assert Scope.currentScope.get("test3", String) == "Level 2 C"

        Scope.exit(scope2)
        assert Scope.currentScope.get("test1", String) == "Level 1 A"
        assert Scope.currentScope.get("test2", String) == "Level 1 B"
        assert Scope.currentScope.get("test3", String) == null

        Scope.exit(scope1)
        assert Scope.currentScope.get("test1", String) == null
        assert Scope.currentScope.get("test2", String) == null
        assert Scope.currentScope.get("test3", String) == null
    }

    def "cannot end the wrong scope id"() {
        when:
        def scope1 = Scope.enter(null, [test1: "a"])
        def scope2 = Scope.enter(null, [test1: "b"])

        Scope.exit(scope1)

        then:
        def e = thrown(RuntimeException)
        e.message.startsWith("Cannot end scope ")
    }

}
