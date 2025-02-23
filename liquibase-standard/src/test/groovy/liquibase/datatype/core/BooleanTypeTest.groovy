package liquibase.datatype.core

import liquibase.database.core.PostgresDatabase
import liquibase.datatype.DataTypeFactory
import spock.lang.Specification
import spock.lang.Unroll

class BooleanTypeTest extends Specification {

    @Unroll
    def "verify type '#inputType' on #database.shortName is converted to #expectedType with #expectedValue value"() {

        when:
        def type = DataTypeFactory.getInstance().fromDescription(inputType, database)

        then:
        type instanceof BooleanType
        type.toDatabaseDataType(database).toString() == expectedType
        type.objectToSql(inputValue, database) == expectedValue

        where:
        inputType | inputValue | database                   | expectedType | expectedValue
        "bit"     | 0          | new PostgresDatabase()     | "BOOLEAN"    | "FALSE"
        "BOOLEAN" | "FALSE"    | new PostgresDatabase()     | "BOOLEAN"    | "FALSE"
        "BIT(1)"  | "TRUE"     | new PostgresDatabase()     | "BOOLEAN"    | "TRUE"
    }
}
