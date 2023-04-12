package liquibase.parser.core.sql

import liquibase.change.core.RawSQLChange
import liquibase.sdk.resource.MockResourceAccessor
import liquibase.serializer.core.yaml.YamlChangeLogSerializer
import net.snowflake.client.jdbc.internal.org.jsoup.internal.StringUtil
import spock.lang.Specification
import spock.lang.Unroll

class SqlChangeLogParserTest extends Specification {

    def "Parse"() {
        when:
        def sql = "create table\n(id int)\nGO\nother statement;another statement"
        def resourceAccessor = new MockResourceAccessor(["test.sql": sql])

        def changelog = new SqlChangeLogParser().parse("test.sql", null, resourceAccessor)

        then:
        changelog.getChangeSets().size() == 1
        changelog.getChangeSets().get(0).getChanges().size() == 1
        ((RawSQLChange) changelog.getChangeSets().get(0).getChanges().get(0)).getSql() == sql
    }
}