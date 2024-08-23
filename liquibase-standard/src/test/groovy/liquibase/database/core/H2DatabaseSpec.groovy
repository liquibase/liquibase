package liquibase.database.core

import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.database.OfflineConnection
import liquibase.structure.core.Column
import liquibase.structure.core.Schema
import liquibase.structure.core.Table
import liquibase.test.JUnitResourceAccessor
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.database.ObjectQuotingStrategy.QUOTE_ALL_OBJECTS
import static liquibase.database.ObjectQuotingStrategy.QUOTE_ONLY_RESERVED_WORDS

class H2DatabaseSpec extends Specification {

    @Unroll("#featureName [#quotingStrategy], [#objectName, #objectType], [#expectedCorrect, #expectedEscape]")
    def "correctObjectName, escapeObjectName"() {
        given:
        def database = new H2Database().tap {
            if (quotingStrategy != null) {
                it.objectQuotingStrategy = quotingStrategy
            }
        }

        expect:
        verifyAll {
            database.correctObjectName(objectName, objectType) == expectedCorrect
            database.escapeObjectName(objectName, objectType) == expectedEscape
        }

        where:
        quotingStrategy           || objectName       | objectType || expectedCorrect  | expectedEscape
        null                      || 'col1'           | Column     || 'COL1'           | 'col1'
        null                      || 'COL1'           | Column     || 'COL1'           | 'COL1'
        null                      || 'Col1'           | Column     || 'COL1'           | 'Col1'
        null                      || 'col with space' | Column     || 'COL WITH SPACE' | '"COL WITH SPACE"'
        QUOTE_ONLY_RESERVED_WORDS || 'col1'           | Column     || 'COL1'           | 'col1'
        QUOTE_ONLY_RESERVED_WORDS || 'COL1'           | Column     || 'COL1'           | 'COL1'
        QUOTE_ONLY_RESERVED_WORDS || 'Col1'           | Column     || 'COL1'           | 'Col1'
        QUOTE_ONLY_RESERVED_WORDS || 'col with space' | Column     || 'COL WITH SPACE' | '"COL WITH SPACE"'
        QUOTE_ALL_OBJECTS         || 'col1'           | Column     || 'col1'           | '"col1"'
        QUOTE_ALL_OBJECTS         || 'COL1'           | Column     || 'COL1'           | '"COL1"'
        QUOTE_ALL_OBJECTS         || 'Col1'           | Column     || 'Col1'           | '"Col1"'
        QUOTE_ALL_OBJECTS         || 'col with space' | Column     || 'col with space' | '"col with space"'
    }

    @Unroll("#featureName [#h2Version, #quotingStrategy], [#columnName], [#expected]")
    def escapeColumnName() {
        given:
        def database = new H2Database().tap {
            it.connection = toOfflineConnection(h2Version)
            if (quotingStrategy != null) {
                it.objectQuotingStrategy = quotingStrategy
            }
        }

        expect:
        database.escapeColumnName(null, null, null, columnName) == expected

        where:
        h2Version | quotingStrategy           || columnName       || expected
        null      | null                      || 'col1'           || 'col1'
        null      | null                      || 'col with space' || '"COL WITH SPACE"'
        null      | null                      || 'value'          || '"VALUE"'
        null      | null                      || 'VALUE'          || '"VALUE"'
        null      | null                      || 'Value'          || '"VALUE"'
        "1.4.200" | null                      || 'col1'           || 'col1'
        "1.4.200" | null                      || 'col with space' || '"COL WITH SPACE"'
        "1.4.200" | null                      || 'value'          || 'value'
        "1.4.200" | null                      || 'VALUE'          || 'VALUE'
        "1.4.200" | null                      || 'Value'          || 'Value'
        null      | QUOTE_ONLY_RESERVED_WORDS || 'col1'           || 'col1'
        null      | QUOTE_ONLY_RESERVED_WORDS || 'col with space' || '"COL WITH SPACE"'
        null      | QUOTE_ONLY_RESERVED_WORDS || 'value'          || '"VALUE"'
        null      | QUOTE_ONLY_RESERVED_WORDS || 'VALUE'          || '"VALUE"'
        null      | QUOTE_ONLY_RESERVED_WORDS || 'Value'          || '"VALUE"'
        "1.4.200" | QUOTE_ONLY_RESERVED_WORDS || 'col1'           || 'col1'
        "1.4.200" | QUOTE_ONLY_RESERVED_WORDS || 'col with space' || '"COL WITH SPACE"'
        "1.4.200" | QUOTE_ONLY_RESERVED_WORDS || 'value'          || 'value'
        "1.4.200" | QUOTE_ONLY_RESERVED_WORDS || 'VALUE'          || 'VALUE'
        "1.4.200" | QUOTE_ONLY_RESERVED_WORDS || 'Value'          || 'Value'
        null      | QUOTE_ALL_OBJECTS         || 'col1'           || '"col1"'
        null      | QUOTE_ALL_OBJECTS         || 'col with space' || '"col with space"'
        null      | QUOTE_ALL_OBJECTS         || 'value'          || '"value"'
        null      | QUOTE_ALL_OBJECTS         || 'VALUE'          || '"VALUE"'
        null      | QUOTE_ALL_OBJECTS         || 'Value'          || '"Value"'
        "1.4.200" | QUOTE_ALL_OBJECTS         || 'col1'           || '"col1"'
        "1.4.200" | QUOTE_ALL_OBJECTS         || 'col with space' || '"col with space"'
        "1.4.200" | QUOTE_ALL_OBJECTS         || 'value'          || '"value"'
        "1.4.200" | QUOTE_ALL_OBJECTS         || 'VALUE'          || '"VALUE"'
        "1.4.200" | QUOTE_ALL_OBJECTS         || 'Value'          || '"Value"'
    }

    @Unroll("#featureName [#quotingStrategy, #preserveSchemaCase], [#objectName, #objectType], [#expected]")
    def escapeObjectName() {
        given:
        def database = new H2Database().tap {
            it.connection = toOfflineConnection()
            if (quotingStrategy != null) {
                it.objectQuotingStrategy = quotingStrategy
            }
        }
        def scopeValues = [:].tap {
            if (preserveSchemaCase != null) {
                it[GlobalConfiguration.PRESERVE_SCHEMA_CASE.getKey()] = preserveSchemaCase
            }
        }

        expect:
        Scope.child(scopeValues, {
            database.escapeObjectName(objectName, objectType)
        } as Scope.ScopedRunnerWithReturn<String>) == expected

        where:
        quotingStrategy           | preserveSchemaCase || objectName | objectType || expected
        null                      | null               || 'tbl_1'    | Table      || 'tbl_1'
        null                      | null               || 'sch_1'    | Schema     || 'sch_1'
        null                      | null               || 'groups'   | Table      || '"GROUPS"'
        null                      | null               || 'groups'   | Schema     || '"GROUPS"'
        null                      | true               || 'tbl_1'    | Table      || 'tbl_1'
        null                      | true               || 'sch_1'    | Schema     || '"sch_1"'
        null                      | true               || 'groups'   | Table      || '"GROUPS"'
        null                      | true               || 'groups'   | Schema     || '"groups"'
        QUOTE_ONLY_RESERVED_WORDS | null               || 'tbl_1'    | Table      || 'tbl_1'
        QUOTE_ONLY_RESERVED_WORDS | null               || 'sch_1'    | Schema     || 'sch_1'
        QUOTE_ONLY_RESERVED_WORDS | null               || 'groups'   | Table      || '"GROUPS"'
        QUOTE_ONLY_RESERVED_WORDS | null               || 'groups'   | Schema     || '"GROUPS"'
        QUOTE_ONLY_RESERVED_WORDS | true               || 'tbl_1'    | Table      || 'tbl_1'
        QUOTE_ONLY_RESERVED_WORDS | true               || 'sch_1'    | Schema     || '"sch_1"'
        QUOTE_ONLY_RESERVED_WORDS | true               || 'groups'   | Table      || '"GROUPS"'
        QUOTE_ONLY_RESERVED_WORDS | true               || 'groups'   | Schema     || '"groups"'
        QUOTE_ALL_OBJECTS         | null               || 'tbl_1'    | Table      || '"tbl_1"'
        QUOTE_ALL_OBJECTS         | null               || 'sch_1'    | Schema     || '"SCH_1"'
        QUOTE_ALL_OBJECTS         | null               || 'groups'   | Table      || '"groups"'
        QUOTE_ALL_OBJECTS         | null               || 'groups'   | Schema     || '"GROUPS"'
        QUOTE_ALL_OBJECTS         | true               || 'tbl_1'    | Table      || '"tbl_1"'
        QUOTE_ALL_OBJECTS         | true               || 'sch_1'    | Schema     || '"sch_1"'
        QUOTE_ALL_OBJECTS         | true               || 'groups'   | Table      || '"groups"'
        QUOTE_ALL_OBJECTS         | true               || 'groups'   | Schema     || '"groups"'
    }

    @Unroll("#featureName [#h2Version, #quotingStrategy], [#tableName], [#expected]")
    def escapeTableName() {
        given:
        def database = new H2Database().tap {
            it.connection = toOfflineConnection(h2Version)
            if (quotingStrategy != null) {
                it.objectQuotingStrategy = quotingStrategy
            }
            it.outputDefaultSchema = false
        }

        expect:
        database.escapeTableName(null, null, tableName) == expected

        where:
        h2Version | quotingStrategy           || tableName || expected
        null      | null                      || 'group'   || '"GROUP"'
        null      | null                      || 'groups'  || '"GROUPS"'
        null      | null                      || 'user'    || '"USER"'
        "1.4.200" | null                      || 'group'   || '"GROUP"'
        "1.4.200" | null                      || 'groups'  || '"GROUPS"'
        "1.4.200" | null                      || 'user'    || 'user'
        null      | QUOTE_ONLY_RESERVED_WORDS || 'group'   || '"GROUP"'
        null      | QUOTE_ONLY_RESERVED_WORDS || 'groups'  || '"GROUPS"'
        null      | QUOTE_ONLY_RESERVED_WORDS || 'user'    || '"USER"'
        "1.4.200" | QUOTE_ONLY_RESERVED_WORDS || 'group'   || '"GROUP"'
        "1.4.200" | QUOTE_ONLY_RESERVED_WORDS || 'groups'  || '"GROUPS"'
        "1.4.200" | QUOTE_ONLY_RESERVED_WORDS || 'user'    || 'user'
        null      | QUOTE_ALL_OBJECTS         || 'group'   || '"group"'
        null      | QUOTE_ALL_OBJECTS         || 'groups'  || '"groups"'
        null      | QUOTE_ALL_OBJECTS         || 'user'    || '"user"'
        "1.4.200" | QUOTE_ALL_OBJECTS         || 'group'   || '"group"'
        "1.4.200" | QUOTE_ALL_OBJECTS         || 'groups'  || '"groups"'
        "1.4.200" | QUOTE_ALL_OBJECTS         || 'user'    || '"user"'
    }

    def toOfflineConnection(String h2Version = null) {
        def params = new StringJoiner("&", "?", "")
        if (h2Version != null) {
            params.add("version=" + h2Version)
        }
        String url = "offline:h2" + params
        def resourceAccessor = new JUnitResourceAccessor()
        new OfflineConnection(url, resourceAccessor)
    }
}
