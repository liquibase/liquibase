package liquibase.command.core.helpers;

import liquibase.UpdateSummaryEnum;
import liquibase.command.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class encapsulates the argument "show-summary", which applies to the update family of commands. The show-summary
 * feature will display a formatted summary table when performing an update command.
 * See {@link liquibase.util.ShowSummaryUtil} for implementation.
 */
public class ShowSummaryArgument extends AbstractCommandStep {
    protected static final String[] COMMAND_NAME = {"showSummary"};

    public static final CommandArgumentDefinition<UpdateSummaryEnum> SHOW_SUMMARY;

    static {
        CommandBuilder commandBuilder = new CommandBuilder(COMMAND_NAME);
        SHOW_SUMMARY = commandBuilder.argument("showSummary", UpdateSummaryEnum.class).description("Type of update results summary to show.  Values can be 'off', 'summary', or 'verbose'.")
                .defaultValue(UpdateSummaryEnum.SUMMARY)
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
    public String[][] defineCommandNames() {
        return new String[][] {COMMAND_NAME};
    }

    @Override
    public List<Class<?>> providedDependencies() {
        return Collections.singletonList(UpdateSummaryEnum.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        UpdateSummaryEnum argumentValue = commandScope.getArgumentValue(SHOW_SUMMARY);
        commandScope.provideDependency(UpdateSummaryEnum.class, argumentValue);
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        if (commandDefinition.getPipeline().size() == 1) {
            commandDefinition.setInternal(true);
        }
    }
}
