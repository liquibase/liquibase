package liquibase.parser.core.xml

import liquibase.change.core.RawSQLChange
import liquibase.changelog.ChangeLogParameters
import liquibase.exception.ChangeLogParseException
import liquibase.parser.core.ParsedNode
import liquibase.resource.ResourceAccessor
import liquibase.sdk.supplier.resource.ResourceSupplier
import spock.lang.Shared
import spock.lang.Specification

class AbstractChangeLogParserTest extends Specification {

    @Shared resourceSupplier = new ResourceSupplier()

    def "null node creates null changelog object"() {
        when:
        def changeLog = createParser(null).parse("com/example/changelog.xml", new ChangeLogParameters(), resourceSupplier.simpleResourceAccessor)
        then:
        changeLog == null
    }

    def "empty node creates empty changelog object"() {
        when:
        def changeLogNode = new ParsedNode(null, "databaseChangeLog")
        def changeLog = createParser(changeLogNode).parse("com/example/changelog.xml", new ChangeLogParameters(), resourceSupplier.simpleResourceAccessor)

        then:
        changeLog.physicalFilePath == "com/example/changelog.xml"
        changeLog.changeSets.size() == 0
        changeLog.preconditions.nestedPreconditions.size() == 0
    }

    def "changeLog with sql nodes parse correctly"() {
        when:
        def changeLogNode = new ParsedNode(null, "databaseChangeLog")
        changeLogNode.addChildren([changeSet: [id: "1", author: "nvoxland"]]).children[0].value = [sql: "select * from x"]
        changeLogNode.addChildren([changeSet: [id: "2", author: "nvoxland"]]).children[1].value = [sql: "select * from y"]
        changeLogNode.addChildren([changeSet: [id: "3", author: "nvoxland"]]).children[2].value = [sql: "select * from z"]

        def changeLog = createParser(changeLogNode).parse("com/example/changelog.xml", new ChangeLogParameters(), resourceSupplier.simpleResourceAccessor)

        then:
        changeLog.preconditions.nestedPreconditions.size() == 0
        changeLog.changeSets.size() == 3
        changeLog.changeSets[0].toString(false) == "com/example/changelog.xml::1::nvoxland"
        changeLog.changeSets[1].toString(false) == "com/example/changelog.xml::2::nvoxland"
        changeLog.changeSets[2].toString(false) == "com/example/changelog.xml::3::nvoxland"

        changeLog.changeSets[0].changes.size() == 1
        ((RawSQLChange) changeLog.changeSets[0].changes[0]).sql == "select * from x"

        changeLog.changeSets[1].changes.size() == 1
        ((RawSQLChange) changeLog.changeSets[1].changes[0]).sql == "select * from y"

        changeLog.changeSets[2].changes.size() == 1
        ((RawSQLChange) changeLog.changeSets[2].changes[0]).sql == "select * from z"
    }


    protected AbstractChangeLogParser createParser(changeLogNode) {
         return new AbstractChangeLogParser() {
            @Override
            protected ParsedNode parseToNode(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
                return changeLogNode
            }

            @Override
            boolean supports(String changeLogFile, ResourceAccessor resourceAccessor) {
                return false
            }

            @Override
            int getPriority() {
                return 0
            }
        }
    }
}
