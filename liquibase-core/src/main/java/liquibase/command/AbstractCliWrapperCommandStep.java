package liquibase.command;

import liquibase.exception.CommandExecutionException;
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

        CommandScope commandScope = resultsBuilder.getCommandScope();

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
     * Called by {@link #run(CommandResultsBuilder)} to create the actual arguments passed to {@link Main#run(String[])}
     */
    protected String[] collectArguments(CommandScope commandScope) throws CommandExecutionException {
        return createArgs(commandScope, Collections.singletonList("sqlFile"));
    }

    protected String[] createArgs(CommandScope commandScope) throws CommandExecutionException {
        return createArgs(commandScope, new ArrayList<String>());
    }

    protected String[] createArgs(CommandScope commandScope, List<String> rhsArgs) throws CommandExecutionException {
        List<String> argsList = new ArrayList<>();
        Map<String, CommandArgumentDefinition<?>> arguments = commandScope.getCommand().getArguments();
        arguments.entrySet().forEach(arg -> {
            if (rhsArgs.contains(arg.getKey())) {
                return;
            }
            String argValue = (commandScope.getArgumentValue(arg.getValue()) != null ? commandScope.getArgumentValue(arg.getValue()).toString() : null);
            if (argValue != null) {
                argsList.add("--" + arg.getKey() + "=" + commandScope.getArgumentValue(arg.getValue()).toString());
            }
        });

        argsList.add(commandScope.getCommand().getName()[0]);

        if (!rhsArgs.isEmpty()) {
            arguments.entrySet().forEach(arg -> {
                if (!rhsArgs.contains(arg.getKey())) {
                    return;
                }
                String argValue = (commandScope.getArgumentValue(arg.getValue()) != null ? commandScope.getArgumentValue(arg.getValue()).toString() : null);
                if (argValue != null) {
                    argsList.add("--" + arg.getKey() + "=" + commandScope.getArgumentValue(arg.getValue()).toString());
                }
            });
        }
        String[] args = new String[argsList.size()];
        argsList.toArray(args);
        return args;
    }

    protected String[] createParametersFromArgs(String[] args, String... params) {
        List<String> argsList = new ArrayList(Arrays.asList(args));
        List<String> toRemove = new ArrayList<>();
        List<String> matchingArgs = new ArrayList<>();
        for (String arg : argsList) {
            for (String paramName : params) {
                if (arg.startsWith(paramName) || arg.startsWith("--" + paramName)) {
                    String trimmed = arg.trim();
                    if (trimmed.charAt(trimmed.length()-1) == '=') {
                        trimmed = trimmed.replaceAll("=","");
                    }
                    if (paramName.startsWith("--")) {
                        matchingArgs.add(trimmed);
                    } else {
                        String[] parts = trimmed.split("=");
                        if (parts.length > 1) {
                            matchingArgs.add(parts[1]);
                        } else {
                            matchingArgs.add(trimmed);
                        }
                    }
                    toRemove.add(arg);
                }
            }
        }

        //
        // Special handling for command parameters
        //
        if (matchingArgs.size() > 0) {
            argsList.removeAll(toRemove);
            argsList.remove(Collections.singleton(null));
            args = new String[argsList.size() + matchingArgs.size()];
            for (int i = 0; i < argsList.size(); i++) {
                args[i] = argsList.get(i);
            }

            int l = args.length - matchingArgs.size();
            for (String matchingArg : matchingArgs) {
                args[l] = matchingArg;
                l++;
            }
        }
        return args;
    }
}
