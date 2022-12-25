package liquibase.command;

import liquibase.Scope;
import liquibase.exception.CommandValidationException;
import liquibase.exception.LiquibaseException;
import liquibase.parser.ChangeLogParserConfiguration;
import liquibase.util.FileUtil;
import liquibase.util.StringUtil;

import java.util.Arrays;

/**
 * Convenience base class for {@link CommandStep} implementations.
 */
public abstract class AbstractCommandStep implements CommandStep {

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

    protected String returnEmptyResourceIfMissingFile(String sqlFile) throws LiquibaseException {
        if (ChangeLogParserConfiguration.WARN_OR_ERROR_ON_MISSING_CHANGELOGS.getCurrentValue()) {
            Scope.getCurrentScope().getLog(getClass()).warning(FileUtil.getFileNotFoundMessage(sqlFile));
            return FileUtil.EMPTY_FILE;
        } else {
            throw new LiquibaseException(FileUtil.getFileNotFoundMessage(sqlFile));
        }
    }
}
