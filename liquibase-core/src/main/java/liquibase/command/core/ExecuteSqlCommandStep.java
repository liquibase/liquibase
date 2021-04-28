package liquibase.command.core;

import liquibase.command.*;
import liquibase.integration.commandline.Main;

public class ExecuteSqlCommandStep extends AbstractCliWrapperCommandStep {

    public static final String[] COMMAND_NAME = {"executeSql"};

    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> USERNAME_ARG;
    public static final CommandArgumentDefinition<String> PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> SQL_ARG;
    public static final CommandArgumentDefinition<String> SQLFILE_ARG;
    public static final CommandArgumentDefinition<String> DELIMITER_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        URL_ARG = builder.argument("url", String.class).required()
            .description("The JDBC database connection URL").build();
        USERNAME_ARG = builder.argument("username", String.class)
            .description("Username to use to connect to the database").build();
        PASSWORD_ARG = builder.argument("password", String.class)
            .description("Password to use to connect to the database").build();
        SQL_ARG = builder.argument("sql", String.class)
            .description("SQL string to execute").build();
        SQLFILE_ARG = builder.argument("sqlFile", String.class)
            .description("SQL script to execute").build();
        DELIMITER_ARG = builder.argument("delimiter", String.class)
            .description("Delimiter to use when executing SQL script").build();
    }

    @Override
    public String[] getName() {
        return COMMAND_NAME;
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        String[] args = createArgs(commandScope);
        int statusCode = Main.run(args);
        addStatusMessage(resultsBuilder, statusCode);
        resultsBuilder.addResult("statusCode", statusCode);
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Execute a SQL string or file");
        commandDefinition.setLongDescription("Execute a SQL string or file");
    }
}
