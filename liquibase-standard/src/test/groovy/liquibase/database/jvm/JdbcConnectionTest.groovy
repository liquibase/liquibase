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
        "jdbc:mariadb://dude:secret@localhost:3306/lbcat"                                      | "jdbc:mariadb:@localhost:3306/lbcat"
        "jdbc:mariadb://localhost:3306/lbcat?user=dude&password=secret"                        | "jdbc:mariadb://localhost:3306/lbcat"
        "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public&user=dude&password=Password123" | "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public"
        "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public&password=Password123&user=dude" | "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public"
        "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public&user=lbcat&private_key_file=/home/dude/rsa_key.p8&private_key_file_pwd=dudeabides" | "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public"
        "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=my_user;password=my_password;" | "jdbc:jtds:sqlserver://localhost:1433/proCatalog;"
        "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=my_user;password=my_password"  | "jdbc:jtds:sqlserver://localhost:1433/proCatalog"
        "jdbc:jtds:sqlserver://localhost:1433/proCatalog;password=my_password;user=my_user;" | "jdbc:jtds:sqlserver://localhost:1433/proCatalog;"
        "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=my_user;"                      | "jdbc:jtds:sqlserver://localhost:1433/proCatalog;"
        "jdbc:oracle:thin:user/password@host:1521/db"                                        | "jdbc:oracle:thin:user@host:1521/db"
        "jdbc:oracle:thin:@host:1521/db"                                                     | "jdbc:oracle:thin:@host:1521/db"
        null                                                                                 | null
    }

    @Unroll
    def "sanitizeUrl"() {
        when:
        def passedInput = input
        def conn = new JdbcConnection() {
            @Override
            protected String getConnectionUrl() throws SQLException {
                return passedInput
            }
        }

        then:
        JdbcConnection.sanitizeUrl(input) == output

        where:
        input                                                                                | output
        "jdbc:postgresql://localhost:6432/intuserdb?user=proschema&password=password"        | "jdbc:postgresql://localhost:6432/intuserdb?user=*****&password=*****"
        "jdbc:postgresql://localhost:6432/intuserdb?password=proschema&user=proschema"       | "jdbc:postgresql://localhost:6432/intuserdb?password=*****&user=*****"
        "jdbc:postgresql://localhost:6432/intuserdb?other=nothing&password=proschema&user=proschema" | "jdbc:postgresql://localhost:6432/intuserdb?other=nothing&password=*****&user=*****"
        "jdbc:mysql://dude:secret@localhost:3306/lbcat"                                      | "jdbc:mysql://*****:*****@localhost:3306/lbcat"
        "jdbc:mysql://localhost:3306/lbcat?user=dude&password=secret"                        | "jdbc:mysql://localhost:3306/lbcat?user=*****&password=*****"
        "jdbc:mariadb://dude:secret@localhost:3306/lbcat"                                    | "jdbc:mariadb://*****:*****@localhost:3306/lbcat"
        "jdbc:mariadb://localhost:3306/lbcat?user=dude&password=secret"                      | "jdbc:mariadb://localhost:3306/lbcat?user=*****&password=*****"
        "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public&user=dude&password=password123" | "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public&user=*****&password=*****"
        "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public&password=Password123&user=dude" | "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public&password=*****&user=*****"
        "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public&user=lbcat&private_key_file=/home/dude/rsa_key.p8&private_key_file_pwd=dudeabides" | "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=*****&schema=public&user=*****&private_key_file=*****&private_key_file_pwd=*****"
        "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=my_user;password=my_password;" | "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=*****;password=*****;"
        "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=my_user;password=my_password"  | "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=*****;password=*****"
        "jdbc:jtds:sqlserver://localhost:1433/proCatalog;password=my_password;user=my_user;" | "jdbc:jtds:sqlserver://localhost:1433/proCatalog;password=*****;user=*****;"
        "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=my_user;"                      | "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=*****;"
        "jdbc:oracle:thin:user/password@host:1521/db"                                        | "jdbc:oracle:thin:*****/*****@host:1521/db"
        "jdbc:oracle:thin:@host:1521/db"                                                     | "jdbc:oracle:thin:@host:1521/db"
        "cosmosdb://maincosmosliquibase.documents.azure.com:t27yJICDSFdR1HN==@maincosmosliquibase.documents.azure.com:443/testdb1" | "cosmosdb://maincosmosliquibase.documents.azure.com:*****@maincosmosliquibase.documents.azure.com:443/testdb1"
        null                                                                                 | null
    }
}
