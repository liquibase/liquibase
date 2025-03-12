package liquibase.command.core;

import liquibase.command.*;

public class FutureRollbackCountSqlCommandStep extends AbstractFutureRollbackCommandStep {

    public static final String[] COMMAND_NAME = {"futureRollbackCountSql"};

    public static final CommandArgumentDefinition<Integer> COUNT_ARG;
    public static final CommandArgumentDefinition<Boolean> OUTPUT_DEFAULT_SCHEMA_ARG;
    public static final CommandArgumentDefinition<Boolean> OUTPUT_DEFAULT_CATALOG_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        COUNT_ARG = builder.argument("count", Integer.class).required()
                .description("Number of changesets to generate rollback SQL for").build();
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
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Generates SQL to sequentially revert <count> number of changes");
    }

    @Override
    public Integer getCount(CommandScope commandScope) {
        return commandScope.getArgumentValue(COUNT_ARG);
    }
}
