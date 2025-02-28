package liquibase.integration.commandline;

import liquibase.Scope;
import liquibase.command.CommandResults;
import liquibase.command.CommandScope;
import liquibase.command.CommonArgumentNames;
import liquibase.exception.CommandValidationException;
import liquibase.exception.MissingRequiredArgumentException;
import liquibase.io.OutputFileHandler;
import liquibase.io.OutputFileHandlerFactory;
import liquibase.util.StringUtil;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        final String[] commandName = LiquibaseCommandLine.getCommandNames(spec.commandLine());
        for (int i=0; i<commandName.length; i++) {
            commandName[i] = StringUtil.toCamelCase(commandName[i]);
        }

        final CommandScope commandScope = new CommandScope(commandName);
        final String outputFile = LiquibaseCommandLineConfiguration.OUTPUT_FILE.getCurrentValue();
        final OutputFileHandlerFactory outputFileHandlerFactory = Scope.getCurrentScope().getSingleton(OutputFileHandlerFactory.class);
        OutputFileHandler outputFileHandler = outputFileHandlerFactory.getOutputFileHandler(outputFile);

        try {
            if (outputFile != null) {
                outputFileHandler.create(outputFile, commandScope);
            }

            return commandScope.execute();
        } catch (CommandValidationException cve) {
            Throwable cause = cve.getCause();
            if (cause instanceof MissingRequiredArgumentException) {
                // This is a list of the arguments which the init project command supports. The thinking here is that if the user
                // forgets to supply one of these arguments, we're going to remind them about the init project command, which
                // can help them figure out what they should be providing here.
                final Set<String> initProjectArguments = Stream.of(CommonArgumentNames.CHANGELOG_FILE, CommonArgumentNames.URL, CommonArgumentNames.USERNAME, CommonArgumentNames.PASSWORD).map(CommonArgumentNames::getArgumentName).collect(Collectors.toSet());
                throw new CommandValidationException(cve.getMessage() + (initProjectArguments.contains(((MissingRequiredArgumentException) cause).getArgumentName()) ? ". If you need to configure new liquibase project files and arguments, run the 'liquibase init project' command." : ""));
            } else {
                throw cve;
            }
        } finally {
            outputFileHandler.close();
        }
    }

    public void setSpec(CommandLine.Model.CommandSpec spec) {
        this.spec = spec;
    }
}
