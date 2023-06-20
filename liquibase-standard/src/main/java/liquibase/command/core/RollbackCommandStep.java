package liquibase.command.core;

import liquibase.Scope;
import liquibase.changelog.RanChangeSet;
import liquibase.changelog.filter.AfterTagChangeSetFilter;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.logging.mdc.MdcKey;

import java.util.List;

/**
 * RollbackCommandStep performs the rollback-to-tag logic. For backwards compatibility issues it is not called "RollbackToTag"
 */
public class RollbackCommandStep extends AbstractRollbackCommandStep {

    public static final String[] COMMAND_NAME = {"rollback"};

    public static final CommandArgumentDefinition<String> TAG_ARG;
    public static final CommandArgumentDefinition<String> TAG_VERSION_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        TAG_ARG = builder.argument("tag", String.class).required()
            .description("Tag to rollback to").build();

        TAG_VERSION_ARG = builder.argument("tagVersion",String.class)
                .description("Tag version to use for multiple occurrences of a tag")
                .setValueHandler((Object input) -> {
                    if (input == null) {
                        return null;
                    }

                    String tagVersion = String.valueOf(input);

                    boolean found = tagVersion.equalsIgnoreCase("oldest") || tagVersion.equalsIgnoreCase("newest");
                    if (!found) {
                        String messageString =
                                "\nWARNING:  The tag version value '" + tagVersion + "' is not valid.  Valid values include: 'OLDEST' or 'NEWEST'";
                        throw new IllegalArgumentException(messageString);
                    }
                    return tagVersion;
                })
                .defaultValue(TAG_VERSION.OLDEST.name())
                .build();

        builder.addArgument(AbstractRollbackCommandStep.ROLLBACK_SCRIPT_ARG).build();
    }

    public enum TAG_VERSION {
        NEWEST, OLDEST
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        String tagToRollBackTo = commandScope.getArgumentValue(TAG_ARG);
        Scope.getCurrentScope().addMdcValue(MdcKey.ROLLBACK_TO_TAG, tagToRollBackTo);

        Database database = (Database) commandScope.getDependency(Database.class);

        List<RanChangeSet> ranChangeSetList = database.getRanChangeSetList();
        TAG_VERSION tagVersion = TAG_VERSION.valueOf(commandScope.getArgumentValue(TAG_VERSION_ARG));
        this.doRollback(resultsBuilder, ranChangeSetList, new AfterTagChangeSetFilter(tagToRollBackTo, ranChangeSetList, tagVersion));
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Rollback changes made to the database based on the specific tag");
    }


}
