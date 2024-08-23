package liquibase.changelog.visitor;

import liquibase.changelog.RanChangeSet;

import java.util.List;

public class StandardValidatingVisitorGenerator implements ValidatingVisitorGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public ValidatingVisitor generateValidatingVisitor(List<RanChangeSet> ranChangeSetList) {
        return new ValidatingVisitor(ranChangeSetList);
    }
}
