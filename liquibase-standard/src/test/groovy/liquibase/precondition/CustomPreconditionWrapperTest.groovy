package liquibase.precondition

import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.database.core.MockDatabase
import liquibase.exception.PreconditionErrorException
import liquibase.parser.core.ParsedNode
import liquibase.parser.core.ParsedNodeException
import liquibase.sdk.supplier.resource.ResourceSupplier
import spock.lang.Shared
import spock.lang.Specification

class CustomPreconditionWrapperTest extends Specification {

    @Shared
            resourceSupplier = new ResourceSupplier()

    def "load works correctly"() {
        when:
        def node = new ParsedNode(null, "customPrecondition")
                .addChild(null, "className", "liquibase.precondition.ExampleCustomPrecondition")
                .addChild(new ParsedNode(null, "param").addChildren([name: "param 1", value: "param 1 value"]))
                .addChild(new ParsedNode(null, "param").addChildren([name: "param 2", value: "param 2 value"]))
                .addChild(new ParsedNode(null, "otherNode").setValue("should be ignored"))
                .addChild(new ParsedNode(null, "param").addChildren([name: "param 3"]).setValue("param 3 value"))
        def precondition = new CustomPreconditionWrapper()
        try {
            precondition.load(node, resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        }

        then:
        precondition.className == "liquibase.precondition.ExampleCustomPrecondition"
        precondition.params.size() == 3
        precondition.getParamValue("param 1") == "param 1 value"
        precondition.getParamValue("param 2") == "param 2 value"
        precondition.getParamValue("param 3") == "param 3 value"

    }

    def "load handles params in a 'params' collection"() {
        when:
        def node = new ParsedNode(null, "customPrecondition")
                .addChild(null, "className", "liquibase.precondition.ExampleCustomPrecondition")
                .addChild(new ParsedNode(null, "params")
                .addChild(new ParsedNode(null, "param").addChildren([name: "param 1", value: "param 1 value"]))
                .addChild(new ParsedNode(null, "param").addChildren([name: "param 2", value: "param 2 value"]))
                .addChild(new ParsedNode(null, "otherNode").setValue("should be ignored"))
                .addChild(new ParsedNode(null, "param").addChildren([name: "param 3"]).setValue("param 3 value"))
        )
        def precondition = new CustomPreconditionWrapper()
        try {
            precondition.load(node, resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        }

        then:
        precondition.className == "liquibase.precondition.ExampleCustomPrecondition"
        precondition.params.size() == 3
        precondition.getParamValue("param 1") == "param 1 value"
        precondition.getParamValue("param 2") == "param 2 value"
        precondition.getParamValue("param 3") == "param 3 value"
    }

    def "validate returns no errors by default — customPrecondition is intentional under the standard trust model (CWE-470 opt-out gate)"() {
        // CWE-470 regression: the default is liquibase.allowCustomChange=true so
        // existing users see no behaviour change.
        //
        // Explicitly scope the flag to "true" instead of reading the ambient default
        // (per @coderabbitai's hermetic-test nit on #7749): another test, the runner,
        // or a stale system property could leave the flag at "false" and turn the
        // intended default-true path into a spurious failure.
        given:
        def precondition = new CustomPreconditionWrapper()
        precondition.setClassName("liquibase.precondition.ExampleCustomPrecondition")

        when:
        def errors = Scope.child([(GlobalConfiguration.ALLOW_CUSTOM_CHANGE.getKey()): "true"],
                { return precondition.validate(new MockDatabase()) } as Scope.ScopedRunnerWithReturn)

        then:
        !errors.hasErrors()
    }

    def "validate fails when liquibase.allowCustomChange=false — embedder opt-out path"() {
        // CWE-470 regression: when the embedder disables custom-Java elements via
        // liquibase.allowCustomChange=false, validate() returns a hard error that
        // names the flag in both =false and =true directions. The flag is shared
        // with customChange (per the audit's "same flag" guidance).
        given:
        def precondition = new CustomPreconditionWrapper()
        precondition.setClassName("liquibase.precondition.ExampleCustomPrecondition")

        when:
        def errors = Scope.child([(GlobalConfiguration.ALLOW_CUSTOM_CHANGE.getKey()): "false"],
                { return precondition.validate(new MockDatabase()) } as Scope.ScopedRunnerWithReturn)

        then:
        errors.hasErrors()
        errors.getErrorMessages().any { it.contains("liquibase.allowCustomChange=false") }
        errors.getErrorMessages().any { it.contains("liquibase.allowCustomChange=true") }
        // Spells out customPrecondition specifically, so the error message points at
        // the offending element rather than reading like a generic 'custom' message.
        errors.getErrorMessages().any { it.contains("customPrecondition") }
    }

    def "check throws PreconditionErrorException BEFORE Class.forName when allowCustomChange=false"() {
        // CWE-470 defense-in-depth regression: check() is the per-evaluation entry
        // point where Class.forName(initialize=true) actually fires. Even if a
        // caller bypasses validate(), check() must reject the load BEFORE the
        // class is loaded — proving this by using a class name that does NOT
        // exist on the classpath. The PRE-fix code would attempt the load and
        // wrap ClassNotFoundException into PreconditionFailedException; AFTER the
        // fix, the gate short-circuits with PreconditionErrorException (NOT
        // PreconditionFailedException — Error bypasses onFail handling so a
        // crafted onFail=MARK_RAN cannot silently swallow the configured-off
        // intent, which is the specific attack vector flagged in the audit).
        given:
        def precondition = new CustomPreconditionWrapper()
        precondition.setClassName("com.example.definitely.not.a.real.CustomPrecondition")

        when:
        Scope.child([(GlobalConfiguration.ALLOW_CUSTOM_CHANGE.getKey()): "false"], {
            precondition.check(new MockDatabase(), null, null, null)
        } as Scope.ScopedRunner)

        then:
        PreconditionErrorException e = thrown()
        // The thrown error mentions the configured-off cause, NOT a
        // ClassNotFoundException — which proves the gate ran first.
        e.errorPreconditions != null
        !e.errorPreconditions.isEmpty()
        e.errorPreconditions[0].toString().contains("liquibase.allowCustomChange=false")
    }

    def "check default-true path still executes the existing load logic (sanity)"() {
        // Sanity that the new gate's short-circuit does not block the normal
        // default-true behaviour. ExampleCustomPrecondition is the existing test
        // fixture used in the load-correctness specs above; it's known to load
        // cleanly and its check() body doesn't have side effects against
        // MockDatabase.
        //
        // Explicitly scope the flag to "true" (per @coderabbitai's hermetic-test
        // nit on #7749) — see the same note on the "validate returns no errors
        // by default" spec above.
        given:
        def precondition = new CustomPreconditionWrapper()
        precondition.setClassName("liquibase.precondition.ExampleCustomPrecondition")

        when: "the flag is enabled"
        Scope.child([(GlobalConfiguration.ALLOW_CUSTOM_CHANGE.getKey()): "true"], {
            precondition.check(new MockDatabase(), null, null, null)
        } as Scope.ScopedRunner)

        then: "no exception thrown by the gate; ExampleCustomPrecondition.check ran normally"
        noExceptionThrown()
    }
}
