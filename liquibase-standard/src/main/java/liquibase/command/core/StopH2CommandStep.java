package liquibase.command.core;

import liquibase.Scope;
import liquibase.command.AbstractCommandStep;
import liquibase.command.CommandDefinition;
import liquibase.command.CommandResultsBuilder;

import java.util.Iterator;

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
        for (Iterator<Thread> iterator = StartH2CommandStep.RUNNING_THREADS.iterator(); iterator.hasNext(); ) {
            Thread runningThread = iterator.next();
            try {
                runningThread.interrupt();
                iterator.remove();
            } catch (Exception e) {
                Scope.getCurrentScope().getLog(getClass()).warning("Error stopping H2 thread", e);
            }
        }
    }
}
