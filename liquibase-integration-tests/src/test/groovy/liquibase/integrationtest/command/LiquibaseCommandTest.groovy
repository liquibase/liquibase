package liquibase.integrationtest.command

import liquibase.command.CommandScope
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.core.HsqlDatabase
import liquibase.database.jvm.JdbcConnection
import liquibase.integrationtest.TestDatabaseConnections
import liquibase.integrationtest.TestSetup
import liquibase.util.StringUtil
import spock.lang.Specification
import spock.lang.Unroll

import java.util.regex.Pattern

import static org.junit.Assume.assumeTrue
import static org.spockframework.util.Assert.fail

class LiquibaseCommandTest extends Specification {

    @Unroll("Execute on #specPermutation.databaseName: #specPermutation.spec.description")
    def "execute valid spec"() {
        setup:
        assumeTrue("Skipping test: " + specPermutation.connectionStatus.errorMessage, specPermutation.connectionStatus.connection != null)

        def spec = specPermutation.spec

        List expectedOutputChecks = new ArrayList()
        if (spec.expectedOutput instanceof List) {
            expectedOutputChecks.addAll(spec.expectedOutput)
        } else {
            expectedOutputChecks.add(spec.expectedOutput)
        }

        if (spec.setup != null) {
            for (def setup : spec.setup) {
                setup.setup(specPermutation.connectionStatus)
            }
        }

        when:
        def commandScope = new CommandScope(spec.command as String[])

        def outputStream = new ByteArrayOutputStream()

        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(specPermutation.connectionStatus.connection))

        commandScope.addArgumentValue("database", database)
        commandScope.setOutput(outputStream)

        commandScope.execute()

        def fullOutput = StringUtil.standardizeLineEndings(StringUtil.trimToEmpty(outputStream.toString()))

        then:
//        def e = thrown(spec.expectedException)

        if (spec.expectedException != null) {
            assert e.toString() == spec.expectedException.toString()
            return
        }

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
                assert expectedOutputCheck.matcher(fullOutput.replace("\r", "").trim()).find() : """
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


        for (def returnedResult : commandScope.getResults().entrySet()) {
            def expectedValue = String.valueOf(spec.expectedResults.get(returnedResult.getKey()))
            def seenValue = String.valueOf(returnedResult.getValue())

            assert expectedValue != "null": "No expectedResult for returned result '" + returnedResult.getKey() + "' of: " + seenValue
            assert seenValue == expectedValue
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
            if (!(database instanceof HsqlDatabase)) {
                continue
            }

            for (Spec spec : allSpecs)
                returnList.add(new SpecPermutation(
                        spec: spec,
                        databaseName: database.shortName,
                        connectionStatus: TestDatabaseConnections.getInstance().getConnection(database.shortName),
                ))
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
    }
}
