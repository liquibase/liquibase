package liquibase.sdk.database

import spock.lang.Specification

class MockResultSetTest extends Specification {

    def "empty resultset"() {
        when:
        def rs = new MockResultSet()

        then:
        assert !rs.next()
    }

    def "null resultset"() {
        when:
        def rs = new MockResultSet(null)

        then:
        assert !rs.next()
    }

    def "can move through resultSet"() {
        when:
        def rs = new MockResultSet([[a:1, b:1], [a:2, b:2], [a:3, b:3]])

        then:
        assert rs.next()
        rs.getInt("a") == 1
        rs.getInt("b") == 1

        assert rs.next()
        rs.getInt("a") == 2
        rs.getInt("b") == 2

        assert rs.next()
        rs.getInt("a") == 3
        rs.getInt("b") == 3

        assert !rs.next()
    }
}
