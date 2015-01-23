package liquibase.actionlogic

import spock.lang.Specification

class RowBasedQueryResultTest extends Specification {

    def "constructor with null value"() {
        expect: new RowBasedQueryResult(null).size() == 0
    }

    def "constructor with single value"() {
        when:
        def result = new RowBasedQueryResult("42")

        then:
        result.size() == 1
        result.asObject(String) == "42"
        result.asObject(Integer) == 42I
        result.asList(String) == ["42"]
        result.asList(Integer) == [42]
        result.getRows().toString() == "[[value=42]]"
    }

    def "constructor with collection of numbers"() {
        when:
        def result = new RowBasedQueryResult(["42", "43", "44"])

        then:
        result.size() == 3
        result.asList(String) == ["42", "43", "44"]
        result.asList(Integer) == [42, 43, 44]
        result.getRows().toString() == "[[value=42], [value=43], [value=44]]"
    }

    def "constructor with collection of maps"() {
        when:
        def result = new RowBasedQueryResult([
                [col1: "42", col2: "cat"],
                [col1:"43", col2: "-1"],
                [col1:"44", col2: "dog", col3: "special"]])

        then:
        result.size() == 3
        result.getRows().toString() == "[[col1=42, col2=cat], [col1=43, col2=-1], [col1=44, col2=dog, col3=special]]"
        result.getRows()[0].get("col1", Integer) == 42I
        result.getRows()[1].get("col1", Integer) == 43I
        result.getRows()[2].get("col1", Integer) == 44I
    }

    def "asObject with collection of numbers"() {
        when:
        def result = new RowBasedQueryResult(["42", "43", "44"])
        result.asObject(String)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Results contained 3 rows"
    }

    def "asObject with collection of maps"() {
        when:
        def result = new RowBasedQueryResult([
                [col1: "42", col2: "cat"],
                [col1:"43", col2: "-1"],
                [col1:"44", col2: "dog", col3: "special"]])
        result.asObject(String)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Results contained 3 rows"
    }

    def "asList with collection of maps"() {
        when:
        def result = new RowBasedQueryResult([
                [col1: "42", col2: "cat"],
                [col1:"43", col2: "-1"],
                [col1:"44", col2: "dog", col3: "special"]])
        result.asList(String)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Row contained 2 values"
    }
}
