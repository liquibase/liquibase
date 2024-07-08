package liquibase.command.core;

import liquibase.UpdateSummaryEnum;
import liquibase.command.*;
import liquibase.database.Database;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class UpdateToTagSqlCommandStep extends UpdateToTagCommandStep {

    public static final String[] COMMAND_NAME = {"updateToTagSql"};

    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> LABEL_FILTER_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;
    public static final CommandArgumentDefinition<String> TAG_ARG;
    public static final CommandArgumentDefinition<Boolean> OUTPUT_DEFAULT_SCHEMA_ARG;
    public static final CommandArgumentDefinition<Boolean> OUTPUT_DEFAULT_CATALOG_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class).required()
                .description("The root changelog").build();
        LABEL_FILTER_ARG = builder.argument("labelFilter", String.class)
                .addAlias("labels")
                .description("Changeset labels to match").build();
        CONTEXTS_ARG = builder.argument("contextFilter", String.class)
                .addAlias("contexts")
                .description("Changeset contexts to match").build();
        TAG_ARG = builder.argument("tag", String.class).required()
            .description("The tag to generate SQL up to").build();
        OUTPUT_DEFAULT_SCHEMA_ARG = builder.argument("outputDefaultSchema", Boolean.class)
                .description("Control whether names of objects in the default schema are fully qualified or not. If true they are. If false, only objects outside the default schema are fully qualified")
                .defaultValue(true)
                .build();
        OUTPUT_DEFAULT_CATALOG_ARG = builder.argument("outputDefaultCatalog", Boolean.class)
                .description("Control whether names of objects in the default catalog are fully qualified or not. If true they are. If false, only objects outside the default catalog are fully qualified")
                .defaultValue(true)
                .build();
    }

    @Override
    public UpdateSummaryEnum getShowSummary(CommandScope commandScope) {
        return UpdateSummaryEnum.OFF;
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Generate the SQL to deploy changes up to the tag");
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        ArrayList<Class<?>> dependencies = new ArrayList<>();
        // The order of these dependencies is important, because we want the writer to be executed before any of the
        // parent dependencies.
        dependencies.add(Writer.class);
        dependencies.add(Database.class);
        dependencies.addAll(super.requiredDependencies());
        dependencies.remove(UpdateSummaryEnum.class); // no update summary for this command, despite the class it is extending having an update summary option
        return dependencies;
    }
}
