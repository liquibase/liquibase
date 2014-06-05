package liquibase.util

import liquibase.change.ColumnConfig
import liquibase.change.ConstraintsConfig
import liquibase.change.core.AddAutoIncrementChange
import liquibase.change.core.CreateTableChange
import liquibase.exception.UnexpectedLiquibaseException
import liquibase.precondition.core.PreconditionContainer
import liquibase.statement.DatabaseFunction
import liquibase.statement.SequenceCurrentValueFunction
import liquibase.statement.SequenceNextValueFunction
import spock.lang.Specification
import spock.lang.Unroll

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

    @Unroll("#featureName #original.class.name #original to #type.name")
    def "convert"() {
        expect:
        ObjectUtil.convert(original, type) == expected

        where:
        original | type    | expected
        null     | Integer | null
        null     | Boolean | null
        "hi"     | String  | "hi"
        1        | Integer | 1
        "1"      | Integer | 1
        "1.0"    | Integer | 1
        ".0"     | Integer | 0
        "1.00"   | Long    | 1
        1L       | Long    | 1
        1        | Long    | 1L
        "1"      | Long    | 1L
        1L       | Integer | 1
        "1.5"    | Float   | 1.5F
        "1.05"   | Double   | 1.05D
        true     | Boolean | true
        false    | Boolean | false
        1        | Boolean | true
        0        | Boolean | false
        1L       | Boolean | true
        0L       | Boolean | false
        1F       | Boolean | true
        0F       | Boolean | false
        1D       | Boolean | true
        0D       | Boolean | false
        "true"   | Boolean | true
        "false"  | Boolean | false
        "t"      | Boolean | true
        "f"      | Boolean | false
        "TRUE"   | Boolean | true
        "FALSE"  | Boolean | false

    }
}
