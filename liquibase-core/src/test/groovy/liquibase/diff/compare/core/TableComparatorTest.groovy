package liquibase.diff.compare.core

import liquibase.sdk.database.MockDatabase
import liquibase.diff.compare.DatabaseObjectComparatorFactory
import liquibase.structure.core.Table
import spock.lang.Specification
import spock.lang.Unroll

class TableComparatorTest extends Specification {

    @Unroll("compare #table1 to #table2 as caseSensitive:#caseSensitive, catalogs:#supportsCatalogs, schemas:#supportsSchemas")
    def comparisions() {
        when:
        def database = new MockDatabase()
        database.supportsSchemas = supportsSchemas
        database.supportsCatalogs = supportsCatalogs
        database.caseSensitive = caseSensitive

        then:
        assert DatabaseObjectComparatorFactory.instance.isSameObject(table1, table2, null, database) == isSame
        assert DatabaseObjectComparatorFactory.instance.isSameObject(table2, table1, null, database) == isSame

        where:
        table1 | table2 | caseSensitive | supportsCatalogs | supportsSchemas | isSame | reason
        new Table(null, null, "a") | new Table(null, null, "a") | false | true  | true  | true  | ""
        new Table(null, null, "a") | new Table().setName("a")   | false | true  | true  | true  | "passing null is the same as not setting a schema"
        new Table(null, null, "a") | new Table(null, null, "b") | false | true  | true  | false | ""
        new Table(null, null, "a") | new Table(null, null, "A") | false | true  | true  | true  | ""
        new Table(null, null, "a") | new Table(null, null, "A") | true  | true  | true  | true  | ""
        new Table("x", "y", "a")   | new Table("x", "y", "A")   | false | true  | true  | true  | ""
        new Table("x", "y", "a")   | new Table("x", "y", "A")   | false | true  | false | true  | ""
        new Table("x", "y", "a")   | new Table("x", "z", "A")   | false | true  | false | true  | "Don't check schemas if database only supports catalogs"
        new Table("x", "y", "a")   | new Table("x", "y", "A")   | false | false | false | true  | "Different catalog/schemas always match if database doesn't support catalogs"
        new Table("x", "y", "a")   | new Table("s", "t", "A")   | false | false | false | true  | "Different catalog/schemas always match if database doesn't support catalogs"
        new Table("X", "Y", "a")   | new Table("X", "Y", "A")   | false | true  | true  | true  | "catalog/schema case never matters"
        new Table("X", "Y", "a")   | new Table("X", "Y", "A")   | true  | true  | true  | true  | "catalog/schema case never matters"
    }
}
