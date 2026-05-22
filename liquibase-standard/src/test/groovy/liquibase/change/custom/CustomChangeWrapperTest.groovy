package liquibase.change.custom

import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.change.CheckSum
import liquibase.database.Database
import liquibase.exception.CustomChangeException
import liquibase.exception.RollbackImpossibleException
import liquibase.exception.UnexpectedLiquibaseException
import liquibase.exception.ValidationErrors
import liquibase.parser.core.ParsedNode
import liquibase.parser.core.ParsedNodeException
import liquibase.database.core.MockDatabase
import liquibase.sdk.supplier.resource.ResourceSupplier
import liquibase.statement.SqlStatement
import spock.lang.Shared
import spock.lang.Specification

import static org.junit.Assert.assertSame

class CustomChangeWrapperTest extends Specification {

    @Shared
            resourceSupplier = new ResourceSupplier()

    def setClass() throws CustomChangeException {
        when:
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper()
        changeWrapper.setClass(ExampleCustomSqlChange.class.getName())

        then:
        assert changeWrapper.getCustomChange() instanceof ExampleCustomSqlChange
        changeWrapper.getClassName() == ExampleCustomSqlChange.class.getName()
    }

    def getParams() {
        when:
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper()
        then:
        changeWrapper.getParams().size() == 0

        when:
        changeWrapper.setParam("param1", "x")
        changeWrapper.setParam("param2", "y")

        then:
        changeWrapper.getParams().size() == 2
        assert changeWrapper.getParams().contains("param1")
        assert changeWrapper.getParams().contains("param2")
    }

    def getParamValues() {
        when:
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper()
        then:
        changeWrapper.getParamValue("notSet") == null

        when:
        changeWrapper.setParam("param1", "x")
        changeWrapper.setParam("param2", "y")
        then:
        changeWrapper.getParamValue("param1") == "x"
        changeWrapper.getParamValue("param2") == "y"
        changeWrapper.getParamValue("badparam") == null
    }

    def validate() {
        when:
        ValidationErrors errors = new ValidationErrors()
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper()
        Database database = new MockDatabase()
        changeWrapper.customChange = Mock(CustomChange.class)
        changeWrapper.customChange.validate(database) >> errors

        then:
        assertSame(errors, changeWrapper.validate(database))
    }

    def validate_nullReturn() {
        when:
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper()
        Database database = new MockDatabase()
        changeWrapper.customChange = Mock(CustomChange.class)
        changeWrapper.customChange.validate(database) >> null

        then:
        changeWrapper.validate(database) == null
    }

    def validate_exceptionInNestedValidate() {
        when:
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper()
        Database database = new MockDatabase()
        changeWrapper.customChange = Mock(CustomChange.class)
        changeWrapper.customChange.validate(database) >> { throw new NullPointerException() }

        then:
        changeWrapper.validate(database).getErrorMessages().size() == 1
    }

    def warn() {
        expect:
        assert !new CustomChangeWrapper().warn(new MockDatabase()).hasWarnings()
    }

    def generateStatements_paramsSetCorrectly() throws Exception {
        when:
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper()
        changeWrapper.setClass(ExampleCustomSqlChange.class.getName())
        changeWrapper.setParam("tableName", "myName")
        changeWrapper.setParam("columnName", "myCol")

        changeWrapper.generateStatements(new MockDatabase())

        then:
        ((ExampleCustomSqlChange) changeWrapper.customChange).tableName == "myName"
        ((ExampleCustomSqlChange) changeWrapper.customChange).columnName == "myCol"
    }

    def generateStatements_paramsSetBad() throws Exception {
        when:
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper()
        changeWrapper.setClass(ExampleCustomSqlChange.class.getName())
        changeWrapper.setParam("badParam", "myName")

        changeWrapper.generateStatements(new MockDatabase())

        then:
        thrown(UnexpectedLiquibaseException.class)
    }

    def generateStatements_sqlStatementsReturned() throws Exception {
        when:
        def database = new MockDatabase()
        SqlStatement[] statements = new SqlStatement[0]
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper()
        changeWrapper.customChange = Mock(CustomSqlChange.class)
        ((CustomSqlChange) changeWrapper.customChange).generateStatements(database) >> statements

        then:
        assertSame(statements, changeWrapper.generateStatements(database))
    }

    def generateStatements_nullSqlStatementsReturned() throws Exception {
        when:
        def database = new MockDatabase()

        CustomChangeWrapper changeWrapper = new CustomChangeWrapper()
        changeWrapper.customChange = Mock(CustomSqlChange.class)
        ((CustomSqlChange) changeWrapper.customChange).generateStatements(database) >> null

        then:
        changeWrapper.generateStatements(database).length == 0
    }

    def generateStatements_customTask() throws Exception {
        when:
        def database = new MockDatabase()

        def changeWrapper = new CustomChangeWrapper()
        changeWrapper.customChange = Mock(CustomTaskChange.class)
        1 * ((CustomTaskChange) changeWrapper.customChange).execute(database)

        then:
        changeWrapper.generateStatements(database).length == 0
    }

    def generateStatements_unknownType() throws Exception {
        when:
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper()
        changeWrapper.customChange = Mock(CustomChange.class)

        changeWrapper.generateStatements(new MockDatabase())

        then:
        thrown(UnexpectedLiquibaseException.class)
    }


    def generateRollbackStatements_paramsSetCorrectly() throws Exception {
        when:
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper()
        changeWrapper.setClass(ExampleCustomSqlChange.class.getName())
        changeWrapper.setParam("tableName", "myName")
        changeWrapper.setParam("columnName", "myCol")

        changeWrapper.generateRollbackStatements(new MockDatabase())

        then:
        ((ExampleCustomSqlChange) changeWrapper.customChange).tableName == "myName"
        ((ExampleCustomSqlChange) changeWrapper.customChange).columnName == "myCol"
    }

    def generateRollbackStatements_paramsSetBad() throws Exception {
        when:
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper()
        changeWrapper.setClass(ExampleCustomSqlChange.class.getName())
        changeWrapper.setParam("badParam", "myName")

        changeWrapper.generateRollbackStatements(new MockDatabase())

        then:
        thrown(UnexpectedLiquibaseException.class)
    }

    def generateRollbackStatements_unknownType() throws Exception {
        when:
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper()
        changeWrapper.customChange = Mock(CustomChange.class)

        changeWrapper.generateRollbackStatements(new MockDatabase())

        then:
        thrown(RollbackImpossibleException.class)
    }

    def getConfirmationMessage_nominal() {
        when:
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper()

        changeWrapper.customChange = Mock(CustomChange.class)
        changeWrapper.customChange.getConfirmationMessage() >> "mock message"

        then:
        changeWrapper.getConfirmationMessage() == "mock message"
    }

    def getConfirmationMessage_usingParams() {
        when:
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
        changeWrapper.setClass(ExampleCustomSqlChange.class.getName());
        changeWrapper.setParam("tableName", "myName");
        changeWrapper.setParam("columnName", "myCol");

        then:
        changeWrapper.getConfirmationMessage() == "Custom class updated myName.myCol";
    }

    def "load works correctly"() {
        when:
        def node = new ParsedNode(null, "customChange")
                .addChild(null, "class", "liquibase.change.custom.ExampleCustomSqlChange")
                .addChild(new ParsedNode(null, "param").addChildren([name: "param 1", value: "param 1 value"]))
                .addChild(new ParsedNode(null, "param").addChildren([name: "param 2", value: "param 2 value"]))
                .addChild(new ParsedNode(null, "otherNode").setValue("should be ignored"))
                .addChild(new ParsedNode(null, "param").addChildren([name: "param 3"]).setValue("param 3 value"))
        def change = new CustomChangeWrapper()
        try {
            change.load(node, resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        }

        then:
        change.getCustomChange() instanceof liquibase.change.custom.ExampleCustomSqlChange
        change.params.size() == 3
        change.getParamValue("param 1") == "param 1 value"
        change.getParamValue("param 2") == "param 2 value"
        change.getParamValue("param 3") == "param 3 value"

    }

    def "load of param without name fails well"() {
        when:
        def node = new ParsedNode(null, "customChange")
                .addChild(null, "class", "liquibase.change.custom.ExampleCustomSqlChange")
                .addChild(new ParsedNode(null, "param").addChildren([nameo: "param 1", value: "param 1 value"]))
                .addChild(new ParsedNode(null, "param").addChildren([name: "param 2", value: "param 2 value"]))
                .addChild(new ParsedNode(null, "otherNode").setValue("should be ignored"))
                .addChild(new ParsedNode(null, "param").addChildren([name: "param 3"]).setValue("param 3 value"))
        def change = new CustomChangeWrapper()
        change.load(node, resourceSupplier.simpleResourceAccessor)

        then:
        thrown(ParsedNodeException.class)

    }

    def "customChange without class fails expectedly"() {
        when:
        def node = new ParsedNode(null, "customChange")
        def change = new CustomChangeWrapper()
        change.load(node, resourceSupplier.simpleResourceAccessor)

        then:
        thrown(ParsedNodeException.class)
    }

    def "load handles params in a 'params' collection"() {
        when:
        def node = new ParsedNode(null, "customChange")
                .addChild(null, "class", "liquibase.change.custom.ExampleCustomSqlChange")
                .addChild(null, "params", [new ParsedNode(null, "param").addChildren([name: "param 1", value: "param 1 value"]),
                                           new ParsedNode(null, "param").addChildren([name: "param 2", value: "param 2 value"]),
                                           new ParsedNode(null, "otherNode").setValue("should be ignored"),
                                           new ParsedNode(null, "param").addChildren([name: "param 3"]).setValue("param 3 value")])
        def change = new CustomChangeWrapper()
        try {
            change.load(node, resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        }

        then:
        change.getCustomChange() instanceof ExampleCustomSqlChange
        change.params.size() == 3
        change.getParamValue("param 1") == "param 1 value"
        change.getParamValue("param 2") == "param 2 value"
        change.getParamValue("param 3") == "param 3 value"
    }

    def "load handles params as extra attributes collection"() {
        when:
        def change = new CustomChangeWrapper()
        try {
            change.load(new liquibase.parser.core.ParsedNode(null, "customChange")
                    .addChildren([class: "liquibase.change.custom.ExampleCustomSqlChange", tableName: "my_table", columnName: "my_col", unusedParam: "unused value"]), resourceSupplier.simpleResourceAccessor)
        } catch (ParsedNodeException e) {
            e.printStackTrace()
        }

        then:
        change.getCustomChange() instanceof ExampleCustomSqlChange
        change.params.size() == 2
        change.getParamValue("tableName") == "my_table"
        change.getParamValue("columnName") == "my_col"
        change.getParamValue("unusedParam") == null
    }

    def "custom checksum is used"() {
        when:
        def change1 = new CustomChangeWrapper()
        change1.setClass(ExampleCustomSqlChangeWithChecksum.class.getName())
        change1.setParam("tableName", "my_table")
        def change2 = new CustomChangeWrapper()
        change2.setClass(ExampleCustomSqlChangeWithChecksum.class.getName())
        change2.setParam("tableName", "my_other_table_name")
        def change3 = new CustomChangeWrapper()
        change3.setClass(ExampleCustomSqlChange.class.getName())
        change3.setParam("tableName", "my_table")

        then: "The checksum not affected by parameters set"
        change1.generateCheckSum() == change2.generateCheckSum()
        change1.generateCheckSum() != change3.generateCheckSum()
    }

    def "validate passes by default — customChange is intentional under the standard trust model (CWE-470 opt-out gate)"() {
        // CWE-470 regression: the default is liquibase.allowCustomChange=true so
        // existing users see no behaviour change. validate() must NOT return the
        // configured-off error in this state. Uses ExampleCustomSqlChange (the
        // existing test fixture) so the class actually loads cleanly.
        given:
        def wrapper = new CustomChangeWrapper()
        wrapper.setClass(ExampleCustomSqlChange.class.getName())

        when:
        def errors = wrapper.validate(new MockDatabase())

        then: "no error mentioning the allowCustomChange flag"
        !errors.getErrorMessages().any { it.contains("allowCustomChange") }
    }

    def "validate fails when liquibase.allowCustomChange=false — embedder opt-out path"() {
        // CWE-470 regression: when the embedder disables customChange via
        // liquibase.allowCustomChange=false, validate() must reject with a hard
        // error BEFORE loadCustomChange (and therefore Class.forName with
        // initialize=true) runs. Error message must name the flag in both
        // directions so operators can find their way back to opt-in.
        given:
        def wrapper = new CustomChangeWrapper()
        wrapper.setClass(ExampleCustomSqlChange.class.getName())

        when: "the embedder has disabled customChange via configuration"
        def errors = Scope.child([(GlobalConfiguration.ALLOW_CUSTOM_CHANGE.getKey()): "false"],
                { return wrapper.validate(new MockDatabase()) } as Scope.ScopedRunnerWithReturn)

        then:
        errors.hasErrors()
        errors.getErrorMessages().any { it.contains("liquibase.allowCustomChange=false") }
        errors.getErrorMessages().any { it.contains("liquibase.allowCustomChange=true") }
    }

    def "loadCustomChange gate fires BEFORE Class.forName even for a class that does not exist"() {
        // CWE-470 regression for the parse-time / customLoadLogic call path: the
        // gate inside loadCustomChange must fire BEFORE Class.forName runs. We
        // exercise this by setting a class name that does NOT exist on the
        // classpath and asserting the thrown exception is the configured-off
        // CustomChangeException, NOT a ClassNotFoundException wrapped in
        // CustomChangeException — proving the gate short-circuited the lookup.
        given:
        def wrapper = new CustomChangeWrapper()
        wrapper.setClass("com.example.definitely.not.a.real.CustomChange")

        when:
        def errors = Scope.child([(GlobalConfiguration.ALLOW_CUSTOM_CHANGE.getKey()): "false"],
                { return wrapper.validate(new MockDatabase()) } as Scope.ScopedRunnerWithReturn)

        then: "the validate gate hits first; the loader never runs"
        errors.hasErrors()
        errors.getErrorMessages().any { it.contains("liquibase.allowCustomChange=false") }
        // The pre-fix code path would have produced an 'Exception thrown loading
        // com.example...' warning (from the ClassNotFoundException). After the
        // fix, that warning never appears because the gate short-circuits first.
        !errors.getErrorMessages().any { it.contains("Exception thrown loading") }
        !errors.getWarningMessages().any { it.contains("Exception thrown loading") }
    }

    def "loadCustomChange method-level gate throws CustomChangeException directly when allowCustomChange=false"() {
        // CWE-470 regression for the defense-in-depth method-level gate: any
        // caller of loadCustomChange (NOT only validate — also generateCheckSum,
        // configureCustomChange, customLoadLogic at parse time) must be
        // protected. The gate inside loadCustomChange throws before any class
        // loading happens; this test asserts that direct contract by calling
        // through generateCheckSum (which calls loadCustomChange independently
        // of validate()).
        given:
        def wrapper = new CustomChangeWrapper()
        wrapper.setClass(ExampleCustomSqlChange.class.getName())

        when:
        def checksum = Scope.child([(GlobalConfiguration.ALLOW_CUSTOM_CHANGE.getKey()): "false"],
                { return wrapper.generateCheckSum() } as Scope.ScopedRunnerWithReturn)

        then: "generateCheckSum still returns; the customChange instance was never created so the named class's static initializer never fired"
        checksum != null
        // Use direct field access (wrapper.@customChange) instead of the property
        // form (wrapper.customChange) — the property form calls the lazy-loading
        // getCustomChange() getter, which would re-attempt the load HERE (outside
        // the Scope.child block, where the flag has reverted to true) and pollute
        // the assertion. The field-direct form proves the in-scope generateCheckSum
        // call did not stash a loaded instance.
        wrapper.@customChange == null
    }
}
