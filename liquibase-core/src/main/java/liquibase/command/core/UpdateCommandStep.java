package liquibase.command.core;

import liquibase.*;
import liquibase.changelog.visitor.*;
import liquibase.command.*;

import java.util.*;

public class UpdateCommandStep extends AbstractUpdateCommandStep implements CleanUpCommandStep {

    public static final String[] LEGACY_COMMAND_NAME = {"migrate"};
    public static String[] COMMAND_NAME = {"update"};

    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> LABEL_FILTER_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;
    public static final CommandArgumentDefinition<String> CHANGE_EXEC_LISTENER_CLASS_ARG;
    public static final CommandArgumentDefinition<String> CHANGE_EXEC_LISTENER_PROPERTIES_FILE_ARG;
    public static final CommandArgumentDefinition<ChangeExecListener> CHANGE_EXEC_LISTENER_ARG;
    public static final CommandArgumentDefinition<UpdateSummaryEnum> SHOW_SUMMARY;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME, LEGACY_COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class)
                .required().description("The root changelog")
                .build();
        LABEL_FILTER_ARG = builder.argument("labelFilter", String.class)
                .addAlias("labels")
                .description("Changeset labels to match")
                .build();
        CONTEXTS_ARG = builder.argument("contexts", String.class)
                .description("Changeset contexts to match")
                .build();
        CHANGE_EXEC_LISTENER_CLASS_ARG = builder.argument("changeExecListenerClass", String.class)
                .description("Fully-qualified class which specifies a ChangeExecListener")
                .build();
        CHANGE_EXEC_LISTENER_PROPERTIES_FILE_ARG = builder.argument("changeExecListenerPropertiesFile", String.class)
                .description("Path to a properties file for the ChangeExecListenerClass")
                .build();
        CHANGE_EXEC_LISTENER_ARG = builder.argument("changeExecListener", ChangeExecListener.class)
                .hidden()
                .build();
        SHOW_SUMMARY = builder.argument("showSummary", UpdateSummaryEnum.class).description("Type of update results summary to show.  Values can be 'off', 'summary', or 'verbose'.")
                .defaultValue(UpdateSummaryEnum.OFF)
                .hidden()
                .setValueHandler(value -> {
                    if (value == null) {
                        return null;
                    }
                    if (value instanceof String && !value.equals("")) {
                        final List<String> validValues = Arrays.asList("OFF", "SUMMARY", "VERBOSE");
                        if (!validValues.contains(((String) value).toUpperCase())) {
                            throw new IllegalArgumentException("Illegal value for `showUpdateSummary'.  Valid values are 'OFF', 'SUMMARY', or 'VERBOSE'");
                        }
                        return UpdateSummaryEnum.valueOf(((String) value).toUpperCase());
                    } else if (value instanceof UpdateSummaryEnum) {
                        return (UpdateSummaryEnum) value;
                    }
                    return null;
                }).build();
    }

    @Override
    public String getChangelogFileArg(CommandScope commandScope) {
        return commandScope.getArgumentValue(CHANGELOG_FILE_ARG);
    }

    @Override
    public String getContextsArg(CommandScope commandScope) {
        return commandScope.getArgumentValue(CONTEXTS_ARG);
    }

    @Override
    public String getLabelFilterArg(CommandScope commandScope) {
        return commandScope.getArgumentValue(LABEL_FILTER_ARG);
    }

    @Override
    public String[] getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public UpdateSummaryEnum getShowSummary(CommandScope commandScope) {
        return commandScope.getArgumentValue(SHOW_SUMMARY);
    }

    @Override
    public String getChangeExecListenerClassArg(CommandScope commandScope) {
        return commandScope.getArgumentValue(CHANGE_EXEC_LISTENER_CLASS_ARG);
    }

    @Override
    protected String getChangeExecListenerPropertiesFileArg(CommandScope commandScope) {
        return commandScope.getArgumentValue(CHANGE_EXEC_LISTENER_PROPERTIES_FILE_ARG);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME, LEGACY_COMMAND_NAME};
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Deploy any changes in the changelog file that have not been deployed");

        if (commandDefinition.is(LEGACY_COMMAND_NAME)) {
            commandDefinition.setHidden(true);
        }

    }
}
