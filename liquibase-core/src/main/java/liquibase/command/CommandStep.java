package liquibase.command;

import liquibase.exception.CommandValidationException;

/**
 * Defines a particular step in a command pipeline.
 * When a command is executed, Liquibase will find all the steps whose {@link #getName()} matches the command
 * and run them in {@link #getOrder(CommandDefinition)} order.
 *
 * @see CommandScope#execute()
 */
public interface CommandStep {

    /**
     * Returned by {@link #getOrder(CommandDefinition)} if you are unsure where in the pipeline your step should go, use this value.
     */
    int ORDER_DEFAULT = 1000;

    /**
     * Returned by {@link #getOrder(CommandDefinition)} if this step should not be a part of the pipeline.
     */
    int ORDER_NOT_APPLICABLE = -1;

    /**
     * The command name + category names this step should be a part of.
     * For example, if it is part of `liquibase update`, this should return {"update"}.
     * If it is a part of `liquibase example init`, this should return {"example", "init"}.
     * <p>
     * This is used to determine the available command names.
     * <p>
     * This can return null if this step is not defining a new command but "cross-cutting" existing commands.
     */
    String[] getName();

    /**
     * The order in the pipeline that this step should be executed in.
     * Logic is generally based off {@link CommandDefinition#getName()} but it can check other things in the definition such as arguments.
     *
     * @return -1 if this step does not apply to the given command
     */
    int getOrder(CommandDefinition commandDefinition);

    /**
     * Called by the command pipeline setup to adjust the {@link CommandDefinition} metadata about the overall command.
     * <b>This method should not be called directly. It is called by the overall pipeline logic in the {@link #getOrder(CommandDefinition)} order.</b>
     */
    void adjustCommandDefinition(CommandDefinition commandDefinition);

    /**
     * Validates that the {@link CommandScope} is correctly set up for this step to run.
     * Any validation in {@link CommandArgumentDefinition#validate(CommandScope)} will be checked previous to this method being called.
     * <b>This method should not be called directly. It is called by the overall pipeline logic in the {@link #getOrder(CommandDefinition)} order.</b>
     */
    void validate(CommandScope commandScope) throws CommandValidationException;

    /**
     * Performs the business logic.
     * <b>This method should not be called directly. It is called by the overall pipeline logic in the {@link #getOrder(CommandDefinition)} order.</b>
     */
    void run(CommandResultsBuilder resultsBuilder) throws Exception;
}
