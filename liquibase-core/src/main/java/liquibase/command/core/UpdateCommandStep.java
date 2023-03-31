package liquibase.command.core;

import liquibase.Scope;
import liquibase.UpdateSummaryEnum;
import liquibase.command.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateCommandStep extends AbstractUpdateCommandStep implements CleanUpCommandStep {

    public static final String[] LEGACY_COMMAND_NAME = {"migrate"};
    public static String[] COMMAND_NAME = {"update"};

    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> LABEL_FILTER_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;

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
        return (UpdateSummaryEnum) commandScope.getDependency(UpdateSummaryEnum.class);
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

    @Override
    public void postUpdateLog() {
        Scope.getCurrentScope().getUI().sendMessage(coreBundle.getString("update.successful"));
    }

    @Override
    public String getHubOperation() {
        return "update";
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        List<Class<?>> deps = new ArrayList<>(super.requiredDependencies());
        deps.add(UpdateSummaryEnum.class);
        return deps;
    }
}
