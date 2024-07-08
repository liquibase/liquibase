package liquibase.datatype.core

import liquibase.database.core.*
import liquibase.database.core.MockDatabase
import spock.lang.Specification
import spock.lang.Unroll

class XMLTypeTest extends Specification {
    @Unroll("#featureName: #object for #database")
    def "objectToSql"() {
        when:
        def type = new XMLType()

        then:
        type.objectToSql(object, database) == expectedSql

        where:
        object                                                  | database                | expectedSql
        null                                                    | new MockDatabase()      | null
        "NULL"                                                  | new MockDatabase()      | null
        "<?xml version=\"1.0\"?><root/>"                        | new MockDatabase()      | "'<?xml version=\"1.0\"?><root/>'"
        "<?xml version='1.0'?><root/>"                          | new DB2Database()       | "'<?xml version=''1.0''?><root/>'"
        "<?xml version='1.0'?><root/>"                          | new MSSQLDatabase()     | "'<?xml version=''1.0''?><root/>'"
        "<?xml version='1.0'?><root>\u30CF\u30ED\u30FC</root>"  | new MSSQLDatabase()     | "N'<?xml version=''1.0''?><root>\u30CF\u30ED\u30FC</root>'"
        "<?xml version='1.0'?><root/>"                          | new OracleDatabase()    | "XMLType('<?xml version=''1.0''?><root/>')"
        "<?xml version='1.0'?><root/>"                          | new PostgresDatabase()  | "xml '<?xml version=''1.0''?><root/>'"
    }
}
