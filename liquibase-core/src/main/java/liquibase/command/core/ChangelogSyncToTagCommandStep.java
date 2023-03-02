package liquibase.command.core;

import liquibase.command.*;

public class ChangelogSyncToTagCommandStep extends ChangelogSyncCommandStep {

    public static final String[] COMMAND_NAME = {"changelogSyncToTag"};

    public static final CommandArgumentDefinition<String> TAG_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        TAG_ARG = builder.argument("tag", String.class).required().description("Tag ID to execute changelogSync to").build();
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        final CommandScope commandScope = resultsBuilder.getCommandScope();
        setTag(commandScope.getArgumentValue(TAG_ARG));
        super.run(resultsBuilder);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Marks all undeployed changesets as executed, up to a tag");
    }
}
