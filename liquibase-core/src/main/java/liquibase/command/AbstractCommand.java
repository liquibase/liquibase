package liquibase.command;

import com.sun.deploy.util.StringUtils;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class AbstractCommand implements LiquibaseCommand {

    @Override
    public int getOrder(CommandScope commandName) {
        if ((commandName != null) && StringUtils.join(Arrays.asList(commandName), " ").equalsIgnoreCase(StringUtils.join(Arrays.asList(getName()), " "))) {
            return PRIORITY_DEFAULT;
        } else {
            return PRIORITY_NOT_APPLICABLE;
        }
    }

    @Override
    public CommandValidationErrors validate() {
        return null;
    }

    @Override
    public SortedSet<CommandArgumentDefinition> getArguments() {
        return new TreeSet<>();
    }
}
