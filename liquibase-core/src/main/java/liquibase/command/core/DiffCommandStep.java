package liquibase.command.core;

import liquibase.change.CheckSum;
import liquibase.command.*;
import liquibase.command.providers.ReferenceDatabase;
import liquibase.database.Database;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;

import java.util.Arrays;
import java.util.List;

public class DiffCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"diff"};
    public static final CommandArgumentDefinition<String> EXCLUDE_OBJECTS_ARG;
    public static final CommandArgumentDefinition<String> INCLUDE_OBJECTS_ARG;
    public static final CommandArgumentDefinition<String> SCHEMAS_ARG;
    public static final CommandArgumentDefinition<String> DIFF_TYPES_ARG;

    public static final CommandResultDefinition<CheckSum> DIFF_RESULT;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        EXCLUDE_OBJECTS_ARG = builder.argument("excludeObjects", String.class)
                .description("Objects to exclude from diff").build();
        INCLUDE_OBJECTS_ARG = builder.argument("includeObjects", String.class)
                .description("Objects to include in diff").build();
        SCHEMAS_ARG = builder.argument("schemas", String.class)
                .description("Schemas to include in diff").build();
        DIFF_TYPES_ARG = builder.argument("diffTypes", String.class)
                .description("Types of objects to compare").build();


        DIFF_RESULT = builder.result("diffResult", CheckSum.class).description("Databases diff result").build();
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class, ReferenceDatabase.class);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        Database targetDatabase = (Database) commandScope.getDependency(Database.class);
        Database referenceDatabase = (Database) commandScope.getDependency(ReferenceDatabase.class);
        sendResults(DiffGeneratorFactory.getInstance().compare(referenceDatabase, targetDatabase, compareControl));
    }

    private void sendResults(DiffResult result) {


    }

//    @Override
//    protected String[] collectArguments(CommandScope commandScope) throws CommandExecutionException {
//        return collectArguments(commandScope, Arrays.asList("format", EXCLUDE_OBJECTS_ARG.getName(), INCLUDE_OBJECTS_ARG.getName()), null);
//    }


    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Compare two databases");
    }
}
