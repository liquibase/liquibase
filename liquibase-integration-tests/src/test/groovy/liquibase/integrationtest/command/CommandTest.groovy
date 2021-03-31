package liquibase.integrationtest.command

import liquibase.CatalogAndSchema
import liquibase.command.CommandScope
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.integrationtest.TestDatabaseConnections
import liquibase.integrationtest.TestFilter
import liquibase.integrationtest.TestSetup
import liquibase.util.StringUtil
import spock.lang.Specification
import spock.lang.Unroll

import java.util.regex.Pattern

import static org.junit.Assume.assumeTrue
import static org.spockframework.util.Assert.fail

class CommandTest extends Specification {

    @Unroll("Execute on db:#specPermutation.databaseName,command:#specPermutation.spec.description")
    def "execute valid spec"() {
        setup:
        assumeTrue("Skipping test: " + specPermutation.connectionStatus.errorMessage, specPermutation.connectionStatus.connection != null)

        def spec = specPermutation.spec

        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(specPermutation.connectionStatus.connection))

        String defaultSchemaName = database.getDefaultSchemaName()
        CatalogAndSchema[] catalogAndSchemas = new CatalogAndSchema[1]
        catalogAndSchemas[0] = new CatalogAndSchema(null, defaultSchemaName)
        database.dropDatabaseObjects(catalogAndSchemas[0])

        List expectedOutputChecks = new ArrayList()
        if (spec.expectedOutput instanceof List) {
            expectedOutputChecks.addAll(spec.expectedOutput)
        } else {
            expectedOutputChecks.add(spec.expectedOutput)
        }

        String changeLogFile = null
        if (spec.setup != null) {
            for (def setup : spec.setup) {
                setup.setup(specPermutation.connectionStatus)
                if (setup.getChangeLogFile() != null) {
                    changeLogFile = setup.getChangeLogFile()
                }
            }
        }

        when:
        def commandScope
        try {
            commandScope = new CommandScope(spec.command as String[])
        }
        catch (Throwable e) {
            if (spec.expectedException != null) {
                assert e.class == spec.expectedException
            }
            throw new RuntimeException(e)
        }
        assert commandScope != null
        def outputStream = new ByteArrayOutputStream()

        commandScope.addArgumentValue("database", database)
        commandScope.addArgumentValue("url", database.getConnection().getURL())
        commandScope.addArgumentValue("schemas", catalogAndSchemas)
        commandScope.addArgumentValue("logLevel", "FINE")
        commandScope.setOutput(outputStream)
        if (changeLogFile != null) {
            commandScope.addArgumentValue("changeLogFile", changeLogFile)
        }
        if (spec.arguments != null) {
            spec.arguments.each { name, value ->
                Object objValue = (Object) value
                commandScope.addArgumentValue(name, objValue)
            }
        }

        def results = commandScope.execute()

        def fullOutput = StringUtil.standardizeLineEndings(StringUtil.trimToEmpty(outputStream.toString()))

        for (def returnedResult : results.getResults().entrySet()) {
            def expectedValue = String.valueOf(spec.expectedResults.get(returnedResult.getKey()))
            def seenValue = String.valueOf(returnedResult.getValue())

            assert expectedValue != "null": "No expectedResult for returned result '" + returnedResult.getKey() + "' of: " + seenValue
            assert seenValue == expectedValue
        }

        then:

//        def e = thrown(spec.expectedException)

        for (def expectedOutputCheck : expectedOutputChecks) {
            if (expectedOutputCheck instanceof String) {
                assert fullOutput.contains(StringUtil.standardizeLineEndings(StringUtil.trimToEmpty(expectedOutputCheck))): """
Command output:
-----------------------------------------
${fullOutput}
-----------------------------------------
Did not contain:
-----------------------------------------
${expectedOutputCheck}
-----------------------------------------
""".trim()
            } else if (expectedOutputCheck instanceof Pattern) {
                assert expectedOutputCheck.matcher(fullOutput.replace("\r", "").trim()).find(): """
Command output:
-----------------------------------------
${fullOutput}
-----------------------------------------
Did not match regexp:
-----------------------------------------
${expectedOutputCheck.toString()}
-----------------------------------------
""".trim()
            } else {
                fail "Unknown expectedOutput check: ${expectedOutputCheck.class.name}"
            }
        }


        where:
        specPermutation << collectSpecPermutations()
    }

    static List<File> collectSpecFiles() {
        def returnFiles = new ArrayList<File>()

        ("src/test/resources/liquibase/integrationtest/command/" as File).eachFileRecurse {
            if (it.name.endsWith("test.groovy")) {
                returnFiles.add(it)
            }
        }

        return returnFiles
    }

    static List<Spec> collectSpecs() {
        def loader = new GroovyClassLoader()
        def returnList = new ArrayList<Spec>()

        for (def specFile : collectSpecFiles()) {
            def specClass = loader.parseClass(specFile)
            for (def specObj : ((Script) specClass.newInstance()).run()) {
                if (!specObj instanceof Spec) {
                    fail "$specFile must contain an array of LiquibaseCommandTest.Spec objects"
                }

                def spec = (Spec) specObj
                if (spec.description == null) {
                    spec.description = StringUtil.join(spec.command, " ")
                }

                returnList.add(spec)
            }
        }

        return returnList
    }

    static List<SpecPermutation> collectSpecPermutations() {
        def returnList = new ArrayList<SpecPermutation>()
        def allSpecs = collectSpecs()

        for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
            for (Spec spec : allSpecs) {
                def permutation = new SpecPermutation(
                        spec: spec,
                        databaseName: database.shortName,
                )
                if (!permutation.shouldRun()) {
                    continue
                }

                permutation.connectionStatus = TestDatabaseConnections.getInstance().getConnection(database.shortName)
                returnList.add(permutation)
            }
        }

        return returnList
    }

    static class Spec {

        /**
         * Description of this test for reporting purposes.
         * If not set, one will be generated for you.
         */
        String description

        /**
         * Command to execute
         */
        List<String> command

        /**
         * Arguments to command as key/value pairs
         */
        Map<String, Object> arguments = new HashMap<>()

        List<TestSetup> setup


        /**
         * Checks for the command output.
         * <li>If a string, assert that the output CONTAINS the string.
         * <li>If a regexp, assert that the regexp FINDs the string.
         */
        List<Object> expectedOutput

        Map<String, Object> expectedResults = new HashMap<>()

        Class<Throwable> expectedException
    }

    private static class SpecPermutation {
        Spec spec
        String databaseName
        TestDatabaseConnections.ConnectionStatus connectionStatus
        String changeLogFile

        boolean shouldRun() {
            def filter = TestFilter.getInstance()

            return filter.shouldRun(TestFilter.DB, databaseName) &&
                    filter.shouldRun("command", spec.command.get(0))
        }
    }
}
