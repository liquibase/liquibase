package liquibase.command.core.helpers;

import liquibase.Scope;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.changelog.visitor.DefaultChangeExecListener;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandBuilder;
import liquibase.command.CommandResultsBuilder;
import liquibase.command.CommandScope;
import liquibase.database.Database;
import liquibase.integration.commandline.ChangeExecListenerUtils;

import java.util.Collections;
import java.util.List;

/**
 * Creates a ChangeExecListener or utilizes the one provided as argument
 */
public class ChangeExecListenerCommandStep extends AbstractHelperCommandStep {

    protected static final String[] COMMAND_NAME = {"changeExecListener"};

    public static final CommandArgumentDefinition<String> CHANGE_EXEC_LISTENER_CLASS_ARG;
    public static final CommandArgumentDefinition<String> CHANGE_EXEC_LISTENER_PROPERTIES_FILE_ARG;
    public static final CommandArgumentDefinition<ChangeExecListener> CHANGE_EXEC_LISTENER_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        CHANGE_EXEC_LISTENER_CLASS_ARG = builder.argument("changeExecListenerClass", String.class)
                .description("Fully-qualified class which specifies a ChangeExecListener").build();
        CHANGE_EXEC_LISTENER_PROPERTIES_FILE_ARG = builder.argument("changeExecListenerPropertiesFile", String.class)
                .description("Path to a properties file for the ChangeExecListenerClass").build();
        CHANGE_EXEC_LISTENER_ARG = builder.argument("changeExecListener", ChangeExecListener.class)
                .hidden().build();
    }

    @Override
    public List<Class<?>> providedDependencies() {
        return Collections.singletonList(ChangeExecListener.class);
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Collections.singletonList(Database.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        Database database = (Database) commandScope.getDependency(Database.class);

        commandScope.provideDependency(ChangeExecListener.class,
                commandScope.getArgumentValue(CHANGE_EXEC_LISTENER_ARG) != null ?
                        commandScope.getArgumentValue(CHANGE_EXEC_LISTENER_ARG) :
                        this.getChangeExecListener(commandScope, database));
    }

    /**
     * Set up a "chain" of ChangeExecListeners. Starting with the custom change exec listener
     * then wrapping that in the DefaultChangeExecListener.
     */
    private DefaultChangeExecListener getChangeExecListener(CommandScope commandScope, Database database) throws Exception {
        DefaultChangeExecListener changeExecListener = new DefaultChangeExecListener();
        ChangeExecListener listener = ChangeExecListenerUtils.getChangeExecListener(database,
                Scope.getCurrentScope().getResourceAccessor(),
                commandScope.getArgumentValue(CHANGE_EXEC_LISTENER_CLASS_ARG),
                commandScope.getArgumentValue(CHANGE_EXEC_LISTENER_PROPERTIES_FILE_ARG));
        changeExecListener.addListener(listener);
        return changeExecListener;
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

}
