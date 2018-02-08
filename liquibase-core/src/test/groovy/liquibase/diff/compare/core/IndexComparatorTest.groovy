package liquibase.diff.compare.core

import liquibase.diff.compare.DatabaseObjectComparatorFactory
import liquibase.database.core.MockDatabase
import liquibase.structure.core.Column
import liquibase.structure.core.Index
import spock.lang.Specification
import spock.lang.Unroll

class IndexComparatorTest extends Specification {
    @Unroll
    def "test equality"() {
        expect:
        assert DatabaseObjectComparatorFactory.instance.isSameObject(constraint1, constraint2, null, new MockDatabase()) == expected

        where:
        constraint1                                                                            | constraint2                                                                            | expected
        new Index("uq_test", null, null, "table_name", new Column("col1"))                     | new Index("uq_test", null, null, "table_name", new Column("col1"))                     | true
        new Index("uq_test", null, "my_schema", "table_name", new Column("col1"))              | new Index("uq_test", null, "my_schema", "table_name", new Column("col1"))              | true
        new Index("uq_test", "my_cat", "my_schema", "table_name", new Column("col1"))          | new Index("uq_test", "my_cat", "my_schema", "table_name", new Column("col1"))          | true
        new Index("uq_test", null, null, "table_name", new Column("col1"), new Column("col2")) | new Index("uq_test", null, null, "table_name", new Column("col1"))                     | true //They should be the same object even if they have different columns
        new Index("uq_test", null, null, "table_name", new Column("col1"))                     | new Index("uq_test", null, null, "table_name", new Column("col1"), new Column("col2")) | true //They should be the same object even if they have different columns
        new Index("uq_test", null, null, "table_name", new Column("col1"))                     | new Index(null, null, null, "table_name", new Column("col1"))                          | true //They should be the same object if only one has a name
        new Index("uq_test", null, null, "table_name", new Column("col1"))                     | new Index("uq_other", null, null, "table_name", new Column("col1"))                    | true //They should be the same object if they have different names but the same columns
        new Index("uq_test", null, null, "table_name", new Column("col1"))                     | new Index("uq_test", null, null, "other_table", new Column("col1"))                    | false //Different if the same name but different tables
        new Index("uq_test", null, "my_schema", "table_name", new Column("col1"))              | new Index("uq_test", null, "other_schema", "table_name", new Column("col1"))           | false //different schemas
        new Index("uq_test", "my_cat", "my_schema", "table_name", new Column("col1"))          | new Index("uq_test", "other_cat", "my_schema", "table_name", new Column("col1"))       | true //different cat

        new Index("uq_test", null, null, null, new Column("col1"))                             | new Index("uq_test", null, null, null, new Column("col1"))                             | true //Same if the same name and no table set
        new Index("uq_test", null, null, null, new Column("col1"), new Column("col2"))         | new Index("uq_test", null, null, null, new Column("col1"))                             | true //Same if the same name and no table set and different column count
        new Index("uq_test", "my_cat", "my_schema", "table_name", new Column("col1"))          | new Index("uq_test", null, null, null, new Column("col1"))                             | true //same if one has a name and the other is fully populated
        new Index("uq_test", null, null, null, new Column("col1"))                             | new Index("uq_test", "my_cat", "my_schema", "table_name", new Column("col1"))          | true //same if one has a name and the other is fully populated

        new Index(null, null, null, "table_name", new Column("col1"))                          | new Index("uq_test", null, null, "table_name", new Column("col1"))                     | true //They should be the same object if only one has a name
        new Index(null, null, null, "table_name", new Column("col1"))                          | new Index(null, null, null, "table_name", new Column("col1"))                          | true //They should be the same object if they have no name but the same columns
        new Index(null, null, null, "table_name", new Column("col1"), new Column("col2"))      | new Index(null, null, null, "table_name", new Column("col1"))                          | false //Different if no name and different columns
        new Index(null, null, null, "table_name", new Column("col1"), new Column("col2"))      | new Index("uq_test", null, null, "table_name", new Column("col1"))                     | false //Different if one has no name name but different columns
    }
}
