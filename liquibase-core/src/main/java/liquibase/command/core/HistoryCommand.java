package liquibase.command.core;

import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.RanChangeSet;
import liquibase.command.AbstractCommand;
import liquibase.command.CommandResult;
import liquibase.command.CommandValidationErrors;
import liquibase.database.Database;

import java.io.PrintStream;
import java.io.Writer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HistoryCommand extends AbstractCommand {

    private Database database;
    private DateFormat dateFormat;
    private PrintStream outputStream = System.out;

    public HistoryCommand() {
        dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    }

    @Override
    public String getName() {
        return "history";
    }

    @Override
    public CommandValidationErrors validate() {
        return new CommandValidationErrors(this);
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public PrintStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(PrintStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    protected CommandResult run() throws Exception {
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
                deployment = new DeploymentDetails();
            }
            deployment.changeSets.add(ranChangeSet);
        }

        if (deployment == null) {
            outputStream.println("No changeSets deployed");
        } else {
            deployment.printReport();
        }

        return new CommandResult("OK");
    }

    private class DeploymentDetails {
        List<RanChangeSet> changeSets = new ArrayList<>();

        void printReport() {
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
