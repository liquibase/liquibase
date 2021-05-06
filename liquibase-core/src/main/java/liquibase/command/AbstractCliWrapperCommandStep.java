package liquibase.command;

import liquibase.Scope;
import liquibase.configuration.ConfiguredValue;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.CommandExecutionException;
import liquibase.integration.IntegrationConfiguration;
import liquibase.util.StringUtil;

import java.util.*;
import java.util.logging.Level;

/**
 * Convenience base class for {@link CommandStep}s that simply wrap calls to {@link liquibase.integration.commandline.Main}
 *
 * @deprecated
 */
public abstract class AbstractCliWrapperCommandStep extends AbstractCommandStep {

    protected String[] createArgs(CommandScope commandScope) throws CommandExecutionException {
        return createArgs(commandScope, new ArrayList<String>());
    }

    protected String[] createArgs(CommandScope commandScope, List<String> rhsArgs) throws CommandExecutionException {
        List<String> argsList = new ArrayList<>();
        Map<String, CommandArgumentDefinition<?>> arguments = commandScope.getCommand().getArguments();
        arguments.entrySet().forEach( arg -> {
            if (rhsArgs.contains(arg.getKey())) {
                return;
            }
            String argValue = (commandScope.getArgumentValue(arg.getValue()) != null ? commandScope.getArgumentValue(arg.getValue()).toString() : null);
            if (argValue != null) {
                argsList.add("--" + arg.getKey() + "=" + commandScope.getArgumentValue(arg.getValue()).toString());
            }
        });


        final Level logLevel = IntegrationConfiguration.LOG_LEVEL.getCurrentValue();
        if (logLevel != null) {
            argsList.add("--logLevel=" + logLevel);
        }

        String classpath = IntegrationConfiguration.CLASSPATH.getCurrentValue();
        if (classpath != null) {
            argsList.add("--classpath=" + classpath);
        }

        argsList.add(commandScope.getCommand().getName()[0]);
        if (! rhsArgs.isEmpty()) {
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

    protected String[] createParametersFromArgs(String[] args, String ... params) {
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
            for (int i=0; i < argsList.size(); i++) {
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

    protected void addStatusMessage(CommandResultsBuilder resultsBuilder, int statusCode) {
        if (statusCode == 0) {
            resultsBuilder.addResult("statusMessage", "Successfully executed " + getName()[0]);
        }
        else {
            resultsBuilder.addResult("statusMessage", "Unsuccessfully executed " + getName()[0]);
        }
    }
}
