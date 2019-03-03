package liquibase.changelog.value;

import liquibase.ContextExpression;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.statement.core.MarkChangeSetRanStatement;

public class ContextsProvider implements ChangeLogColumnValueProvider {

    private static final String SPACE = " ";
    private static final String COMMA = ",";
    private static final String CLOSE_BRACKET = ")";
    private static final String OPEN_BRACKET = "(";
    private static final String AND = " AND ";

    @Override
    public Object getValue(MarkChangeSetRanStatement statement, Database database) throws LiquibaseException {
        ChangeSet changeSet = statement.getChangeSet();
        return ((changeSet.getContexts() == null) || changeSet.getContexts().isEmpty()) ? null : buildFullContext(changeSet);
    }

    private String buildFullContext(ChangeSet changeSet) {
        StringBuilder contextExpression = new StringBuilder();
        boolean notFirstContext = false;
        for (ContextExpression inheritableContext : changeSet.getInheritableContexts()) {
            appendContext(contextExpression, inheritableContext.toString(), notFirstContext);
            notFirstContext = true;
        }
        ContextExpression changeSetContext = changeSet.getContexts();
        if ((changeSetContext != null) && !changeSetContext.isEmpty()) {
            appendContext(contextExpression, changeSetContext.toString(), notFirstContext);
        }
        return contextExpression.toString();
    }

    private void appendContext(StringBuilder contextExpression, String contextToAppend, boolean notFirstContext) {
        boolean complexExpression = contextToAppend.contains(COMMA) || contextToAppend.contains(SPACE);
        if (notFirstContext) {
            contextExpression.append(AND);
        }
        if (complexExpression) {
            contextExpression.append(OPEN_BRACKET);
        }
        contextExpression.append(contextToAppend);
        if (complexExpression) {
            contextExpression.append(CLOSE_BRACKET);
        }
    }
}
