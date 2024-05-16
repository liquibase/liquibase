package liquibase.command.core;

import liquibase.Scope;
import liquibase.command.AbstractCommandStep;
import liquibase.command.CommandDefinition;
import liquibase.command.CommandResultsBuilder;

public class StopH2CommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"init", "stopH2"};

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        super.adjustCommandDefinition(commandDefinition);
        commandDefinition.setHidden(true);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        for (Thread runningThread : StartH2CommandStep.RUNNING_THREADS) {
            try {
                runningThread.interrupt();
            } catch (Exception e) {
                Scope.getCurrentScope().getLog(getClass()).warning("Error stopping H2 thread", e);
            }
        }
    }
}
