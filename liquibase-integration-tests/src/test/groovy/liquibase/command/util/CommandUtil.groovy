package liquibase.command.util;

import liquibase.Scope
import liquibase.UpdateSummaryEnum;
import liquibase.command.CommandResultsBuilder;
import liquibase.command.CommandScope;
import liquibase.command.core.DropAllCommandStep;
import liquibase.command.core.GenerateChangelogCommandStep;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionCommandStep
import liquibase.command.core.helpers.ShowSummaryArgument;
import liquibase.exception.CommandExecutionException;
import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.resource.SearchPathResourceAccessor
import liquibase.sdk.resource.MockResourceAccessor

class CommandUtil {

    private static String testChangelog = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "\n" +
            "<databaseChangeLog\n" +
            "        xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "        xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\">\n" +
            "\n" +
            "<changeSet author=\"mallod\" id=\"enumGenerateChangelogTest-1\">\n" +
            "    <createTable tableName=\"EnumTestTable\">\n" +
            "        <column name=\"id\" type=\"INT\">\n" +
            "            <constraints nullable=\"false\" primaryKey=\"true\"/>\n" +
            "        </column>\n" +
            "        <column name=\"status\" type=\"ENUM('FAILED', 'CANCELLED', 'INGEST', 'IN_PROGRESS', 'COMPLETE')\">\n" +
            "            <constraints nullable=\"false\"/>\n" +
            "        </column>\n" +
            "        <column name=\"name\" type=\"VARCHAR(50)\">\n" +
            "            <constraints nullable=\"false\"/>\n" +
            "        </column>\n" +
            "    </createTable>\n" +
            "</changeSet>\n" +
            "\n" +
            "</databaseChangeLog>"

    static void runUpdateWithTestChangelog(DatabaseTestSystem db, String changelogFile) throws Exception {
        SearchPathResourceAccessor resourceAccessor = new SearchPathResourceAccessor(new MockResourceAccessor([(changelogFile): testChangelog]))
        execUpdateCommandInScope(resourceAccessor, db, changelogFile)
    }

    static void runUpdate(DatabaseTestSystem db, String changelogFile) throws Exception {
        SearchPathResourceAccessor resourceAccessor = new SearchPathResourceAccessor(".")
        execUpdateCommandInScope(resourceAccessor, db, changelogFile)
    }

    static void runUpdate(DatabaseTestSystem db, String changelogFile, String labels, String contexts, String outputFile) throws Exception {
        SearchPathResourceAccessor resourceAccessor = new SearchPathResourceAccessor(".,target/test-classes")
        execUpdateCommandInScope(resourceAccessor, db, changelogFile, labels, contexts, outputFile)
    }
    static void runGenerateChangelog(DatabaseTestSystem db, String outputFile) throws CommandExecutionException {
        CommandScope commandScope = new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, db.getConnectionUrl())
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, db.getUsername())
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, db.getPassword())
        commandScope.addArgumentValue(GenerateChangelogCommandStep.OVERWRITE_OUTPUT_FILE_ARG, true)
        commandScope.addArgumentValue(GenerateChangelogCommandStep.CHANGELOG_FILE_ARG, outputFile)
        OutputStream outputStream = new ByteArrayOutputStream()
        commandScope.setOutput(outputStream)
        commandScope.execute()
    }

    static void runDropAll(DatabaseTestSystem db) throws Exception {
        if (! db.shouldTest()) {
            return;
        }
        DropAllCommandStep step = new DropAllCommandStep()
        CommandScope commandScope = new CommandScope(DropAllCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, db.getConnectionUrl())
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, db.getUsername())
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, db.getPassword())
        OutputStream outputStream = new ByteArrayOutputStream()
        CommandResultsBuilder commandResultsBuilder = new CommandResultsBuilder(commandScope, outputStream)
        step.run(commandResultsBuilder)
    }

    private static void execUpdateCommandInScope(SearchPathResourceAccessor resourceAccessor, DatabaseTestSystem db, String changelogFile,
                                                 String labels, String contexts, String outputFile) {
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()): resourceAccessor
        ]
        Scope.child(scopeSettings, {
            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
            commandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, db.getConnectionUrl())
            commandScope.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, db.getUsername())
            commandScope.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, db.getPassword())
            commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changelogFile)
            commandScope.addArgumentValue(UpdateCommandStep.LABEL_FILTER_ARG, labels)
            commandScope.addArgumentValue(UpdateCommandStep.CONTEXTS_ARG, contexts)
            commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY, UpdateSummaryEnum.SUMMARY)
            if (outputFile != null) {
                OutputStream outputStream = new FileOutputStream(new File(outputFile))
                commandScope.setOutput(outputStream)
            }
            commandScope.execute()
        } as Scope.ScopedRunnerWithReturn<Void>)
    }

    private static void execUpdateCommandInScope(SearchPathResourceAccessor resourceAccessor, DatabaseTestSystem db, String changelogFile) {
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()): resourceAccessor
        ]
        Scope.child(scopeSettings, {
            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
            commandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, db.getConnectionUrl())
            commandScope.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, db.getUsername())
            commandScope.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, db.getPassword())
            commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changelogFile)
            commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY, UpdateSummaryEnum.SUMMARY)
            commandScope.execute()
        } as Scope.ScopedRunnerWithReturn<Void>)
    }
}
