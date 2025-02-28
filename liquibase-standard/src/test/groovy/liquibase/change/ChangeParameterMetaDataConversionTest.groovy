package liquibase.change

import com.example.liquibase.change.ChangeWithPrimitiveFields
import liquibase.serializer.LiquibaseSerializable
import spock.lang.Specification
import spock.lang.Unroll

class ChangeParameterMetaDataConversionTest extends Specification {

    @Unroll
    def "supports invoking primitive setter with #boxedValue boxed value and expect #expectedResult"() {
        when:
        def change = new ChangeWithPrimitiveFields()
        def metaData = new ChangeParameterMetaData(change, parameterName, parameterName, "", new HashMap<String, Object>(), "", type, new String[0], new String[0], "", LiquibaseSerializable.SerializationType.DIRECT_VALUE)

        and:
        metaData.setValue(change, boxedValue)

        then:
        getter(change) == expectedResult

        where:
        parameterName | type          | boxedValue                    | getter                                           | expectedResult
        "aBoolean"       | boolean.class | Boolean.TRUE                  | { ChangeWithPrimitiveFields it -> it.isaBoolean() }    | true
        "aByte"       | byte.class    | Byte.valueOf((byte) 10)       | { ChangeWithPrimitiveFields it -> it.getaByte() }   | (byte) 10
        "aChar"       | char.class    | Character.valueOf((char) 'a') | { ChangeWithPrimitiveFields it -> it.getaChar() }   | (char) 'a'
        "aDouble"     | double.class  | Double.valueOf(20.0d)         | { ChangeWithPrimitiveFields it -> it.getaDouble() } | 20.0d
        "aFloat"      | float.class   | Float.valueOf(30.0f)          | { ChangeWithPrimitiveFields it -> it.getaFloat() }  | 30.0f
        "anInt"       | int.class     | Integer.valueOf(40)           | { ChangeWithPrimitiveFields it -> it.getAnInt() }   | 40
        "aLong"       | long.class    | Long.valueOf(50L)             | { ChangeWithPrimitiveFields it -> it.getaLong() }   | 50L
        "aShort"      | short.class   | Short.valueOf((short) 60)     | { ChangeWithPrimitiveFields it -> it.getaShort() }  | (short) 60
    }

}
