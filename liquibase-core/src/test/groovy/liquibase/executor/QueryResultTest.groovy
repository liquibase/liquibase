package liquibase.executor

import spock.lang.Specification
import spock.lang.Unroll

class QueryResultTest extends Specification {

    @Unroll
    def "toList of map values"() {
        expect:
        new QueryResult(resultSet).toList() == resultSet.collect({ new Row(it)})
        new QueryResult(null).toList() == []

        where:
        resultSet << [
                [],
                [[a: 1, b:2]],
                [[a: 1, b:2], [a:3, b:4]]
        ]
    }

    def "toList of object values"() {
        expect:
        new QueryResult(null).toList(Integer.class) == []
        new QueryResult([]).toList(Integer.class) == []
        new QueryResult([[a: 1]]).toList(Integer.class) == [1]
        new QueryResult([[a: 1], [a: 2]]).toList(Integer.class) == [1, 2]
        new QueryResult([[a: 1], [b: 2]]).toList(Integer.class) == [1, 2]
        new QueryResult([[a: 1], [b: 2]]).toList(Long.class) == [1L, 2L]
        new QueryResult([[a: "1"], [b: "2"]]).toList(Long.class) == [1L, 2L]
        new QueryResult([[a: 1], [a: null], [a: 3]]).toList(Integer.class) == [1, null, 3]
    }

    @Unroll
    def "toList of object values with problems"() {
        when:
        new QueryResult(resultSet).toList(Integer.class)
        then:
        thrown(IllegalArgumentException)

        where:
        resultSet << [
                [[a:1, b:2]],
                [[a:1], [a:2, b:2]],
                [[a:"a string"]],
        ]
    }

    def "toObject" () {
        expect:
        new QueryResult(null).toObject(Integer.class) == null
        new QueryResult([]).toObject(Integer.class) == null
        new QueryResult([[a: 1]]).toObject(Integer.class) == 1
        new QueryResult([[a: 1L]]).toObject(Long.class) == 1L
    }

    def "toObject with default" () {
        expect:
        new QueryResult(null).toObject(5) == 5
        new QueryResult(null).toObject(null) == null
        new QueryResult([]).toObject(10) == 10
        new QueryResult([[a: 1]]).toObject(4) == 1
        new QueryResult([[a: 1]]).toObject(4L) == 1L
        new QueryResult([[a: 1L]]).toObject(7L) == 1L
        new QueryResult([[a: 1L]]).toObject(7) == 1L
    }

    @Unroll
    def "toObject with problems"() {
        when:
        new QueryResult(resultSet).toObject(Integer.class)
        then:
        thrown(IllegalArgumentException)

        where:
        resultSet << [
                [[a:1, b:2]],
                [[a:1], [a:2]],
                [[a:"a string"]],
        ]
    }
}
