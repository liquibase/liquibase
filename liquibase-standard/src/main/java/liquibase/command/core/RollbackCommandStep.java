package liquibase.command.core;

import liquibase.Scope;
import liquibase.TagVersionEnum;
import liquibase.changelog.RanChangeSet;
import liquibase.changelog.filter.AfterTagChangeSetFilter;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.logging.mdc.MdcKey;
import liquibase.report.RollbackReportParameters;
import liquibase.util.StringUtil;

import java.util.Arrays;
import java.util.Collections;
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
                .setValueHandler(TagVersionEnum::handleTagVersionInput)
                .defaultValue(TagVersionEnum.OLDEST.name())
                .build();

        builder.addArgument(AbstractRollbackCommandStep.ROLLBACK_SCRIPT_ARG).build();
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        RollbackReportParameters rollbackReportParameters = new RollbackReportParameters();
        rollbackReportParameters.setCommandTitle(
                StringUtil.upperCaseFirst(Arrays.toString(
                        defineCommandNames()[0]).replace("[","").replace("]","").replace("rollback", "rollback ").trim()));
        resultsBuilder.addResult("rollbackReport", rollbackReportParameters);
        CommandScope commandScope = resultsBuilder.getCommandScope();

        String tagToRollBackTo = commandScope.getArgumentValue(TAG_ARG);
        Scope.getCurrentScope().addMdcValue(MdcKey.ROLLBACK_TO_TAG, tagToRollBackTo);

        Database database = (Database) commandScope.getDependency(Database.class);
        rollbackReportParameters.getDatabaseInfo().setDatabaseType(database.getDatabaseProductName());
        rollbackReportParameters.getDatabaseInfo().setVersion(database.getDatabaseProductVersion());
        rollbackReportParameters.setJdbcUrl(database.getConnection().getURL());

        List<RanChangeSet> ranChangeSetList = database.getRanChangeSetList();
        TagVersionEnum tagVersion = TagVersionEnum.valueOf(commandScope.getArgumentValue(TAG_VERSION_ARG));
        this.doRollback(resultsBuilder, ranChangeSetList, new AfterTagChangeSetFilter(tagToRollBackTo, ranChangeSetList, tagVersion), rollbackReportParameters);
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
