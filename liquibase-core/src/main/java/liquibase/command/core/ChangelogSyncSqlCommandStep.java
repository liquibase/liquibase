package liquibase.command.core;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.util.LoggingExecutorTextUtil;

import java.io.*;

public class ChangelogSyncSqlCommandStep extends ChangelogSyncCommandStep {

    public static final String[] COMMAND_NAME = {"changelogSyncSql"};

    public static final CommandArgumentDefinition<Boolean> OUTPUT_DEFAULT_SCHEMA_ARG;
    public static final CommandArgumentDefinition<Boolean> OUTPUT_DEFAULT_CATALOG_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        OUTPUT_DEFAULT_SCHEMA_ARG = builder.argument("outputDefaultSchema", Boolean.class)
                .description("Control whether names of objects in the default schema are fully qualified or not. If true they are. If false, only objects outside the default schema are fully qualified")
                .defaultValue(true).build();
        OUTPUT_DEFAULT_CATALOG_ARG = builder.argument("outputDefaultCatalog", Boolean.class)
                .description("Control whether names of objects in the default catalog are fully qualified or not. If true they are. If false, only objects outside the default catalog are fully qualified")
                .defaultValue(true).build();
        builder.addArgument(CHANGELOG_FILE_ARG).build();
        builder.addArgument(LABEL_FILTER_ARG).build();
        builder.addArgument(CONTEXTS_ARG).build();
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        final CommandScope commandScope = resultsBuilder.getCommandScope();
        final Database database = (Database) commandScope.getDependency(Database.class);
        final Writer output = getOutputWriter(resultsBuilder);
        final String changelogFile = commandScope.getArgumentValue(CHANGELOG_FILE_ARG);

        Executor oldTemplate = getAndReplaceJdbcExecutor(output, database);
        LoggingExecutorTextUtil.outputHeader("SQL to add all changesets to database history table", database, changelogFile);
        super.run(resultsBuilder);
        flushOutputWriter(output);
        Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", database, oldTemplate);
    }

    private static Writer getOutputWriter(CommandResultsBuilder resultsBuilder) throws UnsupportedEncodingException {
        PrintStream outputStream = new PrintStream(resultsBuilder.getOutputStream());
        String charsetName = GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue();
        return new OutputStreamWriter(outputStream, charsetName);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME};
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Output the raw SQL used by Liquibase when running changelogSync");
    }

    private Executor getAndReplaceJdbcExecutor(Writer output, Database database) {
        Executor oldTemplate = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
        final LoggingExecutor loggingExecutor = new LoggingExecutor(oldTemplate, output, database);
        Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("logging", database, loggingExecutor);
        Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", database, loggingExecutor);
        return oldTemplate;
    }
    private void flushOutputWriter(Writer output) throws LiquibaseException {
        if (output == null) {
            return;
        }

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }
    }


}
