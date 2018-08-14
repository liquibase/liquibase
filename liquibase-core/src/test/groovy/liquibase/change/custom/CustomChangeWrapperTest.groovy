package liquibase.change.custom

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

    def setClassLoader() {
        when:
        URLClassLoader classLoader = new URLClassLoader(new URL[0])
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper()
        changeWrapper.setClassLoader(classLoader)

        then:
        assertSame(classLoader, changeWrapper.getClassLoader())
    }

    def setClass() throws CustomChangeException {
        when:
        CustomChangeWrapper changeWrapper = new CustomChangeWrapper()
        changeWrapper.setClassLoader(getClass().getClassLoader())
        changeWrapper.setClass(ExampleCustomSqlChange.class.getName())

        then:
        assert changeWrapper.getCustomChange() instanceof ExampleCustomSqlChange
        changeWrapper.getClassName() == ExampleCustomSqlChange.class.getName()
    }

//    @Test TODO: Cannot get this test to fire exception code
//    public void setClass_childClassLoader() throws Exception {
//        File testRootDir = new File(getClass().getResource("/"+ExampleCustomSqlChange.class.getName().replace(".","/")+".class").toURI()).getParentFile().getParentFile().getParentFile().getParentFile();
//        File liquibaseRootDir = new File(getClass().getResource("/"+CustomChange.class.getName().replace(".","/")+".class").toURI()).getParentFile().getParentFile().getParentFile().getParentFile();
//
//        ClassLoader childClassLoader = new URLClassLoader(new URL[] {testRootDir.toURI().toURL(), liquibaseRootDir.toURI().toURL()});
//        CustomChangeWrapper changeWrapper = new CustomChangeWrapper();
//        changeWrapper.setClassLoader(childClassLoader);
//
//        changeWrapper.setClass(ExampleCustomSqlChange.class.getName());
//
//        assert changeWrapper.getCustomChange() instanceof ExampleCustomSqlChange
//    }

    def setClass_classloaderNotSet() throws CustomChangeException {
        when:
        new CustomChangeWrapper().setClass(ExampleCustomSqlChange.class.getName())

        then:
        thrown(CustomChangeException.class)
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
        changeWrapper.setClassLoader(getClass().getClassLoader())
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
        changeWrapper.setClassLoader(getClass().getClassLoader())
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
        changeWrapper.setClassLoader(getClass().getClassLoader())
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
        changeWrapper.setClassLoader(getClass().getClassLoader())
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
        changeWrapper.setClassLoader(getClass().getClassLoader());
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
        change.classLoader != null
        change.resourceAccessor == resourceSupplier.simpleResourceAccessor
        change.getCustomChange() instanceof liquibase.change.custom.ExampleCustomSqlChange
        change.params.size() == 3
        change.getParamValue("param 1") == "param 1 value"
        change.getParamValue("param 2") == "param 2 value"
        change.getParamValue("param 3") == "param 3 value"

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
        change.classLoader != null
        change.resourceAccessor == resourceSupplier.simpleResourceAccessor
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
        change.classLoader != null
        change.resourceAccessor == resourceSupplier.simpleResourceAccessor
        change.getCustomChange() instanceof ExampleCustomSqlChange
        change.params.size() == 2
        change.getParamValue("tableName") == "my_table"
        change.getParamValue("columnName") == "my_col"
        change.getParamValue("unusedParam") == null
    }
}
