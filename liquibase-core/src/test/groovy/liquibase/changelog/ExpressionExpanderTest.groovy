package liquibase.changelog

import liquibase.Scope
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.DatabaseChangeLog
import liquibase.changelog.ExpressionExpander
import liquibase.exception.ChangeLogParseException
import liquibase.parser.ChangeLogParserConfiguration
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.parser.ChangeLogParserConfiguration.MissingPropertyMode.*

class ExpressionExpanderTest extends Specification {

    @Unroll
    def "expandExpressions"() {
        when:
        def params = new ChangeLogParameters();
        params.set("param1", "value1");
        params.set("param2", "value2");
        params.set("param.value", "dot value");
        params.set("value1", "nested value1");

        def changelog = new DatabaseChangeLog()
        changelog.setChangeLogParameters(params)


        then:
        Scope.child([
                (ChangeLogParserConfiguration.MISSING_PROPERTY_MODE.key)    : mode,
                (ChangeLogParserConfiguration.SUPPORT_PROPERTY_ESCAPING.key): true,
        ], { ->
            assert new ExpressionExpander(params).expandExpressions(input, changelog) == expected
        } as Scope.ScopedRunner)

        where:
        mode     | input                                                                                             | expected
        PRESERVE | null                                                                                              | null
        PRESERVE | ""                                                                                                | ""
        PRESERVE | "A Simple String"                                                                                 | "A Simple String"
        PRESERVE | "With { brackets } in it"                                                                         | "With { brackets } in it"
        PRESERVE | "A string with one expression \${param1} set"                                                     | "A string with one expression value1 set"
        PRESERVE | "A string with two expressions \${param1} and \${param2} set"                                     | "A string with two expressions value1 and value2 set"
        EMPTY    | "A string with two expressions \${param1} and \${param2} set"                                     | "A string with two expressions value1 and value2 set"
        ERROR    | "A string with two expressions \${param1} and \${param2} set"                                     | "A string with two expressions value1 and value2 set"
        PRESERVE | "A string no expressions \${notset} or \${  noset } set"                                          | "A string no expressions \${notset} or \${  noset } set"
        EMPTY    | "A string no expressions \${notset} or \${  noset } set"                                          | "A string no expressions  or  set"
        PRESERVE | "A string no expressions \${notset.orParams} set"                                                 | "A string no expressions \${notset.orParams} set"
        EMPTY    | "A string no expressions \${notset.orParams} set"                                                 | "A string no expressions  set"
        PRESERVE | "\${: user.name}"                                                                                 | "\${user.name}"
        EMPTY    | "\${: user.name}"                                                                                 | "\${user.name}"
        ERROR    | "\${: user.name}"                                                                                 | "\${user.name}"
        PRESERVE | "\${: user.name}\${: user.name}"                                                                  | "\${user.name}\${user.name}"
        PRESERVE | "\${: user.name} and \${: user.name} are literals"                                                | "\${user.name} and \${user.name} are literals"
        PRESERVE | "\${: param1 } and \${: param.value} are literals but this isn't: \${param.value}"                | "\${param1} and \${param.value} are literals but this isn't: dot value"
        PRESERVE | "\${: param.value} is a literal, \${param.value} is a variable"                                   | "\${param.value} is a literal, dot value is a variable"
        PRESERVE | "\${param.value} is a variable, \${: param.value} is a literal"                                   | "dot value is a variable, \${param.value} is a literal"
        PRESERVE | "\${param1} is a variable, \${: param1} and \${: param2} are literals but this isn't: \${param2}" | "value1 is a variable, \${param1} and \${param2} are literals but this isn't: value2"
        PRESERVE | "\${:\${:param1}} = \${\${param1}}"                                                               | "\${\${param1}} = nested value1"
        EMPTY    | "\${:\${:param1}} = \${\${param1}}"                                                               | "\${\${param1}} = nested value1"
        ERROR    | "\${:\${:param1}} = \${\${param1}}"                                                               | "\${\${param1}} = nested value1"
    }


    @Unroll
    def "expandExpressions with ERROR configured unknown properties"() {
        when:
        def params = new ChangeLogParameters();
        params.set("param1", "value1");
        params.set("param2", "value2");

        def changelog = new DatabaseChangeLog("test/file.xml")
        changelog.setChangeLogParameters(params)

        Scope.child(ChangeLogParserConfiguration.MISSING_PROPERTY_MODE.key, ERROR, { ->
            new ExpressionExpander(params).expandExpressions(input, changelog)
        })

        then:
        def e = thrown(ChangeLogParseException)
        e.message == expected

        where:
        input                                        | expected
        "One invalid \${unset}"                      | "Could not resolve expression `\${unset}` in file test/file.xml"
        "One invalid \${unset} one valid \${param1}" | "Could not resolve expression `\${unset}` in file test/file.xml"
        "One valid \${param1} one invalid \${unset}" | "Could not resolve expression `\${unset}` in file test/file.xml"

    }
}
