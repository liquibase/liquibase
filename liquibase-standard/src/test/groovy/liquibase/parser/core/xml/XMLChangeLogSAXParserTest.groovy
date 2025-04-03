package liquibase.parser.core.xml

import liquibase.*
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
import liquibase.util.LiquibaseUtil
import spock.lang.Specification
import spock.lang.Unroll

class XMLChangeLogSAXParserTest extends Specification {

    def INSECURE_XML = """
<!DOCTYPE databaseChangeLog [
        <!ENTITY insecure SYSTEM "file:///invalid.txt">
        ]>

<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">

    <changeSet id="1" author="example">
        <output>&insecure;</output>
    </changeSet>

</databaseChangeLog>
"""

    def INVALID_XML = """
<!DOCTYPE databaseChangeLog [
        <!ENTITY insecure SYSTEM "file:///invalid.txt">
        ]>

<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">

    <iDontKnowWhatImDoing />

</databaseChangeLog>
"""

    @Unroll
    def testAllProvidedChangesetsAreLoaded() throws ChangeLogParseException, Exception {
        given:
        def xmlParser = new XMLChangeLogSAXParser()
        final def changeLog = xmlParser.parse("liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/master.changelog.xml",
                new ChangeLogParameters(), new JUnitResourceAccessor())

        when:
        final def changeSets = new ArrayList<ChangeSet>()
        Scope.child(GlobalConfiguration.ALLOW_DUPLICATED_CHANGESETS_IDENTIFIERS.key, allowDuplicatedChangesetIdentifiers, { ->
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
        })


        then:
        changeSets.size() == totalSize
        changeSets.get(0).toString() == "liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog4.xml::1::testuser"
        changeSets.get(1).toString() == "liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog4.xml::1::testuser"
        changeSets.get(1).getContextFilter().getContexts().size() == 1
        changeSets.get(2).toString() == "liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog4.xml::1::testuser"
        changeSets.get(2).getLabels().getLabels().size() == 1
        changeSets.get(3).toString() == "liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog4.xml::1::testuser"
        changeSets.get(3).getLabels().getLabels().size() == 2
        changeSets.get(4).toString() == "liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog4.xml::1::testuser"
        changeSets.get(4).getDbmsSet().size() == 1
        changeSets.get(5).toString() == "liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog1.xml::1::testuser"

        if (!allowDuplicatedChangesetIdentifiers) {
            changeSets.get(6).toString() == "liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog4.xml::1::testuser"
            changeSets.get(7).toString() == "liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog4.xml::1::testuser"
            changeSets.get(13).toString() == "liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog3.xml::1::testuser"
        } else {
            changeSets.get(6).toString() == "liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog3.xml::1::testuser"
            changeSets.get(7).toString() == "liquibase/parser/core/xml/ignoreDuplicatedChangeLogs/included.changelog2.xml::1::testuser"
        }


        where:
        allowDuplicatedChangesetIdentifiers | totalSize
        false                               | 14
        true                                | 8



    }

    def "uses liquibase.secureParsing by default"() {
        when:
        def resourceAccessor = new MockResourceAccessor(["com/example/insecure.xml": INSECURE_XML])

        new XMLChangeLogSAXParser().parse("com/example/insecure.xml", new ChangeLogParameters(), resourceAccessor)

        then:
        def e = thrown(ChangeLogParseException)
        e.message.contains("Unable to resolve xml entity file:///invalid.txt. liquibase.secureParsing is set to 'true'")
    }

    def "allows liquibase.secureParsing=false to disable secure parsing"() {
        when:
        def resourceAccessor = new MockResourceAccessor(["com/example/insecure.xml": INSECURE_XML])

        Scope.child(GlobalConfiguration.SECURE_PARSING.key, "false", { ->
            new XMLChangeLogSAXParser().parse("com/example/insecure.xml", new ChangeLogParameters(), resourceAccessor)
        })


        then:
        def e = thrown(ChangeLogParseException)
        e.message.contains("Error Reading Changelog File: " + File.separator + "invalid.txt")
    }

    def "by default validate XML file based on XSD files"() {
        given:
        def file = "com/example/invalid.xml"

        when:
        def resourceAccessor = new MockResourceAccessor(["com/example/invalid.xml": INVALID_XML])
        new XMLChangeLogSAXParser().parse(file, new ChangeLogParameters(), resourceAccessor)

        then:
        def e = thrown(ChangeLogParseException)
        e.message.contains("Error parsing line")
        e.message.contains("iDontKnowWhatImDoing")

    }


    def "setting validation flag to false will cause the XML to not be validated"() {
        given:
        def file = "com/example/invalid.xml"

        when:
        def resourceAccessor = new MockResourceAccessor(["com/example/invalid.xml": INVALID_XML])

        then:
        Scope.child(GlobalConfiguration.VALIDATE_XML_CHANGELOG_FILES.key, "false", { ->
            def d = new XMLChangeLogSAXParser().parse(file, new ChangeLogParameters(), resourceAccessor)
            assert d.physicalFilePath == file
            assert d.getChangeSets().isEmpty()
        })

    }

    def "getSchemaVersion"() {
        expect:
        XMLChangeLogSAXParser.getSchemaVersion() == "latest" //because test run in an environment with build.version == DEV
    }

    @Unroll
    def "computeSchemaVersion"() {
        expect:
        XMLChangeLogSAXParser.computeSchemaVersion(buildVersion) == expected

        where:
        buildVersion              | expected
        LiquibaseUtil.DEV_VERSION | "latest"
        "4.11.0"                  | "4.11"
        "4.11.1"                  | "4.11"
        "4"                       | "latest" //weird versions go to latest
        ""                        | "latest" //weird versions go to latest
        null                      | "latest" //weird versions go to latest
    }

}
