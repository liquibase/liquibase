package liquibase.integration.commandline;

import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.configuration.core.DefaultsFileValueProvider;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.util.StringUtil;
import liquibase.util.SystemUtil;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CommandRunner implements Runnable {

    private CommandLine.Model.CommandSpec spec;

    @Override
    public void run() {
        List<String> command = new ArrayList<>();
        command.add(spec.commandLine().getCommandName());

        CommandLine parentCommand = spec.commandLine().getParent();
        while (!parentCommand.getCommandName().equals("liquibase")) {
            command.add(0, parentCommand.getCommandName());
            parentCommand = parentCommand.getParent();
        }
        final CommandLine rootCommand = parentCommand;

        String classpath = getBootstrapSetting(parentCommand, CommandLineConfiguration.CLASSPATH);
        if (StringUtil.trimToNull(classpath) == null) {
            classpath = ".";
        }
        final String bootstrapClasspath = classpath;

        final CompositeResourceAccessor createResourceAccessor = createResourceAccessor(bootstrapClasspath);

        try {
            Scope.child(Scope.Attr.resourceAccessor, createResourceAccessor, () -> {
                String defaultsFile = getBootstrapSetting(rootCommand, CommandLineConfiguration.DEFAULTS_FILE);

                if (defaultsFile != null) {
                    Scope.getCurrentScope().getLog(getClass()).fine("Using defaults file " + defaultsFile);
                    Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).registerProvider(new DefaultsFileValueProvider(defaultsFile));
                }

                Map<String, Object> finalScopeValues = new HashMap<>();
                //check if classpath changed from defaultsFile
                String newClasspath = CommandLineConfiguration.CLASSPATH.getCurrentValue();
                if (newClasspath != null && !newClasspath.equals(bootstrapClasspath)) {
                    finalScopeValues.put(Scope.Attr.resourceAccessor.name(), createResourceAccessor(newClasspath));
                }

                Scope.child(finalScopeValues, () -> {
                    try {
                        String[] commandName = new String[command.size()];
                        for (int i=0; i<command.size(); i++) {
                            commandName[i] = StringUtil.toCamelCase(command.get(i));
                        }
                        final CommandScope commandScope = new CommandScope(commandName);

                        for (CommandLine.Model.OptionSpec option : spec.commandLine().getParseResult().matchedOptions()) {
                            commandScope.addArgumentValue(toCommandArgumentDefinition(option), option.getValue());
                        }

                        for (CommandLine.Model.OptionSpec option : rootCommand.getParseResult().matchedOptions()) {
                            commandScope.addArgumentValue(toCommandArgumentDefinition(option), option.getValue());
                        }

                        commandScope.execute();
                    } catch (CommandExecutionException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }


//        parseResult.matchedOptions()
    }

    private CompositeResourceAccessor createResourceAccessor(String classpath) {
        List<File> classpathFiles = splitClasspath(classpath);
        return new CompositeResourceAccessor(new FileSystemResourceAccessor(classpathFiles.toArray(new File[0])), new ClassLoaderResourceAccessor());
    }

    private List<File> splitClasspath(String bootstrapClasspath) {
        List<File> classpathFiles = new ArrayList<>();
        if (StringUtil.trimToNull(bootstrapClasspath) != null) {
            String[] splitClasspath;
            if (SystemUtil.isWindows()) {
                splitClasspath = bootstrapClasspath.split(";");
            } else {
                splitClasspath = bootstrapClasspath.split(":");
            }
            for (String entry : splitClasspath) {
                classpathFiles.add(new File(".", entry));
            }
        }
        return classpathFiles;
    }

    private <T> T getBootstrapSetting(CommandLine rootCommand, ConfigurationDefinition<T> config) {
        final CommandLine.Model.OptionSpec matchedOption = rootCommand.getParseResult().matchedOption(StringUtil.toKabobCase(config.getKey().replace(".", "-")));
        if (matchedOption != null) {
            return matchedOption.getValue();
        }

        return config.getCurrentValue();
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
