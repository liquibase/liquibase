package liquibase.util

import liquibase.database.DatabaseFactory
import liquibase.structure.core.DataType
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Tests to ensure correction functionality of SqlUtil
 */
class SqlUtilTest extends Specification {
    @Unroll("SqlUtil.parseValue(#value, #dbName, #dataType)")
    def "ParseValue"() {
        when:
        def db = DatabaseFactory.getInstance().getDatabase(dbName)
        def type = new DataType(dataType)
        if (dataTypeId != null)
            type.setDataTypeId(dataTypeId)
        def result = SqlUtil.parseValue(db, value, type)

        then:
        result == expectedObject

        where:
        value                           | dbName   | dataType    | expectedObject | dataTypeId
        (int)3                          | "oracle" | "int"       | (Integer)3     | null
        ""                              | "mock"   | "char(3)"   | ""             | 23
        ""                              | "mock"   | "int"       | null           | null
    }
}
