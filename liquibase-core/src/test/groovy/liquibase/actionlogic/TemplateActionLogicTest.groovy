package liquibase.actionlogic

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.action.MockAction
import liquibase.action.QuerySqlAction
import liquibase.sdk.database.MockDatabase
import liquibase.test.JUnitResourceAccessor
import spock.lang.Specification
import spock.lang.Unroll

class TemplateActionLogicTest extends Specification {

    @Unroll()
    def "priority correctly set"() {
        when:
        def logic = new TemplateActionLogic(spec)

        then:
        logic.getPriority(action, JUnitScope.instance) == priority
        logic.getPriority(new QuerySqlAction("select * from dual"), JUnitScope.instance) == -1

        where:
        spec                                                  | action           | priority
        ">priority 1\n>action:liquibase.action.MockAction"    | new MockAction() | 1
        ">PRIORITY 12\n>Action   liquibase.action.MockAction" | new MockAction() | 12
    }

    @Unroll()
    def "validation logic set correctly from headers"() {
        when:
        def logic = new TemplateActionLogic(">priority 1\n>action:liquibase.action.MockAction\n" + spec)

        then:
        logic.validate(new MockAction(action), JUnitScope.getInstance(new MockDatabase())).toString() == expected

        where:
        spec                                  | action                  | expected
        ""                                    | [tableName: "my_table"] |  "No errors"
        ">required: columnName"               | [tableName: "my_table"] | "columnName is required"
        ">required: tableName"                | [tableName: "my_table"] | "No errors"
        ">required: "                         | [tableName: "my_table"] | "No errors"
        ">required: tableName, columnName"    | [tableName: "my_table"] | "columnName is required"
        ">unsupported: tableName"             | [tableName: "my_table"] | "tableName is not allowed on mock"
        ">unsupported: tableName, columnName" | [tableName: "my_table"] | "tableName is not allowed on mock"
        ">unsupported: tableName, columnName" | [columnName: "my_col"]  | "columnName is not allowed on mock"
    }

    @Unroll()
    def "parse errors from bad headers"() {
        when:
        new TemplateActionLogic(spec)

        then:
        def e = thrown(TemplateActionLogic.ParseException)
        e.message == message

        where:
        spec                                         | message
        null                                         | "Null specification"
        ""                                           | "Missing '>priority: ##' configuration"
        ">priority: -5"                              | "Priority '>priority: -5' must be greater than zero"
        ">priority: 5"                               | "Missing '>action: com.example.ActionClass' configuration"
        ">priority: 5\n>action: liquibase.Liquibase" | "Class in '>action: liquibase.Liquibase' must implement liquibase.action.Action"
        ">priority: 5\n>action: liquibase.Invalid"   | "Cannot find action class liquibase.Invalid"
    }

    @Unroll()
    def "parse errors from bad template"() {
        when:
        def action = new MockAction(new HashMap<String, Object>())
        new TemplateActionLogic(">priority:1\n>action:liquibase.action.MockAction\n" + template).fillTemplate(action, JUnitScope.instance)

        then:
        def e = thrown(TemplateActionLogic.ParseException)
        e.message == message

        where:
        template                                                            | message
        "#if(\$name) something #if(\$other) nested if #end #end"            | "Cannot nest #if statements"
        "#if(\$name) something #else first else #else other else #end #end" | "Cannot include multiple #else clauses in an #if statement"
    }

    @Unroll
    def "comment examples"() {
        when:
        def action = new MockAction([
                name         : "this test",
                type         : "number(23)",
                complexString: "  a string    \$with #stuff  \$name    ",
                schemaName   : "my_schema",
                catalogName  : "my_catalog",
        ])


        def scope = JUnitScope.getInstance(new MockDatabase()).child("scopevalue", 551)

        then:
        new TemplateActionLogic(template).fillTemplate(action, scope) == expected

        where:
        template                                                                                                       | expected
        ">priority: 10\n>action:liquibase.action.MockAction\n##a line comment"                                         | ""
        "##start comment\n>priority: 10\n>action:liquibase.action.MockAction\n##a line comment\nreal text\nand more\n" | "real text and more"
    }

    @Unroll()
    def "fillTemplate examples"() {
        when:
        def action = new MockAction([
                name         : "this test",
                type         : "number(23)",
                complexString: "  a string    \$with #stuff  \$name    ",
                schemaName   : "my_schema",
                catalogName  : "my_catalog",
        ])


        def scope = JUnitScope.getInstance(new MockDatabase()).child("scopeValue", 551)

        then:
        new TemplateActionLogic(">priority:1\n>action:liquibase.action.MockAction\n" + template).fillTemplate(action, scope) == expected

        where:
        template                                                                                                                           | expected
        ""                                                                                                                                 | ""
        "      "                                                                                                                           | ""
        "a simple template"                                                                                                                | "a simple template"
        "  whitespace   is collapsed    to a \n  single  space.\nEven \nother\nlines   "                                                   | "whitespace is collapsed to a single space. Even other lines"
        "with a \$name set"                                                                                                                | "with a this test set"
        "\$name and again \$name"                                                                                                          | "this test and again this test"
        "\$name is type \$type"                                                                                                            | "this test is type number(23)"
        "Can see scope value \$scopeValue and \$name"                                                                                      | "Can see scope value 551 and this test"
        "I know \$name but not \$unset"                                                                                                    | "I know this test but not \$unset"
        "  spaces    still correct    in \$name name"                                                                                      | "spaces still correct in this test name"
        "complex: '\$complexString'"                                                                                                       | "complex: '  a string    \$with #stuff  \$name    '"
        "we can reference #escapeName(\$schemaName, \$catalogName, \$name, Sequence.class) things"                                         | "we can reference `my_schema`.`my_catalog`.`this test` things"
        "short reference #escapeName(\$name, Catalog) and \$type"                                                                          | "short reference `this test` and number(23)"
        "\$catalogName   and \n \$schemaName go to   \n\n  #escapeName(\$catalogName, Table   ) or #escapeName(   \$schemaName, Table   )" | "my_catalog and my_schema go to `my_catalog` or `my_schema`"
    }

    @Unroll()
    def "fillTemplate with if statements"() {
        when:
        def action = new MockAction([
                name         : "obj_name",
                type         : "number(23)",
                complexString: "  a string    \$with #stuff  \$name    ",
                schemaName   : "my_schema",
                catalogName  : "my_catalog",
                booleanTrue  : true,
                booleanFalse : false,
        ])


        def scope = JUnitScope.getInstance(new MockDatabase()).child("scopeValue", 551)

        then:
        new TemplateActionLogic(">priority:1\n>action:liquibase.action.MockAction\n" + template).fillTemplate(action, scope) == expected

        where:
        template                                                                       | expected
        "start string #if(\$name) \$name #end then the rest"                           | "start string obj_name then the rest"
        "start string     #if    ( \$name ) \$name     #end then the rest"             | "start string obj_name then the rest"
        "start string#if(\$name)\$name#end then the rest"                              | "start string obj_name then the rest"
        "start string #if(\$name)\n    \$name\n#end\nthen the rest"                    | "start string obj_name then the rest"
        "invalid string #if(\$invalid) \$name #end then the rest"                      | "invalid string then the rest"
        "boolean true #if (\$booleanTrue) body value #end then the rest"               | "boolean true body value then the rest"
        "boolean false #if (\$booleanFalse) body value #end then the rest"             | "boolean false then the rest"
        "boolean false#if(\$booleanFalse)body value#end then the rest"                 | "boolean false then the rest"
        "branch test #if(\$name) main branch #else other branch #end then the rest"    | "branch test main branch then the rest"
        "branch test #if(\$invalid) main branch #else other branch #end then the rest" | "branch test other branch then the rest"
    }
}
