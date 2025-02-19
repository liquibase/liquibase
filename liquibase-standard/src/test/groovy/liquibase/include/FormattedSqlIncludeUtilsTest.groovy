package liquibase.include

import liquibase.changelog.DatabaseChangeLog
import liquibase.exception.ChangeLogParseException
import liquibase.precondition.core.PreconditionContainer
import liquibase.precondition.core.SqlPrecondition
import liquibase.precondition.core.TableExistsPrecondition
import liquibase.precondition.core.ViewExistsPrecondition
import liquibase.resource.SearchPathResourceAccessor
import spock.lang.Specification

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.containsInAnyOrder
import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs
import static spock.util.matcher.HamcrestSupport.that

class FormattedSqlIncludeUtilsTest extends Specification {

    def "from string #line1 to include"() {
        when:
        def pattern = FormattedSqlIncludeUtils.INCLUDE_PATTERN.matcher(line1)
        pattern.find()
        def matchResult = pattern.toMatchResult()
        def include = FormattedSqlIncludeUtils.handleInclude(line1, new SearchPathResourceAccessor("target/test-classes"),
                new DatabaseChangeLog(){
                    @Override
                    String getPhysicalFilePath(){
                        return "liquibase/empty.changelog.xml";
                    }
                }, matchResult)

        def actualTableExistsPreconditions = include.preconditions.nestedPreconditions
                .findAll { p -> p.class == TableExistsPrecondition.class }

        def actualViewExistsPreconditions = include.preconditions.nestedPreconditions
                .findAll { p -> p.class == ViewExistsPrecondition.class }

        def actualViewPrecondition = (ViewExistsPrecondition) actualViewExistsPreconditions[0]

        def actualSqlPreconditions = include.preconditions.nestedPreconditions
                .findAll { p -> p.class == SqlPrecondition.class }

        def expectedTableExistsPrecondition1 = new TableExistsPrecondition()
        expectedTableExistsPrecondition1.tableName = "table1"
        expectedTableExistsPrecondition1.schemaName = "schema1"

        def expectedTableExistsPrecondition2 = new TableExistsPrecondition()
        expectedTableExistsPrecondition2.tableName = "table2"
        expectedTableExistsPrecondition2.schemaName = "schema2"
        expectedTableExistsPrecondition2.catalogName = "catalog2"

        def expectedViewExistsPrecondition = new ViewExistsPrecondition()
        expectedViewExistsPrecondition.catalogName= "viewCatalog"
        expectedViewExistsPrecondition.viewName = "view1"
        expectedViewExistsPrecondition.schemaName = "viewSchema"

        def expectedSqlPrecondition1 = new SqlPrecondition()
        expectedSqlPrecondition1.expectedResult = 2
        expectedSqlPrecondition1.sql = "\"SELECT COUNT(*) FROM 'example_table2'\""

        def expectedSqlPrecondition2 = new SqlPrecondition()
        expectedSqlPrecondition2.expectedResult = 0
        expectedSqlPrecondition2.sql = "\"SELECT COUNT(*) FROM example_table\""

        def expectedSqlPrecondition3 = new SqlPrecondition()
        expectedSqlPrecondition3.expectedResult = 3
        expectedSqlPrecondition3.sql = "SELECT COUNT(*) FROM example_table3"

        then:
        noExceptionThrown()
        include.file == "test-changelog.xml"
        include.preconditions.onError == PreconditionContainer.ErrorOption.WARN
        include.labels.labels.size() == 3
        include.labels.labels.count("l1") == 1
        include.labels.labels.count("l2") == 1
        include.labels.labels.count("l3") == 1
        include.relativeToChangelogFile == true
        include.errorIfMissing == true
        include.ignore == true
        include.context.contexts.size() == 3
        include.context.contexts.count("c1") == 1
        include.context.contexts.count("c2") == 1
        include.context.contexts.count("c3") == 1
        include.logicalFilePath == "logicalFilePath"

        include.preconditions.nestedPreconditions.size() == 6
        actualViewExistsPreconditions.size() == 1
        actualTableExistsPreconditions.size() == 2
        actualSqlPreconditions.size() == 3

        expect:
        that actualViewPrecondition, samePropertyValuesAs(expectedViewExistsPrecondition)

        assertThat(actualTableExistsPreconditions, containsInAnyOrder(
                samePropertyValuesAs(expectedTableExistsPrecondition1),
                samePropertyValuesAs(expectedTableExistsPrecondition2)))

        assertThat(actualSqlPreconditions, containsInAnyOrder(
                samePropertyValuesAs(expectedSqlPrecondition1),
                samePropertyValuesAs(expectedSqlPrecondition2),
                samePropertyValuesAs(expectedSqlPrecondition3)))

        where:
        line1 | _
        '--include  preconditions   onError:WARN labels:l1,l2,l3 file:test-changelog.xml precondition-sql-check "SELECT COUNT(*) FROM \'example_table2\'" expectedResult:2 relativeToChangelogFile:true  errorIfMissing:true precondition-sql-check expectedResult:0 "SELECT COUNT(*) FROM example_table"  precondition-sql-check SELECT COUNT(*) FROM example_table3 expectedResult:3 ignore:true precondition-table-exists table:table1 schema:schema1 precondition-view-exists catalog:viewCatalog view:view1 schema:viewSchema context:c1,c2,c3 logicalFilePath:logicalFilePath precondition-table-exists catalog:catalog2 table:table2 schema:schema2' | _


    }

    def "from string #line2 to include"() {
        when:
        def pattern = FormattedSqlIncludeUtils.INCLUDE_PATTERN.matcher(line2)
        pattern.find()
        def matchResult = pattern.toMatchResult()
        def include = FormattedSqlIncludeUtils.handleInclude(line2, new SearchPathResourceAccessor("target/test-classes"),
                new DatabaseChangeLog(){
                    @Override
                    String getPhysicalFilePath(){
                        return "liquibase/empty.changelog.xml";
                    }
                }, matchResult)

        def actualTableExistsPrecondition = (TableExistsPrecondition) include.preconditions.nestedPreconditions
                .find { p -> p.class == TableExistsPrecondition.class }

        def actualViewExistsPrecondition = (ViewExistsPrecondition) include.preconditions.nestedPreconditions
                .find { p -> p.class == ViewExistsPrecondition.class }

        def actualSqlPrecondition = (SqlPrecondition) include.preconditions.nestedPreconditions
                .find { p -> p.class == SqlPrecondition.class }

        def expectedTableExistsPrecondition = new TableExistsPrecondition()
        expectedTableExistsPrecondition.tableName = "table_1"
        expectedTableExistsPrecondition.schemaName = "schema_1"


        def expectedViewExistsPrecondition = new ViewExistsPrecondition()
        expectedViewExistsPrecondition.catalogName= "viewCatalog"
        expectedViewExistsPrecondition.viewName = "view_1"
        expectedViewExistsPrecondition.schemaName = "viewSchema"


        def expectedSqlPrecondition = new SqlPrecondition()
        expectedSqlPrecondition.expectedResult = 35
        expectedSqlPrecondition.sql = "SELECT COUNT(*) FROM example_table"

        then:
        noExceptionThrown()
        include.file == "test-changelog.xml"
        include.preconditions.onError == PreconditionContainer.ErrorOption.HALT
        include.preconditions.onFail == PreconditionContainer.FailOption.HALT
        include.labels.labels.size() == 1
        include.labels.labels.count("l1") == 1
        include.relativeToChangelogFile == false
        include.errorIfMissing == false
        include.ignore == false
        include.context.contexts.size() == 3
        include.context.contexts.count("cf1") == 1
        include.context.contexts.count("cf2") == 1
        include.context.contexts.count("cf3") == 1
        include.logicalFilePath == "logicalPath"

        include.preconditions.nestedPreconditions.size() == 3

        expect:
        that actualViewExistsPrecondition, samePropertyValuesAs(expectedViewExistsPrecondition)
        that actualTableExistsPrecondition, samePropertyValuesAs(expectedTableExistsPrecondition)
        that actualSqlPrecondition, samePropertyValuesAs(expectedSqlPrecondition)

        where:
        line2 | _
        '--include   labels:l1      file:test-changelog.xml relativeToChangelogFile:false  errorIfMissing:false   precondition-sql-check SELECT COUNT(*) FROM example_table expectedResult:35 ignore:false   contextFilter:cf1,cf2,cf3 logicalFilePath:logicalPath precondition-table-exists table:table_1 schema:schema_1     precondition-view-exists   catalog:viewCatalog view:view_1 schema:viewSchema' | _

    }

    def "from string #line3 to include"() {
        when:
        def pattern = FormattedSqlIncludeUtils.INCLUDE_PATTERN.matcher(line3)
        pattern.find()
        def matchResult = pattern.toMatchResult()
        def include = FormattedSqlIncludeUtils.handleInclude(line3, new SearchPathResourceAccessor("target/test-classes"),
                new DatabaseChangeLog(){
                    @Override
                    String getPhysicalFilePath(){
                        return "liquibase/empty.changelog.xml";
                    }
                }, matchResult)

        then:
        noExceptionThrown()
        include.file == "test-changelog.xml"
        include.preconditions == null
        include.labels.labels.empty
        include.relativeToChangelogFile == true
        include.errorIfMissing == true
        include.context.contexts.empty
        include.logicalFilePath == "logicalPath"

        where:
        line3 | _
        '   --   include         file:test-changelog.xml relativeToChangelogFile:true  errorIfMissing:true     logicalFilePath:logicalPath    ' | _

    }

    def "from string #line4 to include"() {
        when:
        def pattern = FormattedSqlIncludeUtils.INCLUDE_PATTERN.matcher(line4)
        pattern.find()
        def matchResult = pattern.toMatchResult()
        FormattedSqlIncludeUtils.handleInclude(line4, new SearchPathResourceAccessor("target/test-classes"),
                new DatabaseChangeLog(){
                    @Override
                    String getPhysicalFilePath(){
                        return "liquibase/empty.changelog.xml";
                    }
                }, matchResult)

        then:
        def clpe = thrown(ChangeLogParseException.class)
        clpe.message == "malformed liquibase formatted sql: 'relativeToChangelogFile:true  errorIfMissing:true     logicalFilePath:logicalPath' no element matching the following regex '\\s+file:\\S+\\b(.xml|.json|.yml|.yaml|.sql)\\b' found"

        where:
        line4 | _
        '   --   include          relativeToChangelogFile:true  errorIfMissing:true     logicalFilePath:logicalPath    ' | _
    }

    def "from string #line5 to include"() {
        when:
        def pattern = FormattedSqlIncludeUtils.INCLUDE_PATTERN.matcher(line5)
        pattern.find()
        def matchResult = pattern.toMatchResult()
        FormattedSqlIncludeUtils.handleInclude(line5, new SearchPathResourceAccessor("target/test-classes"),
                new DatabaseChangeLog(){
                    @Override
                    String getPhysicalFilePath(){
                        return "liquibase/empty.changelog.xml";
                    }
                }, matchResult)

        then:
        def clpe = thrown(ChangeLogParseException.class)
        clpe.message == "error occurred while parsing liquibase formatted sql '--include relativeToChangelogFile:true errorIfMissing:true file:test-changelog.xml logicalFilePath:logicalPath  testException' cannot map this part 'testException'"

        where:
        line5 | _
        '--include relativeToChangelogFile:true errorIfMissing:true file:test-changelog.xml logicalFilePath:logicalPath  testException' | _

    }
}
