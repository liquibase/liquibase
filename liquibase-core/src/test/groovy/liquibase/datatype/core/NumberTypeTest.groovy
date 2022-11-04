package liquibase.datatype.core

import liquibase.database.core.H2Database
import liquibase.database.core.PostgresDatabase
import spock.lang.Specification
import spock.lang.Unroll

class NumberTypeTest extends Specification {

    @Unroll
    def "toDatabaseDataType"() {
        when:
        def type = new NumberType()
        for (def param : params) {
            type.addParameter(param)
        }

        then:
        type.toDatabaseDataType(database).toSql() == expected

        where:
        params     | database               | expected
        []         | new PostgresDatabase() | "numeric"
        [1]        | new PostgresDatabase() | "numeric(1)"
        [2000]     | new PostgresDatabase() | "numeric"
        ["1", "2"] | new PostgresDatabase() | "numeric(1, 2)"
        ["*", "0"] | new PostgresDatabase() | "numeric(*, 0)"
        []         | new H2Database()       | "numeric"
        [1]        | new H2Database()       | "numeric(1)"
        ["1", "2"] | new H2Database()       | "numeric(1, 2)"
    }
}
