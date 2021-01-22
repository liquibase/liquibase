package liquibase.command;

import liquibase.plugin.AbstractPluginFactory;

/**
 * Manages {@link LiquibaseCommand} implementations.
 */
public class CommandFactory extends AbstractPluginFactory<LiquibaseCommand> {

    @Override
    protected Class<LiquibaseCommand> getPluginClass() {
        return LiquibaseCommand.class;
    }

    @Override
    protected int getPriority(LiquibaseCommand obj, Object... args) {
        return obj.getPriority((String) args[0]);
    }

    public LiquibaseCommand getCommand(String commandName) {
        return getPlugin(commandName);
    }
}
