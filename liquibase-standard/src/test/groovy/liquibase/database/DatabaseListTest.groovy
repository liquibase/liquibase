package liquibase.database

import liquibase.database.core.H2Database
import liquibase.database.core.MSSQLDatabase
import liquibase.database.core.MySQLDatabase
import liquibase.database.core.OracleDatabase
import liquibase.exception.ValidationErrors
import spock.lang.Specification
import spock.lang.Unroll

class DatabaseListTest extends Specification {

    @Unroll
    def "check if database matches dbms definition (definition: #definition, database: #database, returnValueIfEmpty: #returnValueIfEmpty)"() {
        when:
        def result = DatabaseList.definitionMatches(definition, database, returnValueIfEmpty)

        then:
        result == expectedResult

        where:
        definition           | database             | returnValueIfEmpty | expectedResult
        "all"                | new MySQLDatabase()  | false              | true
        "all, oracle"        | new MySQLDatabase()  | false              | true
        "none"               | new MySQLDatabase()  | false              | false
        "none, oracle"       | new MySQLDatabase()  | false              | false
        ""                   | new OracleDatabase() | true               | true
        ""                   | new OracleDatabase() | false              | false
        (String) null        | new OracleDatabase() | true               | true
        (String) null        | new OracleDatabase() | false              | false
        "   "                | new OracleDatabase() | true               | true
        "   "                | new OracleDatabase() | false              | false
        "oracle"             | new OracleDatabase() | false              | true
        "oracle,mysql,mssql" | new OracleDatabase() | false              | true
        "oracle,mysql,mssql" | new MySQLDatabase()  | false              | true
        "oracle,mysql,mssql" | new MSSQLDatabase()  | false              | true
        "oracle,mysql,mssql" | new H2Database()     | false              | false
        "!h2"                | new MySQLDatabase()  | false              | true
        "!h2"                | new MySQLDatabase()  | true               | true
        "!h2"                | new H2Database()     | false              | false
        "!h2"                | new H2Database()     | true               | false
        "!h2,mysql"          | new H2Database()     | false              | false
        "!h2,mysql"          | new MySQLDatabase()  | false              | true
    }

    @Unroll
    def "validate definition parameters dbms: #dbms"() {
        when:
        def vErrors = new ValidationErrors()
        DatabaseList.validateDefinitions(dbms, vErrors)
        boolean valid = !vErrors.hasErrors()

        then:
        valid == expectedResult

        where:
        dbms             | expectedResult
        "all"            | true
        "none"           | true
        "mysqlll"        | false
        "mysql"          | true
        "mariadb"        | true
        "mysql,mariadb"  | true
        "mysql, mariadb" | true
    }

}
