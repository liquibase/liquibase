package liquibase.integrationtest.command

import liquibase.CatalogAndSchema
import liquibase.Scope
import liquibase.change.Change
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.DatabaseChangeLog
import liquibase.command.CommandArgumentDefinition
import liquibase.command.CommandScope
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.hub.HubService
import liquibase.hub.core.MockHubService
import liquibase.integrationtest.CustomTestSetup
import liquibase.integrationtest.TestDatabaseConnections
import liquibase.integrationtest.TestFilter
import liquibase.integrationtest.TestSetup
import liquibase.integrationtest.setup.SetupChangelogHistory
import liquibase.integrationtest.setup.SetupDatabaseStructure
import liquibase.parser.ChangeLogParser
import liquibase.parser.ChangeLogParserFactory
import liquibase.test.JUnitResourceAccessor
import liquibase.util.FileUtil
import liquibase.util.StringUtil
import org.codehaus.groovy.control.CompilerConfiguration
import spock.lang.Specification
import spock.lang.Unroll

import java.util.regex.Pattern

import static org.junit.Assume.assumeTrue
import static org.spockframework.util.Assert.fail

class CommandTest extends Specification {

    @Unroll("Run {db:#specPermutation.databaseName,command:#specPermutation.spec.joinedCommand} #specPermutation.spec.description")
    def "run spec"() {
        setup:
        assumeTrue("Skipping test: " + specPermutation.connectionStatus.errorMessage, specPermutation.connectionStatus.connection != null)

        def spec = specPermutation.spec

        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(specPermutation.connectionStatus.connection))

        String defaultSchemaName = database.getDefaultSchemaName()
        CatalogAndSchema[] catalogAndSchemas = new CatalogAndSchema[1]
        catalogAndSchemas[0] = new CatalogAndSchema(null, defaultSchemaName)
        database.dropDatabaseObjects(catalogAndSchemas[0])

        List expectedOutputChecks = new ArrayList()
        if (spec._expectedOutput instanceof List) {
            expectedOutputChecks.addAll(spec._expectedOutput)
        } else {
            expectedOutputChecks.add(spec._expectedOutput)
        }

        when:
        def commandScope
        try {
            commandScope = new CommandScope(spec.testSetup.command as String[])
        }
        catch (Throwable e) {
            if (spec._expectedException != null) {
                assert e.class == spec._expectedException
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

        if (spec.setup != null) {
            for (def setup : spec.setup) {
                setup.setup(specPermutation.connectionStatus)
            }
        }
        if (spec._customSetup != null) {
            for (def customSetup : spec._customSetup) {
                customSetup.customSetup(specPermutation.connectionStatus, commandScope)
            }
        }

        if (spec.arguments != null) {
            spec.arguments.each { name, value ->
                String key;
                if (name instanceof CommandArgumentDefinition) {
                    key = name.getName()
                }  else {
                    key = (String) name
                }
                Object objValue = (Object) value
                commandScope.addArgumentValue(key, objValue)
            }
        }
        def setupScopeId = Scope.enter([
                ("liquibase.plugin." + HubService.name): MockHubService,
        ])

        def results = commandScope.execute()

        Scope.exit(setupScopeId)

        def fullOutput = StringUtil.standardizeLineEndings(StringUtil.trimToEmpty(outputStream.toString()))

        if (spec._expectedResults.size() > 0 && results.getResults().isEmpty()) {
            throw new RuntimeException("Results were expected but none were found for " + spec._command)
        }
        for (def returnedResult : results.getResults().entrySet()) {
            def expectedValue = String.valueOf(spec._expectedResults.get(returnedResult.getKey()))
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

    static void addDatabaseChangeLogToScope(String changeLogFile, CommandScope commandScope) {
        //
        // Create a temporary changelog file
        //
        URL url = Thread.currentThread().getContextClassLoader().getResource(changeLogFile)
        File f = new File(url.toURI())
        String contents = FileUtil.getContents(f)
        File outputFile = File.createTempFile("changeLog-", ".xml", new File("target/test-classes"))
        FileUtil.write(contents, outputFile)
        changeLogFile = outputFile.getName()
        commandScope.addArgumentValue("changeLogFile", changeLogFile)

        //
        // Parse the file to get the DatabaseChangeLog and add it to the CommandScope
        //
        JUnitResourceAccessor resourceAccessor = new JUnitResourceAccessor()
        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor)
        ChangeLogParameters changeLogParameters = new ChangeLogParameters()
        DatabaseChangeLog databaseChangeLog = parser.parse(changeLogFile, changeLogParameters, resourceAccessor)
        commandScope.addArgumentValue("changeLog", databaseChangeLog)
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
        def config = new CompilerConfiguration()
        def shell = new GroovyShell(this.class.classLoader, config)

        def returnList = new ArrayList<Spec>()

        for (def specFile : collectSpecFiles()) {
            Class specClass

            try {
                def returnValue = shell.evaluate(specFile)

                if (!returnValue instanceof CommandTestSetup) {
                    fail("${specFile} is not a CommandTest specification")
                }

                def commandTestSetup = (CommandTestSetup) returnValue

                for (def specObj : commandTestSetup.specs) {
                    if (!specObj instanceof Spec) {
                        fail "$specFile must contain an array of LiquibaseCommandTest.Spec objects"
                    }

                    def spec = (Spec) specObj

                    spec.joinedCommand = StringUtil.join(spec.testSetup.command, "")

                    if (spec.description == null) {
                        spec.description = StringUtil.join((Collection) spec.testSetup.command, " ")
                    }

                    spec.validate()

                    returnList.add(spec)
                }
            } catch (Throwable e) {
                throw new RuntimeException("Error parsing ${specFile}: ${e.message}", e)
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

    static define(@DelegatesTo(CommandTestSetup) Closure closure) {
        CommandTestSetup setup = new CommandTestSetup()

        def code = closure.rehydrate(setup, this, setup)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        setup.validate()

        return setup
    }

    static class CommandTestSetup {

        /**
         * Command to test
         */
        List<String> command

        List<Spec> specs = new ArrayList<>()

        void run(@DelegatesTo(Spec) Closure cl) {
            def spec = new Spec()
            def code = cl.rehydrate(spec, this, this)
            code.resolveStrategy = Closure.DELEGATE_ONLY
            code()

            spec.testSetup = this;
            this.specs.add(spec)
        }

        void validate() throws IllegalArgumentException {
            if (command == null || command.size() == 0) {
                throw new IllegalArgumentException("'command' is required")
            }

        }

    }

    static class Spec {

        private String joinedCommand
        private CommandTestSetup testSetup

        /**
         * Description of this test for reporting purposes.
         * If not set, one will be generated for you.
         */
        String description

        /**
         * Arguments to command as key/value pairs
         */
        Map<String, Object> arguments = new HashMap<>()

        private List<TestSetup> setup

        private List<CustomTestSetup> _customSetup
        private List<Object> _expectedOutput
        private Map<String, Object> _expectedResults = new HashMap<>()
        private Class<Throwable> _expectedException

        Spec customSetup(CustomTestSetup... customSetup) {
            this._customSetup = customSetup
            this
        }

        String createTempResource(String prefix, String suffix) {
            File f = File.createTempFile(prefix, suffix, new File("target/test-classes"))
            return "target/test-classes/" + f.getName();
        }

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
        Spec expectedOutput(Object... output) {
            this._expectedOutput = output
            this
        }


        Spec expectedResults(Map<String, Object> results) {
            this._expectedResults = results
            this
        }

        Spec expectedException(Class<Throwable> exception) {
            this._expectedException = exception
            this
        }

        void validate() {
        }
    }

    private static class SpecPermutation {
        Spec spec
        String databaseName
        TestDatabaseConnections.ConnectionStatus connectionStatus

        boolean shouldRun() {
            def filter = TestFilter.getInstance()

            return filter.shouldRun(TestFilter.DB, databaseName) &&
                    filter.shouldRun("command", spec.joinedCommand)
        }
    }

    static class TestSetupDefinition {

        private List<TestSetup> setups = new ArrayList<>();

        void add(TestSetup setup) {
            this.setups.add(setup)
        }

        void setDatabase(List<Change> changes) {
            this.setups.add(new SetupDatabaseStructure(changes))
        }

        void setHistory(List<SetupChangelogHistory.HistoryEntry> changes) {
            this.setups.add(new SetupChangelogHistory(changes))
        }


        private void validate() throws IllegalArgumentException {

        }

        private List<TestSetup> build() {
            return setups
        }

    }
}
