package liquibase.command;

import liquibase.exception.CommandExecutionException;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.integration.commandline.Main;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * Convenience base class for {@link CommandStep}s that simply wrap calls to {@link liquibase.integration.commandline.Main}
 *
 * @deprecated
 */
public abstract class AbstractCliWrapperCommandStep extends AbstractCommandStep {

    @Override
    public final void run(CommandResultsBuilder resultsBuilder) throws Exception {
        preRunCheck(resultsBuilder);

        final OutputStream outputStream = resultsBuilder.getOutputStream();
        PrintStream printStream = null;

        if (outputStream != null) {
            printStream = new PrintStream(outputStream);
            Main.setOutputStream(printStream);
        }

        final CommandScope commandScope = resultsBuilder.getCommandScope();

        String[] args = collectArguments(commandScope);
        int statusCode = Main.run(args);
        if (statusCode != 0) {
            throw new CommandExecutionException("Unexpected error running liquibase");
        }
        resultsBuilder.addResult("statusCode", statusCode);

        if (printStream != null) {
            printStream.close();
        }
    }

    protected void preRunCheck(CommandResultsBuilder resultsBuilder) {

    }

    /**
     * Called by {@link #run(CommandResultsBuilder)} to create the actual arguments passed to {@link Main#run(String[])}.
     * <p>
     * Implementations should generally call {@link #collectArguments(CommandScope, List, String)}
     * and possibly {@link #removeArgumentValues(String[], String...)}
     */

    protected abstract String[] collectArguments(CommandScope commandScope) throws CommandExecutionException;

    /**
     * Collects the values from commandScope into an argument array to pass to {@link Main}.
     * All arguments will values in commandScope will be passed as global arguments EXCEPT for ones listed in the commandArguments.
     * If main takes a "positional argument" like `liquibase tag tagName`, specify the commandScope argument that should be converted to a positional argument in "positionalArgumentName".
     *
     * @see #removeArgumentValues(String[], String...) If any arguments should not have a value (like a --verbose flag), see
     */
    protected String[] collectArguments(CommandScope commandScope, List<String> commandArguments, String positionalArgumentName) throws CommandExecutionException {
        if (commandArguments == null) {
            commandArguments = Collections.emptyList();
        }

        final List<String> finalLegacyCommandArguments = commandArguments;

        List<String> argsList = new ArrayList<>();
        Map<String, CommandArgumentDefinition<?>> arguments = commandScope.getCommand().getArguments();
        arguments.forEach((key, value) -> {
            if (value.getHidden() && !LiquibaseCommandLineConfiguration.SHOW_HIDDEN_ARGS.getCurrentValue()) { // This may impact tests that use the legacy Main class. Previously hidden arguments were not being hidden when executing.
                return;
            }
            if (finalLegacyCommandArguments.contains(key)) {
                return;
            }

            if (positionalArgumentName != null && positionalArgumentName.equalsIgnoreCase(key)) {
                return;
            }
            Object argumentValue = commandScope.getArgumentValue(value);
            String argValue = (argumentValue != null ? argumentValue.toString() : null);
            if (argValue != null) {
                argsList.add("--" + key);
                argsList.add(argumentValue.toString());
            }
        });

        argsList.add(commandScope.getCommand().getName()[0]);

        arguments.forEach((key, value) -> {
            if (key.equalsIgnoreCase(positionalArgumentName)) {
                Object argumentValue = commandScope.getArgumentValue(value);
                String argValue = (argumentValue != null ? argumentValue.toString() : null);
                if (argValue != null) {
                    argsList.add(argValue);
                }
                return;
            }

            if (!finalLegacyCommandArguments.contains(key)) {
                return;
            }
            Object argumentValue = commandScope.getArgumentValue(value);
            String argValue = (argumentValue != null ? argumentValue.toString() : null);
            if (argValue != null) {
                argsList.add("--" + key);
                argsList.add(argumentValue.toString());
            }
        });
        String[] args = new String[argsList.size()];
        argsList.toArray(args);
        return args;
    }

    protected String[] removeArgumentValues(String[] allArguments, String... argumentsThatTakeNoValue) {
        List<String> returnArgs = new ArrayList<>();
        Set<String> argsToStrip = new HashSet<>(Arrays.asList(argumentsThatTakeNoValue));
        final Iterator<String> iterator = Arrays.asList(allArguments).iterator();
        while (iterator.hasNext()) {
            final String arg = iterator.next();
            returnArgs.add(arg);
            if (argsToStrip.contains(arg.replace("--", ""))) {
                if (iterator.hasNext()) {
                    iterator.next();
                }
            }
        }

        return returnArgs.toArray(new String[0]);
    }
}
