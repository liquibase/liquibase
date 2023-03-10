package liquibase.command.util;

import liquibase.command.CommandResultsBuilder;
import liquibase.command.CommandScope;
import liquibase.command.core.DropAllCommandStep;
import liquibase.command.core.GenerateChangelogCommandStep;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionCommandStep;
import liquibase.exception.CommandExecutionException;
import liquibase.extension.testing.testsystem.DatabaseTestSystem;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class CommandUtil {

    public static void runUpdate(DatabaseTestSystem db, String changelogFile) throws Exception {
        UpdateCommandStep step = new UpdateCommandStep();

        CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME);
        commandScope.addArgumentValue(UpdateCommandStep.URL_ARG, db.getConnectionUrl());
        commandScope.addArgumentValue(UpdateCommandStep.USERNAME_ARG, db.getUsername());
        commandScope.addArgumentValue(UpdateCommandStep.PASSWORD_ARG, db.getPassword());
        commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changelogFile);

        OutputStream outputStream = new ByteArrayOutputStream();
        CommandResultsBuilder commandResultsBuilder = new CommandResultsBuilder(commandScope, outputStream);
        step.run(commandResultsBuilder);
    }

    public static void runGenerateChangelog(DatabaseTestSystem db, String outputFile) throws CommandExecutionException {
        CommandScope commandScope = new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME);
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, db.getConnectionUrl());
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, db.getUsername());
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, db.getPassword());
        commandScope.addArgumentValue(GenerateChangelogCommandStep.CHANGELOG_FILE_ARG, outputFile);
        OutputStream outputStream = new ByteArrayOutputStream();
        commandScope.setOutput(outputStream);
        commandScope.execute();
    }

    public static void runDropAll(DatabaseTestSystem db) throws Exception {
        DropAllCommandStep step = new DropAllCommandStep();
        CommandScope commandScope = new CommandScope(DropAllCommandStep.COMMAND_NAME);
        commandScope.addArgumentValue(UpdateCommandStep.URL_ARG, db.getConnectionUrl());
        commandScope.addArgumentValue(UpdateCommandStep.USERNAME_ARG, db.getUsername());
        commandScope.addArgumentValue(UpdateCommandStep.PASSWORD_ARG, db.getPassword());
        OutputStream outputStream = new ByteArrayOutputStream();
        CommandResultsBuilder commandResultsBuilder = new CommandResultsBuilder(commandScope, outputStream);
        step.run(commandResultsBuilder);
    }
}
