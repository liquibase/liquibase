package liquibase.command.core;

import liquibase.command.*;
import liquibase.command.core.helpers.DatabaseChangelogCommandStep;
import liquibase.database.Database;
import liquibase.util.LoggingExecutorTextUtil;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class RollbackCountSqlCommandStep extends RollbackCountCommandStep {

    public static final String[] COMMAND_NAME = {"rollbackCountSql"};

    public static final CommandArgumentDefinition<Boolean> OUTPUT_DEFAULT_SCHEMA_ARG;
    public static final CommandArgumentDefinition<Boolean> OUTPUT_DEFAULT_CATALOG_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        OUTPUT_DEFAULT_SCHEMA_ARG = builder.argument("outputDefaultSchema", Boolean.class)
                .description("Control whether names of objects in the default schema are fully qualified or not. If true they are. If false, only objects outside the default schema are fully qualified")
                .defaultValue(true)
                .build();
        OUTPUT_DEFAULT_CATALOG_ARG = builder.argument("outputDefaultCatalog", Boolean.class)
                .description("Control whether names of objects in the default catalog are fully qualified or not. If true they are. If false, only objects outside the default catalog are fully qualified")
                .defaultValue(true)
                .build();
        builder.addArgument(COUNT_ARG).build();
        builder.addArgument(AbstractRollbackCommandStep.ROLLBACK_SCRIPT_ARG).build();
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        List<Class<?>> dependencies = new ArrayList<>();
        dependencies.add(Writer.class);
        dependencies.addAll(super.requiredDependencies());
        return dependencies;
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        Integer changesToRollback = commandScope.getArgumentValue(COUNT_ARG);
        String changeLogFile = commandScope.getArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG);
        Database database = (Database) commandScope.getDependency(Database.class);
        LoggingExecutorTextUtil.outputHeader("Rollback " + changesToRollback + " Change(s) Script", database, changeLogFile);
        super.run(resultsBuilder);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Generate the SQL to rollback the specified number of changes");
    }
}
