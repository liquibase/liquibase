package liquibase.command.core;

import liquibase.command.CommandArgument;
import liquibase.command.CommandResult;
import liquibase.command.CommandValidationErrors;
import liquibase.command.LiquibaseCommand;
import lombok.Getter;
import lombok.Setter;

import java.util.SortedSet;

@Setter
@Getter
public class MockLiquibaseCommand implements LiquibaseCommand {

    private String value1;

    @Override
    public String getName() {
        return "mock";
    }


    @Override
    public int getPriority(String commandName) {
        return PRIORITY_DEFAULT;
    }

    @Override
    public SortedSet<CommandArgument> getArguments() {
        return null;
    }

    @Override
    public CommandValidationErrors validate() {
        return null;
    }

    @Override
    public CommandResult run() throws Exception {
        return new CommandResult("Mock command ran with value1 = "+value1);
    }
}
