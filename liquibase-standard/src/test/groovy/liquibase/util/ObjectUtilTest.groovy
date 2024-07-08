package liquibase.util

import liquibase.UpdateSummaryEnum
import liquibase.change.ColumnConfig
import liquibase.change.ConstraintsConfig
import liquibase.change.core.AddAutoIncrementChange
import liquibase.change.core.CreateTableChange
import liquibase.command.core.HistoryFormat
import liquibase.database.ObjectQuotingStrategy
import liquibase.exception.UnexpectedLiquibaseException
import liquibase.precondition.core.PreconditionContainer
import liquibase.statement.DatabaseFunction
import liquibase.statement.SequenceCurrentValueFunction
import liquibase.statement.SequenceNextValueFunction
import spock.lang.Specification
import spock.lang.Unroll

import java.beans.PropertyDescriptor

class ObjectUtilTest extends Specification {

    @Unroll
    def "getProperty examples"() {
        expect:
        ObjectUtil.getProperty(object, property) == expected

        where:
        object                  | property     | expected
        new Date(1821212)       | "time"       | 1821212L
        new CreateTableChange() | "tableName"  | null
        new ConstraintsConfig() | "primaryKey" | null //accepts is* format
    }

    def "getProperty throws exception if property does not exist"() {
        when:
        ObjectUtil.getProperty(new CreateTableChange(), "invalid")

        then:
        thrown(UnexpectedLiquibaseException.class)
    }

    @Unroll
    def "hasProperty examples"() {
        expect:
        ObjectUtil.hasReadProperty(object, property) == readExists
        ObjectUtil.hasWriteProperty(object, property) == writeExists
        ObjectUtil.hasProperty(object, property) == readWrite

        where:
        object                  | property    | readExists | writeExists | readWrite
        new Date(1821212)       | "time"      | true       | true        | true
        new Date(1821212)       | "notThere"  | false      | false       | false
        new CreateTableChange() | "tableName" | true       | true        | true
        new CreateTableChange() | "time"      | false      | false       | false
    }

    @Unroll
    def "setProperty examples"() {
        expect:
        ObjectUtil.setProperty(object, property, value)
        ObjectUtil.getProperty(object, property).toString() == value.toString()
        ObjectUtil.getProperty(object, property).getClass() == finalValueType

        where:
        object                       | property               | value         | finalValueType
        new CreateTableChange()      | "tableName"            | "my_table"    | String.class
        new AddAutoIncrementChange() | "startWith"            | "123"         | BigInteger.class
        new ConstraintsConfig()      | "primaryKey"           | "true"        | Boolean.class
        new ConstraintsConfig()      | "primaryKey"           | "false"       | Boolean.class
        new ColumnConfig()           | "valueComputed"        | "my_func()"   | DatabaseFunction.class
        new ColumnConfig()           | "valueSequenceNext"    | "seq_next"    | SequenceNextValueFunction.class
        new ColumnConfig()           | "valueSequenceCurrent" | "seq_current" | SequenceCurrentValueFunction.class
        new PreconditionContainer()  | "onSqlOutput"          | "IGNORE"      | PreconditionContainer.OnSqlOutputOption.class
    }

    def "convert #input"() {
        expect:
        ObjectUtil.convert(input, targetClass) == expected

        where:
        input                                  | targetClass           | expected
        null                                   | String                | null
        "xyz"                                  | String                | "xyz"
        "78bff7f0-dd80-4f53-9bb3-f3cbb9a15ba5" | UUID                  | UUID.fromString("78bff7f0-dd80-4f53-9bb3-f3cbb9a15ba5")
        "2021-05-09 00:54:57.574616"           | Date                  | new ISODateFormat().parse("2021-05-09 00:54:57.574616")
        "2021-05-09"                           | Date                  | new ISODateFormat().parse("2021-05-09")
        ObjectQuotingStrategy.LEGACY           | String                | "LEGACY"
        "LEGACY"                               | ObjectQuotingStrategy | ObjectQuotingStrategy.LEGACY
        "legacy"                               | ObjectQuotingStrategy | ObjectQuotingStrategy.LEGACY
        "1"                                    | Integer               | Integer.valueOf(1)
        Long.valueOf(1)                        | Integer               | Integer.valueOf(1)
        Long.valueOf(1)                        | Short                 | Short.valueOf("1")
        Integer.valueOf(1)                     | Long                  | Long.valueOf(1)
        Long.valueOf(1)                        | Byte                  | Byte.valueOf("1")
        Long.valueOf(1)                        | BigInteger            | BigInteger.valueOf(1)
        Float.valueOf(2.3)                     | BigDecimal            | BigDecimal.valueOf(2.3)
        BigDecimal.valueOf(2.3)                | Float                 | Float.valueOf(2.3)

        ""                                     | Integer               | Integer.valueOf(0)
        "1"                                    | Integer               | Integer.valueOf(1)
        "1"                                    | Short                 | Short.valueOf("1")
        "1"                                    | Long                  | Long.valueOf(1)
        "1"                                    | Byte                  | Byte.valueOf("1")
        "1"                                    | BigInteger            | BigInteger.valueOf(1)
        "2.3"                                  | BigDecimal            | BigDecimal.valueOf(2.3)
        "2.3"                                  | Float                 | Float.valueOf(2.3)

        "true"                                 | Boolean               | true
        "TruE"                                 | Boolean               | true
        "TRUE"                                 | Boolean               | true
        "t"                                    | Boolean               | true
        "T"                                    | Boolean               | true
        "yes"                                  | Boolean               | true
        "false"                                | Boolean               | false
        "FALSE"                                | Boolean               | false
        "FaLsE"                                | Boolean               | false
        [1, 2, 3] as Object[]                  | List                  | [1, 2, 3] as List
        [1, 2, 3] as Set                       | List                  | [1, 2, 3] as List
        "a value"                              | StringClauses         | new StringClauses().append("a value")
        Object.class.name                      | Class                 | Object
        "/path/to/file.txt"                    | File                  | new File("/path/to/file.txt")
        "OFF"                                  | UpdateSummaryEnum.class | UpdateSummaryEnum.OFF
        "SUMMARY"                              | UpdateSummaryEnum.class | UpdateSummaryEnum.SUMMARY
        "SUMmary"                              | UpdateSummaryEnum.class | UpdateSummaryEnum.SUMMARY
        "summary"                              | UpdateSummaryEnum.class | UpdateSummaryEnum.SUMMARY
        "VERBOSE"                              | UpdateSummaryEnum.class | UpdateSummaryEnum.VERBOSE
    }

    @Unroll
    def "Should log warning using parameterName #parameterName when unable to find valid value"() {
        when:
        //This shows as a warning but passes as expected. Not sure what to do about that.
        ObjectUtil.convert(input, clazz, parameterName)
        then:
        def e = thrown(IllegalArgumentException)
        e.message == expected

        where:
        clazz                    | input  | parameterName  | expected
        UpdateSummaryEnum.class  | "blah" | "Summary Mode" | "The summary mode value 'blah' is not valid. Acceptable values are 'OFF', 'SUMMARY', 'VERBOSE'"
        UpdateSummaryEnum.class  | "blah" | null           | "Invalid value 'blah'. Acceptable values are 'OFF', 'SUMMARY', 'VERBOSE'"
    }

    @Unroll
    def "convert with invalid input #input throws an exception"() {
        when:
        ObjectUtil.convert(input, targetClass)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == expected

        where:
        input                                  | targetClass            | expected
        "test"                                 | HistoryFormat          | "Invalid value 'test'. Acceptable values are 'TABULAR', 'TEXT'"
        "xyz"                                  | Integer                | "java.lang.NumberFormatException: For input string: \"xyz\""
    }

    @Unroll
    def getPropertyType() {
        expect:
        ObjectUtil.getPropertyType(object, property) == expected

        where:
        object            | property  | expected
        new Date(1821212) | "time"    | long
        new Date(1821212) | "invalid" | null
        null              | "time"    | null
    }

    @Unroll
    def setProperty() {
        given:
        def object = new CreateTableChange()

        when:
        ObjectUtil.setProperty(object, "tableName", propertyValue)

        then:
        object.tableName == expectedValue

        where:
        propertyValue | expectedValue
        "new value"   | "new value"
        123           | "123"
    }

    @Unroll
    def defaultIfNull() {
        expect:
        ObjectUtil.defaultIfNull(input, value) == expected

        where:
        input | value | expected
        "1"   | null  | "1"
        "1"   | "2"   | "1"
        null  | "2"   | "2"
        null  | null  | null
    }

    def "getDescriptors"() {
        when:
        def descriptors = ObjectUtil.getDescriptors(CreateTableChange)

        def descriptorsMap = new HashMap<String, PropertyDescriptor>()
        for (def descriptor : descriptors) {
            descriptorsMap[descriptor.name] = descriptor
        }

        then:
        descriptorsMap["columns"].readMethod.name == "getColumns"
        descriptorsMap["columns"].writeMethod.name == "setColumns"
    }

}
