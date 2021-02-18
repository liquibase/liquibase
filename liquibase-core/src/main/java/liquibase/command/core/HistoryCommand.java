package liquibase.command.core;

import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.RanChangeSet;
import liquibase.command.AbstractCommand;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandScope;
import liquibase.database.Database;
import liquibase.ui.UIService;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HistoryCommand extends AbstractCommand {

    public static final CommandArgumentDefinition<Database> DATABASE_ARG;
    public static final CommandArgumentDefinition<DateFormat> DATE_FORMAT_ARG;

    static {
        final CommandArgumentDefinition.Builder builder = new CommandArgumentDefinition.Builder(HistoryCommand.class);
        DATABASE_ARG = builder.define("database", Database.class).required().build();
        DATE_FORMAT_ARG = builder.define("dateFormat", DateFormat.class).defaultValue(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)).build();
    }

    @Override
    public String[] getName() {
        return new String[]{"history"};
    }

    @Override
    public void run(CommandScope commandScope) throws Exception {
        Database database = DATABASE_ARG.getValue(commandScope);

        ChangeLogHistoryService historyService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);

        commandScope.getOutput().println("Liquibase History for " + database.getConnection().getURL());
        commandScope.getOutput().println("");

        DeploymentDetails deployment = null;
        for (RanChangeSet ranChangeSet : historyService.getRanChangeSets()) {
            final String thisDeploymentId = ranChangeSet.getDeploymentId();
            if (deployment == null || !Objects.equals(thisDeploymentId, deployment.getDeploymentId())) {
                if (deployment != null) {
                    deployment.printReport(commandScope.getOutput());
                }
                deployment = new DeploymentDetails(commandScope);
            }
            deployment.changeSets.add(ranChangeSet);
        }

        if (deployment == null) {
            commandScope.getOutput().println("No changeSets deployed");
        } else {
            deployment.printReport(commandScope.getOutput());
        }
    }

    private class DeploymentDetails {
        private final CommandScope commandScope;
        List<RanChangeSet> changeSets = new ArrayList<>();

        public DeploymentDetails(CommandScope commandScope) {
            this.commandScope = commandScope;
        }

        void printReport(PrintWriter output) {
            DateFormat dateFormat = DATE_FORMAT_ARG.getValue(commandScope);

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
