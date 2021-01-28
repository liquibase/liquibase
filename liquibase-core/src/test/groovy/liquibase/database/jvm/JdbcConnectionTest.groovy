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
        "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=my_user;password=my_password;" | "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=my_user;"
        "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=my_user;password=my_password"  | "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=my_user"
        "jdbc:jtds:sqlserver://localhost:1433/proCatalog;password=my_password;user=my_user;" | "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=my_user;"
        "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=my_user;"                      | "jdbc:jtds:sqlserver://localhost:1433/proCatalog;user=my_user;"
        null                                                                                 | null
    }
}
