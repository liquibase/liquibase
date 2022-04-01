package liquibase.database.jvm

import spock.lang.Specification
import spock.lang.Unroll

import java.sql.SQLException

class JdbcConnectionTest extends Specification {

    @Unroll
    def "getUrl"() {
        when:
        def passedInput = input
        def conn = new JdbcConnection() {
            @Override
            protected String getConnectionUrl() throws SQLException {
                return passedInput
            }
        }

        then:
        conn.getURL() == output

        where:
        input                                                                                | output
        "jdbc:postgresql://localhost:6432/intuserdb?user=proschema&password=proschema"       | "jdbc:postgresql://localhost:6432/intuserdb"
        "jdbc:postgresql://localhost:6432/intuserdb?password=proschema&user=proschema"       | "jdbc:postgresql://localhost:6432/intuserdb"
        "jdbc:mysql://dude:secret@localhost:3306/lbcat"                                      | "jdbc:mysql:@localhost:3306/lbcat"
        "jdbc:mysql://localhost:3306/lbcat?user=dude&password=secret"                        | "jdbc:mysql://localhost:3306/lbcat"
        "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public&user=dude&password=Password123" | "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public"
        "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public&password=Password123&user=dude" | "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public"
        "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public&user=lbcat&private_key_file=/home/dude/rsa_key.p8&private_key_file_pwd=dudeabides" | "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public"
        "jdbc:cosmosdb:AccountEndpoint=myAccountEndpoint;AccountKey=myAccountKey;"           | "jdbc:cosmosdb:AccountEndpoint=myAccountEndpoint;"
        "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=my_user;password=my_password;" | "jdbc:jtds:sqlserver://localhost:1433/proCatalog;"
        "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=my_user;password=my_password"  | "jdbc:jtds:sqlserver://localhost:1433/proCatalog"
        "jdbc:jtds:sqlserver://localhost:1433/proCatalog;password=my_password;user=my_user;" | "jdbc:jtds:sqlserver://localhost:1433/proCatalog;"
        "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=my_user;"                      | "jdbc:jtds:sqlserver://localhost:1433/proCatalog;"
        "jdbc:oracle:thin:user/password@host:1521/db"                                        | "jdbc:oracle:thin:user@host:1521/db"
        "jdbc:oracle:thin:@host:1521/db"                                                     | "jdbc:oracle:thin:@host:1521/db"
        null                                                                                 | null
    }
}
