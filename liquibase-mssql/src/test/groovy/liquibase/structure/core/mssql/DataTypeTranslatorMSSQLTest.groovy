package liquibase.structure.core.mssql

import liquibase.JUnitScope
import liquibase.database.core.mssql.MSSQLDatabase
import liquibase.structure.core.DataType
import spock.lang.Specification
import spock.lang.Unroll

class DataTypeTranslatorMSSQLTest extends Specification {


    @Unroll
    def "escapeDataType"() {
        expect:
        new DataTypeTranslatorMSSQL().toSql(dataType, JUnitScope.instance) == expected

        where:
        dataType                    | expected
        new DataType("int")         | "[int]"
        new DataType("INT")         | "[INT]"
        new DataType("varchar", 10) | "[varchar](10)"
        new DataType("[int]")       | "[int]"
    }

}
