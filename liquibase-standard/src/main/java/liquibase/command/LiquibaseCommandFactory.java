package liquibase.command;


import liquibase.plugin.AbstractPluginFactory;

/**
 * @deprecated Used for supporting old-style {@link LiquibaseCommand} implementations
 */
class LiquibaseCommandFactory extends AbstractPluginFactory<LiquibaseCommand> {

    protected LiquibaseCommandFactory() {
    }

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

    public <T extends CommandResult> T execute(LiquibaseCommand<T> command) throws CommandExecutionException {
        command.validate();
        try {
            return command.run();
        } catch (Exception e) {
            if (e instanceof CommandExecutionException) {
                throw (CommandExecutionException) e;
            } else {
                throw new CommandExecutionException(e);
            }
        }

    }


}
