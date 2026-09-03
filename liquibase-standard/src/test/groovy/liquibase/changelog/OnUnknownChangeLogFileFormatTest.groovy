package liquibase.changelog

import liquibase.ContextExpression
import liquibase.Labels
import liquibase.Scope
import liquibase.exception.UnknownChangelogFormatException
import liquibase.logging.core.BufferedLogService
import liquibase.parser.ChangeLogParserConfiguration
import liquibase.parser.core.ParsedNode
import liquibase.sdk.resource.MockResourceAccessor
import spock.lang.Specification

import java.util.logging.Level

/**
 * Tests for OnUnknownFileFormat handling in include and includeAll operations.
 * Verifies that unrecognized file extensions are handled consistently across both operations.
 */
class OnUnknownChangeLogFileFormatTest extends Specification {

    def validXml = '''<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.4.xsd">

    <changeSet id="1" author="test">
        <createTable tableName="person">
            <column name="id" type="int">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="firstname" type="varchar(50)"/>
        </createTable>
    </changeSet>
</databaseChangeLog>'''

    def validSql = '''-- valid sql
create table test_table (id int);'''

    def "include with OnUnknownFileFormat FAIL throws exception for unrecognized extension"() {
        when:
        def resourceAccessor = new MockResourceAccessor([
                "com/example/file.unknown_ext": validXml,
        ])
        def onUnknownFileFormat = DatabaseChangeLog.OnUnknownFileFormat.FAIL
        def rootChangeLog = new DatabaseChangeLog("com/example/root.xml")
        rootChangeLog.include("com/example/file.unknown_ext", false, true, resourceAccessor,
                new ContextExpression("context1"), new Labels("label1"), false, null,
                onUnknownFileFormat, new ModifyChangeSets(null, null))

        then:
        def e = thrown(UnknownChangelogFormatException)
        assert e.getMessage().startsWith("Cannot find parser that supports com/example/file.unknown_ext")
    }

    def "include with OnUnknownFileFormat WARN logs warning for unrecognized extension"() {
        when:
        def resourceAccessor = new MockResourceAccessor([
                "com/example/file.unknown_ext": validXml,
        ])
        def onUnknownFileFormat = DatabaseChangeLog.OnUnknownFileFormat.WARN

        BufferedLogService bufferLog = new BufferedLogService()
        def rootChangeLog = new DatabaseChangeLog("com/example/root.xml")

        Scope.child([
                (Scope.Attr.logService.name()): bufferLog,
                (ChangeLogParserConfiguration.ON_MISSING_INCLUDE_CHANGELOG.getKey()): ChangeLogParserConfiguration.MissingIncludeConfiguration.WARN,
        ], new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                rootChangeLog.include("com/example/file.unknown_ext", false, true, resourceAccessor,
                        new ContextExpression("context1"), new Labels("label1"), false, null,
                        onUnknownFileFormat, new ModifyChangeSets(null, null))
            }
        })

        def changeSets = rootChangeLog.changeSets

        then:
        changeSets.isEmpty() == true
        bufferLog.getLogAsString(Level.WARNING).contains("included file com/example/file.unknown_ext is not a recognized file type")
    }

    def "include with OnUnknownFileFormat SKIP silently ignores unrecognized extension"() {
        when:
        def resourceAccessor = new MockResourceAccessor([
                "com/example/file.unknown_ext": validXml,
        ])
        def onUnknownFileFormat = DatabaseChangeLog.OnUnknownFileFormat.SKIP
        def rootChangeLog = new DatabaseChangeLog("com/example/root.xml")
        rootChangeLog.include("com/example/file.unknown_ext", false, true, resourceAccessor,
                new ContextExpression("context1"), new Labels("label1"), false, null,
                onUnknownFileFormat, new ModifyChangeSets(null, null))

        def changeSets = rootChangeLog.changeSets

        then:
        changeSets.isEmpty() == true
    }

    def "includeAll with unrecognized extension logs warning and processes valid files"() {
        when:
        def resourceAccessor = new MockResourceAccessor([
                "com/example/children/file1.xml": validXml,
                "com/example/children/file2.unknown_ext": validXml,
                "com/example/children/file3.xml": validXml,
        ])

        BufferedLogService bufferLog = new BufferedLogService()
        def changeLogFile = new DatabaseChangeLog("com/example/root.xml")

        Scope.child([
                (Scope.Attr.logService.name()): bufferLog,
                (ChangeLogParserConfiguration.ON_MISSING_INCLUDE_CHANGELOG.getKey()): ChangeLogParserConfiguration.MissingIncludeConfiguration.WARN,
        ], new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                changeLogFile.includeAll("com/example/children", false, null, true,
                        changeLogFile.getStandardChangeLogComparator(), resourceAccessor,
                        new ContextExpression(), new Labels(), false, null, 0, Integer.MAX_VALUE)
            }
        })

        def changeSets = changeLogFile.changeSets
        def warnings = bufferLog.getLogAsString(Level.WARNING)

        then:
        changeSets.size() == 2
        warnings.contains("included file com/example/children/file2.unknown_ext is not a recognized file type")
    }

    def "include via load with unrecognized extension logs warning and continues"() {
        given:
        def resourceAccessor = new MockResourceAccessor([
                "changelog.xml": validXml,
                "file.unknown_ext": validXml
        ])

        BufferedLogService bufferLog = new BufferedLogService()
        def rootChangeLog = new DatabaseChangeLog("changelog.xml")

        when:
        Scope.child([
                (Scope.Attr.logService.name()): bufferLog,
                (ChangeLogParserConfiguration.ON_MISSING_INCLUDE_CHANGELOG.getKey()): ChangeLogParserConfiguration.MissingIncludeConfiguration.WARN,
        ], new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                rootChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                        .addChildren([include: [file: "file.unknown_ext", relativeToChangelogFile: false]])
                        , resourceAccessor)
            }
        })

        def changeSets = rootChangeLog.changeSets
        def warnings = bufferLog.getLogAsString(Level.WARNING)

        then:
        changeSets.isEmpty() == true
        warnings.contains("included file file.unknown_ext is not a recognized file type")
    }

    def "combined includeAll and include with unrecognized extensions"() {
        given:
        def resourceAccessor = new MockResourceAccessor([
                "changelog.xml": validXml,
                "standalone.unknown_ext": validXml,
                "standalone.valid.sql": validSql,
                "changes/script.sql": validSql,
                "changes/change.xml": validXml,
                "changes/.gitignore": "*.class",
                "changes/file.unknown_ext": "some text"
        ])

        BufferedLogService bufferLog = new BufferedLogService()
        def rootChangeLog = new DatabaseChangeLog("changelog.xml")

        when:
        Scope.child([
                (Scope.Attr.logService.name()): bufferLog,
                (ChangeLogParserConfiguration.ON_MISSING_INCLUDE_CHANGELOG.getKey()): ChangeLogParserConfiguration.MissingIncludeConfiguration.WARN,
        ], new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                rootChangeLog.load(new ParsedNode(null, "databaseChangeLog")
                        .addChildren([includeAll: [path: "changes", relativeToChangelogFile: true]])
                        .addChildren([include: [file: "standalone.unknown_ext", relativeToChangelogFile: true]])
                        .addChildren([include: [file: "standalone.valid.sql", relativeToChangelogFile: true]])
                        , resourceAccessor)
            }
        })

        def changeSets = rootChangeLog.changeSets
        def warnings = bufferLog.getLogAsString(Level.WARNING)

        then:
        changeSets.size() == 3
        warnings.contains("included file changes/file.unknown_ext is not a recognized file type")
        warnings.contains("included file standalone.unknown_ext is not a recognized file type")
    }
}
