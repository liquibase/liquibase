package liquibase.change.core

import liquibase.parser.core.ParsedNode
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
        change.load(new ParsedNode(null, "executeCommand")
                .addChildren([executable:"/usr/bin/test", os:"linux,mac"])
                .addChild(new ParsedNode(null, "arg").addChild(null, "value", "-out"))
                .addChild(new ParsedNode(null, "arg").addChild(null, "value", "-test"))
        , resourceSupplier.simpleResourceAccessor)

        then:
        change.executable == "/usr/bin/test"
        that change.getOs(), Matchers.contains(["linux", "mac"].toArray())
        that change.args, Matchers.contains("-out", "-test")
    }
}
