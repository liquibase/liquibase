package liquibase.diff.compare.core

import liquibase.diff.compare.DatabaseObjectComparatorFactory
import liquibase.database.core.MockDatabase
import liquibase.structure.core.Column
import liquibase.structure.core.UniqueConstraint
import spock.lang.Specification
import spock.lang.Unroll

class UniqueConstraintComparatorTest extends Specification {

    @Unroll
    def "test equality"() {
        expect:
        assert DatabaseObjectComparatorFactory.instance.isSameObject(constraint1, constraint2, null, new MockDatabase()) == expected

        where:
        constraint1                                                                                       | constraint2                                                                                       | expected
        new UniqueConstraint("uq_test", null, null, "table_name", new Column("col1"))                     | new UniqueConstraint("uq_test", null, null, "table_name", new Column("col1"))                     | true
        new UniqueConstraint("uq_test", null, "my_schema", "table_name", new Column("col1"))              | new UniqueConstraint("uq_test", null, "my_schema", "table_name", new Column("col1"))              | true
        new UniqueConstraint("uq_test", "my_cat", "my_schema", "table_name", new Column("col1"))          | new UniqueConstraint("uq_test", "my_cat", "my_schema", "table_name", new Column("col1"))          | true
        new UniqueConstraint("uq_test", null, null, "table_name", new Column("col1"), new Column("col2")) | new UniqueConstraint("uq_test", null, null, "table_name", new Column("col1"))                     | true //They should be the same object even if they have different columns
        new UniqueConstraint("uq_test", null, null, "table_name", new Column("col1"))                     | new UniqueConstraint("uq_test", null, null, "table_name", new Column("col1"), new Column("col2")) | true //They should be the same object even if they have different columns
        new UniqueConstraint("uq_test", null, null, "table_name", new Column("col1"))                     | new UniqueConstraint(null, null, null, "table_name", new Column("col1"))                          | true //They should be the same object if only one has a name
        new UniqueConstraint("uq_test", null, null, "table_name", new Column("col1"))                     | new UniqueConstraint("uq_other", null, null, "table_name", new Column("col1"))                    | true //They should be the same object if they have different names but the same columns
        new UniqueConstraint("uq_test", null, null, "table_name", new Column("col1"))                     | new UniqueConstraint("uq_test", null, null, "other_table", new Column("col1"))                    | false //Different if the same name but different tables
        new UniqueConstraint("uq_test", null, "my_schema", "table_name", new Column("col1"))              | new UniqueConstraint("uq_test", null, "other_schema", "table_name", new Column("col1"))           | false //different schemas
        new UniqueConstraint("uq_test", "my_cat", "my_schema", "table_name", new Column("col1"))          | new UniqueConstraint("uq_test", "other_cat", "my_schema", "table_name", new Column("col1"))       | true //different cat

        new UniqueConstraint("uq_test", null, null, null, new Column("col1"))                             | new UniqueConstraint("uq_test", null, null, null, new Column("col1"))                             | true //Same if the same name and no table set
        new UniqueConstraint("uq_test", null, null, null, new Column("col1"), new Column("col2"))         | new UniqueConstraint("uq_test", null, null, null, new Column("col1"))                             | true //Same if the same name and no table set and different column count
        new UniqueConstraint("uq_test", "my_cat", "my_schema", "table_name", new Column("col1"))          | new UniqueConstraint("uq_test", null, null, null, new Column("col1"))                             | true //same if one has a name and the other is fully populated
        new UniqueConstraint("uq_test", null, null, null, new Column("col1"))                             | new UniqueConstraint("uq_test", "my_cat", "my_schema", "table_name", new Column("col1"))          | true //same if one has a name and the other is fully populated

        new UniqueConstraint(null, null, null, "table_name", new Column("col1"))                          | new UniqueConstraint("uq_test", null, null, "table_name", new Column("col1"))                     | true //They should be the same object if only one has a name
        new UniqueConstraint(null, null, null, "table_name", new Column("col1"))                          | new UniqueConstraint(null, null, null, "table_name", new Column("col1"))                          | true //They should be the same object if they have no name but the same columns
        new UniqueConstraint(null, null, null, "table_name", new Column("col1"), new Column("col2"))      | new UniqueConstraint(null, null, null, "table_name", new Column("col1"))                          | false //Different if no name and different columns
        new UniqueConstraint(null, null, null, "table_name", new Column("col1"), new Column("col2"))      | new UniqueConstraint("uq_test", null, null, "table_name", new Column("col1"))                          | false //Different if one has no name name but different columns
    }
}
