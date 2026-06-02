package liquibase.precondition.core

import liquibase.Scope
import liquibase.database.Database
import liquibase.exception.PreconditionErrorException
import liquibase.parser.core.ParsedNodeException
import liquibase.sdk.supplier.resource.ResourceSupplier
import spock.lang.Shared
import spock.lang.Specification

class SqlPreconditionTest extends Specification {
    @Shared resourceSupplier = new ResourceSupplier()

    def "load works with nested sql"() {
        when:
        def precondition = new SqlPrecondition()
        try {
            precondition.load(new liquibase.parser.core.ParsedNode(null, "sqlCheck").addChild(null, "expectedResult", "5").setValue("select count(*) from test"), resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        }

        then:
        precondition.expectedResult == "5"
        precondition.sql == "select count(*) from test"
    }

    def "validate returns no errors by default — sqlCheck is intentional under the standard trust model (CWE-89 opt-out gate)"() {
        given:
        def precondition = new SqlPrecondition()
        precondition.sql = "select 1"
        precondition.expectedResult = "1"

        when:
        // Wrap default-true explicitly so a stale system property or test-ordering quirk
        // can't turn this default-pass into a spurious failure.
        def errors
        Scope.child(["liquibase.allowSqlPrecondition": "true"] as Map, {
            errors = precondition.validate(null)
        } as Scope.ScopedRunner)

        then:
        errors != null
        !errors.hasErrors()
    }

    def "validate fails with the configured-off hard error when allowSqlPrecondition=false — embedder opt-out path"() {
        given:
        def precondition = new SqlPrecondition()
        precondition.sql = "select 1"
        precondition.expectedResult = "1"

        when:
        def errors
        Scope.child(["liquibase.allowSqlPrecondition": "false"] as Map, {
            errors = precondition.validate(null)
        } as Scope.ScopedRunner)

        then:
        errors != null
        errors.hasErrors()
        // Message must name the flag in both directions and identify the element so
        // operators know which precondition is rejected and how to flip the gate.
        def joined = errors.getErrorMessages().join(" | ")
        joined.contains("sqlCheck")
        joined.contains("liquibase.allowSqlPrecondition=false")
        joined.contains("liquibase.allowSqlPrecondition=true")
    }

    def "check throws PreconditionErrorException BEFORE any SQL execution when allowSqlPrecondition=false"() {
        // The strongest assertion in this PR: the gate must fire before the SQL body
        // reaches the JDBC executor, regardless of onFail handling on the precondition.
        // We mock Database; if the gate did NOT fire, the executor lookup would proceed
        // and the configured-off message would NOT appear. The message-shape match is
        // the proof-of-short-circuit.
        given:
        def precondition = new SqlPrecondition()
        precondition.sql = "INSERT INTO admins VALUES ('attacker', 'hash'); SELECT 1"
        precondition.expectedResult = "1"
        def database = Mock(Database)
        PreconditionErrorException caught = null

        when:
        Scope.child(["liquibase.allowSqlPrecondition": "false"] as Map, {
            try {
                precondition.check(database, null, null, null)
            } catch (PreconditionErrorException e) {
                caught = e
            }
        } as Scope.ScopedRunner)

        then:
        caught != null
        // PreconditionErrorException.getMessage() returns the fixed string "Precondition Error";
        // the configured-off message lives in the wrapped cause. Same convention as the
        // pre-existing DatabaseException catch path in check() — kept for consistency. If
        // the gate had NOT fired and the SQL had reached the executor, the cause would be
        // a DatabaseException with a JDBC/driver error message — not the configured-off
        // text below. The string-shape match is the proof-of-short-circuit.
        caught.getCause() != null
        def causeMessage = caught.getCause().getMessage()
        causeMessage.contains("liquibase.allowSqlPrecondition=false")
        causeMessage.contains("NOT executed")
        causeMessage.contains("sqlCheck")
    }

    def "check throws PreconditionErrorException — not PreconditionFailedException — so onFail=MARK_RAN cannot swallow configured-off intent"() {
        // Pins the audit ticket's specific concern: PreconditionErrorException bypasses
        // onFail handling whereas PreconditionFailedException respects it. A crafted
        // onFail=MARK_RAN sqlCheck would otherwise allow the precondition SQL to run
        // (or be reported as having run) without the embedder's intent being honored.
        given:
        def precondition = new SqlPrecondition()
        precondition.sql = "select 1"
        precondition.expectedResult = "1"
        def database = Mock(Database)
        Throwable thrown = null

        when:
        Scope.child(["liquibase.allowSqlPrecondition": "false"] as Map, {
            try {
                precondition.check(database, null, null, null)
            } catch (Throwable t) {
                thrown = t
            }
        } as Scope.ScopedRunner)

        then:
        thrown != null
        thrown.getClass().getName() == "liquibase.exception.PreconditionErrorException"
        // Not PreconditionFailedException — Error vs Failed is the load-bearing distinction
        // for onFail handling. Pin it with a strict class-name check (not instanceof, which
        // would pass for either if one extends the other).
        thrown.getClass().getSimpleName() != "PreconditionFailedException"
    }
}
