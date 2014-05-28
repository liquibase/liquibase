package liquibase.datatype;

import liquibase.database.core.H2Database
import liquibase.datatype.core.BigIntType;
import liquibase.datatype.core.IntType;
import liquibase.datatype.core.VarcharType
import liquibase.sdk.database.MockDatabase;
import org.junit.Test
import spock.lang.Specification
import spock.lang.Unroll;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class DataTypeFactoryTest extends Specification {

    @Unroll("#featureName: #liquibaseString")
    public void parse() throws Exception {
        when:
        def parsed = DataTypeFactory.getInstance().fromDescription(liquibaseString, new MockDatabase())
        if (databaseString == null) {
            databaseString = liquibaseString
        }

        then:
        expectedType.getName() == parsed.getClass().getName()
        databaseString == parsed.toString()

        where:
        liquibaseString                           | databaseString | expectedType      | isAutoIncrement
        "int"                                     | null           | IntType.class     | false
        "varchar(255)"                            | null           | VarcharType.class | false
        "int{autoIncrement:true}"                 | "int"          | IntType.class     | true
        "int{autoIncrement:false}"                | "int"          | IntType.class     | true
        "int{}"                                   | "int"          | IntType.class     | false
        "varchar COLLATE Latin1_General_BIN"      | null           | VarcharType.class | false
        "varchar(255) COLLATE Latin1_General_BIN" | null           | VarcharType.class | false
        "character varying(256)"                  | "varchar(256)" | VarcharType.class | false
        "serial8"                                 | "bigint"       | BigIntType        | true
    }
}
