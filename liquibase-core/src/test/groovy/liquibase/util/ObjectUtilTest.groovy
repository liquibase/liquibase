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

    @Unroll
    def "convert valid examples"() {
        expect:
        ObjectUtil.convert(object, targetClass) == expected

        where:
        object          | targetClass | expected
        null            | String      | null
        "3"             | String      | "3"
        3I              | Integer     | 3I
        3I              | Float       | 3F
        new Double(3.5) | Float       | 3.5F
        3I              | Byte        | new Byte("3")
        3I              | Short       | new Short("3")
        3F              | Integer     | 3I
        3.5F            | Double      | new Double(3.5)
        3I              | Long        | 3L
        3.5F            | BigDecimal  | new BigDecimal("3.5")
        3I              | BigInteger  | new BigInteger("3")
        "3"             | Byte        | new Byte("3")
        "3.0"           | Byte        | new Byte("3")
        ""              | Byte        | new Byte("0")
        "3"             | Short       | new Short("3")
        "3.0"           | Short       | new Short("3")
        ""              | Short       | new Short("0")
        "3"             | Integer     | 3I
        "3.0"           | Integer     | 3I
        ""              | Integer     | 0I
        "-3"            | Integer     | -3I
        "3"             | Long        | 3L
        "3.0"           | Long        | 3L
        ""              | Long        | 0I
        "3"             | Float       | 3.0F
        "3.0"           | Float       | 3.0F
        "3.3"           | Float       | 3.3F
        "-3.3"          | Float       | -3.3F
        ""              | Float       | 0F
        "3"             | Double      | new Double("3")
        "3.0"           | Double      | new Double("3")
        "3.3"           | Double      | new Double("3.3")
        ""              | Double      | new Double("0")
        "3"             | BigDecimal  | new BigDecimal("3")
        "3.0"           | BigDecimal  | new BigDecimal("3")
        "3.3"           | BigDecimal  | new BigDecimal("3.3")
        ""              | BigDecimal  | new BigDecimal("0")
        "3"             | BigInteger  | new BigInteger("3")
        "3.0"           | BigInteger  | new BigInteger("3")
        ""              | BigInteger  | new BigInteger("0")
        "t"             | Boolean     | true
        "true"          | Boolean     | true
        "TRUE"          | Boolean     | true
        "True"          | Boolean     | true
        "yes"           | Boolean     | true
        "1"             | Boolean     | true
        "1.0"           | Boolean     | true
        1I              | Boolean     | true
        1F              | Boolean     | true
        true            | Boolean     | true
        "f"             | Boolean     | false
        "false"         | Boolean     | false
        "FALSE"         | Boolean     | false
        "False"         | Boolean     | false
        "no"            | Boolean     | false
        "0"             | Boolean     | false
        "0.0"           | Boolean     | false
        0I              | Boolean     | false
        0F              | Boolean     | false
        false           | Boolean     | false
        "x"             | Boolean     | false
        ["x", "y"]      | String      | "[x, y]"
        ["x", "y"]      | Collection  | ["x", "y"]
    }

    @Unroll("#featureName: #object to #targetClass")
    def "convert invalid examples"() {
        when:
        ObjectUtil.convert(object, targetClass)

        then:
        thrown(IllegalArgumentException)

        where:
        object       | targetClass
        "3.3"        | Integer
        3.3          | Byte
        3.3          | Short
        3.3          | Integer
        3.3          | Long
        3.3          | BigInteger
        -274         | Byte
        274          | Byte
        -50000       | Short
        50000        | Short
        -3147483648L | Integer
        3147483648L  | Integer
    }
}
