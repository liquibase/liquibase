package liquibase.command;

import liquibase.exception.CommandValidationException;
import liquibase.util.StringUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

/**
 * Convenience base class for {@link CommandStep} implementations.
 */
public abstract class AbstractCommandStep implements CommandStep {

    protected static ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");

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
