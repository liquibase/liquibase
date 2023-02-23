package liquibase.command.core;

import liquibase.command.*;
import liquibase.configuration.ConfigurationValueObfuscator;
import liquibase.database.Database;

import java.util.Collections;
import java.util.List;

public class ChangelogSyncCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"changelogSync"};

    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> LABEL_FILTER_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class).required()
            .description("The root changelog file").build();
        LABEL_FILTER_ARG = builder.argument("labelFilter", String.class)
                .addAlias("labels")
                .description("Label expression to use for filtering which changes to mark as executed").build();
        CONTEXTS_ARG = builder.argument("contexts", String.class)
            .description("Context string to use for filtering which changes to mark as executed").build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME};
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Marks all changes as executed in the database");
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Collections.singletonList(Database.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {

    }
}
