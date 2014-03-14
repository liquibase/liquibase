package liquibase.sdk.watch;

import liquibase.command.AbstractCommand;
import liquibase.command.CommandExecutionException;
import liquibase.command.CommandValidationErrors;
import liquibase.command.LiquibaseCommand;

public class WatchCommand extends AbstractCommand {

    @Override
    public String getName() {
        return "watch";
    }

    @Override
    public CommandValidationErrors validate() {
        return new CommandValidationErrors(this);
    }

    @Override
    protected Object run() throws Exception {
        return "Started";
    }

}
