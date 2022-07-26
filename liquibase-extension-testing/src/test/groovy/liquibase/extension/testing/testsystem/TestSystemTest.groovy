package liquibase.extension.testing.testsystem

import liquibase.Scope
import liquibase.extension.testing.testsystem.core.H2TestSystem
import liquibase.plugin.Plugin
import spock.lang.Specification

import java.sql.SQLException

class TestSystemTest extends Specification {

    def shouldTest() {
        expect:
        !new ThisTestSystem().shouldTest()
        new H2TestSystem().shouldTest()
    }

    def getPriority() {
        expect:
        new ThisTestSystem().getPriority(TestSystem.Definition.parse("invalid")) == Plugin.PRIORITY_NOT_APPLICABLE
        new ThisTestSystem().getPriority(TestSystem.Definition.parse("testing")) == Plugin.PRIORITY_DEFAULT
        new ThisTestSystem().getPriority(TestSystem.Definition.parse("testing:profile")) == Plugin.PRIORITY_DEFAULT
    }

    def getConfiguredValue() {
        expect:
        Scope.child([
                "liquibase.sdk.testSystem.other.prop1"  : "other scoped val",
                "liquibase.sdk.testSystem.testing.prop1": "system scoped val",
                "liquibase.sdk.testSystem.default.prop1": "default scoped val",
        ], { ->
            assert new ThisTestSystem().getConfiguredValue("prop1", String, false) == "system scoped val"
        } as Scope.ScopedRunner)

        Scope.child([
                "liquibase.sdk.testSystem.other.prop1"  : "other scoped val",
                "liquibase.sdk.testSystem.default.prop1": "default scoped val",
        ], { ->
            assert new ThisTestSystem().getConfiguredValue("prop1", String, false) == "default scoped val"
        } as Scope.ScopedRunner)

        Scope.child([
                "liquibase.sdk.testSystem.other.prop1"             : "other scoped val",
                "liquibase.sdk.testSystem.testing.prop1"           : "system scoped val",
                "liquibase.sdk.testSystem.testing.profiles.x.prop1": "x scoped val",
                "liquibase.sdk.testSystem.default.prop1"           : "default scoped val",
        ], { ->

            def system = new ThisTestSystem(TestSystem.Definition.parse("testing:x"))
            assert system.getConfiguredValue("prop1", String, false) == "x scoped val"
        } as Scope.ScopedRunner)

        Scope.child([
                "liquibase.sdk.testSystem.other.prop1"             : "other scoped val",
                "liquibase.sdk.testSystem.testing.prop1"           : "system scoped val",
                "liquibase.sdk.testSystem.testing.profiles.x.prop1": "x scoped val",
                "liquibase.sdk.testSystem.default.prop1"           : "default scoped val",
        ], { ->

            def system = new ThisTestSystem(TestSystem.Definition.parse("testing:x?prop1=val1"))
            assert system.getConfiguredValue("prop1", String, false) == "val1"
        } as Scope.ScopedRunner)


        Scope.child([
                "liquibase.sdk.testSystem.testing.prop1"           : "val1",
                "liquibase.sdk.testSystem.testing.prop2": "I see \${prop1}",
                "liquibase.sdk.testSystem.testing.prop3": "I see \${ prop1 }",
        ], { ->

            def system = new ThisTestSystem(TestSystem.Definition.parse("testing"))
            assert system.getConfiguredValue("prop1", String) == "val1"
            assert system.getConfiguredValue("prop2", String) == "I see val1"
            assert system.getConfiguredValue("prop3", String) == "I see val1"
        } as Scope.ScopedRunner)
    }

    private static class ThisTestSystem extends TestSystem {

        private boolean running = false;

        ThisTestSystem() {
            super("testing")
        }

        ThisTestSystem(TestSystem.Definition definition) {
            super(definition)
        }

        @Override
        void start() throws SQLException, Exception {
            running = true
        }

        @Override
        void stop() throws Exception {
            running = false
        }
    }
}
