package liquibase.command.core;

import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.RanChangeSet;
import liquibase.command.*;
import liquibase.database.Database;

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HistoryCommand extends AbstractCommand {

    public static final CommandArgumentDefinition<Database> DATABASE_ARG;
    public static final CommandArgumentDefinition<DateFormat> DATE_FORMAT_ARG;
    public static final CommandArgumentDefinition<PrintStream> OUTPUT_ARG;

    static {
        final CommandArgumentDefinition.Builder builder = new CommandArgumentDefinition.Builder();
        DATABASE_ARG = builder.define("database", Database.class).required().build();
        DATE_FORMAT_ARG = builder.define("dateFormat", DateFormat.class).defaultValue(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)).build();
        OUTPUT_ARG = builder.define("output", PrintStream.class).defaultValue(System.out).build();
    }

    @Override
    public String[] getName() {
        return new String[]{"history"};
    }

    @Override
    public void run(CommandScope commandScope) throws Exception {
        Database database = DATABASE_ARG.getValue(commandScope);
        PrintStream outputStream = OUTPUT_ARG.getValue(commandScope);

        ChangeLogHistoryService historyService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);

        outputStream.println("Liquibase History for " + database.getConnection().getURL());
        outputStream.println("");

        DeploymentDetails deployment = null;
        for (RanChangeSet ranChangeSet : historyService.getRanChangeSets()) {
            final String thisDeploymentId = ranChangeSet.getDeploymentId();
            if (deployment == null || !Objects.equals(thisDeploymentId, deployment.getDeploymentId())) {
                if (deployment != null) {
                    deployment.printReport();
                }
                deployment = new DeploymentDetails(commandScope);
            }
            deployment.changeSets.add(ranChangeSet);
        }

        if (deployment == null) {
            outputStream.println("No changeSets deployed");
        } else {
            deployment.printReport();
        }
    }

    private class DeploymentDetails {
        private final CommandScope commandScope;
        List<RanChangeSet> changeSets = new ArrayList<>();

        public DeploymentDetails(CommandScope commandScope) {
            this.commandScope = commandScope;
        }

        void printReport() {
            DateFormat dateFormat = DATE_FORMAT_ARG.getValue(commandScope);
            PrintStream outputStream = OUTPUT_ARG.getValue(commandScope);

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

            outputStream.println(message);

            for (RanChangeSet changeSet : changeSets) {
                outputStream.println("  " + changeSet.toString());
            }

            outputStream.println("");
        }

        String getDeploymentId() {
            if (changeSets.size() == 0) {
                return null;
            }
            return changeSets.get(0).getDeploymentId();
        }
    }

}
