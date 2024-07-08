package liquibase.command.core.helpers;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.command.CleanUpCommandStep;
import liquibase.command.CommandResultsBuilder;
import liquibase.command.CommandScope;
import liquibase.database.Database;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;

public abstract class AbstractOutputWriterCommandStep extends AbstractHelperCommandStep implements CleanUpCommandStep {

    private OutputStreamWriter outputStreamWriter;

    @Override
    public List<Class<?>> providedDependencies() {
        return Collections.singletonList(getProvidedWriterDependency());
    }

    /**
     * Get the dependency that is provided by this class. In some cases this is {@link java.io.Writer}, but might be
     * other similar classes depending on the implementation.
     */
    public abstract Class<?> getProvidedWriterDependency();

    @Override
    public List<Class<?>> requiredDependencies() {
        return Collections.singletonList(getDatabaseDependency());
    }

    /**
     * Get the database dependency that is required by this class.
     */
    public abstract Class<?> getDatabaseDependency();

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

        commandScope.provideDependency(getProvidedWriterDependency(), outputStreamWriter);
    }

    @Override
    public String[][] defineCommandNames() {
        String name = "outputWriterCommandStep-" + getDatabaseDependency().getSimpleName();
        return new String[][]{{name}};
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
