package liquibase.changelog.value;

import liquibase.statement.core.MarkChangeSetRanStatement;

public class CommentsProvider extends LimittableValueProvider {

    @Override
    protected String extractValue(MarkChangeSetRanStatement statement) {
        return statement.getChangeSet()
                .getComments();
    }
}
