package liquibase.extension.testing.command

import liquibase.CatalogAndSchema
import liquibase.Scope
import liquibase.change.Change
import liquibase.command.CommandArgumentDefinition
import liquibase.command.CommandFactory
import liquibase.command.CommandResults
import liquibase.command.CommandScope
import liquibase.configuration.AbstractMapConfigurationValueProvider
import liquibase.configuration.ConfigurationValueProvider
import liquibase.configuration.LiquibaseConfiguration
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.extension.testing.TestDatabaseConnections
import liquibase.extension.testing.TestFilter
import liquibase.extension.testing.setup.*
import liquibase.hub.HubService
import liquibase.hub.core.MockHubService
import liquibase.integration.IntegrationConfiguration
import liquibase.util.FileUtil
import liquibase.util.StringUtil
import org.codehaus.groovy.control.CompilerConfiguration
import spock.lang.Specification
import spock.lang.Unroll

import java.util.logging.Level
import java.util.regex.Pattern

class CommandTests extends Specification {

    private static List<CommandTestDefinition> commandTestDefinitions

    private ConfigurationValueProvider propertiesProvider

    def setup() {
        def properties = new Properties()

        getClass().getClassLoader().getResources("liquibase.test.local.properties").each {
            it.withReader {
                properties.load(it)
            }
        }

        propertiesProvider = new AbstractMapConfigurationValueProvider() {
            @Override
            protected Map<?, ?> getMap() {
                return properties
            }

            @Override
            protected String getSourceDescription() {
                return "liquibase.test.local.properties"
            }

            @Override
            int getPrecedence() {
                return 1
            }
        }

        Scope.currentScope.getSingleton(LiquibaseConfiguration).registerProvider(propertiesProvider)
    }

    def cleanup() {
        Scope.currentScope.getSingleton(LiquibaseConfiguration).unregisterProvider(propertiesProvider)
    }

    @Unroll("#featureName: #commandTestDefinition.testFile.name")
    def "check CommandTest definition"() {
        expect:
        def commandDefinition = Scope.currentScope.getSingleton(CommandFactory).getCommand(commandTestDefinition.getCommand() as String[])
        assert commandDefinition != null: "Cannot find specified command ${commandTestDefinition.getCommand()}"

        assert commandTestDefinition.testFile.name == commandTestDefinition.getCommand().join("") + ".test.groovy": "Incorrect test file name"

        for (def runTest : commandTestDefinition.runTests) {
            for (def arg : runTest.arguments.keySet()) {
                assert commandDefinition.arguments.containsKey(arg): "Unknown argument '${arg}' in run ${runTest.description}"
            }
        }

        where:
        commandTestDefinition << getCommandTestDefinitions()
    }

    @Unroll("#featureName: #commandTestDefinition.testFile.name")
    def "check command signature"() {
        expect:
        def commandDefinition = Scope.currentScope.getSingleton(CommandFactory).getCommand(commandTestDefinition.getCommand() as String[])
        assert commandDefinition != null: "Cannot find specified command ${commandTestDefinition.getCommand()}"

        StringWriter signature = new StringWriter()
        signature.print """
Short Description: ${commandDefinition.getShortDescription() ?: "MISSING"}
Long Description: ${commandDefinition.getLongDescription() ?: "MISSING"}
"""
        signature.println "Required Args:"
        def foundRequired = false
        for (def argDef : commandDefinition.arguments.values()) {
            if (!argDef.required) {
                continue
            }
            foundRequired = true
            signature.println "  ${argDef.name} (${argDef.dataType.simpleName}) ${argDef.description ?: "MISSING DESCRIPTION"}"
        }
        if (!foundRequired) {
            signature.println "  NONE"
        }


        signature.println "Optional Args:"
        def foundOptional = false
        for (def argDef : commandDefinition.arguments.values()) {
            if (argDef.required) {
                continue
            }
            foundOptional = true
            signature.println "  ${argDef.name} (${argDef.dataType.simpleName}) ${argDef.description ?:  "MISSING DESCRIPTION"}"
            signature.println "    Default: ${argDef.defaultValueDescription}"
        }
        if (!foundOptional) {
            signature.println "  NONE"
        }


        assert StringUtil.standardizeLineEndings(StringUtil.trimToEmpty(signature.toString())) ==
               StringUtil.standardizeLineEndings(StringUtil.trimToEmpty(commandTestDefinition.signature))

        where:
        commandTestDefinition << getCommandTestDefinitions()
    }


    @Unroll("Run {db:#permutation.databaseName,command:#permutation.definition.commandTestDefinition.joinedCommand} #permutation.definition.description")
    def "run"() {
        setup:
        org.junit.Assume.assumeTrue("Skipping test: " + permutation.connectionStatus.errorMessage, permutation.connectionStatus.connection != null)

        def testDef = permutation.definition

        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(permutation.connectionStatus.connection))

        String defaultSchemaName = database.getDefaultSchemaName()
        CatalogAndSchema[] catalogAndSchemas = new CatalogAndSchema[1]
        catalogAndSchemas[0] = new CatalogAndSchema(null, defaultSchemaName)
        database.dropDatabaseObjects(catalogAndSchemas[0])

        List expectedOutputChecks = new ArrayList()
        if (testDef.expectedOutput instanceof List) {
            expectedOutputChecks.addAll(testDef.expectedOutput)
        } else {
            expectedOutputChecks.add(testDef.expectedOutput)
        }

        when:
        final commandScope
        try {
            commandScope = new CommandScope(testDef.commandTestDefinition.command as String[])
        }
        catch (Throwable e) {
            if (testDef.expectedException != null) {
                assert e.class == testDef.expectedException
            }
            throw new RuntimeException(e)
        }
        assert commandScope != null
        def outputStream = new ByteArrayOutputStream()

        commandScope.addArgumentValue("database", database)
        commandScope.addArgumentValue("url", database.getConnection().getURL())
        commandScope.addArgumentValue("schemas", catalogAndSchemas)
        //commandScope.addArgumentValue("logLevel", "INFO")
        commandScope.setOutput(outputStream)

        if (testDef.setup != null) {
            for (def setup : testDef.setup) {
                setup.setup(permutation.connectionStatus)
            }
        }

        if (testDef.arguments != null) {
            testDef.arguments.each { name, value ->
                String key;
                if (name instanceof CommandArgumentDefinition) {
                    key = name.getName()
                } else {
                    key = (String) name
                }
                Object objValue = (Object) value
                commandScope.addArgumentValue(key, objValue)
            }
        }

        def results = Scope.child([
                (IntegrationConfiguration.LOG_LEVEL.getKey()): Level.INFO,
                ("liquibase.plugin." + HubService.name): MockHubService
        ], {
                return commandScope.execute()
        } as Scope.ScopedRunnerWithReturn<CommandResults>)

        if (testDef.expectedResults.size() > 0 && results.getResults().isEmpty()) {
            throw new RuntimeException("Results were expected but none were found for " + testDef.commandTestDefinition.command)
        }

        then:
        for (def returnedResult : results.getResults().entrySet()) {
            def expectedValue = String.valueOf(testDef.expectedResults.get(returnedResult.getKey()))
            def seenValue = String.valueOf(returnedResult.getValue())

            assert expectedValue != "null": "No expectedResult for returned result '" + returnedResult.getKey() + "' of: " + seenValue
            assert seenValue == expectedValue
        }

        def fullOutput = StringUtil.standardizeLineEndings(StringUtil.trimToEmpty(outputStream.toString()))
        if (fullOutput.length() == 0) {
            assert true
        }

        if (fullOutput.length() > 0) {
            for (def expectedOutputCheck : expectedOutputChecks) {
                if (expectedOutputCheck == null) {
                    continue
                }

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
                    org.spockframework.util.Assert.fail "Unknown expectedOutput check: ${expectedOutputCheck.class.name}"
                }
            }
        }

        where:
        permutation << getAllRunTestPermutations()
    }

    static List<CommandTestDefinition> getCommandTestDefinitions() {
        if (commandTestDefinitions == null) {
            commandTestDefinitions = new ArrayList<>()
            def config = new CompilerConfiguration()
            def shell = new GroovyShell(this.class.classLoader, config)

            ("src/test/resources/liquibase/extension/testing/command/" as File).eachFileRecurse {
                if (!it.name.endsWith("test.groovy")) {
                    return
                }

                try {
                    def returnValue = shell.evaluate(it)

                    if (!returnValue instanceof CommandTestDefinition) {
                        org.spockframework.util.Assert.fail("${it} is not a CommandTest definition")
                    }

                    def definition = (CommandTestDefinition) returnValue
                    definition.testFile = it
                    commandTestDefinitions.add(definition)

                } catch (Throwable e) {
                    throw new RuntimeException("Error parsing ${it}: ${e.message}", e)
                }
            }
        }

        return commandTestDefinitions
    }

    static List<RunTestPermutation> getAllRunTestPermutations() {
        def returnList = new ArrayList<RunTestPermutation>()


        for (def commandTestDef : getCommandTestDefinitions()) {
            for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
                for (RunTestDefinition runTest : commandTestDef.runTests) {
                    def permutation = new RunTestPermutation(
                            definition: runTest,
                            databaseName: database.shortName,
                    )

                    if (!permutation.shouldRun()) {
                        continue
                    }

                    permutation.connectionStatus = TestDatabaseConnections.getInstance().getConnection(database.shortName)
                    returnList.add(permutation)
                }
            }
        }

        return returnList
    }

    static define(@DelegatesTo(CommandTestDefinition) Closure closure) {
        CommandTestDefinition commandTestDefinition = new CommandTestDefinition()

        def code = closure.rehydrate(commandTestDefinition, this, commandTestDefinition)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        commandTestDefinition.joinedCommand = StringUtil.join(commandTestDefinition.command, "")

        commandTestDefinition.validate()

        return commandTestDefinition
    }

    static class CommandTestDefinition {

        /**
         * Command to test
         */
        List<String> command

        private String joinedCommand

        File testFile;

        List<RunTestDefinition> runTests = new ArrayList<>()

        String signature

        void run(@DelegatesTo(RunTestDefinition) Closure cl) {
            def runTest = new RunTestDefinition()
            def code = cl.rehydrate(runTest, this, this)
            code.resolveStrategy = Closure.DELEGATE_ONLY
            code()

            runTest.commandTestDefinition = this;

            if (runTest.description == null) {
                runTest.description = StringUtil.join((Collection) this.command, " ")
            }

            runTest.validate()

            this.runTests.add(runTest)
        }

        void validate() throws IllegalArgumentException {
            if (command == null || command.size() == 0) {
                throw new IllegalArgumentException("'command' is required")
            }
        }

    }

    static class RunTestDefinition {

        CommandTestDefinition commandTestDefinition

        /**
         * Description of this test for reporting purposes.
         * If not set, one will be generated for you.
         */
        String description

        /**
         * Arguments to command as key/value pairs
         */
        Map<String, ?> arguments = new HashMap<>()

        private List<TestSetup> setup

        private List<Object> expectedOutput
        private Map<String, ?> expectedResults = new HashMap<>()
        private Class<Throwable> expectedException

        def setup(@DelegatesTo(TestSetupDefinition) Closure closure) {
            def setupDef = new TestSetupDefinition()

            def code = closure.rehydrate(setupDef, this, setupDef)
            code.resolveStrategy = Closure.DELEGATE_ONLY
            code()

            setupDef.validate()

            this.setup = setupDef.build()
        }

        /**
         * Checks for the command output.
         * <li>If a string, assert that the output CONTAINS the string.
         * <li>If a regexp, assert that the regexp FINDs the string.
         */
        def setExpectedOutput(List<Object> output) {
            this.expectedOutput = output
        }


        def setExpectedResults(Map<String, ?> results) {
            this.expectedResults = results
        }

        def setExpectedException(Class<Throwable> exception) {
            this.expectedException = exception
        }

        void validate() {
        }
    }

    private static class RunTestPermutation {
        RunTestDefinition definition
        String databaseName
        TestDatabaseConnections.ConnectionStatus connectionStatus

        boolean shouldRun() {
            def filter = TestFilter.getInstance()

            return filter.shouldRun(TestFilter.DB, databaseName) &&
                    filter.shouldRun("command", definition.commandTestDefinition.joinedCommand)
        }
    }

    static class TestSetupDefinition {

        private List<TestSetup> setups = new ArrayList<>();

        void run(TestSetup setup) {
            this.setups.add(setup)
        }

        /**
         * Set up the database structure
         */
        void setDatabase(List<Change> changes) {
            this.setups.add(new SetupDatabaseStructure(changes))
        }

        /**
         * Set up the database changelog history
         */
        void setHistory(List<HistoryEntry> changes) {
            this.setups.add(new SetupChangelogHistory(changes))
        }

        /**
         * Run a changelog
         */
        void runChangelog(String changeLogPath) {
            runChangelog(changeLogPath, null)
        }

        /**
         * Run a changelog with labels
         */
        void runChangelog(String changeLogPath, String labels) {
            this.setups.add(new SetupRunChangelog(changeLogPath, labels))
        }

        /**
         *
         * Create a specified file with the contents from a source
         *
         * @param originalFile
         * @param newFile
         *
         */
        void createTempResource(String originalFile, String newFile) {
            URL url = Thread.currentThread().getContextClassLoader().getResource(originalFile)
            File f = new File(url.toURI())
            String contents = FileUtil.getContents(f)
            File outputFile = new File("target/test-classes", newFile)
            FileUtil.write(contents, outputFile)
        }

        /**
         *
         * Copy a specified file to another path
         *
         * @param originalFile
         * @param newFile
         *
         */
        void copyResource(String originalFile, String newFile) {
            URL url = Thread.currentThread().getContextClassLoader().getResource(originalFile)
            File f = new File(url.toURI())
            String contents = FileUtil.getContents(f)
            File outputFile = new File("target/test-classes", newFile)
            FileUtil.write(contents, outputFile)
            println "Copied file " + originalFile + " to file " + newFile

        }

        /**
         *
         * Delete the specified resource
         *
         * @param fileToDelete
         *
         */
        void cleanTempResource(String fileToDelete) {
            URL url = Thread.currentThread().getContextClassLoader().getResource(fileToDelete)
            if (url == null) {
                return
            }
            File f = new File(url.toURI())
            if (f.exists()) {
                f.delete()
            }
        }

        /**
         * Mark the changeSets within a changelog as ran without actually running them
         */
        void syncChangelog(String changeLogPath) {
            this.setups.add(new SetupChangeLogSync(changeLogPath))
        }

        void rollback(Integer count, String changeLogPath) {
            this.setups.add(new SetupRollbackCount(count, changeLogPath))
        }


        private void validate() throws IllegalArgumentException {

        }

        private List<TestSetup> build() {
            return setups
        }

    }
}
