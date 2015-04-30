package liquibase.diff.output

import liquibase.sdk.database.MockDatabase
import liquibase.structure.core.Column
import liquibase.structure.core.Index
import liquibase.structure.core.Table
import spock.lang.Specification
import spock.lang.Unroll

class StandardObjectChangeFilterTest extends Specification {

    def referenceDatabase = new MockDatabase()
    def comparisionDatabase = new MockDatabase()

    def "null and empty filter does not filter anything"() {
        expect:
        assert new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.INCLUDE, "").include(new Table("cat_name", "schema_name", "test_table"), referenceDatabase, comparisionDatabase)
        assert new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.INCLUDE, null).include(new Table("cat_name", "schema_name", "test_table"), referenceDatabase, comparisionDatabase)

        assert new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.EXCLUDE, "").include(new Table("cat_name", "schema_name", "test_table"), referenceDatabase, comparisionDatabase)
        assert new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.EXCLUDE, null).include(new Table("cat_name", "schema_name", "test_table"), referenceDatabase, comparisionDatabase)
    }

    @Unroll("#featureName '#filter' against #object")
    def "filter for all objects by name"() {
        expect:
        new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.INCLUDE, filter).include(object, referenceDatabase, comparisionDatabase) == expected
        new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.EXCLUDE, filter).include(object, referenceDatabase, comparisionDatabase) != expected

        where:
        filter           | object                                                   | expected
        "table_name"     | new Table("cat_name", "schema_name", "table_name")       | true
        "col_name"       | new Column("col_name")                                   | true
        "TABLE_NAME"     | new Table("cat_name", "schema_name", "table_name")       | false
        "TABLE_NAME"     | new Table("cat_name", "schema_name", "TABLE_NAME")       | true
        "table_.*"       | new Table("cat_name", "schema_name", "table_name")       | true
        "table_.*"       | new Table("cat_name", "schema_name", "table_here")       | true
        "table_.*"       | new Table("cat_name", "schema_name", "not_a_table_here") | false
        "table1, table2" | new Table("cat_name", "schema_name", "table1")           | true
        "table1, table2" | new Table("cat_name", "schema_name", "table2")           | true
        "table1, table2" | new Table("cat_name", "schema_name", "table3")           | false
    }

    @Unroll("#featureName '#filter' against #object")
    def "filter for certain object types by name"() {
        expect:
        new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.INCLUDE, filter).include(object, referenceDatabase, comparisionDatabase) == expected
        new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.EXCLUDE, filter).include(object, referenceDatabase, comparisionDatabase) != expected

        where:
        filter                                       | object                                                   | expected
        "table:table_name"                           | new Table("cat_name", "schema_name", "table_name")       | true
        "table:col_name"                             | new Column("col_name")                                   | false
        "column:col_name"                            | new Column("col_name")                                   | true
        "table:TABLE_NAME"                           | new Table("cat_name", "schema_name", "table_name")       | false
        "table:TABLE_NAME"                           | new Table("cat_name", "schema_name", "TABLE_NAME")       | true
        "table:(?i)TABLE_NAME"                       | new Table("cat_name", "schema_name", "table_name")       | true
        "table:table_.*"                             | new Table("cat_name", "schema_name", "table_name")       | true
        "table:table_.*"                             | new Column("table_name")                                 | false
        "table:table_.*"                             | new Table("cat_name", "schema_name", "table_here")       | true
        "table:table_.*"                             | new Table("cat_name", "schema_name", "not_a_table_here") | false
        "table:table1, table:table2, column:column1" | new Table("cat_name", "schema_name", "table1")           | true
        "table:table1, table:table2, column:column1" | new Table("cat_name", "schema_name", "table2")           | true
        "table:table1, table:table2, column:column1" | new Column("column1")                                    | true
        "table:table1, table:table2, column:column1" | new Table("cat_name", "schema_name", "table3")           | false
        "table:table1, .*keep"                       | new Table("cat_name", "schema_name", "table3")           | false
        "table:table1, .*keep"                       | new Table("cat_name", "schema_name", "table1")           | true
        "table:table1, .*keep"                       | new Table("cat_name", "schema_name", "this_keep")        | true
        "table:table1, .*keep"                       | new Column("this_keep")                                  | true
    }

    @Unroll("#featureName '#filter' against #object")
    def "filter applies to nested objects"() {
        expect:
        new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.INCLUDE, filter).include(object, referenceDatabase, comparisionDatabase) == expected
        new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.EXCLUDE, filter).include(object, referenceDatabase, comparisionDatabase) != expected

        where:
        filter             | object                                                                           | expected
        "table:table_name" | new Column(Table, "cat_name", "schema_name", "table_name", "id")                 | true
        "table:table_name" | new Column(Table, "cat_name", "schema_name", "other_table", "id")                | false
        "table:table_name" | new Index("idx_test", "cat_name", "schema_name", "table_name", new Column("id")) | true
        "table_name"       | new Column(Table, "cat_name", "schema_name", "table_name", "id")                 | true
    }

}
