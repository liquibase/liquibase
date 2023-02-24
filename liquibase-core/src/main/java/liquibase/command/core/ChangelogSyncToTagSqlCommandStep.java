package liquibase.command.core;

import liquibase.command.*;

public class ChangelogSyncToTagSqlCommandStep extends ChangelogSyncSqlCommandStep {

    public static final String[] COMMAND_NAME = {"changelogSyncToTagSql"};

    public static final CommandArgumentDefinition<String> TAG_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);

        TAG_ARG = builder.argument("tag", String.class).required()
                .description("Tag ID to execute changelogSync to").build();

        builder.addArgument(OUTPUT_DEFAULT_SCHEMA_ARG).build();
        builder.addArgument(OUTPUT_DEFAULT_CATALOG_ARG).build();
        builder.addArgument(CHANGELOG_FILE_ARG).build();
        builder.addArgument(LABEL_FILTER_ARG).build();
        builder.addArgument(CONTEXTS_ARG).build();
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
        commandDefinition.setShortDescription("Output the raw SQL used by Liquibase when running changelogSyncToTag");
    }
}
