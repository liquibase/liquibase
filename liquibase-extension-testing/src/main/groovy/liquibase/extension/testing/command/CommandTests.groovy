package liquibase.extension.testing.command

import liquibase.AbstractExtensibleObject
import liquibase.CatalogAndSchema
import liquibase.Scope
import liquibase.change.Change
import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.command.CommandArgumentDefinition
import liquibase.command.CommandFactory
import liquibase.command.CommandResults
import liquibase.command.CommandScope
import liquibase.command.core.InternalSnapshotCommandStep
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
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration
import liquibase.integration.commandline.Main
import liquibase.logging.core.BufferedLogService
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.InputStreamList
import liquibase.resource.ResourceAccessor
import liquibase.ui.ConsoleUIService
import liquibase.ui.InputHandler
import liquibase.ui.UIService
import liquibase.util.FileUtil
import liquibase.util.StringUtil
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.Assert
import org.junit.Assume
import org.junit.ComparisonFailure
import spock.lang.Specification
import spock.lang.Unroll

import java.util.logging.Level
import java.util.regex.Pattern

class CommandTests extends Specification {

    private static List<CommandTestDefinition> commandTestDefinitions

    public static final PATTERN_FLAGS = Pattern.MULTILINE|Pattern.DOTALL|Pattern.CASE_INSENSITIVE

    private ConfigurationValueProvider propertiesProvider

    def setup() {
        def properties = new Properties()

        getClass().getClassLoader().getResources("liquibase.test.properties").each {
            it.withReader {
                properties.load(it)
            }
        }

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
        def commandDefinition = Scope.currentScope.getSingleton(CommandFactory).getCommandDefinition(commandTestDefinition.getCommand() as String[])
        assert commandDefinition != null: "Cannot find specified command ${commandTestDefinition.getCommand()}"

        assert commandTestDefinition.testFile.name == commandTestDefinition.getCommand().join("") + ".test.groovy": "Incorrect test file name"

        assert commandDefinition.getShortDescription() == null || commandDefinition.getShortDescription() != commandDefinition.getLongDescription() : "Short and long description should not be identical. If there is nothing more to say in the long description, return null"

        for (def runTest : commandTestDefinition.runTests) {
            for (def arg : runTest.arguments.keySet()) {
                assert commandDefinition.arguments.containsKey(arg): "Unknown argument '${arg}' in run ${runTest.description}"
            }
        }

        where:
        commandTestDefinition << getCommandTestDefinitions()
    }

    @Unroll("#featureName: #commandTestDefinition.testFile.name")
    def "check for secure configurations"() {
        expect:
        def commandDefinition = Scope.currentScope.getSingleton(CommandFactory).getCommandDefinition(commandTestDefinition.getCommand() as String[])
        assert commandDefinition != null: "Cannot find specified command ${commandTestDefinition.getCommand()}"
        for (def argDef : commandDefinition.arguments.values()) {
            if (argDef.name.toLowerCase().contains("password")) {
                assert argDef.valueObfuscator != null : "${argDef.name} should be obfuscated OR explicitly set an obfuscator of ConfigurationValueObfuscator.NONE"
            }
        }

        where:
        commandTestDefinition << getCommandTestDefinitions()
    }

    @Unroll("#featureName: #commandTestDefinition.testFile.name")
    def "check command signature"() {
        expect:
        def commandDefinition = Scope.currentScope.getSingleton(CommandFactory).getCommandDefinition(commandTestDefinition.getCommand() as String[])
        assert commandDefinition != null: "Cannot find specified command ${commandTestDefinition.getCommand()}"

        StringWriter signature = new StringWriter()
        signature.print """
Short Description: ${commandDefinition.getShortDescription() ?: "MISSING"}
Long Description: ${commandDefinition.getLongDescription() ?: "NOT SET"}
"""
        signature.println "Required Args:"
        def foundRequired = false
        for (def argDef : commandDefinition.arguments.values()) {
            if (!argDef.required) {
                continue
            }
            foundRequired = true
            signature.println "  ${argDef.name} (${argDef.dataType.simpleName}) ${argDef.description ?: "MISSING DESCRIPTION"}"
            if (argDef.valueObfuscator != null) {
                signature.println("    OBFUSCATED")
            }
        }
        if (!foundRequired) {
            signature.println "  NONE"
        }


        signature.println "Optional Args:"
        def foundOptional = false
        for (def argDef : commandDefinition.arguments.values()) {
            if (argDef.required || argDef.hidden) {
                continue
            }
            foundOptional = true
            signature.println "  ${argDef.name} (${argDef.dataType.simpleName}) ${argDef.description ?:  "MISSING DESCRIPTION"}"
            signature.println "    Default: ${argDef.defaultValueDescription}"
            if (argDef.valueObfuscator != null) {
                signature.println("    OBFUSCATED")
            }
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
        Main.runningFromNewCli = true
        Assume.assumeTrue("Skipping test: " + permutation.connectionStatus.errorMessage, permutation.connectionStatus.connection != null)

        def testDef = permutation.definition

        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(permutation.connectionStatus.connection))

        //clean regular database
        String defaultSchemaName = database.getDefaultSchemaName()
        CatalogAndSchema[] catalogAndSchemas = new CatalogAndSchema[1]
        catalogAndSchemas[0] = new CatalogAndSchema(null, defaultSchemaName)
        database.dropDatabaseObjects(catalogAndSchemas[0])

        //clean alt database
        Database altDatabase = null
        if (permutation.connectionStatus.altConnection != null) {
            altDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(permutation.connectionStatus.altConnection))
            String altDefaultSchemaName = altDatabase.getDefaultSchemaName()
            CatalogAndSchema[] altCatalogAndSchemas = new CatalogAndSchema[1]
            altCatalogAndSchemas[0] = new CatalogAndSchema(null, altDefaultSchemaName)
            altDatabase.dropDatabaseObjects(altCatalogAndSchemas[0])
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

        def runScope = new RunSettings(
                database: database,
                url: permutation.connectionStatus.url,
                username: permutation.connectionStatus.username,
                password: permutation.connectionStatus.password,

                altDatabase: altDatabase,
                altUrl: permutation.connectionStatus.altUrl,
                altUsername: permutation.connectionStatus.altUsername,
                altPassword: permutation.connectionStatus.altPassword,
        )

        def uiOutputWriter = new StringWriter()
        def uiErrorWriter = new StringWriter()
        def logService = new BufferedLogService()
        def outputStream = new ByteArrayOutputStream()
        if (testDef.outputFile != null) {
            outputStream = new FileOutputStream(testDef.outputFile)
        }

        commandScope.addArgumentValue("database", database)
        commandScope.setOutput(outputStream)

        if (testDef.setup != null) {
            for (def setup : testDef.setup) {
                setup.setup(permutation.connectionStatus)
            }
        }

        if (testDef.arguments != null) {
            testDef.arguments.each { name, value ->
                String key
                if (name instanceof CommandArgumentDefinition) {
                    key = name.getName()
                } else {
                    key = (String) name
                }
                Object objValue = (Object) value
                if (value instanceof Closure) {
                    objValue = ((Closure) objValue).call(runScope)
                }

                commandScope.addArgumentValue(key, objValue)
            }
        }

        boolean exceptionThrown = false
        def results = Scope.child([
                (LiquibaseCommandLineConfiguration.LOG_LEVEL.getKey()): Level.INFO,
                ("liquibase.plugin." + HubService.name)               : MockHubService,
                (Scope.Attr.resourceAccessor.name())                  : testDef.resourceAccessor ?
                                                                            testDef.resourceAccessor : Scope.getCurrentScope().getResourceAccessor(),
                (Scope.Attr.ui.name())                                : testDef.testUI ? testDef.testUI.initialize(uiOutputWriter, uiErrorWriter) :
                                                                                         new TestUI(uiOutputWriter, uiErrorWriter),
                (Scope.Attr.logService.name())                        : logService
        ], {
            try {
                def returnValue = commandScope.execute()
                assert testDef.expectedException == null : "An exception was expected but the command completed successfully"
                return returnValue
            }
            catch (Exception e) {
                exceptionThrown = true
                if (testDef.expectedException == null) {
                    throw e
                } else {
                    assert e.class == testDef.expectedException
                    if (testDef.expectedExceptionMessage != null) {
                        checkOutput("Exception message", e.getMessage(), Collections.singletonList(testDef.expectedExceptionMessage))
                    }
                    return
                }
            }
        } as Scope.ScopedRunnerWithReturn<CommandResults>)

        //
        // Check to see if there was supposed to be an exception
        //

        if (testDef.expectedResults.size() > 0 && (results == null || results.getResults().isEmpty())) {
            throw new RuntimeException("Results were expected but none were found for " + testDef.commandTestDefinition.command)
        }

        then:
        checkOutput("Command Output", outputStream.toString(), testDef.expectedOutput)
        checkOutput("UI Output", uiOutputWriter.toString(), testDef.expectedUI)
        checkOutput("UI Error Output", uiErrorWriter.toString(), testDef.expectedUIErrors)
        checkOutput("Log Messages", logService.getLogAsString(Level.FINE), testDef.expectedLogs)

        checkFileContent(testDef.expectedFileContent, "Command File Content")
        checkDatabaseContent(testDef.expectedDatabaseContent, database, "Database snapshot content")

        if (!testDef.expectedResults.isEmpty()) {
            for (def returnedResult : results.getResults().entrySet()) {
                def expectedResult = testDef.expectedResults.get(returnedResult.getKey())
                def expectedValue = expectedResult instanceof Closure ? expectedResult.call() : String.valueOf(expectedResult)
                def seenValue = String.valueOf(returnedResult.getValue())

                assert expectedValue != "null": "No expectedResult for returned result '" + returnedResult.getKey() + "' of: " + seenValue
                assert seenValue == expectedValue
            }
        }
        if (testDef.expectFileToExist != null) {
            assert testDef.expectFileToExist.exists(): "File '${testDef.expectFileToExist.getName()}' should exist"
        }
        if (testDef.expectFileToNotExist != null) {
            assert !testDef.expectFileToNotExist.exists(): "File '${testDef.expectFileToNotExist.getName()}' should not exist"
        }

        where:
        permutation << getAllRunTestPermutations()
    }

    static OutputCheck assertNotContains(String substring) {
        return assertNotContains(substring, false)
    }

    static OutputCheck assertNotContains(String substring, boolean caseInsensitive) {
        return new OutputCheck() {
            @Override
            def check(String actual) throws AssertionError {
                actual = (caseInsensitive && actual != null ? actual.toLowerCase() : actual)
                substring = (caseInsensitive && substring != null ? substring.toLowerCase() : substring)
                assert !actual.contains(StringUtil.standardizeLineEndings(StringUtil.trimToEmpty(substring))): "$actual does not contain: '$substring'"
            }
        }
    }

    static OutputCheck assertContains(String substring) {
        return assertContains(substring, null)
    }

    static OutputCheck assertContains(String substring, final Integer occurrences) {
        return new OutputCheck() {
            @Override
            def check(String actual) throws AssertionError {
                String edited = StringUtil.standardizeLineEndings(StringUtil.trimToEmpty(substring))
                if (occurrences == null) {
                    boolean b = actual.contains(edited)
                    assert b: "$actual does not contain: '$substring'"
                } else {
                    int count = (actual.split(Pattern.quote(edited), -1).length) - 1
                    assert count == occurrences: "$actual does not contain '$substring' $occurrences times.  It appears $count times"
                }
            }
        }
    }

    static void checkDatabaseContent(Map<String, ?> expectedDatabaseContent, Database database, String outputDescription) {
        if (expectedDatabaseContent.size() == 0) {
            return
        }
        for (def check : expectedDatabaseContent) {
            File f = takeDatabaseSnapshot(database, check.key)
            String contents = FileUtil.getContents(f)
            contents = StringUtil.standardizeLineEndings(StringUtil.trimToEmpty(contents))
            contents = contents.replaceAll(/\s+/, " ")
            List<Object> checks = check.getValue()
            checkOutput(outputDescription, contents, checks)
        }
    }

    private static File takeDatabaseSnapshot(Database database, String format) {
        final ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database)
        changeLogService.init()
        changeLogService.reset()

        File destDir = new File("target/test-classes")
        File tempFile = File.createTempFile("snapshot-", "." + format, destDir)
        tempFile.deleteOnExit()
        CatalogAndSchema[] schemas = new CatalogAndSchema[1]
        schemas[0] = new CatalogAndSchema(null, database.getDefaultSchemaName())
        CommandScope snapshotCommand = new CommandScope("internalSnapshot")
        snapshotCommand
                .addArgumentValue(InternalSnapshotCommandStep.DATABASE_ARG, database)
                .addArgumentValue(InternalSnapshotCommandStep.SCHEMAS_ARG, schemas)
                .addArgumentValue(InternalSnapshotCommandStep.SERIALIZER_FORMAT_ARG, "txt")

        Writer outputWriter = new FileWriter(tempFile)
        String result = InternalSnapshotCommandStep.printSnapshot(snapshotCommand, snapshotCommand.execute())
        outputWriter.write(result)
        outputWriter.flush()
        return tempFile
    }

    static void checkFileContent(Map<String, ?> expectedFileContent, String outputDescription) {
        for (def check : expectedFileContent) {
            String path = check.key
            List<Object> checks = check.value
            String contents = FileUtil.getContents(new File(path))
            contents = StringUtil.standardizeLineEndings(StringUtil.trimToEmpty(contents))
            contents = contents.replaceAll(/\s+/, " ")
            checkOutput(outputDescription, contents, checks)
        }
    }

    static void checkOutput(String outputDescription, String fullOutput, List<Object> checks) {
        fullOutput = StringUtil.standardizeLineEndings(StringUtil.trimToEmpty(fullOutput))

        if (fullOutput.length() == 0) {
            assert checks == null || checks.size() == 0: "$outputDescription was empty but checks were defined"
        } else {
            for (def expectedOutputCheck : checks) {
                if (expectedOutputCheck == null) {
                    continue
                }

                if (expectedOutputCheck instanceof String) {
                    if (!fullOutput.replaceAll(/\s+/," ")
                            .contains(StringUtil.standardizeLineEndings(StringUtil.trimToEmpty(expectedOutputCheck)).replaceAll(/\s+/," "))) {
                        throw new ComparisonFailure("$outputDescription does not contain expected", expectedOutputCheck, fullOutput)
                    }
                } else if (expectedOutputCheck instanceof Pattern) {
                    String patternString = StringUtil.standardizeLineEndings(StringUtil.trimToEmpty(((Pattern) expectedOutputCheck).pattern()))
                    //expectedOutputCheck = Pattern.compile(patternString, Pattern.MULTILINE | Pattern.DOTALL)
                    def matcher = expectedOutputCheck.matcher(fullOutput)
                    assert matcher.groupCount() == 0: "Unescaped parentheses in regexp /$expectedOutputCheck/"
                    assert matcher.find(): "$outputDescription\n$fullOutput\n\nDoes not match regexp\n\n/$expectedOutputCheck/"
                } else if (expectedOutputCheck instanceof OutputCheck) {
                    try {
                        ((OutputCheck) expectedOutputCheck).check(fullOutput)
                    } catch (AssertionError e) {
                        Assert.fail("$fullOutput : ${e.getMessage()}")
                    }
                } else {
                    Assert.fail "Unknown $outputDescription check type: ${expectedOutputCheck.class.name}"
                }
            }
        }
    }

    static List<CommandTestDefinition> getCommandTestDefinitions() {
        if (commandTestDefinitions == null) {
            commandTestDefinitions = new ArrayList<>()
            def config = new CompilerConfiguration()
            def shell = new GroovyShell(this.class.classLoader, config)

            def path = "src/test/resources/liquibase/extension/testing/command/"
            try {
                (path as File).eachFileRecurse {
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
            catch (Exception e) {
                String message = "Error loading tests in ${path}: ${e.message}"
                throw new RuntimeException("${message}.\nIf running CommandTests directly, make sure you are choosing the classpath of the module you want to test")
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

        File testFile

        List<RunTestDefinition> runTests = new ArrayList<>()

        String signature

        void run(@DelegatesTo(RunTestDefinition) Closure testClosure) {
            run(null, testClosure)

        }

        void run(String description, @DelegatesTo(RunTestDefinition) Closure testClosure) {
            def runTest = new RunTestDefinition()
            def code = testClosure.rehydrate(runTest, this, this)
            code.resolveStrategy = Closure.DELEGATE_ONLY
            code()

            runTest.commandTestDefinition = this

            runTest.description = description
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
        private String description

        /**
         * Arguments to command as key/value pairs
         */
        private Map<String, ?> arguments = new HashMap<>()
        private Map<String, ?> expectedFileContent = new HashMap<>()
        private Map<String, Object> expectedDatabaseContent = new HashMap<>()

        private List<TestSetup> setup

        //
        // Allow the test spec to set its own UIService
        //
        private TestUI testUI

        //
        // Allow the test to provide a custom ResourceAccessor
        def ResourceAccessor resourceAccessor

        private List<Object> expectedOutput = new ArrayList<>()
        private List<Object> expectedUI = new ArrayList<>()
        private List<Object> expectedUIErrors = new ArrayList<>()
        private List<Object> expectedLogs = new ArrayList<>()

        private File outputFile

        private Map<String, ?> expectedResults = new HashMap<>()
        private Class<Throwable> expectedException
        private Object expectedExceptionMessage
        private File expectFileToExist
        private File expectFileToNotExist

        def setup(@DelegatesTo(TestSetupDefinition) Closure closure) {
            def setupDef = new TestSetupDefinition()

            def code = closure.rehydrate(setupDef, this, setupDef)
            code.resolveStrategy = Closure.DELEGATE_ONLY
            code()

            setupDef.validate()

            this.setup = setupDef.build()
        }

        def setOutputFile(File outputFile) {
            this.outputFile = outputFile
        }

        def setTestUI(UIService testUI) {
            this.testUI = testUI
        }

        def setResourceAccessor(ResourceAccessor resourceAccessor) {
            this.resourceAccessor = resourceAccessor
        }

        /**
         * Sets the command arguments
         * <li>If value is an object, use that as the value
         * <li>If value is a closure, run it as a function with `it` being a {@link RunSettings} instance
         */
        def setArguments(Map<String, Object> args) {
            this.arguments = args
        }

        def setExpectedFileContent(Map<String, Object> content) {
            this.expectedFileContent = content
        }

        def setExpectedDatabaseContent(Map<String, Object> content) {
            this.expectedDatabaseContent = content
        }

        /**
         * Checks for the command output.
         * <li>If a string, assert that the output CONTAINS the string.
         * <li>If a regexp, assert that the regexp FINDs the string.
         */
        def setExpectedOutput(List<Object> output) {
            this.expectedOutput = output
        }

        def setExpectedOutput(String output) {
            this.expectedOutput.add(output)
        }

        def setExpectedOutput(Pattern output) {
            this.expectedOutput.add(output)
        }

        def setExpectedFileContent(List<Map<String, List<String>>> checks) {
            this.expectedFileContent.addAll(checks)
        }

        /**
         * Checks for the UI output.
         * <li>If a string, assert that the output CONTAINS the string.
         * <li>If a regexp, assert that the regexp FINDs the string.
         */
        def setExpectedUI(List<Object> output) {
            this.expectedUI = output
        }

        def setExpectedUI(String output) {
            this.expectedUI.add(output)
        }

        def setExpectedUI(Pattern output) {
            this.expectedUI.add(output)
        }

        /**
         * Checks for the UI error output.
         * <li>If a string, assert that the output CONTAINS the string.
         * <li>If a regexp, assert that the regexp FINDs the string.
         */
        def setExpectedUIErrors(List<Object> output) {
            this.expectedUIErrors = output
        }

        def setExpectedUIErrors(String output) {
            this.expectedUIErrors = new ArrayList<>()
            this.expectedUIErrors.add(output)
        }

        def setExpectedUIErrors(Pattern output) {
            this.expectedUIErrors = new ArrayList<>()
            this.expectedUIErrors.add(output)
        }

        /**
         * Checks for log output.
         * <li>If a string, assert that the output CONTAINS the string.
         * <li>If a regexp, assert that the regexp FINDs the string.
         */
        def setExpectedLogs(List<Object> output) {
            this.expectedLogs = output
        }

        def setExpectedLogs(String output) {
            this.expectedLogs = new ArrayList<>()
            this.expectedLogs.add(output)
        }

        def setExpectedLogs(Pattern output) {
            this.expectedLogs = new ArrayList<>()
            this.expectedLogs.add(output)
        }


        def setExpectedResults(Map<String, ?> results) {
            this.expectedResults = results
        }

        def setExpectedException(Class<Throwable> exception) {
            this.expectedException = exception
        }

        def setExpectedExceptionMessage(Object expectedExceptionMessage) {
            this.expectedExceptionMessage = expectedExceptionMessage
        }

        def setExpectFileToExist(File expectedFile) {
            this.expectFileToExist = expectedFile
        }

        def setExpectFileToNotExist(File expectedFile) {
            this.expectFileToNotExist = expectedFile
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

    static class FileContent {
        String path
        List<Object> strings
    }

    static class TestSetupDefinition {

        private List<TestSetup> setups = new ArrayList<>()

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
         * Set up the "alt" database structure
         */
        void setAltDatabase(List<Change> changes) {
            this.setups.add(new SetupAltDatabaseStructure(changes))
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
         * Run a changelog
         */
        void base64Encode(String filePath) {
            File f = new File(filePath)
            String contents = f.getText()
            String encoded = Base64.getEncoder().encodeToString(contents.getBytes())
            f.write(encoded)
        }

        /**
         * Run a changelog with labels
         */
        void runChangelog(String changeLogPath, String labels) {
            this.setups.add(new SetupRunChangelog(changeLogPath, labels))
        }

        void createTempResource(String originalFile, String newFile) {
            this.setups.add(new SetupCreateTempResources(originalFile, newFile))
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

        void modifyChangeLogId(String originalFile, String newChangeLogId) {
            this.setups.add(new SetupModifyChangelog(originalFile, newChangeLogId))
        }

        /**
         *
         * Delete the specified resources
         *
         * @param fileToDeletes
         *
         */
        void cleanResources(String... filesToDelete) {
            this.setups.add(new SetupCleanResources(filesToDelete))
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

    static class RunSettings {
        String url
        String username
        String password
        Database database

        String altUrl
        String altUsername
        String altPassword
        Database altDatabase
    }

    interface OutputCheck {
        def check(String actual) throws AssertionError
    }

    interface FileContentCheck {
        def check(String path) throws AssertionError
    }

    //
    // If the regular ClassLoaderResourceAccessor is unable to locate a
    // file then look again by using only the file name.  This helps tests
    // to locate files that they write and then try to read
    //
    static class ClassLoaderResourceAccessorForTest extends ClassLoaderResourceAccessor {
        @Override
        public InputStreamList openStreams(String relativeTo, String streamPath) throws IOException {
            InputStreamList list = super.openStreams(relativeTo, streamPath)
            if (list != null && ! list.isEmpty()) {
                return list
            }
            return super.openStreams(relativeTo, new File(streamPath).getName())
        }
    }

    //
    // Override of ConsoleUIService so that we
    // can supply a CannedConsoleWrapper with the answers
    // to the prompts
    //
    static class TestUIWithAnswers extends TestUI {
        private ConsoleUIService consoleUIService

        TestUIWithAnswers(String[] answers) {
            ConsoleUIService.ConsoleWrapper consoleWrapper = new CannedConsoleWrapper(answers)
            consoleUIService = new ConsoleUIServiceWrapper(consoleWrapper)
            consoleUIService.setAllowPrompt(true)
        }

        @Override
        def <T> T prompt(String prompt, T valueIfNoEntry, InputHandler<T> inputHandler, Class<T> type) {
            return consoleUIService.prompt(prompt, valueIfNoEntry, inputHandler, type)
        }

        class ConsoleUIServiceWrapper extends ConsoleUIService {
            ConsoleUIServiceWrapper(ConsoleUIService.ConsoleWrapper console) {
                super(console)
            }

            @Override
            void sendMessage(String message) {
                getOutput().println(message)
            }
        }
    }

    //
    // Class to help with interactive tests
    // The answers are assumed to be in the correct order and number
    //
    static class CannedConsoleWrapper extends ConsoleUIService.ConsoleWrapper {
        private String[] answers
        private int count

        CannedConsoleWrapper(String[] answers) {
            super(null)
            this.answers = answers
        }

        @Override
        String readLine() {
            //
            // Get the answer, increment the counter
            //
            String answer = answers[count]
            count++
            return answer
        }

        @Override
        boolean supportsInput() {
            return true
        }
    }

    static class TestUI extends AbstractExtensibleObject implements UIService {

        private Writer output
        private Writer errorOutput

        TestUI() {}

        TestUI(Writer output, Writer errorOutput) {
            this.output = output
            this.errorOutput = errorOutput
        }

        TestUI initialize(Writer output, Writer errorOutput) {
            this.output = output
            this.errorOutput = errorOutput
            return this
        }

        Writer getOutput() {
            return output
        }

        Writer getErrorOutput() {
            return errorOutput
        }

        @Override
        int getPriority() {
            return -1
        }

        @Override
        void sendMessage(String message) {
            output.println(message)
        }

        @Override
        void sendErrorMessage(String message) {
            errorOutput.println(message)
        }

        @Override
        void sendErrorMessage(String message, Throwable exception) {
            errorOutput.println(message)
            exception.printStackTrace(errorOutput)
        }

        @Override
        def <T> T prompt(String prompt, T valueIfNoEntry, InputHandler<T> inputHandler, Class<T> type) {
            return valueIfNoEntry
        }

        @Override
        void setAllowPrompt(boolean allowPrompt) throws IllegalArgumentException {
            if (allowPrompt) {
                throw new RuntimeException("Cannot allow prompts in tests")
            }
        }

        @Override
        boolean getAllowPrompt() {
            return false
        }
    }
}
