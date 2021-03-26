package liquibase.command;

import liquibase.exception.CommandExecutionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Convenience base class for {@link CommandStep}s that simply wrap calls to {@link liquibase.integration.commandline.Main}
 */
public abstract class AbstractCliWrapperCommandStep extends AbstractCommandStep {

    protected String[] createArgs(CommandScope commandScope) throws CommandExecutionException {
        List<String> argsList = new ArrayList<>();
        Map<String, CommandArgumentDefinition<?>> arguments = commandScope.getCommand().getArguments();
        arguments.entrySet().forEach( arg -> {
            String argValue = (commandScope.getArgumentValue(arg.getValue()) != null ? commandScope.getArgumentValue(arg.getValue()).toString() : null);
            if (argValue != null) {
                argsList.add("--" + arg.getKey() + "=" + commandScope.getArgumentValue(arg.getValue()).toString());
            }
        });
        if (commandScope.getArgumentValue("logLevel") != null) {
            argsList.add("--logLevel=" + commandScope.getArgumentValue("logLevel"));
        }
        argsList.add(commandScope.getCommand().getName()[0]);
        String[] args = new String[argsList.size()];
        argsList.toArray(args);
        return args;
    }

    protected String[] createParametersFromArgs(String[] args, String ... params) {
        List<String> argsList = new ArrayList(Arrays.asList(args));
        List<String> toRemove = new ArrayList<>();
        String matchingArg = null;
        for (String arg : argsList) {
            for (String paramName : params) {
                if (arg.startsWith("--" + paramName)) {
                    String[] parts = arg.split("=");
                    if (parts.length == 2) {
                        matchingArg = parts[1];
                    }
                    toRemove.add(arg);
                }
            }
        }

        //
        // Special handling for the count parameter
        //
        if (matchingArg != null) {
            argsList.removeAll(toRemove);
            args = new String[argsList.size() + 1];
            for (int i=0; i < argsList.size(); i++) {
                args[i] = argsList.get(i);
            }
            args[args.length - 1] = matchingArg;
        }
        return args;
    }

    protected void addStatusMessage(CommandResultsBuilder resultsBuilder, int statusCode) {
        if (statusCode == 0) {
            resultsBuilder.addResult("statusMessage", "Successfully executed " + getName()[0]);
        }
        else {
            resultsBuilder.addResult("statusMessage", "Unsuccessfully executed " + getName()[0]);
        }
    }
}
