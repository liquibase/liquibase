package liquibase.command;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.configuration.*;
import liquibase.database.Database;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.CommandValidationException;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.listener.LiquibaseListener;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcManager;
import liquibase.logging.mdc.MdcObject;
import liquibase.logging.mdc.MdcValue;
import liquibase.logging.mdc.customobjects.ExceptionDetails;
import liquibase.util.StringUtil;
import lombok.Getter;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

/**
 * The primary facade used for executing commands.
 * This object gets configured with the command to run and the input arguments associated with it,
 * then is populated with the result output after {@link #execute()} is called.
 * <p>
 * Named similarly to {@link Scope} because they both define a self-contained unit of values, but this
 * scope is specific to a command rather than being a global scope.
 */
public class CommandScope {

    private static final String NO_PREFIX_REGEX = ".*\\.";
    public static final Pattern NO_PREFIX_PATTERN = Pattern.compile(NO_PREFIX_REGEX);
    private final CommandDefinition commandDefinition;

    private final SortedMap<String, Object> argumentValues = new TreeMap<>();

    private final Map<Class<?>, Object> dependencies = new HashMap<>();

    /**
     * Config key including the command name. Example `liquibase.command.update`
     */
    private final String completeConfigPrefix;

    /**
     * Config key without the command name. Example `liquibase.command`
     */
    private final String shortConfigPrefix;

    private OutputStream outputStream;
    @Getter
    private Date operationStartTime;

    /**
     * Creates a new scope for the given command.
     */
    public CommandScope(String... commandName) throws CommandExecutionException {
        setOutput(System.out);

        final CommandFactory commandFactory = Scope.getCurrentScope().getSingleton(CommandFactory.class);

        this.commandDefinition = commandFactory.getCommandDefinition(commandName);

        completeConfigPrefix = "liquibase.command." + StringUtil.join(Arrays.asList(this.getCommand().getName()), ".");
        shortConfigPrefix = "liquibase.command";


    }

    /**
     * Returns the {@link CommandDefinition} for the command in this scope.
     */
    public CommandDefinition getCommand() {
        return commandDefinition;
    }

    /**
     * Returns the complete configuration prefix (without a trailing period) for the command in this scope.
     *
     * @return the complete configuration prefix for the command in this scope
     */
    public String getCompleteConfigPrefix() {
        return completeConfigPrefix;
    }

    /**
     * Adds the given key/value pair to the stored argument data.
     *
     * @see #addArgumentValue(CommandArgumentDefinition, Object) for a type-safe version
     */
    public CommandScope addArgumentValue(String argument, Object value) {
        this.argumentValues.put(argument, value);

        return this;
    }

    /**
     * Adds the given key/value pair to the stored argument data.
     */
    public <T> CommandScope addArgumentValue(CommandArgumentDefinition<T> argument, T value) {
        this.argumentValues.put(argument.getName(), value);

        return this;
    }


    /**
     * Returns the detailed information about how an argument is set.
     * <br><br>
     * Prefers values set with {@link #addArgumentValue(String, Object)}, but will also check {@link liquibase.configuration.LiquibaseConfiguration}
     * for settings of liquibase.command.${commandName(s)}.${argumentName} or liquibase.command.${argumentName}
     */
    public <T> ConfiguredValue<T> getConfiguredValue(CommandArgumentDefinition<T> argument) {
        ConfigurationDefinition<T> configDef = createConfigurationDefinition(argument, true);
        ConfiguredValue<T> providedValue = configDef.getCurrentConfiguredValue(new CommandScopeValueProvider());

        if (!providedValue.found() || providedValue.wasDefaultValueUsed()) {
            ConfigurationDefinition<T> noCommandConfigDef = createConfigurationDefinition(argument, false);
            ConfiguredValue<T> noCommandNameProvidedValue = noCommandConfigDef.getCurrentConfiguredValue();
            if (noCommandNameProvidedValue.found() && !noCommandNameProvidedValue.wasDefaultValueUsed()) {
                providedValue = noCommandNameProvidedValue;
            }
        }

        return providedValue;
    }

    /**
     * Convenience method for {@link #getConfiguredValue(CommandArgumentDefinition)}, returning {@link ConfiguredValue#getValue()} along with any
     * {@link CommandArgumentDefinition#getValueConverter()} applied
     */
    public <T> T getArgumentValue(CommandArgumentDefinition<T> argument) {
        final T value = getConfiguredValue(argument).getValue();
        return ConfigurationValueUtils.convertDataType(argument.getName(), value, argument.getValueConverter());
    }

    /**
     * Assign a value to a given provided dependency. So if a CommandStep provides class X, at
     * {@link CommandStep#run(CommandResultsBuilder)} method it needs to provide the value for X using this method.
     * commandScope.provideDependency(LockService.class, lockService);
     * <p>
     * Means that this class will LockService.class using object lock
     */
    public CommandScope provideDependency(Class<?> clazz, Object value) {
        this.dependencies.put(clazz, value);

        return this;
    }

    /**
     * Retrieves the registered dependency object provided by this class identifier
     */
    public <T> Object getDependency(Class<T> clazz) {
        return this.dependencies.get(clazz);
    }

    /**
     * Sets the output stream for this command.
     * The command output sent to this stream should not include status/progress type output, only the actual output itself.
     * Think "what would be piped out", not "what the user is told about what is happening".
     */
    public CommandScope setOutput(OutputStream outputStream) {
        /*
        This is an UnclosableOutputStream because we do not want individual command steps to inadvertently (or
        intentionally) close the System.out OutputStream. Closing System.out renders it unusable for other command
        steps which expect it to still be open.  If the passed OutputStream is null then we do not create it.
         */
        if (outputStream != null) {
            this.outputStream = new UnclosableOutputStream(outputStream);
        } else {
            this.outputStream = null;
        }

        return this;
    }

    public void validate() throws CommandValidationException {
        for (ConfigurationValueProvider provider : Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getProviders()) {
            provider.validate(this);
        }

        for (CommandArgumentDefinition<?> definition : commandDefinition.getArguments().values()) {
            definition.validate(this);
        }

        final List<CommandStep> pipeline = commandDefinition.getPipeline();

        Scope.getCurrentScope().getLog(getClass()).fine("Pipeline for command '" + StringUtil.join(commandDefinition.getName(), " ") + ": " + StringUtil.join(pipeline, " then ", obj -> obj.getClass().getName()));

        for (CommandStep step : pipeline) {
            step.validate(this);
        }
    }

    /**
     * Executes the command in this scope, and returns the results.
     */
    public CommandResults execute() throws CommandExecutionException {
        operationStartTime = new Date();
        Scope.getCurrentScope().addMdcValue(MdcKey.OPERATION_START_TIME, Instant.ofEpochMilli(operationStartTime.getTime()).toString());
        // We don't want to reset the command name even when defining another CommandScope during execution
        // because we intend on keeping this value as the command entered to the console
        if (!Scope.getCurrentScope().isMdcKeyPresent(MdcKey.LIQUIBASE_COMMAND_NAME)) {
            Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_COMMAND_NAME, String.join(" ", commandDefinition.getName()));
        }
        CommandResultsBuilder resultsBuilder = new CommandResultsBuilder(this, outputStream);
        final List<CommandStep> pipeline = commandDefinition.getPipeline();
        final List<CommandStep> executedCommands = new ArrayList<>();
        Optional<Exception> thrownException = Optional.empty();
        validate();
        try {
            addOutputFileToMdc();
            for (CommandStep command : pipeline) {
                try {
                    Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_INTERNAL_COMMAND, getCommandStepName(command));
                    Scope.getCurrentScope().getLog(CommandScope.class).fine(String.format("Executing internal command %s", getCommandStepName(command)));
                    command.run(resultsBuilder);
                } catch (Exception runException) {
                    // Suppress the exception for now so that we can run the cleanup steps even when encountering an exception.
                    thrownException = Optional.of(runException);
                    Scope.getCurrentScope().addMdcValue(MdcKey.OPERATION_OUTCOME, MdcValue.COMMAND_FAILED, false);
                    break;
                }
                executedCommands.add(command);
            }

            // To find the correct database source if there was an exception
            // we need to examine the database connection prior to closing it.
            // That means this must run prior to any cleanup command steps.
            Database database = (Database) getDependency(Database.class);
            String source = null;
            if (database != null) {
                source = ExceptionDetails.findSource(database);
            }

            // after executing our pipeline, runs cleanup in inverse order
            for (int i = executedCommands.size() - 1; i >= 0; i--) {
                CommandStep command = pipeline.get(i);
                if (command instanceof CleanUpCommandStep) {
                    ((CleanUpCommandStep) command).cleanUp(resultsBuilder);
                }
            }
            if (thrownException.isPresent()) { // Now that we've executed all our cleanup, rethrow the exception if there was one
                if (!executedCommands.isEmpty()) {
                    logPrimaryExceptionToMdc(thrownException.get(), source);
                }
                throw thrownException.get();
            } else {
                Scope.getCurrentScope().addMdcValue(MdcKey.OPERATION_OUTCOME, MdcValue.COMMAND_SUCCESSFUL, false);
            }
        } catch (Exception e) {
            if (e instanceof CommandExecutionException) {
                throw (CommandExecutionException) e;
            } else {
                throw new CommandExecutionException(e);
            }
        } finally {
            try (MdcObject operationStopTime = Scope.getCurrentScope().addMdcValue(MdcKey.OPERATION_STOP_TIME, Instant.ofEpochMilli(new Date().getTime()).toString())) {
                Scope.getCurrentScope().getLog(getClass()).info("Command execution complete");
            }
            try {
                if (this.outputStream != null) {
                    this.outputStream.flush();
                }
            } catch (Exception e) {
                Scope.getCurrentScope().getLog(getClass()).warning("Error flushing command output stream: " + e.getMessage(), e);
            }
        }

        return resultsBuilder.build();
    }

    private void logPrimaryExceptionToMdc(Throwable exception, String source) {
        ExceptionDetails exceptionDetails = new ExceptionDetails(exception, source);
        if (exceptionDetails.getPrimaryException() != null) {
            MdcManager mdcManager = Scope.getCurrentScope().getMdcManager();
            try (MdcObject primaryExceptionObject = mdcManager.put(MdcKey.EXCEPTION_DETAILS, exceptionDetails)) {
                Scope.getCurrentScope().getLog(getClass()).info("Logging exception.");
            }
            Scope.getCurrentScope().getUI().sendMessage("ERROR: Exception Details");
            Scope.getCurrentScope().getUI().sendMessage(exceptionDetails.getFormattedPrimaryException());
            Scope.getCurrentScope().getUI().sendMessage(exceptionDetails.getFormattedPrimaryExceptionReason());
            Scope.getCurrentScope().getUI().sendMessage(exceptionDetails.getFormattedPrimaryExceptionSource());
        }
    }

    private void addOutputFileToMdc() throws Exception {
        Scope.child((LiquibaseListener) null, () -> {
            String outputFilePath = LiquibaseCommandLineConfiguration.OUTPUT_FILE.getCurrentValue();
            if (outputFilePath != null) {
                Scope.getCurrentScope().addMdcValue(MdcKey.OUTPUT_FILE, outputFilePath);
            }
            String outputFileEncoding = GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue();
            if (outputFileEncoding != null) {
                Scope.getCurrentScope().addMdcValue(MdcKey.OUTPUT_FILE_ENCODING, outputFileEncoding);
            }
            if (outputFilePath != null) {
                Scope.getCurrentScope().getLog(CommandScope.class).fine("Writing output to '" + outputFilePath + "' with encoding '" + outputFileEncoding + "'");
            } else {
                Scope.getCurrentScope().getLog(CommandScope.class).fine("Writing output with encoding '" + outputFileEncoding + "'");
            }
        });
    }

    private <T> ConfigurationDefinition<T> createConfigurationDefinition(CommandArgumentDefinition<T> argument, boolean includeCommandName) {
        final String key;
        if (includeCommandName) {
            key = completeConfigPrefix;
        } else {
            key = shortConfigPrefix;
        }

        return new ConfigurationDefinition.Builder(key)
                .define(argument.getName(), argument.getDataType())
                .addAliases(argument.getAliases())
                .setDefaultValue(argument.getDefaultValue())
                .setDescription(argument.getDescription())
                .setValueHandler(argument.getValueConverter())
                .setValueObfuscator(argument.getValueObfuscator())
                .buildTemporary();
    }

    /**
     * Returns a string of the entire defined command names, joined together with spaces
     *
     * @param commandStep the command step to get the name of
     * @return the full command step name definition delimited by spaces or an empty string if there are no defined command names
     */
    private String getCommandStepName(CommandStep commandStep) {
        StringBuilder commandStepName = new StringBuilder();
        String[][] commandDefinition = commandStep.defineCommandNames();
        if (commandDefinition != null) {
            for (String[] commandNames : commandDefinition) {
                if (commandStepName.length() != 0) {
                    commandStepName.append(" ");
                }
                commandStepName.append(String.join(" ", commandNames));
            }
        }
        return commandStepName.toString();
    }

    /**
     * This class is a wrapper around OutputStreams, and makes them impossible for callers to close.
     */
    private static class UnclosableOutputStream extends FilterOutputStream {
        public UnclosableOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }

        /**
         * This method does not actually close the underlying stream, but rather only flushes it. Callers should not be
         * closing the stream they are given.
         */
        @Override
        public void close() throws IOException {
            out.flush();
        }
    }

    /**
     * Adapts the command-scoped arguments into the overall ValueProvider system
     */
    private class CommandScopeValueProvider extends AbstractMapConfigurationValueProvider {

        @Override
        public int getPrecedence() {
            return -1;
        }

        @Override
        protected Map<?, ?> getMap() {
            return CommandScope.this.argumentValues;
        }

        @Override
        protected String getSourceDescription() {
            return "Command argument";
        }

        @Override
        protected boolean keyMatches(String wantedKey, String storedKey) {
            if (wantedKey.contains(".")) {
                return super.keyMatches(NO_PREFIX_PATTERN.matcher(wantedKey).replaceFirst(""), storedKey);
            } else {
                return super.keyMatches(wantedKey, storedKey);
            }
        }

    }
}
