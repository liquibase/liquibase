package liquibase.command;

import liquibase.exception.CommandExecutionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class AbstractWrapperCommand extends AbstractCommand {

    protected String[] createArgs(CommandScope commandScope) throws CommandExecutionException {
        List<String> argsList = new ArrayList<>();
        Map<String, CommandArgumentDefinition> arguments = commandScope.getArguments();
        arguments.entrySet().forEach( arg -> {
            String argValue = (arg.getValue().getValue(commandScope) != null ? arg.getValue().getValue(commandScope).toString() : null);
            if (argValue != null) {
                argsList.add("--" + arg.getKey() + "=" + arg.getValue().getValue(commandScope).toString());
            }
        });
        argsList.add(commandScope.getCommand()[0]);
        String[] args = new String[argsList.size()];
        argsList.toArray(args);
        return args;
    }

    protected String[] createParametersFromArgs(String[] args, String paramName) {
        List<String> argsList = new ArrayList(Arrays.asList(args));
        List<String> toRemove = new ArrayList<>();
        String tagArg = null;
        for (String arg : argsList) {
            if (arg.startsWith("--" + paramName)) {
                String[] parts = arg.split("=");
                if (parts.length == 2) {
                    tagArg = parts[1];
                }
                toRemove.add(arg);
            }
        }

        //
        // Special handling for the count parameter
        //
        if (tagArg != null) {
            argsList.removeAll(toRemove);
            args = new String[argsList.size() + 1];
            for (int i=0; i < argsList.size(); i++) {
                args[i] = argsList.get(i);
            }
            args[args.length - 1] = tagArg;
        }
        return args;
    }
}
