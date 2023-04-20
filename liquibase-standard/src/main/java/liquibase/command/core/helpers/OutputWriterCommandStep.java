package liquibase.command.core.helpers;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.logging.mdc.MdcKey;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

/**
 * This helper class can be run prior to any command (but usually the *-sql commands, like update-sql) to redirect
 * the SQL to the console, rather than running it against an actual database.
 */
public class OutputWriterCommandStep extends AbstractHelperCommandStep implements CleanUpCommandStep {
    protected static final String[] COMMAND_NAME = {"outputWriterCommandStep"};

    private static OutputStreamWriter outputStreamWriter;

    @Override
    public List<Class<?>> providedDependencies() {
        return Collections.singletonList(Writer.class);
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Collections.singletonList(Database.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        String charsetName = GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue();
        outputStreamWriter = new OutputStreamWriter(resultsBuilder.getOutputStream(), charsetName);
        Database database = (Database) commandScope.getDependency(Database.class);
        Executor databaseExecutor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
        LoggingExecutor loggingExecutor = new LoggingExecutor(databaseExecutor, outputStreamWriter, database);
        Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", database, loggingExecutor);
        Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("logging", database, loggingExecutor);

        commandScope.provideDependency(Writer.class, outputStreamWriter);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void cleanUp(CommandResultsBuilder resultsBuilder) {
        if (outputStreamWriter != null) {
            Scope.getCurrentScope().getSingleton(ExecutorService.class).reset();
            try {
                outputStreamWriter.close();
            } catch (IOException e) {
                Scope.getCurrentScope().getLog(getClass()).warning("Failed to close output stream writer.", e);
            }
        }
    }
}
