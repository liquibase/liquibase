package liquibase.changelog.value;

import liquibase.database.Database;
import liquibase.statement.core.MarkChangeSetRanStatement;

import static liquibase.util.StringUtil.trimToEmpty;

abstract class LimittableValueProvider implements ChangeLogColumnValueProvider {

    private static final String ELLIPSIS = "...";
    private static final int MAX_COMMENT_LENGTH = 250;

    @Override
    public final Object getValue(MarkChangeSetRanStatement statement, Database database) {
        String extractedValue = extractValue(statement);
        return limitSize(trimToEmpty(extractedValue));
    }

    protected abstract String extractValue(MarkChangeSetRanStatement statement);

    private String limitSize(String string) {
        if (string.length() > MAX_COMMENT_LENGTH) {
            return string.substring(0, MAX_COMMENT_LENGTH - ELLIPSIS.length()) + ELLIPSIS;
        }
        return string;
    }
}
