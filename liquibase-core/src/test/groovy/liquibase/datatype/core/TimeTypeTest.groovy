package liquibase.datatype.core

import liquibase.database.core.*
import liquibase.exception.UnexpectedLiquibaseException
import spock.lang.Specification
import spock.lang.Unroll

class TimeTypeTest extends Specification {
    @Unroll
    def "toDatabaseType"() {
        when:
        def type = new TimeType()
        for (param in params) {
            type.addParameter(param)
        }

        then:
        type.toDatabaseDataType(database).toString() == expected

        where:
        params  | database               | expected
        []      | new PostgresDatabase() | "time WITHOUT TIME ZONE"
        [0]     | new PostgresDatabase() | "time(0) WITHOUT TIME ZONE"
        [6]     | new PostgresDatabase() | "time(6) WITHOUT TIME ZONE"
        [7]     | new PostgresDatabase() | "time WITHOUT TIME ZONE"
        []      | new MySQLDatabase()    | "time"

    }
}
