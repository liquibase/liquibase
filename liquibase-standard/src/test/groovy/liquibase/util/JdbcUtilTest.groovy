package liquibase.util


import liquibase.database.core.H2Database
import liquibase.exception.DatabaseException
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.*

class JdbcUtilTest extends Specification {

    def closeStatement() {
        given:
        def stmt = Mock(Statement)
        stmt.close() //first close is fine
        stmt.close() >> { new RuntimeException("test exception") }

        when:
        JdbcUtil.closeStatement(stmt)
        JdbcUtil.closeStatement(stmt)

        then:
        2 * stmt.close() //close called twice and no exceptions passed along
    }

    def closeResultSet() {
        given:
        def rs = Mock(ResultSet)
        rs.close() //first close is fine
        rs.close() >> { new RuntimeException("test exception") }

        when:
        JdbcUtil.closeResultSet(rs)
        JdbcUtil.closeResultSet(rs)

        then:
        2 * rs.close() //close called twice and no exceptions passed along
    }

    def close() {
        given:
        def stmt = Mock(Statement)
        stmt.close() //first close is fine
        stmt.close() >> { new RuntimeException("test exception") }

        def rs = Mock(ResultSet)
        rs.close() //first close is fine
        rs.close() >> { new RuntimeException("test exception") }

        when:
        JdbcUtil.close(rs, stmt)
        JdbcUtil.close(rs, stmt)

        then:
        2 * stmt.close() //close called twice and no exceptions passed along
        2 * rs.close() //close called twice and no exceptions passed along
    }

    @Unroll
    def getResultSetValue() {
        given:
        def rs = Mock(ResultSet)
        rs.getObject(3) >> object
        rs.getString(3) >> "clob data"
        rs.getBytes(3) >> ([0, 1, 2] as byte[])

        expect:
        JdbcUtil.getResultSetValue(rs, 3) == expected

        where:
        object     | expected
        null       | null
        "a string" | "a string"
        15         | 15
        Mock(Clob) | "clob data"
        Mock(Blob) | [0, 1, 2] as byte[]
    }

    @Unroll
    def isNumeric() {
        expect:
        JdbcUtil.isNumeric(input) == expected

        where:
        input          | expected
        Types.BIT      | true
        Types.BIGINT   | true
        Types.DECIMAL  | true
        Types.DOUBLE   | true
        Types.FLOAT    | true
        Types.INTEGER  | true
        Types.NUMERIC  | true
        Types.REAL     | true
        Types.SMALLINT | true
        Types.TINYINT  | true

        Types.BOOLEAN  | false
        Types.VARCHAR  | false
    }

    def "requiredSingleResult"() {
        expect:
        JdbcUtil.requiredSingleResult(["x"]) == "x"

    }

    @Unroll
    def "requiredSingleResult exception"() {
        when:
        JdbcUtil.requiredSingleResult(input)

        then:
        def e = thrown(DatabaseException)
        e.message == error

        where:
        input      | error
        null       | "Empty result set, expected one row"
        []         | "Empty result set, expected one row"
        ["x", "y"] | "Result set larger than one row"
    }

    @Unroll
    def getValueForColumn() {
        given:
        def data = [
                "lower": "Lower column",
                "UPPER": "Upper column",
        ]
        def columns = data.keySet() as List

        def rs = Mock(ResultSet)
        def metadata = Mock(ResultSetMetaData)
        rs.getMetaData() >> metadata

        metadata.getColumnCount() >> data.size()
        metadata.getColumnLabel(_) >> { columns[it[0] - 1] }
        rs.getString(_) >> { data[it[0] - 1] }


        when:
        rs.close()

        then:
        JdbcUtil.getValueForColumn(rs, columnName, new H2Database()) == expected

        where:
        columnName | expected
        "UPPER"    | "Upper column"
        "upper"    | "Upper column"
        "lower"    | "Lower column"
        "lower"    | "Lower column"
        "invalid"  | null
    }

}
