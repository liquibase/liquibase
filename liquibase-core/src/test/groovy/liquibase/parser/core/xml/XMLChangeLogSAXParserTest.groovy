package liquibase.parser.core.xml

import liquibase.Contexts
import liquibase.GlobalConfiguration
import liquibase.LabelExpression
import liquibase.RuntimeEnvironment
import liquibase.Scope
import liquibase.changelog.ChangeLogIterator
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.changelog.filter.ChangeSetFilterResult
import liquibase.changelog.visitor.ChangeSetVisitor
import liquibase.database.Database
import liquibase.database.core.MockDatabase
import liquibase.exception.ChangeLogParseException
import liquibase.exception.LiquibaseException
import liquibase.sdk.resource.MockResourceAccessor
import liquibase.test.JUnitResourceAccessor
import spock.lang.Specification

class XMLChangeLogSAXParserTest extends Specification {

    def INSECURE_XML = """
<!DOCTYPE databaseChangeLog [
        <!ENTITY insecure SYSTEM "https://localhost/insecure">
        ]>

<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.6.xsd">

    <changeSet id="1" author="example">
        <output>&insecure;</output>
    </changeSet>

</databaseChangeLog>
"""

    def testIgnoreDuplicateChangeSets() throws ChangeLogParseException, Exception {
        when:
        def xmlParser = new XMLChangeLogSAXParser()
        def changeLog = xmlParser.parse("liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/master.changelog.xml",
                new ChangeLogParameters(), new JUnitResourceAccessor())

        final List<ChangeSet> changeSets = new ArrayList<ChangeSet>()

        new ChangeLogIterator(changeLog).run(new ChangeSetVisitor() {
            @Override
            ChangeSetVisitor.Direction getDirection() {
                return ChangeSetVisitor.Direction.FORWARD
            }

            @Override
            void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
                changeSets.add(changeSet)
            }
        }, new RuntimeEnvironment(new MockDatabase(), new Contexts(), new LabelExpression()))


        then:
        changeSets.size() == 8
        changeSets.get(0).toString() == "liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog4.xml::1::testuser"
        changeSets.get(1).toString() == "liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog4.xml::1::testuser"
        changeSets.get(1).getContexts().getContexts().size() == 1
        changeSets.get(2).toString() == "liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog4.xml::1::testuser"
        changeSets.get(2).getLabels().getLabels().size() == 1
        changeSets.get(3).toString() == "liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog4.xml::1::testuser"
        changeSets.get(3).getLabels().getLabels().size() == 2
        changeSets.get(4).toString() == "liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog4.xml::1::testuser"
        changeSets.get(4).getDbmsSet().size() == 1
        changeSets.get(5).toString() == "liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog1.xml::1::testuser"
        changeSets.get(6).toString() == "liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog3.xml::1::testuser"
        changeSets.get(7).toString() == "liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog2.xml::1::testuser"
    }

    def "uses liquibase.secureParsing by default"() {
        when:
        def resourceAccessor = new MockResourceAccessor(["com/example/insecure.xml": INSECURE_XML])

        new XMLChangeLogSAXParser().parse("com/example/insecure.xml", new ChangeLogParameters(), resourceAccessor)

        then:
        def e = thrown(ChangeLogParseException)
        e.message.contains("Failed to read external document 'insecure'")
    }

    def "allows liquibase.secureParsing=false to disable secure parsing"() {
        when:
        def resourceAccessor = new MockResourceAccessor(["com/example/insecure.xml": INSECURE_XML])

        Scope.child(GlobalConfiguration.SECURE_PARSING.key, "false", { ->
            new XMLChangeLogSAXParser().parse("com/example/insecure.xml", new ChangeLogParameters(), resourceAccessor)
        })


        then:
        def e = thrown(ChangeLogParseException)
        e.message.contains("Connection refused")
    }

}
