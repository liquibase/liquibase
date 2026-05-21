package liquibase.change.core

import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.exception.SetupException
import liquibase.parser.core.ParsedNodeException
import liquibase.sdk.supplier.resource.ResourceSupplier
import org.hamcrest.Matchers
import spock.lang.Shared
import spock.lang.Specification

import static spock.util.matcher.HamcrestSupport.that

class ExecuteShellCommandChangeTest extends Specification {

    @Shared resourceSupplier = new ResourceSupplier()

    def "load works correctly"() {
        when:
        def change = new ExecuteShellCommandChange()
        try {
            change.load(new liquibase.parser.core.ParsedNode(null, "executeCommand")
                    .addChildren([executable: "/usr/bin/test", os: "linux,mac"])
                    .addChild(new liquibase.parser.core.ParsedNode(null, "arg").addChild(null, "value", "-out"))
                    .addChild(new liquibase.parser.core.ParsedNode(null, "arg").addChild(null, "value", "-test"))
                    , resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        } catch (SetupException e) {
            e.printStackTrace()
        }

        then:
        change.executable == "/usr/bin/test"
        that change.getOs(), Matchers.contains(["linux", "mac"].toArray())
        that change.args, Matchers.contains("-out", "-test")
    }

    def "load handles nested 'args' collection"() {
        when:
        def change = new ExecuteShellCommandChange()
        try {
            change.load(new liquibase.parser.core.ParsedNode(null, "executeCommand")
                    .addChildren([executable: "/usr/bin/test", os: "linux,mac"])
                    .addChild(new liquibase.parser.core.ParsedNode(null, "args")
                    .addChild(new liquibase.parser.core.ParsedNode(null, "arg").addChild(null, "value", "-out"))
                    .addChild(new liquibase.parser.core.ParsedNode(null, "arg").addChild(null, "value", "-test"))
            ), resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        }

        then:
        change.executable == "/usr/bin/test"
        that change.getOs(), Matchers.contains(["linux", "mac"].toArray())
        that change.args, Matchers.contains("-out", "-test")
    }

    def "test getTimoutInMillis"() {
        when:
        def change = new ExecuteShellCommandChange()
        try {
            change.load(new liquibase.parser.core.ParsedNode(null, "executeCommand")
                    .addChildren([executable: "/usr/bin/test", os: "linux,mac", timeout:"10s"])
                    .addChild(new liquibase.parser.core.ParsedNode(null, "args")
                    .addChild(new liquibase.parser.core.ParsedNode(null, "arg").addChild(null, "value", "-out"))
                    .addChild(new liquibase.parser.core.ParsedNode(null, "arg").addChild(null, "value", "-test"))
            ), resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        }

        then:
        change.executable == "/usr/bin/test"
        change.timeout == "10s"
        that change.getOs(), Matchers.contains(["linux", "mac"].toArray())
        that change.args, Matchers.contains("-out", "-test")
    }

    def "validate passes by default — executeCommand is intentional under the standard trust model (CWE-78 opt-out gate)"() {
        // CWE-78 regression: the default is liquibase.allowExecuteCommand=true so
        // existing users see no behaviour change. validate() must NOT return an
        // error in this state, and the timeout/etc. existing checks still run.
        given:
        def change = new ExecuteShellCommandChange()
        change.setExecutable("/usr/bin/test")

        when:
        def errors = change.validate(null)

        then:
        !errors.hasErrors()
    }

    def "validate fails when liquibase.allowExecuteCommand=false — embedder opt-out path"() {
        // CWE-78 regression: when the embedder sets liquibase.allowExecuteCommand=false,
        // executeCommand changes must be rejected at validation time BEFORE
        // ProcessBuilder.start() runs. The error message must name the flag so
        // operators can find their way back to the documented opt-in.
        given:
        def change = new ExecuteShellCommandChange()
        change.setExecutable("/usr/bin/test")

        when: "the embedder has disabled executeCommand via configuration"
        def errors = Scope.child([(GlobalConfiguration.ALLOW_EXECUTE_COMMAND.getKey()): "false"],
                { return change.validate(null) } as Scope.ScopedRunnerWithReturn)

        then:
        errors.hasErrors()
        // Specifically the disabled-by-config error, not the generic timeout error.
        errors.getErrorMessages().any { it.contains("liquibase.allowExecuteCommand=false") }
        errors.getErrorMessages().any { it.contains("liquibase.allowExecuteCommand=true") }
    }

    def "validate gate short-circuits — unrelated timeout-validation errors do not appear when the change is disabled"() {
        // CWE-78 regression: the gate runs first and short-circuits, so a
        // changelog that has BOTH an invalid timeout AND is rejected by the flag
        // returns only the configuration error. This keeps operator-facing
        // messaging focused — they need to fix the config or move the changelog,
        // not chase a timeout format question that is irrelevant once the change
        // is disabled.
        given:
        def change = new ExecuteShellCommandChange()
        change.setExecutable("/usr/bin/test")
        change.setTimeout("definitely not a valid duration")

        when:
        def errors = Scope.child([(GlobalConfiguration.ALLOW_EXECUTE_COMMAND.getKey()): "false"],
                { return change.validate(null) } as Scope.ScopedRunnerWithReturn)

        then:
        errors.hasErrors()
        // Only the gate's message; the timeout-validation message must NOT appear.
        errors.getErrorMessages().any { it.contains("liquibase.allowExecuteCommand=false") }
        !errors.getErrorMessages().any { it.contains("Invalid value specified for timeout") }
    }

    def "validate when flag is true still runs the existing timeout validation"() {
        // Sanity check that the new gate's short-circuit does not skip the
        // existing checks when the flag is at its default (true).
        given:
        def change = new ExecuteShellCommandChange()
        change.setExecutable("/usr/bin/test")
        change.setTimeout("definitely not a valid duration")

        when:
        def errors = change.validate(null)

        then:
        errors.hasErrors()
        errors.getErrorMessages().any { it.contains("Invalid value specified for timeout") }
        !errors.getErrorMessages().any { it.contains("liquibase.allowExecuteCommand") }
    }
}
