package liquibase.command.core;

import liquibase.command.CommandArgument;
import liquibase.command.CommandResult;
import liquibase.command.CommandValidationErrors;
import liquibase.command.LiquibaseCommand;
import lombok.Getter;

import java.util.SortedSet;

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

    public void setValue1(String value1) {
        this.value1 = value1;
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
