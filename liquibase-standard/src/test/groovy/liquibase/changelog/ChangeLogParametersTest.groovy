package liquibase.changelog

import liquibase.*
import liquibase.database.core.MockDatabase
import liquibase.database.core.MySQLDatabase
import liquibase.exception.ChangeLogParseException
import liquibase.exception.UnexpectedLiquibaseException
import liquibase.exception.UnknownChangeLogParameterException
import liquibase.parser.ChangeLogParserConfiguration
import spock.lang.Specification
import spock.lang.Unroll

class ChangeLogParametersTest extends Specification {

    @Unroll
    def "correctly finds values"() {
        when:
        def changelog = new DatabaseChangeLog("com/example/changelog.txt")
        def childChangelog = new DatabaseChangeLog("com/example/child.txt")
        def siblingChangelog = new DatabaseChangeLog("com/example/sibling.txt")

        def database = new MockDatabase()

        System.setProperty("path", "system property overriding env variable")
        def changeLogParameters = new ChangeLogParameters(database)

        changeLogParameters.set("param1", "value 1")
        changeLogParameters.set("doubleSet", "originalValue")
        changeLogParameters.set("doubleSet", "newValue")
        changeLogParameters.set("param2", "value 2")
        changeLogParameters.set("user.home", "overridden system property")
        changeLogParameters.set("path", "overridden env property")
        changeLogParameters.set("database.currentDateTimeFunction", "overridden currentDateTimeFunction")

        changeLogParameters.set("dbmsProperty", "h2 value", new ContextExpression(), new Labels(), "h2", true, changelog)
        changeLogParameters.set("dbmsProperty", "oracle value", new ContextExpression(), new Labels(), "oracle", true, changelog)
        changeLogParameters.set("dbmsProperty", "mysql,hsqldb value", new ContextExpression(), new Labels(), "mysql,hsqldb", true, changelog)
        changeLogParameters.set("dbmsProperty", "fallback dbms value", new ContextExpression(), new Labels(), null, true, changelog)

        changeLogParameters.set("dbmsAndContext", "h2 context1 value", new ContextExpression("context1"), new Labels(), "h2", true, changelog)
        changeLogParameters.set("dbmsAndContext", "h2 context2 value", new ContextExpression("context2"), new Labels(), "h2", true, changelog)
        changeLogParameters.set("dbmsAndContext", "h2 no context value", new ContextExpression(), new Labels(), "h2", true, changelog)

        changeLogParameters.set("labelParam", "labelParam value from label 1", new ContextExpression(), new Labels("label1"), null, true, changelog)
        changeLogParameters.set("labelParam", "labelParam value from label 2", new ContextExpression(), new Labels("label2"), null, true, changelog)

        changeLogParameters.set("globalsAcrossFiles", "globalsAcrossFiles from base", new ContextExpression(), new Labels(), null, true, changelog)
        changeLogParameters.set("globalsAcrossFiles", "globalsAcrossFiles from child", new ContextExpression(), new Labels(), null, true, childChangelog)
        changeLogParameters.set("globalsAcrossFiles", "globalsAcrossFiles from sibling", new ContextExpression(), new Labels(), null, true, siblingChangelog)

        //if a local parameter redefines a global parameter, the global parameter still takes precedence
        changeLogParameters.set("localVsGlobal", "localVsGlobal as global", new ContextExpression(), new Labels(), null, true, changelog)
        changeLogParameters.set("localVsGlobal", "localVsGlobal as local", new ContextExpression(), new Labels(), null, false, childChangelog)

        changeLogParameters.setDatabase(null)  //clear regardless, to control whether we are filtering by a db or not
        if (filter != null) {
            if (filter["contexts"] != null) {
                changeLogParameters.setContexts(new Contexts((String) filter["contexts"]))
            }
            if (filter["labels"] != null) {
                changeLogParameters.setLabels(new LabelExpression((String) filter["labels"]))
            }
            changeLogParameters.setDatabase((String) filter["db"])
        }

        then:
        changeLogParameters.getValue(key, childChangelog) == expected
        changeLogParameters.hasValue(key, childChangelog) == (expected != null)

        where:
        key                                | filter                               | expected
        "invalid"                          | null                                 | null
        "param1"                           | null                                 | "value 1"
        "PARAM1"                           | null                                 | "value 1"
        "param1"                           | [db: "h2"]                           | "value 1" //filter matches param with no settings
        "param2"                           | null                                 | "value 2"
        "doubleSet"                        | null                                 | "originalValue"
        "user.home"                        | null                                 | System.getProperty("user.home") //system property takes priority
        "PATH"                             | null                                 | System.getenv("PATH") //env variable takes priority over everything
        "path"                             | null                                 | System.getenv("PATH") //env variable takes priority over everything
        "database.currentDateTimeFunction" | null                                 | "DATETIME()" //database takes priority over set value
        "dbmsProperty"                     | [db: "h2"]                           | "h2 value"
        "dbmsProperty"                     | [db: "oracle"]                       | "oracle value"
        "dbmsProperty"                     | [db: "mysql"]                        | "mysql,hsqldb value"
        "dbmsProperty"                     | [db: "hsqldb"]                       | "mysql,hsqldb value"
        "dbmsProperty"                     | [db: "derby"]                        | "fallback dbms value"
        "dbmsProperty"                     | null                                 | "h2 value" //doesn't care about the database setting
        "dbmsAndContext"                   | [db: "h2", contexts: "context1"]     | "h2 context1 value"
        "dbmsAndContext"                   | [db: "h2", contexts: "context2"]     | "h2 context2 value"
        "dbmsAndContext"                   | [db: "h2"]                           | "h2 context1 value" // takes the first one set regardless of context
        "dbmsAndContext"                   | [db: "oracle", contexts: "context1"] | null // no match since db doesn't match, even though context does
        "labelParam"                       | [labels: "label1"]                    | "labelParam value from label 1"
        "labelParam"                       | [labels: "label2"]                    | "labelParam value from label 2"
        "labelParam"                       | [labels: "invalid"]                   | null
        "globalsAcrossFiles"               | null                                 | "globalsAcrossFiles from base"
        "localVsGlobal"                    | null                                 | "localVsGlobal as global"
    }


    @Unroll
    def "getValue with null changelog"() {
        when:
        def database = new MockDatabase()

        def changeLogParameters = new ChangeLogParameters(database)

        changeLogParameters.set("param1", "value 1")

        then:
        changeLogParameters.getValue("param1", null) == "value 1"
        changeLogParameters.hasValue("param1", null)
    }

    def expandExpressions_MissingParameterThrow() throws Exception {
        when:
        Scope.child(Collections.singletonMap(ChangeLogParserConfiguration.MISSING_PROPERTY_MODE.getKey(), ChangeLogParserConfiguration.MissingPropertyMode.ERROR), {
            ->
            ChangeLogParameters changeLogParameters = new ChangeLogParameters(new MySQLDatabase())
            DatabaseChangeLog changeLog = new DatabaseChangeLog("db_changelog.yml")
            changeLogParameters.set("bytesarray_type", "BYTEA", new ContextExpression(), new Labels(), "postgresql", false, changeLog)
            changeLogParameters.set("bytesarray_type", "java.sql.Types.BLOB", new ContextExpression(), new Labels(), "hana", false, changeLog)
            changeLog.setChangeLogParameters(changeLogParameters)

            changeLogParameters.expandExpressions("\${bytesarray_type}", changeLog)
        } as Scope.ScopedRunner)

        then:
        def e = thrown(UnknownChangeLogParameterException)
        e.getMessage().contains("Could not resolve expression")
    }

    def expandExpressions_MissingParameterEmpty() throws Exception {
        when:
        def expanded = Scope.child(Collections.singletonMap(ChangeLogParserConfiguration.MISSING_PROPERTY_MODE.getKey(), ChangeLogParserConfiguration.MissingPropertyMode.EMPTY), {
            ->
            ChangeLogParameters changeLogParameters = new ChangeLogParameters(new MySQLDatabase())
            DatabaseChangeLog changeLog = new DatabaseChangeLog("db_changelog.yml")
            changeLogParameters.set("bytesarray_type", "BYTEA", new ContextExpression(), new Labels(), "postgresql", false, changeLog)
            changeLogParameters.set("bytesarray_type", "java.sql.Types.BLOB", new ContextExpression(), new Labels(), "hana", false, changeLog)
            changeLog.setChangeLogParameters(changeLogParameters)

            return changeLogParameters.expandExpressions("12\${bytesarray_type}34", changeLog)
        } as Scope.ScopedRunnerWithReturn)

        then:
        expanded == "1234"
    }
}
