package liquibase.change.core

import liquibase.exception.SetupException
import liquibase.executor.ExecutorService
import liquibase.parser.core.ParsedNodeException
import liquibase.sdk.database.MockDatabase
import liquibase.sdk.executor.MockExecutor
import liquibase.sdk.supplier.resource.ResourceSupplier
import liquibase.statement.SqlStatement
import org.hamcrest.Matchers
import spock.lang.Shared
import spock.lang.Specification

import static spock.util.matcher.HamcrestSupport.that

class ExecuteShellCommandChangeTest extends Specification {

    @Shared resourceSupplier = new ResourceSupplier()

    def "load works correctly"() {
        when:
        def change = new ExecuteShellCommandChange()

        then:
        null == change.getOs()

        when:
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
        that change.getOsList(), Matchers.contains(["linux", "mac"].toArray())
        that change.getStringArgs(), Matchers.contains("-out", "-test")
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
        change.os == "linux,mac"
        change.args[0].value == "-out"
        change.args[1].value == "-test"
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
        change.getOsList() == ["linux", "mac"]
        that change.getStringArgs(), Matchers.contains("-out", "-test")
    }

    def "test execution"() {
        when:
        String currentOS = System.getProperty("os.name");
        def change = new ExecuteShellCommandChange()
        change.load(new liquibase.parser.core.ParsedNode(null, "executeCommand")
                .addChildren([executable: "test", os: "linux,mac", timeout:"10s"])
                .addChild(new liquibase.parser.core.ParsedNode(null, "args")
                        .addChild(new liquibase.parser.core.ParsedNode(null, "arg").addChild(null, "value", "-out"))
                        .addChild(new liquibase.parser.core.ParsedNode(null, "arg").addChild(null, "value", "-test"))
                ), resourceSupplier.simpleResourceAccessor)
        def db = new MockDatabase();
        ExecutorService.getInstance().setExecutor(db, new MockExecutor())
        SqlStatement[] stmts = change.generateStatements(db)

        then:
        stmts.size() == 1
        stmts[0].toString() == "test -out -test"
    }
}
