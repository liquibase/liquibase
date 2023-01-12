package liquibase.command;

import liquibase.Scope;
import liquibase.exception.CommandValidationException;
import liquibase.util.StringUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Convenience base class for {@link CommandStep} implementations.
 */
public abstract class AbstractCommandStep implements CommandStep {

    /**
     * Verifies if any step in the pipeline of the commandDefinition requires an argument of type
     * @param commandDefinition the master command definition
     * @param type the generic type of the argument, ie: CommandArgumentDefinition<Database> (in this case, Database.class)
     *
     * @return true in case it has the argument with that generic type
     */
    protected boolean isCommandDefinitionHasArgumentOfType(CommandDefinition commandDefinition, Class<?> type) {
        for (CommandStep step : commandDefinition.getPipeline()) {
            Field[] declaredFields = step.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) && CommandArgumentDefinition.class.isAssignableFrom(field.getType())) {
                    try {
                        CommandArgumentDefinition<?> argumentDefinition = (CommandArgumentDefinition<?>) field.get(null);
                        if (type.isAssignableFrom(argumentDefinition.getDataType())) {
                            return true;
                        }
                    } catch (IllegalAccessException e) {
                        Scope.getCurrentScope().getLog(getClass()).warning(String.format("Error accessing public field %s of step %s. Details: %s",
                                field.getName(), Arrays.deepToString(step.defineCommandNames()), e.getMessage()));
                        return false;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Collections.emptyList();
    }

    @Override
    public List<Class<?>> providedDependencies() {
        return Collections.emptyList();
    }

    /**
     * @return {@link #ORDER_DEFAULT} if the command scope's name matches {@link #defineCommandNames()}. Otherwise {@link #ORDER_NOT_APPLICABLE}
     */
    @Override
    public int getOrder(CommandDefinition commandDefinition) {
        final String[][] definedCommandNames = defineCommandNames();
        if (definedCommandNames != null) {
            for (String[] thisCommandName : definedCommandNames) {
                if ((thisCommandName != null) && StringUtil.join(Arrays.asList(thisCommandName), " ").equalsIgnoreCase(StringUtil.join(Arrays.asList(commandDefinition.getName()), " "))) {
                    return ORDER_DEFAULT;
                }
            }
        }
        return ORDER_NOT_APPLICABLE;
    }

    /**
     * Default implementation does no additional validation.
     */
    @Override
    public void validate(CommandScope commandScope) throws CommandValidationException {
    }

    /**
     * Default implementation makes no changes
     */
    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {

    }
}
