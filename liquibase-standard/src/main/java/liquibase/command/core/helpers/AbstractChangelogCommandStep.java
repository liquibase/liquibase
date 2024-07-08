package liquibase.command.core.helpers;

import liquibase.Scope;
import liquibase.change.ChangeFactory;
import liquibase.change.ReplaceIfExists;
import liquibase.command.AbstractCommandStep;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandBuilder;
import liquibase.command.CommandScope;
import liquibase.exception.CommandValidationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractChangelogCommandStep extends AbstractCommandStep {

    protected static final String[] COMMAND_NAME = {"abstractChangelogCommandStep"};
    public static final CommandArgumentDefinition<String> RUN_ON_CHANGE_TYPES_ARG;
    public static final CommandArgumentDefinition<String> REPLACE_IF_EXISTS_TYPES_ARG;
    public static final CommandArgumentDefinition<Boolean> SKIP_OBJECT_SORTING;

    static {
        final CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        RUN_ON_CHANGE_TYPES_ARG = builder.argument("runOnChangeTypes", String.class)
                .defaultValue("none").description("Sets runOnChange=\"true\" for changesets containing solely changes of these types (e. g. createView, createProcedure, ...).").build();
        final String replaceIfExistsTypeNames = supportedReplaceIfExistsTypes().collect(Collectors.joining(", "));
        REPLACE_IF_EXISTS_TYPES_ARG = builder.argument("replaceIfExistsTypes", String.class)
                .defaultValue("none")
                .description(String.format("Sets replaceIfExists=\"true\" for changes of these types (supported types: %s)", replaceIfExistsTypeNames)).build();
        SKIP_OBJECT_SORTING = builder.argument("skipObjectSorting", Boolean.class)
                .defaultValue(false)
                .description("When true will skip object sorting. This can be useful on databases that have a lot of packages/procedures that are " +
                        "linked to each other").build();
    }

    protected static void validateRunOnChangeTypes(final CommandScope commandScope) throws CommandValidationException {
        final Collection<String> runOnChangeTypes = new ArrayList(Arrays.asList(commandScope.getArgumentValue(RUN_ON_CHANGE_TYPES_ARG).split("\\s*,\\s*")));
        final Collection<String> supportedRunOnChangeTypes = supportedRunOnChangeTypes().collect(Collectors.toList());
        supportedRunOnChangeTypes.add("none");
        runOnChangeTypes.removeAll(supportedRunOnChangeTypes);
        if (!runOnChangeTypes.isEmpty())
            throw new CommandValidationException("Invalid types for --run-on-change-types: " + runOnChangeTypes.stream().collect(Collectors.joining(", ")));
    }

    protected static void validateReplaceIfExistsTypes(final CommandScope commandScope) throws CommandValidationException {
        final Collection<String> replaceIfExistsTypes = new ArrayList(Arrays.asList(commandScope.getArgumentValue(REPLACE_IF_EXISTS_TYPES_ARG).split("\\s*,\\s*")));
        final Collection<String> supportedReplaceIfExistsTypes = supportedReplaceIfExistsTypes().collect(Collectors.toList());
        supportedReplaceIfExistsTypes.add("none");
        replaceIfExistsTypes.removeAll(supportedReplaceIfExistsTypes);
        if (!replaceIfExistsTypes.isEmpty())
            throw new CommandValidationException("Invalid types for --replace-if-exists-types: " + replaceIfExistsTypes.stream().collect(Collectors.joining(", ")));
    }

    protected static Stream<String> supportedRunOnChangeTypes() {
        final ChangeFactory changeFactory = Scope.getCurrentScope().getSingleton(ChangeFactory.class);
        return changeFactory.getDefinedChanges().stream();
    }

    protected static Stream<String> supportedReplaceIfExistsTypes() {
        final ChangeFactory changeFactory = Scope.getCurrentScope().getSingleton(ChangeFactory.class);
        return changeFactory.getDefinedChanges().stream().filter(changeType -> changeFactory.create(changeType) instanceof ReplaceIfExists);
    }
}
