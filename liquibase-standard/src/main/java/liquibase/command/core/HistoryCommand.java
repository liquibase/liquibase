package liquibase.command.core;

import liquibase.command.AbstractCommand;
import liquibase.command.CommandResult;
import liquibase.command.CommandScope;
import liquibase.command.CommandValidationErrors;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.database.Database;

import java.io.PrintStream;
import java.text.DateFormat;

import static liquibase.command.core.HistoryCommandStep.DATE_FORMAT_ARG;
import static liquibase.command.core.HistoryCommandStep.FORMAT_ARG;

/**
 * @deprecated Implement commands with {@link liquibase.command.CommandStep} and call them with {@link liquibase.command.CommandFactory#getCommandDefinition(String...)}.
 */
public class HistoryCommand extends AbstractCommand {

    private Database database;
    private final DateFormat dateFormat;
    private HistoryFormat format;
    private PrintStream outputStream = System.out;

    public HistoryCommand() {
        dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    }

    @Override
    public String getName() {
        return "history";
    }

    @Override
    public CommandValidationErrors validate() {
        return new CommandValidationErrors(this);
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public PrintStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(PrintStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public CommandResult run() throws Exception {
        final CommandScope commandScope = new CommandScope(HistoryCommandStep.COMMAND_NAME);
        commandScope.setOutput(getOutputStream());

        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, this.getDatabase());
        commandScope.addArgumentValue(DATE_FORMAT_ARG, this.dateFormat);
        commandScope.addArgumentValue(FORMAT_ARG, this.format);

        commandScope.execute();

        return new CommandResult("OK");
    }
}
