package liquibase.command;

import liquibase.exception.CommandArgumentValidationException;
import liquibase.util.StringUtil;

import java.util.Arrays;

public abstract class AbstractCommand implements LiquibaseCommand {

    @Override
    public int getOrder(CommandScope commandScope) {
        final String[] thisCommandName = getName();

        if ((thisCommandName != null) && StringUtil.join(Arrays.asList(thisCommandName), " ").equalsIgnoreCase(StringUtil.join(Arrays.asList(commandScope.getCommand()), " "))) {
            return ORDER_DEFAULT;
        } else {
            return ORDER_NOT_APPLICABLE;
        }
    }

    @Override
    public void validate(CommandScope commandScope) throws CommandArgumentValidationException {
    }
}
