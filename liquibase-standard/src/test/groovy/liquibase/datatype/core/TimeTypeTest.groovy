package liquibase.datatype.core

import liquibase.database.core.DerbyDatabase
import liquibase.database.core.PostgresDatabase
import spock.lang.Specification
import spock.lang.Unroll

class TimeTypeTest extends Specification {
    @Unroll
    def "toDatabaseType"() {
        when:
        def type = new TimeType()
        for (def param : params) {
            type.addParameter(param)
        }
        type.finishInitialization(rawType)

        then:
        type.toDatabaseDataType(database).toString() == expected

        where:
        rawType                  | params  | database               | expected
        "time"                   | []      | new DerbyDatabase()    | "time"
        "time"                   | []      | new PostgresDatabase() | "time WITHOUT TIME ZONE"
        "timetz"                 | []      | new PostgresDatabase() | "time WITH TIME ZONE"
        "time(2)"                | []      | new PostgresDatabase() | "time(2) WITHOUT TIME ZONE"
        "time(2) with time zone" | []      | new PostgresDatabase() | "time(2) WITH TIME ZONE"
        "time"                   | [11, 3] | new PostgresDatabase() | "time(3) WITHOUT TIME ZONE"
        "time with time zone"    | [11, 3] | new PostgresDatabase() | "time(3) WITH TIME ZONE"
    }
}
