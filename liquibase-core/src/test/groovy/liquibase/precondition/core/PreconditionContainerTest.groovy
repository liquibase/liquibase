package liquibase.precondition.core

import liquibase.exception.SetupException
import liquibase.parser.core.ParsedNode
import liquibase.parser.core.ParsedNodeException
import liquibase.sdk.supplier.resource.ResourceSupplier
import spock.lang.Shared
import spock.lang.Specification

class PreconditionContainerTest extends Specification {

    @Shared resourceSupplier = new ResourceSupplier()

    def "load handles empty node with params"() {
        when:
        def node = new ParsedNode(null, "preConditions").addChildren([onFail: "MARK_RAN", onError: "CONTINUE", onSqlOutput: "IGNORE", onFailMessage: "I Failed", onErrorMessage: "I Errored"])
        def container = new PreconditionContainer()
        try {
            container.load(node, resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        } catch (SetupException e) {
            e.printStackTrace()
        }

        then:
        container.onFail.toString() == "MARK_RAN"
        container.onError.toString() == "CONTINUE"
        container.onSqlOutput.toString() == "IGNORE"
        container.onFailMessage == "I Failed"
        container.onErrorMessage == "I Errored"
    }

    def "load handles node with single in value"() {
        when:
        def node = new ParsedNode(null, "preConditions").addChildren([onFail: "MARK_RAN"]).setValue(new ParsedNode(null, "tableExists").addChildren([tableName: "my_table"]))
        def container = new PreconditionContainer()
        try {
            container.load(node, resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        } catch (SetupException e) {
            e.printStackTrace()
        }

        then:
        container.onFail.toString() == "MARK_RAN"
        container.nestedPreconditions.size() == 1
        ((TableExistsPrecondition) container.nestedPreconditions[0]).tableName == "my_table"

    }

    def "load handles node with collection of preconditions in value"() {
        when:
        def node = new ParsedNode(null, "preConditions").addChildren([onFail: "MARK_RAN"]).setValue([
                new ParsedNode(null, "runningAs").addChildren([username: "my_user"]),
                new ParsedNode(null, "tableExists").addChildren([tableName: "my_table"]),
        ])
        def container = new PreconditionContainer()
        try {
            container.load(node, resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        } catch (SetupException e) {
            e.printStackTrace()
        }

        then:
        container.onFail.toString() == "MARK_RAN"
        container.nestedPreconditions.size() == 2
        ((RunningAsPrecondition) container.nestedPreconditions[0]).username == "my_user"
        ((TableExistsPrecondition) container.nestedPreconditions[1]).tableName == "my_table"

    }

    def "load handles node with preconditions as children"() {
        when:
        def node = new ParsedNode(null, "preConditions").addChildren([onFail: "MARK_RAN"])
                .addChild(new ParsedNode(null, "runningAs").addChildren([username: "my_user"]))
                .addChild(new ParsedNode(null, "tableExists").addChildren([tableName: "my_table"]))

        def container = new PreconditionContainer()
        try {
            container.load(node, resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        } catch (SetupException e) {
            e.printStackTrace()
        }

        then:
        container.onFail.toString() == "MARK_RAN"
        container.nestedPreconditions.size() == 2
        ((RunningAsPrecondition) container.nestedPreconditions[0]).username == "my_user"
        ((TableExistsPrecondition) container.nestedPreconditions[1]).tableName == "my_table"

    }

    def "load handles nested preconditions"() {
        when:
        def node = new ParsedNode(null, "preConditions").addChildren([onFail: "MARK_RAN"])
                .addChild(new ParsedNode(null, "runningAs").addChildren([username: "my_user"]))
                .addChild(new ParsedNode(null, "or")
                    .addChildren([runningAs: [username: "other_user"]])
                    .addChildren([runningAs: [username: "yet_other_user"]])
                )
                .addChild(new ParsedNode(null, "tableExists").addChildren([tableName: "my_table"]))

        def container = new PreconditionContainer()
        try {
            container.load(node, resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        } catch (SetupException e) {
            e.printStackTrace()
        }

        then:
        container.onFail.toString() == "MARK_RAN"
        container.nestedPreconditions.size() == 3
        ((RunningAsPrecondition) container.nestedPreconditions[0]).username == "my_user"

        ((OrPrecondition) container.nestedPreconditions[1]).nestedPreconditions.size() == 2
        ((RunningAsPrecondition) ((OrPrecondition) container.nestedPreconditions[1]).nestedPreconditions[0]).username == "other_user"
        ((RunningAsPrecondition) ((OrPrecondition) container.nestedPreconditions[1]).nestedPreconditions[1]).username == "yet_other_user"

        ((TableExistsPrecondition) container.nestedPreconditions[2]).tableName == "my_table"

    }

}
