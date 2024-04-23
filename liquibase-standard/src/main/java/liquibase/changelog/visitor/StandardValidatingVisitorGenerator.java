package liquibase.changelog.visitor;

public class StandardValidatingVisitorGenerator implements ValidatingVisitorGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public ValidatingVisitor generateValidatingVisitor() {
        return new ValidatingVisitor();
    }
}
