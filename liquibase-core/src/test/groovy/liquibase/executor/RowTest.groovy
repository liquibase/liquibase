package liquibase.executor

import spock.lang.Specification
import spock.lang.Unroll

class RowTest extends Specification {

    @Unroll
    def "constructor and toString"() {
        expect:
        new Row(passed).toString() == expected

        where:
        passed        | expected
        null          | "[]"
        new HashMap() | "[]"
        [a: 1, b: 2]  | "[a=1, b=2]"
    }

    @Unroll
    def "equals logic"() {
        expect:

        new Row(row1).equals(new Row(row2)) == equal

        where:
        row1          | row2          | equal
        null          | null          | true
        null          | new HashMap() | true
        new HashMap() | null          | true
        [a: 1, b: 2]  | [a: 1, b: 2]  | true
        [a: 1, b: 2]  | [a: 1]        | false
        [a: 1, b: 2]  | null        | false
    }

    def "getColumns"() {
        when:
        def row = new Row([a: 1, b:2]);

        then:
        row.getColumns() instanceof SortedSet
        row.getColumns() == new TreeSet(["a", "b"])
    }

    def "getSingleValue with value"() {
        when:
        def row = new Row([a: 1])

        then:
        row.getSingleValue(Integer.class) == 1
        row.getSingleValue(Long.class) == 1L
        row.getSingleValue(String.class) == "1"
    }

    def "getSingleValue with null value"() {
        when:
        def row = new Row([a: null])

        then:
        row.getSingleValue(Integer.class) == null
        row.getSingleValue(Long.class) == null
        row.getSingleValue(String.class) == null
    }

    def "getSingleValue with multiple values"() {
        when:
        def row = new Row([a: 1, b: 2])
        row.getSingleValue(String.class)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Row contained 2 values"
    }

    def "getSingleValue with no values"() {
        when:
        def row = new Row(null)

        then:
        row.getSingleValue(String.class) == null
    }

    def "get logic with values"() {
        when:
        def row = new Row([a: 1, b:2])

        then:
        row.get("a", String.class) == "1"
        row.get("a", Long.class) == 1L
        row.get("b", String.class) == "2"
        row.get("b", Long.class) == 2L

        row.get("c", String.class) == null
        row.get("A", String.class) == null
    }

    def "get logic with default value"() {
        when:
        def row = new Row([a: 1, b:2, c: null])

        then:
        row.get("a", 5) == 1
        row.get("a", 5L) == 1L
        row.get("a", "5") == "1"

        row.get("c", 5) == 5
        row.get("c", "5") == "5"

        row.get("c", null) == null
        row.get("a", null) == 1
    }

    def "getSingleValue with default value"() {
        when:
        def rowWithValue = new Row([a: 1])
        def rowWithoutValue = new Row([a: null])

        then:
        rowWithValue.getSingleValue(5) == 1
        rowWithValue.getSingleValue(5L) == 1L
        rowWithValue.getSingleValue("5") == "1"

        rowWithoutValue.getSingleValue(5) == 5
        rowWithoutValue.getSingleValue("5") == "5"
    }

    def "hasColumn logic"() {
        when:
        def row = new Row([a: 1, b:2, c: null])

        then:
        assert row.hasColumn("a")
        assert row.hasColumn("b")
        assert row.hasColumn("c")
        assert !row.hasColumn("d")
        assert !row.hasColumn("A")


    }
}
