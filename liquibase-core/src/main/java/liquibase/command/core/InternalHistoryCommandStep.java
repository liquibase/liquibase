package liquibase.command.core;

import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.RanChangeSet;
import liquibase.command.*;
import liquibase.database.Database;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InternalHistoryCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"internalHistory"};

    public static final CommandArgumentDefinition<Database> DATABASE_ARG;
    public static final CommandArgumentDefinition<DateFormat> DATE_FORMAT_ARG;

    public static final CommandResultDefinition<DeploymentHistory> DEPLOYMENTS_RESULT;

    static {
        final CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        DATABASE_ARG = builder.argument("database", Database.class).required().build();
        DATE_FORMAT_ARG = builder.argument("dateFormat", DateFormat.class)
                .defaultValue(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT), "Platform specific 'short' format")
                .build();

        DEPLOYMENTS_RESULT = builder.result("deployments", DeploymentHistory.class).build();
    }

    @Override
    public String[] getName() {
        return COMMAND_NAME;
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        PrintWriter output = new PrintWriter(resultsBuilder.getOutputStream());

        CommandScope commandScope = resultsBuilder.getCommandScope();

        DeploymentHistory deploymentHistory = new DeploymentHistory();

        Database database = commandScope.getArgumentValue(DATABASE_ARG);

        ChangeLogHistoryService historyService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);

        output.println("Liquibase History for " + database.getConnection().getURL());
        output.println("");

        DeploymentDetails deployment = null;
        for (RanChangeSet ranChangeSet : historyService.getRanChangeSets()) {
            final String thisDeploymentId = ranChangeSet.getDeploymentId();
            if (deployment == null || !Objects.equals(thisDeploymentId, deployment.getDeploymentId())) {
                if (deployment != null) {
                    deployment.printReport(output);
                }
                deployment = new DeploymentDetails(commandScope);
                deploymentHistory.deployments.add(deployment);
            }
            deployment.changeSets.add(ranChangeSet);
        }

        if (deployment == null) {
            output.println("No changeSets deployed");
        } else {
            deployment.printReport(output);
        }

        resultsBuilder.addResult(DEPLOYMENTS_RESULT, deploymentHistory);
        output.flush();
    }

    public static class DeploymentHistory {
        private List<DeploymentDetails> deployments = new ArrayList<>();

        public List<DeploymentDetails> getDeployments() {
            return deployments;
        }

        @Override
        public String toString() {
            return deployments.size() + " past deployments";
        }
    }

    public static class DeploymentDetails {
        private final CommandScope commandScope;
        List<RanChangeSet> changeSets = new ArrayList<>();

        public DeploymentDetails(CommandScope commandScope) {
            this.commandScope = commandScope;
        }

        void printReport(PrintWriter output) {
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
            String message = "- Database updated at " + dateFormat.format(firstChangeSet.getDateExecuted()) + ". Applied " + changeSets.size() + " changeSet(s)";

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

        String getDeploymentId() {
            if (changeSets.size() == 0) {
                return null;
            }
            return changeSets.get(0).getDeploymentId();
        }
    }

}
