package liquibase.command.core;

import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.RanChangeSet;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcObject;
import liquibase.logging.mdc.customobjects.History;
import liquibase.util.TableOutput;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class InternalHistoryCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"internalHistory"};

    public static final CommandArgumentDefinition<Database> DATABASE_ARG;
    public static final CommandArgumentDefinition<DateFormat> DATE_FORMAT_ARG;
    public static final CommandArgumentDefinition<HistoryFormat> FORMAT_ARG;

    public static final CommandResultDefinition<DeploymentHistory> DEPLOYMENTS_RESULT;

    static {
        final CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        DATABASE_ARG = builder.argument("database", Database.class).required().build();
        DATE_FORMAT_ARG = builder.argument("dateFormat", DateFormat.class)
                .defaultValue(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT), "Platform specific 'short' format")
                .build();
        FORMAT_ARG = builder.argument("format", HistoryFormat.class)
                .description("History output format")
                .defaultValue(HistoryFormat.TABULAR)
                .build();

        DEPLOYMENTS_RESULT = builder.result("deployments", DeploymentHistory.class).build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        super.adjustCommandDefinition(commandDefinition);
        commandDefinition.setInternal(true);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        PrintWriter output = new PrintWriter(resultsBuilder.getOutputStream());

        CommandScope commandScope = resultsBuilder.getCommandScope();

        DeploymentHistory deploymentHistory = new DeploymentHistory();

        Database database = commandScope.getArgumentValue(DATABASE_ARG);

        ChangeLogHistoryService historyService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database);

        String headerMsg = "Liquibase History for " + database.getConnection().getURL();
        output.println(headerMsg);
        output.println("");

        ReportPrinter deployment = null;
        List<RanChangeSet> ranChangeSets = historyService.getRanChangeSets();
        List<History.Changeset> mdcChangesets = new ArrayList<>(ranChangeSets.size());
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            final String thisDeploymentId = ranChangeSet.getDeploymentId();
            if (deployment == null || !Objects.equals(thisDeploymentId, deployment.getDeploymentId())) {
                if (deployment != null) {
                    deployment.printReport(output);
                }
                deployment = DeploymentPrinterFactory.create(commandScope);
                deploymentHistory.deployments.add(deployment);
            }
            deployment.addChangeSet(ranChangeSet);
            mdcChangesets.add(new History.Changeset(ranChangeSet));
        }

        if (deployment == null) {
            output.println("No changesets deployed");
        } else {
            deployment.printReport(output);
        }

        try (MdcObject historyMdcObject = Scope.getCurrentScope().addMdcValue(MdcKey.HISTORY, new History(database.getConnection().getURL(), ranChangeSets.size(), mdcChangesets))) {
            Scope.getCurrentScope().getLog(getClass()).fine(headerMsg);
        }

        resultsBuilder.addResult(DEPLOYMENTS_RESULT, deploymentHistory);
        output.flush();
    }

    public static class DeploymentHistory {
        private final List<ReportPrinter> deployments = new ArrayList<>();

        @Override
        public String toString() {
            return deployments.size() + " past deployments";
        }
    }

    interface ReportPrinter {
        String getDeploymentId();

        void addChangeSet(RanChangeSet changeSet);

        void printReport(PrintWriter output) throws LiquibaseException;
    }

    static class DeploymentPrinterFactory {

        static ReportPrinter create(CommandScope scope) {
            switch (scope.getArgumentValue(FORMAT_ARG)) {
                case TABULAR:
                    return new TabularDeploymentDetails(scope);
                case TEXT:
                default:
                    return new LegacyDeploymentDetails(scope);
            }
        }
    }

    public static class LegacyDeploymentDetails implements ReportPrinter {
        private final CommandScope commandScope;
        List<RanChangeSet> changeSets = new ArrayList<>();

        public LegacyDeploymentDetails(CommandScope commandScope) {
            this.commandScope = commandScope;
        }

        @Override
        public void addChangeSet(RanChangeSet changeSet) {
            changeSets.add(changeSet);
        }

        @Override
        public void printReport(PrintWriter output) {
            DateFormat dateFormat = commandScope.getArgumentValue(DATE_FORMAT_ARG);
            if (dateFormat == null) {
                dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
            }

            String executionTime = null;
            RanChangeSet firstChangeSet = changeSets.get(0);
            if (changeSets.size() > 1) {
                RanChangeSet last = changeSets.get(changeSets.size() - 1);

                long executionMs = last.getDateExecuted().getTime() - firstChangeSet.getDateExecuted().getTime();
                executionTime = (executionMs / 1000F) + "s";
            }
            String message = "- Database updated at " + dateFormat.format(firstChangeSet.getDateExecuted()) + ". Applied " + changeSets.size() + " changeset(s)";

            if (executionTime != null) {
                message += " in " + executionTime;
            }

            message += ", DeploymentId: " + firstChangeSet.getDeploymentId();

            output.println(message);

            for (RanChangeSet changeSet : changeSets) {
                output.println("  " + changeSet.toString());
            }

            output.println("");
        }

        @Override
        public String getDeploymentId() {
            return changeSets.stream()
                    .findFirst()
                    .map(RanChangeSet::getDeploymentId)
                    .orElse(null);
        }
    }

    public static class TabularDeploymentDetails implements ReportPrinter {

        private static final List<String> HEADERS = Arrays.asList(
                "Deployment ID",
                "Update Date",
                "Changelog Path",
                "Changeset Author",
                "Changeset ID",
                "Tag");

        private final List<RanChangeSet> changeSets;
        private final CommandScope commandScope;

        public TabularDeploymentDetails(CommandScope commandScope) {
            this.commandScope = commandScope;
            this.changeSets = new ArrayList<>();
        }

        @Override
        public void addChangeSet(RanChangeSet changeSet) {
            changeSets.add(changeSet);
        }

        @Override
        public void printReport(PrintWriter output) throws LiquibaseException {
            DateFormat dateFormat = getDateFormat();
            List<List<String>> data = changeSets.stream()
                    .map(
                            changeSet -> Arrays.asList(
                                    changeSet.getDeploymentId(),
                                    dateFormat.format(changeSet.getDateExecuted()),
                                    changeSet.getChangeLog(),
                                    changeSet.getAuthor(),
                                    changeSet.getId(),
                                    changeSet.getTag() == null ? "" : changeSet.getTag()
                            )
                    )
                    .collect(Collectors.toList());
            data.add(0, HEADERS);
            TableOutput.formatUnwrappedOutput(data, true, output);
            output.println();
        }

        @Override
        public String getDeploymentId() {
            return changeSets.stream()
                    .findFirst()
                    .map(RanChangeSet::getDeploymentId)
                    .orElse(null);
        }

        private DateFormat getDateFormat() {
            DateFormat dateFormat = commandScope.getArgumentValue(DATE_FORMAT_ARG);
            if (dateFormat == null) {
                dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
            }
            return dateFormat;
        }
    }


}
