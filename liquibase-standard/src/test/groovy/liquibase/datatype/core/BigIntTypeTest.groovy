package liquibase.datatype.core;

import liquibase.database.core.*;
import liquibase.datatype.DataTypeFactory;
import spock.lang.Specification;
import spock.lang.Unroll;

class BigIntTypeTest extends Specification {

    @Unroll
    def "toDatabaseType '#input' on #database.shortName"() {

        when:
        def type = DataTypeFactory.getInstance().fromDescription(input, database)

        then:
        type instanceof BigIntType
        type.toDatabaseDataType(database).toString() == expected

        where:
        input         | database                   | expected
        "bigint"      | new H2Database()           | "BIGINT"
        "bigint(20)"  | new H2Database()           | "BIGINT"
        "bigint"      | new MSSQLDatabase()        | "bigint"
        "bigint"      | new PostgresDatabase()     | "BIGINT"
        "bigint"      | new InformixDatabase()     | "INT8"
        "bigint"      | new MySQLDatabase()        | "BIGINT"
        "bigint"      | new DerbyDatabase()        | "BIGINT"
        "bigint"      | new HsqlDatabase()         | "BIGINT"
        "bigint"      | new FirebirdDatabase()     | "BIGINT"
        "bigint"      | new SybaseASADatabase()    | "BIGINT"

        //These are the some of the parsing combinations being tested for BigIntType for some of the supported DBs.
        // Some of these scenarios are being also tested in DataTypeFactoryTest class.
    }
}
