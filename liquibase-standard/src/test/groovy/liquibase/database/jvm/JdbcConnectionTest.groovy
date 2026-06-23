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
        "jdbc:databricks://databricks.azuredatabricks.net:443/default;transportMode=http;ssl=1;AuthMech=11;Auth_Flow=1;httpPath=/sql/1.0/warehouses/warehouseId;ConnCatalog=myCatalog;ConnSchema=mySchema;OAuth2ClientId=MyClientID;OAuth2Secret=MySecret;" | "jdbc:databricks://databricks.azuredatabricks.net:443/default;transportMode=http;ssl=1;AuthMech=11;Auth_Flow=1;httpPath=/sql/1.0/warehouses/warehouseId;ConnCatalog=myCatalog;ConnSchema=mySchema;OAuth2ClientId=MyClientID;"
        // Generic jdbc: userinfo fallback (CWE-693) — postgresql + third-party drivers
        "jdbc:postgresql://liquibaseuser:secret@host:5432/db"                                | "jdbc:postgresql:@host:5432/db"
        "jdbc:sqlserver://user:secret@host:1433/db"                                          | "jdbc:sqlserver:@host:1433/db"
        "jdbc:vendor-x://liquibaseuser:secret@host:9999/db"                                  | "jdbc:vendor-x:@host:9999/db"
        // CWE-693 follow-up: the generic userinfo matcher is now scoped to authority-position
        // '@' only — these three URLs put '@' in a property value or query string, NOT in
        // userinfo, and must NOT trigger the generic strip. The dedicated property/query
        // filters (FILTER_CREDS_PW_TO_BLANK, FILTER_CREDS_USER_TO_BLANK) handle them.
        // Without the scoped matcher fix, the MSSQL case below stripped 'host:1433;...;user=admin@'
        // leaving 'tenant.com;password=pass' — caught by liquibase-pro MSSQLDatabaseTest.
        "jdbc:sqlserver://host:1433;databaseName=mydb;authentication=ActiveDirectoryPassword;user=admin@tenant.com;password=pass" | "jdbc:sqlserver://host:1433;databaseName=mydb;authentication=ActiveDirectoryPassword"
        // Password value is deliberately longer than 1 char to avoid a pre-existing literal-
        // replace collision in FILTER_CREDS_PASSWORD where ':p' would match 'jdbc:p[ostgresql]'.
        // That collision exists independently of this CWE-693 follow-up; tracked as a separate
        // latent issue in the sanitizeUrl obfuscation step. Use 'secret123' here so the test
        // pins the regression-of-interest (authority-position scoping) cleanly.
        "jdbc:postgresql://host:5432/db?user=admin@tenant.com&password=secret123"            | "jdbc:postgresql://host:5432/db"
        "jdbc:somevendor://host:5432/db;property=value@with-at-sign;password=p"              | "jdbc:somevendor://host:5432/db;property=value@with-at-sign"
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
        "jdbc:databricks://databricks.azuredatabricks.net:443/default;transportMode=http;ssl=1;AuthMech=11;Auth_Flow=1;httpPath=/sql/1.0/warehouses/warehouseId;ConnCatalog=myCatalog;ConnSchema=mySchema;OAuth2ClientId=MyClientID;OAuth2Secret=MySecret;" | "jdbc:databricks://databricks.azuredatabricks.net:443/default;transportMode=http;ssl=1;AuthMech=11;Auth_Flow=1;httpPath=/sql/1.0/warehouses/warehouseId;ConnCatalog=myCatalog;ConnSchema=mySchema;OAuth2ClientId=MyClientID;OAuth2Secret=*****;"
        // Generic jdbc: userinfo fallback (CWE-693): postgresql + sqlserver + third-party drivers
        // previously bypassed sanitizeUrl because their dialects had no matcher registered.
        "jdbc:postgresql://liquibaseuser:secret@host:5432/db"                                | "jdbc:postgresql://*****:*****@host:5432/db"
        "jdbc:sqlserver://user:secret@host:1433/db"                                          | "jdbc:sqlserver://*****:*****@host:1433/db"
        "jdbc:vendor-x://liquibaseuser:secret@host:9999/db"                                  | "jdbc:vendor-x://*****:*****@host:9999/db"
        // CWE-693 follow-up: '@' inside property value / query string must NOT trigger the
        // userinfo obfuscator. Authority preserved; user/password obfuscated by dedicated
        // property filters.
        "jdbc:sqlserver://host:1433;databaseName=mydb;authentication=ActiveDirectoryPassword;user=admin@tenant.com;password=pass" | "jdbc:sqlserver://host:1433;databaseName=mydb;authentication=ActiveDirectoryPassword;user=*****;password=*****"
        "jdbc:postgresql://host:5432/db?user=admin@tenant.com&password=secret123"            | "jdbc:postgresql://host:5432/db?user=*****&password=*****"
        "jdbc:somevendor://host:5432/db;property=value@with-at-sign;password=p"              | "jdbc:somevendor://host:5432/db;property=value@with-at-sign;password=*****"
        null                                                                                 | null
    }

    @Unroll
    def "sanitizeUrl, replacing with empty"() {
        when:
        def passedInput = input
        def conn = new JdbcConnection() {
            @Override
            protected String getConnectionUrl() throws SQLException {
                return passedInput
            }
        }

        then:
        JdbcConnection.sanitizeUrl(input, true) == output

        where:
        input                                                                                | output
        "jdbc:postgresql://localhost:6432/intuserdb?user=proschema&password=password"        | "jdbc:postgresql://localhost:6432/intuserdb?user=*****&password=*****"
        "jdbc:postgresql://localhost:6432/intuserdb?password=proschema&user=proschema"       | "jdbc:postgresql://localhost:6432/intuserdb?password=*****&user=*****"
        "jdbc:postgresql://localhost:6432/intuserdb?other=nothing&password=proschema&user=proschema" | "jdbc:postgresql://localhost:6432/intuserdb?other=nothing&password=*****&user=*****"
        "jdbc:mysql://dude:secret@localhost:3306/lbcat"                                      | "jdbc:mysql://localhost:3306/lbcat"
        "jdbc:mysql://localhost:3306/lbcat?user=dude&password=secret"                        | "jdbc:mysql://localhost:3306/lbcat?user=*****&password=*****"
        "jdbc:mariadb://dude:secret@localhost:3306/lbcat"                                    | "jdbc:mariadb://localhost:3306/lbcat"
        "jdbc:mariadb://localhost:3306/lbcat?user=dude&password=secret"                      | "jdbc:mariadb://localhost:3306/lbcat?user=*****&password=*****"
        "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public&user=dude&password=password123" | "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public&user=*****&password=*****"
        "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public&password=Password123&user=dude" | "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public&password=*****&user=*****"
        "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=lbcat&schema=public&user=lbcat&private_key_file=/home/dude/rsa_key.p8&private_key_file_pwd=dudeabides" | "jdbc:snowflake://ba89345.us-east-2.aws.snowflakecomputing.com?warehouse=COMPUTE_WH&db=*****&schema=public&user=*****&private_key_file=*****&private_key_file_pwd=*****"
        "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=my_user;password=my_password;" | "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=*****;password=*****;"
        "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=my_user;password=my_password"  | "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=*****;password=*****"
        "jdbc:jtds:sqlserver://localhost:1433/proCatalog;password=my_password;user=my_user;" | "jdbc:jtds:sqlserver://localhost:1433/proCatalog;password=*****;user=*****;"
        "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=my_user;"                      | "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=*****;"
        "jdbc:oracle:thin:user/password@host:1521/db"                                        | "jdbc:oracle:thin:@host:1521/db"
        "jdbc:oracle:thin:@host:1521/db"                                                     | "jdbc:oracle:thin:@host:1521/db"
        "cosmosdb://maincosmosliquibase.documents.azure.com:t27yJICDSFdR1HN==@maincosmosliquibase.documents.azure.com:443/testdb1" | "cosmosdb://maincosmosliquibase.documents.azure.com:*****@maincosmosliquibase.documents.azure.com:443/testdb1"
        "jdbc:databricks://databricks.azuredatabricks.net:443/default;transportMode=http;ssl=1;AuthMech=11;Auth_Flow=1;httpPath=/sql/1.0/warehouses/warehouseId;ConnCatalog=myCatalog;ConnSchema=mySchema;OAuth2ClientId=MyClientID;OAuth2Secret=MySecret;" | "jdbc:databricks://databricks.azuredatabricks.net:443/default;transportMode=http;ssl=1;AuthMech=11;Auth_Flow=1;httpPath=/sql/1.0/warehouses/warehouseId;ConnCatalog=myCatalog;ConnSchema=mySchema;OAuth2ClientId=MyClientID;OAuth2Secret=*****;"
        // Generic jdbc: userinfo fallback (CWE-693): replaceWithEmpty path
        "jdbc:postgresql://liquibaseuser:secret@host:5432/db"                                | "jdbc:postgresql://host:5432/db"
        "jdbc:sqlserver://user:secret@host:1433/db"                                          | "jdbc:sqlserver://host:1433/db"
        "jdbc:vendor-x://liquibaseuser:secret@host:9999/db"                                  | "jdbc:vendor-x://host:9999/db"
        // CWE-693 follow-up — the strongest regression assertion in this set. Without the
        // scoped-matcher fix the MSSQL Azure-AD URL had 'host:1433' replaced by 'tenant.com'
        // here (the buggy generic strip left 'tenant.com;password=pass' which then got
        // further trimmed by MSSQLDatabase.sanitizeAndStripParams to 'jdbc:sqlserver://tenant.com').
        // The fix means 'host:1433' is preserved; only the user/password property values
        // are masked.
        "jdbc:sqlserver://host:1433;databaseName=mydb;authentication=ActiveDirectoryPassword;user=admin@tenant.com;password=pass" | "jdbc:sqlserver://host:1433;databaseName=mydb;authentication=ActiveDirectoryPassword;user=*****;password=*****"
        "jdbc:postgresql://host:5432/db?user=admin@tenant.com&password=secret123"            | "jdbc:postgresql://host:5432/db?user=*****&password=*****"
        "jdbc:somevendor://host:5432/db;property=value@with-at-sign;password=p"              | "jdbc:somevendor://host:5432/db;property=value@with-at-sign;password=*****"
        null                                                                                 | null
    }
}
