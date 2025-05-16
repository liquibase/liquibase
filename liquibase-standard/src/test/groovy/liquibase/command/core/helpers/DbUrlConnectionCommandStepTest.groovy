package liquibase.command.core.helpers

import spock.lang.Specification
import spock.lang.Unroll

class DbUrlConnectionCommandStepTest extends Specification {

    @Unroll
    def "query parameters are stripped from URL #url"(String url, String expectedStrippedUrl) {
        when:
        def strippedUrl = DbUrlConnectionCommandStep.removeQueryParameters(url)
        then:
        strippedUrl.equals(expectedStrippedUrl)
        where:
        url                                                                                                         | expectedStrippedUrl
        "jdbc:h2:tcp://localhost:9090/mem:dev"                                                                      | "jdbc:h2:tcp://localhost:9090/mem:dev"
        // I don't think this is actually a valid H2 URL, but H2 URLs force the code to enter the liquibase.command.core.helpers.DbUrlConnectionCommandStep.removeQueryParameters catch block, which is the purpose of this test.
        "jdbc:h2:tcp://localhost:9090/mem:dev?myparam=true"                                                         | "jdbc:h2:tcp://localhost:9090/mem:dev"
        "jdbc:postgresql://localhost:5432/postgres"                                                                 | "jdbc:postgresql://localhost:5432/postgres"
        "jdbc:postgresql://localhost:5432/postgres?schema=hello"                                                    | "jdbc:postgresql://localhost:5432/postgres"
        "jdbc:sqlserver://server.database.windows.net:1433;database=mydb?encrypt=true&trustServerCertificate=false" | "jdbc:sqlserver://server.database.windows.net:1433"
        "jdbc:sqlserver://server.database.windows.net:1433;database=mydb;encrypt=true;trustServerCertificate=false" | "jdbc:sqlserver://server.database.windows.net:1433"
        "jdbc:oracle:thin:TEST/password@localhost:1523/FREEPDB1"                                                    | "jdbc:oracle:thin:TEST/password@localhost:1523/FREEPDB1"
        "jdbc:oracle:thin:TEST/password@localhost:1523/FREEPDB1?TNS_ADMIN=/path/to/wallet"                          | "jdbc:oracle:thin:TEST/password@localhost:1523/FREEPDB1"
        "jdbc:oracle:thin:@localhost:1521:orcl?TNS_ADMIN=/path/to/wallet"                                           | "jdbc:oracle:thin:@localhost:1521:orcl"
        "jdbc:mysql://localhost:3306/mydb?user=root&password=secret"                                                | "jdbc:mysql://localhost:3306/mydb"
        "https://dynamodb.eu-west-3.amazonaws.com/"                                                                 | "https://dynamodb.eu-west-3.amazonaws.com"
        "https://dynamodb.eu-west-3.amazonaws.com"                                                                  | "https://dynamodb.eu-west-3.amazonaws.com"
        "https://dynamodb.eu-west-3.amazonaws.com?queryParam=queryValue"                                            | "https://dynamodb.eu-west-3.amazonaws.com"
    }
}
