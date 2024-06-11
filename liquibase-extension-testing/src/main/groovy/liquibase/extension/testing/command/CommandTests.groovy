package liquibase.extension.testing.command

import liquibase.AbstractExtensibleObject
import liquibase.CatalogAndSchema
import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.change.Change
import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.command.*
import liquibase.command.core.InternalSnapshotCommandStep
import liquibase.configuration.AbstractMapConfigurationValueProvider
import liquibase.configuration.ConfigurationValueProvider
import liquibase.configuration.LiquibaseConfiguration
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.extension.testing.TestFilter
import liquibase.extension.testing.setup.*
import liquibase.extension.testing.setup.SetupCleanResources.CleanupMode
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration
import liquibase.integration.commandline.Main
import liquibase.logging.core.BufferedLogService
import liquibase.resource.*
import liquibase.ui.ConsoleUIService
import liquibase.ui.InputHandler
import liquibase.ui.UIService
import liquibase.util.FileUtil
import liquibase.util.StreamUtil
import liquibase.util.StringUtil
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.Assert
import org.junit.ComparisonFailure
import org.junit.jupiter.api.Assumptions
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.environment.OperatingSystem

import java.util.logging.Level
import java.util.regex.Pattern
import java.util.stream.Collectors

class CommandTests extends Specification {

    private static List<CommandTestDefinition> commandTestDefinitions

    public static final PATTERN_FLAGS = Pattern.MULTILINE|Pattern.DOTALL|Pattern.CASE_INSENSITIVE

    public static String NOT_NULL = "not_null"

    private ConfigurationValueProvider propertiesProvider
    private ConfigurationValueProvider searchPathPropertiesProvider

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
        if (searchPathPropertiesProvider != null) {
            Scope.currentScope.getSingleton(LiquibaseConfiguration).unregisterProvider(searchPathPropertiesProvider)
        }
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

        def liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration)
        for (def runTest : commandTestDefinition.runTests) {
            for (def arg : runTest.globalArguments.keySet()) {
                assert liquibaseConfiguration.getRegisteredDefinition(arg) != null: "Unknown global argument '${arg}' in run ${runTest.description}"
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
        def actualSignature = signature.toString()
        def expectedSignature = commandTestDefinition.signature

        if (expectedSignature == NOT_NULL) {
            assert actualSignature != null: "The result is null"
        } else {
            assert StringUtil.standardizeLineEndings(StringUtil.trimToEmpty(actualSignature)) ==
                    StringUtil.standardizeLineEndings(StringUtil.trimToEmpty(expectedSignature))
        }

        where:
        commandTestDefinition << getCommandTestDefinitions()
    }


    @Unroll("Run {db:#permutation.databaseName,command:#permutation.definition.commandTestDefinition.joinedCommand} #permutation.definition.description")
    def "run"() {
        setup:
        Main.runningFromNewCli = true
        Assumptions.assumeTrue(permutation.testSetupEnvironment.connection != null, "Skipping test: " + permutation.testSetupEnvironment.errorMessage)

        def testDef = permutation.definition

        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(permutation.testSetupEnvironment.connection))

        //clean regular database
        String defaultSchemaName = database.getDefaultSchemaName()
        CatalogAndSchema[] catalogAndSchemas = new CatalogAndSchema[1]
        catalogAndSchemas[0] = new CatalogAndSchema(null, defaultSchemaName)
        database.dropDatabaseObjects(catalogAndSchemas[0])

        //clean alt database
        Database altDatabase = null
        if (permutation.testSetupEnvironment.altConnection != null) {
            altDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(permutation.testSetupEnvironment.altConnection))
            String altDefaultSchemaName = altDatabase.getDefaultSchemaName()
            CatalogAndSchema[] altCatalogAndSchemas = new CatalogAndSchema[1]
            altCatalogAndSchemas[0] = new CatalogAndSchema(null, altDefaultSchemaName)
            altDatabase.dropDatabaseObjects(altCatalogAndSchemas[0])
        }

        Scope.getCurrentScope().getMdcManager().clear()

        when:
        if (testDef.supportedOs != null) {
            def currentOs = OperatingSystem.getCurrent()
            Assumptions.assumeTrue(testDef.supportedOs.contains(currentOs), "The current operating system (" + currentOs.name + ") does not support this test.")
        }
        def commandScope
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
                url: permutation.testSetupEnvironment.url,
                username: permutation.testSetupEnvironment.username,
                password: permutation.testSetupEnvironment.password,

                altDatabase: altDatabase,
                altUrl: permutation.testSetupEnvironment.altUrl,
                altUsername: permutation.testSetupEnvironment.altUsername,
                altPassword: permutation.testSetupEnvironment.altPassword,
        )

        def uiOutputWriter = new StringWriter()
        def uiErrorWriter = new StringWriter()
        def logService = new BufferedLogService()
        def outputStream = new ByteArrayOutputStream()
        if (testDef.outputFile != null) {
            outputStream = new FileOutputStream(testDef.outputFile)
        }

        commandScope.setOutput(outputStream)

        if (testDef.setup != null) {
            for (def setup : testDef.setup) {
                setup.setup(permutation.testSetupEnvironment)
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

        def resourceAccessor = Scope.getCurrentScope().getResourceAccessor()

        if (testDef.searchPath != null) {
            def config = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class)

            searchPathPropertiesProvider = new AbstractMapConfigurationValueProvider() {
                @Override
                protected Map<?, ?> getMap() {
                    return Collections.singletonMap(GlobalConfiguration.SEARCH_PATH.getKey(), testDef.searchPath)
                }

                @Override
                protected String getSourceDescription() {
                    return "command tests search path override"
                }

                @Override
                int getPrecedence() {
                    return 1
                }
            }

            config.registerProvider(searchPathPropertiesProvider)
            resourceAccessor = new SearchPathResourceAccessor(testDef.searchPath)
        }

        def scopeSettings = [
                (LiquibaseCommandLineConfiguration.LOG_LEVEL.getKey()): Level.INFO,
                (Scope.Attr.resourceAccessor.name())                  : testDef.resourceAccessor ?
                                                                            testDef.resourceAccessor : resourceAccessor,
                (Scope.Attr.ui.name())                                : testDef.testUI ? testDef.testUI.initialize(uiOutputWriter, uiErrorWriter) :
                                                                                         new TestUI(uiOutputWriter, uiErrorWriter),
                (Scope.Attr.logService.name())                        : logService
        ]

        if (testDef.globalArguments != null) {
            scopeSettings.putAll(testDef.globalArguments)
        }

        Exception savedException = null
        def results = Scope.child(scopeSettings, {
            try {
                if (testDef.commandTestDefinition.beforeMethodInvocation != null) {
                    testDef.commandTestDefinition.beforeMethodInvocation.call()
                }
                def returnValue = commandScope.execute()
                assert testDef.expectedException == null : "An exception was expected but the command completed successfully"
                return returnValue
            }
            catch (Exception e) {
                savedException = e
                if (testDef.expectedException == null) {
                    if (testDef.setup != null) {
                        for (def setup : testDef.setup) {
                            setup.cleanup()
                        }
                    }
                    throw e
                } else {
                    assert e.class == testDef.expectedException
                    if (testDef.expectedExceptionMessage != null) {
                        checkOutput("Exception message", e.getMessage(), Collections.singletonList(testDef.expectedExceptionMessage))
                    }
                    return
                }
            } finally {
                if (testDef.commandTestDefinition.afterMethodInvocation != null) {
                    testDef.commandTestDefinition.afterMethodInvocation.call()
                }
            }
        } as Scope.ScopedRunnerWithReturn<CommandResults>)

        if (savedException != null && savedException.getCause() != null && savedException.getCause() instanceof CommandFailedException) {
            CommandFailedException cfe = (CommandFailedException) savedException.getCause()
            results = cfe.getResults()
        }

        //
        // Check to see if there was supposed to be an exception
        //
        if (testDef.expectedResults.size() > 0 && (results == null || results.getResults().isEmpty())) {
            String logString = logService.getLogAsString(Level.FINE)
            throw new RuntimeException("Results were expected but none were found for " + testDef.commandTestDefinition.command + "\n" + logString)
        }

        then:
            try {
                checkOutput("Command Output", outputStream.toString(), testDef.expectedOutput)
                checkOutput("UI Output", uiOutputWriter.toString(), testDef.expectedUI)
                checkOutput("UI Error Output", uiErrorWriter.toString(), testDef.expectedUIErrors)
                checkOutput("Log Messages", logService.getLogAsString(Level.FINE), testDef.expectedLogs)

                checkFileContent(testDef.expectedFileContent, "Command File Content")
                checkDatabaseContent(testDef.expectedDatabaseContent, database, "Database snapshot content")

                if (!testDef.expectedResult.isEmpty()) {
                    def entrySet = testDef.expectedResult.entrySet()
                    def oneEntry = entrySet.iterator().next()
                    assert results.getResult(oneEntry.getKey()) == oneEntry.getValue()
                }
                if (!testDef.expectedResults.isEmpty()) {
                    for (def returnedResult : results.getResults().entrySet()) {
                        def expectedResult = testDef.expectedResults.get(returnedResult.getKey())
                        def expectedValue = expectedResult instanceof Closure ? expectedResult.call() : String.valueOf(expectedResult)
                        def seenValue = String.valueOf(returnedResult.getValue())

                        assert expectedValue != "null": "No expectedResult for returned result '" + returnedResult.getKey() + "' of: " + seenValue
                        if (expectedValue instanceof Closure) {
                            assert expectedValue.call(returnedResult)
                        } else if (expectedValue == NOT_NULL) {
                            assert seenValue != null: "The result is null"
                        } else {
                            assert seenValue == expectedValue
                        }
                    }
                }
                if (testDef.expectFileToExist != null) {
                    assert testDef.expectFileToExist.exists(): "File '${testDef.expectFileToExist.getAbsolutePath()}' should exist"
                }
                if (testDef.expectFileToNotExist != null) {
                    assert !testDef.expectFileToNotExist.exists(): "File '${testDef.expectFileToNotExist.getAbsolutePath()}' should not exist"
                }
                if (testDef.expectations != null) {
                    Scope.child([
                            "database": database,
                    ], new Scope.ScopedRunner() {
                        @Override
                        void run() throws Exception {
                            testDef.expectations.call()
                        }
                    })
                }
            } finally {
                if (testDef.setup != null) {
                    for (def setup : testDef.setup) {
                        setup.cleanup()
                    }
                }
            }


            where:
            permutation << getAllRunTestPermutations()
    }

    /**
     *
     * Compare the contents of two files, optionally filtering out
     * lines that contain a specified string.
     *
     * @param   f1                           The baseline file
     * @param   f2                           The new output file
     * @param   filter                       The filter string (can be NULL)
     * @return  OutputCheck                  Closure to be used at test run execution
     *
     */
    static OutputCheck assertFilesEqual(File f1, File f2, String... filters) {
        return new OutputCheck() {
            private String baselineContents
            private String actualContents
            @Override
            def check(String actual) throws AssertionError {
                List<String> lines1 = f1.readLines()
                if (filters) {
                    lines1 = lines1.findAll({ line ->
                        filters.every() { filter ->
                            ! line.contains(filter)
                        }
                    })
                }
                String contents1 = StringUtil.join(lines1, "\n")
                this.baselineContents = contents1

                List<String> lines2 = f2.readLines()
                if (filters) {
                    lines2 = lines2.findAll({ line ->
                        filters.every() { filter ->
                            ! line.contains(filter)
                        }
                    })
                }
                String contents2 = StringUtil.join(lines2, "\n")
                this.actualContents = contents2

                assert lines1.size() == lines2.size()
                assert contents1 == contents2
            }

            @Override
            String getExpected() {
                return this.baselineContents
            }

            @Override
            String getCheckedOutput() {
                return this.actualContents
            }
        }
    }

    static OutputCheck assertNotContains(String substring) {
        return assertNotContains(substring, false)
    }

    static OutputCheck assertNotContains(String substring, boolean caseInsensitive) {
        return new OutputCheck() {
            private String actualContents
            @Override
            def check(String actual) throws AssertionError {
                actual = (caseInsensitive && actual != null ? actual.toLowerCase() : actual)
                this.actualContents = actual
                substring = (caseInsensitive && substring != null ? substring.toLowerCase() : substring)
                assert !actual.contains(StringUtil.standardizeLineEndings(StringUtil.trimToEmpty(substring))): "$actual does not contain: '$substring'"
            }

            @Override
            String getExpected() {
                return substring
            }

            @Override
            String getCheckedOutput() {
                return this.actualContents
            }
        }
    }

    static OutputCheck assertContains(String substring) {
        return assertContains(substring, null)
    }

    static OutputCheck assertContains(String substring, final Integer occurrences) {
        return assertContains(substring, occurrences, false)
    }

    static OutputCheck assertContains(String substring, final Integer occurrences, final Boolean removeWhitespaceFromExpected) {
        return new OutputCheck() {
            private String actualContents
            @Override
            def check(String actual) throws AssertionError {
                this.actualContents = actual
                String edited = StringUtil.standardizeLineEndings(StringUtil.trimToEmpty(substring))
                if (Boolean.TRUE == removeWhitespaceFromExpected) {
                    edited = edited.replaceAll(/\s+/," ")
                }
                if (occurrences == null) {
                    boolean b = actual.contains(edited)
                    assert b: "$actual does not contain: '$substring'"
                } else {
                    int count = (actual.split(Pattern.quote(edited), -1).length) - 1
                    assert count == occurrences: "$actual does not contain '$substring' $occurrences times.  It appears $count times"
                }
            }

            @Override
            String getExpected() {
                return substring
            }

            @Override
            String getCheckedOutput() {
                return this.actualContents
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

    static File takeDatabaseSnapshot(Database database, String format) {
        final ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database)
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
                .addArgumentValue(InternalSnapshotCommandStep.SERIALIZER_FORMAT_ARG, format)

        Writer outputWriter = new FileWriter(tempFile)
        String result = InternalSnapshotCommandStep.printSnapshot(snapshotCommand, snapshotCommand.execute())
        outputWriter.write(result)
        outputWriter.flush()
        return tempFile
    }

    static void checkFileContent(Map<String, ?> expectedFileContent, String outputDescription) {
        expectedFileContent.each { def check ->
            String path = check.key
            List<Object> checks = check.value
            File f = new File(path)
            String contents
            if (f.exists()) {
                contents = FileUtil.getContents(f)
            } else {
                final PathHandlerFactory pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class)
                def resource = pathHandlerFactory.getResource(path)
                if (resource.exists()) {
                    contents = StreamUtil.readStreamAsString(resource.openInputStream())
                } else {
                    contents = null
                    throw new FileNotFoundException("File ${path} not found")
                }
            }

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

                if (expectedOutputCheck instanceof GString) {
                    expectedOutputCheck = expectedOutputCheck.toString()
                }

                if (expectedOutputCheck instanceof String) {
                    if (!fullOutput.replaceAll(/\s+/," ")
                            .contains(StringUtil.standardizeLineEndings(StringUtil.trimToEmpty(expectedOutputCheck)).replaceAll(/\s+/," "))) {
                        throw new ComparisonFailure("$outputDescription does not contain expected", expectedOutputCheck, fullOutput)
                    }
                } else if (expectedOutputCheck instanceof Pattern) {
                    def matcher = expectedOutputCheck.matcher(fullOutput)
                    assert matcher.groupCount() == 0: "Unescaped parentheses in regexp /$expectedOutputCheck/"
                    if (!matcher.find()) {
                        throw new ComparisonFailure("$outputDescription\n$fullOutput\n\nDoes not match regexp\n\n/$expectedOutputCheck/", expectedOutputCheck.toString(), fullOutput)
                    }
                } else if (expectedOutputCheck instanceof OutputCheck) {
                    try {
                        ((OutputCheck) expectedOutputCheck).check(fullOutput)
                    } catch (AssertionError e) {
                        throw new ComparisonFailure(e.getMessage(), expectedOutputCheck.expected, expectedOutputCheck.checkedOutput)
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
                throw new RuntimeException("${message}.\n\n!!------------- TEST EXECUTION FAILURE -------------!!\n" +
                        "\nIf you are running CommandTests directly through your IDE, make sure you are including the module with your 'test.groovy' files in your classpath.\n" +
                        "\nNOTE: For example, if you are running these tests in liquibase-core, use the liquibase-integration-tests module as the classpath in your run configuration.\n" +
                        "\n!!--------------------------------------------------!!", e)

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


                    def system = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem(database.shortName)
                    if (system.shouldTest()) {
                        system.start()
                        permutation.testSetupEnvironment = new TestSetupEnvironment(system, null)
                        returnList.add(permutation)
                    }
                }
            }
        }

        if (returnList.isEmpty()) {
            throw new RuntimeException("Required test systems not found! " +
                    "Make sure your test systems are specified in your liquibase.sdk.yaml " +
                    "and that you are not accidentally filtering out all tests.")
        }

        def descriptions =
                returnList.stream()
                        .map({ rtp -> rtp.definition.commandTestDefinition.joinedCommand + ": '" + rtp.definition.description + "'" })
                        .collect(Collectors.toList())

        def duplicateDescriptions =
                descriptions.stream()
                        .filter({ d -> Collections.frequency(descriptions, d) > 1 })
                        .distinct().collect(Collectors.toList())

        if (!duplicateDescriptions.isEmpty()) {
            throw new Exception("There are duplicate command test definitions with the same description: " + StringUtil.join(duplicateDescriptions, "; "))
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
        /**
         * An optional method that will be called after the execution of each run command. This is executed within
         * the same scope as the command that is run for the test. This method will always be called, regardless of
         * exceptions thrown from within the test.
         */
        Closure<Void> afterMethodInvocation
        /**
         * An optional method that will be called before the execution of each run command. This is executed within
         * the same scope as the command that is run for the test. Exceptions thrown from this method will cause the
         * test to fail.
         */
        Closure<Void> beforeMethodInvocation

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

        private Map<String, ?> globalArguments = new HashMap<>()

        private String searchPath
        private ArrayList<OperatingSystem> supportedOs

        /**
         * Arguments to command as key/value pairs
         */
        private Map<String, ?> arguments = new HashMap<>()
        private Map<String, ?> expectedFileContent = new HashMap<>()
        private Map<String, Object> expectedDatabaseContent = new HashMap<>()
        private Closure<Void> expectations = null;

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
        private Map<String, ?> expectedResult = new HashMap<>()
        private Class<Throwable> expectedException
        private Object expectedExceptionMessage
        private File expectFileToExist
        private File expectFileToNotExist

        def setExpectedResult(Map<String, ?> expectedResult) {
            this.expectedResult = expectedResult
        }

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

        def setGlobalArguments(Map<String, Object> args) {
            this.globalArguments = args
        }

        def setSearchPath(String searchPath) {
            this.searchPath = searchPath
        }

        def setSupportedOs(ArrayList<OperatingSystem> supportedOs) {
            this.supportedOs = supportedOs;
        }

        def setExpectedFileContent(Map<String, Object> content) {
            this.expectedFileContent = content
        }

        def setExpectations(Closure<Void> expectations) {
            this.expectations = expectations;
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
        TestSetupEnvironment testSetupEnvironment

        boolean shouldRun() {
            def filter = TestFilter.getInstance()

            return filter.shouldRun(TestFilter.DB, databaseName) &&
                    filter.shouldRun("command", definition.commandTestDefinition.joinedCommand) &&
                    filter.shouldRun("def", definition.description)
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

        /**
         * Run a changelog with labels
         */
        void runChangelog(String changeLogPath, String labels, String searchPath) {
            this.setups.add(new SetupRunChangelog(changeLogPath, labels, searchPath))
        }

        /*
         * Create files and directories
         */
        void createTempResource(String originalFile, String newFile) {
            this.setups.add(new SetupCreateTempResources(originalFile, newFile))
        }

        void createTempResource(String originalFile, String newFile, String baseDir) {
            this.setups.add(new SetupCreateTempResources(originalFile, newFile, baseDir))
        }

        void registerValueProvider(Closure<ConfigurationValueProvider> configurationValueProvider) {
            this.setups.add(new SetupConfigurationValueProvider(configurationValueProvider))
        }

        /**
         * @param fileLastModifiedDate if not null, the newly created file's last modified date will be set to this value
         */
        void createTempResource(String originalFile, String newFile, String baseDir, Date fileLastModifiedDate) {
            this.setups.add(new SetupCreateTempResources(originalFile, newFile, baseDir, fileLastModifiedDate))
        }

        void createTempDirectoryResource(String directory) {
            this.setups.add(new SetupCreateDirectoryResources(directory))
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
            copyResource(originalFile, newFile, true)
        }

        void copyResource(String originalFile, String newFile, boolean writeInTargetTestClasses) {
            this.setups.add(new SetupCreateTempResources(originalFile, newFile, writeInTargetTestClasses ? "target/test-classes" : "."))
        }

        /**
         *
         * Delete the specified resources
         *
         * @param filesToDelete
         *
         */
        void cleanResources(String... filesToDelete) {
            this.setups.add(new SetupCleanResources(filesToDelete))
        }

        /**
         *
         * Delete the specified resources after the test using a FilenameFilter
         *
         * @param filter
         * @param resourceDirectory
         *
         */
        void cleanResourcesAfter(FilenameFilter filter, File resourceDirectory) {
            this.setups.add(new SetupCleanResources(CleanupMode.CLEAN_ON_CLEANUP, filter, resourceDirectory))
        }

        /**
         *
         * Delete the specified resources at possibly setup and cleanup
         *
         * @param filesToDelete
         *
         */
        void cleanResources(CleanupMode cleanOnSetup, String... filesToDelete) {
            this.setups.add(new SetupCleanResources(cleanOnSetup, filesToDelete))
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

        void modifyProperties(File propsFile, String key, String value) {
            this.setups.add(new SetupModifyProperties(propsFile, key, value))
        }

        void modifyTextFile(File textFile, String originalString, String newString) {
            this.setups.add(new SetupModifyTextFile(textFile, originalString, newString))
        }

        void modifyDbCredentials(File textFile) {
            this.setups.add(new SetupModifyDbCredentials(textFile))
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

    public static String createRandomFilePath(String suffix) {
        String rand = "target/test-classes/" + StringUtil.randomIdentifier(10) + "." + suffix
        rand
    }

    interface OutputCheck {
        def check(String actual) throws AssertionError
        /**
         * @return the expected value from this output check
         */
        String getExpected()

        /**
         * @return the baseline contents from this output check
         */
        String getCheckedOutput()
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
        List<Resource> getAll(String path) throws IOException {
            def list = super.getAll(path)
            if (list != null && !list.isEmpty()) {
                return list;
            }

            return super.getAll(new File(path).getName())
        }

        @Override
        List<Resource> search(String path, boolean recursive) throws IOException {
            def list = super.search(path, recursive)
            if (list != null && ! list.isEmpty()) {
                return list
            }

            return super.search(new File(path).getName(), recursive)
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
            super(null, false)
            this.answers = answers
        }

        @Override
        String readLine() {
            //
            // Get the answer, increment the counter
            //
            if (answers.size() <= count) {
                throw new Exception("The test specified " + answers.size() + " prompt response(s), but the CLI is asking for an additional prompt response. Something is broken.")
            }
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
            if (exception != null) {
                exception.printStackTrace(errorOutput)
            }
        }

        @Override
        def <T> T prompt(String prompt, T valueIfNoEntry, InputHandler<T> inputHandler, Class<T> type) {
            this.sendMessage(prompt + ": ");
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
