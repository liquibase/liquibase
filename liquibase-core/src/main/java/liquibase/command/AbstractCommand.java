package liquibase.command;

import com.sun.deploy.util.StringUtils;
import liquibase.exception.CommandArgumentValidationException;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class AbstractCommand implements LiquibaseCommand {

    @Override
    public int getOrder(CommandScope commandScope) {
        final String[] thisCommandName = getName();

        if ((thisCommandName != null) && StringUtils.join(Arrays.asList(thisCommandName), " ").equalsIgnoreCase(StringUtils.join(Arrays.asList(commandScope.getCommand()), " "))) {
            return ORDER_DEFAULT;
        } else {
            return ORDER_NOT_APPLICABLE;
        }
    }

    @Override
    public void validate(CommandScope commandScope) throws CommandArgumentValidationException {
    }
}
