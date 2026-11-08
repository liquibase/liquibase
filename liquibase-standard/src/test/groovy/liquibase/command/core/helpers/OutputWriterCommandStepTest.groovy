package liquibase.command.core.helpers

import liquibase.Scope
import liquibase.command.CommandFactory
import liquibase.command.CommandResultsBuilder
import liquibase.command.CommandScope
import liquibase.command.core.ChangelogSyncSqlCommandStep
import liquibase.command.core.RollbackCountCommandStep
import liquibase.command.core.RollbackCountSqlCommandStep
import liquibase.command.core.UpdateSqlCommandStep
import liquibase.database.Database
import liquibase.database.core.MockDatabase
import liquibase.exception.CommandExecutionException
import liquibase.executor.ExecutorService
import liquibase.executor.LoggingExecutor
import liquibase.lockservice.LockService
import liquibase.lockservice.LockServiceFactory
import spock.lang.Specification
import spock.lang.Unroll

class OutputWriterCommandStepTest extends Specification {

    private Database database

    def cleanup() {
        LockServiceFactory.reset()
        if (database != null) {
            def executorService = Scope.getCurrentScope().getSingleton(ExecutorService)
            executorService.clearExecutor("jdbc", database)
            executorService.clearExecutor("logging", database)
        }
    }

    private static class NoSqlOutputDatabase extends MockDatabase {
        @Override
        boolean supportsSqlOutput() {
            return false
        }

        @Override
        String getDisplayName() {
            return "NoSQL Mock"
        }
    }

    def "run() registers a LoggingExecutor for databases which support SQL output"() {
        given:
        database = new MockDatabase()
        def step = new OutputWriterCommandStep()
        def commandScope = new CommandScope(UpdateSqlCommandStep.COMMAND_NAME)
                .provideDependency(Database, database)
        def resultsBuilder = new CommandResultsBuilder(commandScope, new ByteArrayOutputStream())

        when:
        step.run(resultsBuilder)

        then:
        def executorService = Scope.getCurrentScope().getSingleton(ExecutorService)
        executorService.getExecutor("jdbc", database) instanceof LoggingExecutor
        executorService.getExecutor("logging", database) instanceof LoggingExecutor
        commandScope.getDependency(Writer) != null

        cleanup:
        step.cleanUp(resultsBuilder)
    }

    def "run() fails fast without registering a LoggingExecutor for databases which cannot produce SQL output"() {
        given:
        database = new NoSqlOutputDatabase()
        def step = new OutputWriterCommandStep()
        def commandScope = new CommandScope(UpdateSqlCommandStep.COMMAND_NAME)
                .provideDependency(Database, database)
        def resultsBuilder = new CommandResultsBuilder(commandScope, new ByteArrayOutputStream())

        when:
        step.run(resultsBuilder)

        then:
        def e = thrown(CommandExecutionException)
        e.message == "update-sql is not supported for NoSQL Mock: this database cannot produce SQL output"
        !(Scope.getCurrentScope().getSingleton(ExecutorService).getExecutor("jdbc", database) instanceof LoggingExecutor)
        commandScope.getDependency(Writer) == null

        when: "cleanUp() is still called by the pipeline after the failure"
        step.cleanUp(resultsBuilder)

        then:
        noExceptionThrown()
    }

    @Unroll
    def "#commandName fails before acquiring the changelog lock for databases which cannot produce SQL output"() {
        given:
        database = new NoSqlOutputDatabase()
        def lockService = Mock(LockService)
        LockServiceFactory.setInstance(Mock(LockServiceFactory) {
            getLockService(_) >> lockService
        })
        def commandScope = new CommandScope(commandName)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, "changelog.xml")
        additionalArguments.each { arg, value -> commandScope.addArgumentValue(arg, value) }
        commandScope.setOutput(new ByteArrayOutputStream())

        when:
        commandScope.execute()

        then:
        def e = thrown(CommandExecutionException)
        e.message.contains("${expectedName} is not supported for NoSQL Mock: this database cannot produce SQL output")
        0 * lockService.waitForLock()
        0 * lockService.acquireLock()

        where:
        commandName                                | expectedName             | additionalArguments
        UpdateSqlCommandStep.COMMAND_NAME          | "update-sql"             | [:]
        RollbackCountSqlCommandStep.COMMAND_NAME   | "rollback-count-sql"     | [(RollbackCountCommandStep.COUNT_ARG): 1]
        ChangelogSyncSqlCommandStep.COMMAND_NAME   | "changelog-sync-sql"     | [:]
    }

    def "output writer steps run before the changelog lock is acquired and the changelog history is initialized"() {
        when:
        def violations = []
        for (def command : Scope.getCurrentScope().getSingleton(CommandFactory).getCommands(true)) {
            def pipeline = command.pipeline*.class
            def writerIndex = pipeline.findIndexOf { AbstractOutputWriterCommandStep.isAssignableFrom(it) }
            if (writerIndex < 0) {
                continue
            }
            [LockServiceCommandStep, DatabaseChangelogCommandStep].each { stepClass ->
                def index = pipeline.indexOf(stepClass)
                if (index >= 0 && index < writerIndex) {
                    violations << "${command.name.join(' ')}: ${stepClass.simpleName} runs before ${pipeline[writerIndex].simpleName}"
                }
            }
        }

        then:
        violations == []
    }
}
