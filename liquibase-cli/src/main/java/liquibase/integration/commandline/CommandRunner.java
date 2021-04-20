package liquibase.integration.commandline;

import liquibase.command.CommandResults;
import liquibase.command.CommandScope;
import liquibase.util.StringUtil;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

class CommandRunner implements Callable<CommandResults> {

    private CommandLine.Model.CommandSpec spec;

    @Override
    public CommandResults call() throws Exception {
        List<String> command = new ArrayList<>();
        command.add(spec.commandLine().getCommandName());

        CommandLine parentCommand = spec.commandLine().getParent();
        while (!parentCommand.getCommandName().equals("liquibase")) {
            command.add(0, parentCommand.getCommandName());
            parentCommand = parentCommand.getParent();
        }
        final CommandLine rootCommand = parentCommand;


        String[] commandName = new String[command.size()];
        for (int i = 0; i < command.size(); i++) {
            commandName[i] = StringUtil.toCamelCase(command.get(i));
        }
        final CommandScope commandScope = new CommandScope(commandName);

        for (CommandLine.Model.OptionSpec option : spec.commandLine().getParseResult().matchedOptions()) {
            commandScope.addArgumentValue(toCommandArgumentDefinition(option), option.getValue());
        }

        for (CommandLine.Model.OptionSpec option : rootCommand.getParseResult().matchedOptions()) {
            commandScope.addArgumentValue(toCommandArgumentDefinition(option), option.getValue());
        }

        return commandScope.execute();
    }


    private String toCommandArgumentDefinition(CommandLine.Model.OptionSpec option) {
        if (option.names().length > 1) {
            throw new RuntimeException("Found too many names for " + option.toString());
        }

        final String argName = option.names()[0];
        return StringUtil.toCamelCase(argName.replaceFirst("^--", ""));
    }

    public void setSpec(CommandLine.Model.CommandSpec spec) {
        this.spec = spec;
    }
}
