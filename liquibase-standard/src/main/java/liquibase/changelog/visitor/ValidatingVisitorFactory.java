package liquibase.changelog.visitor;

import liquibase.plugin.AbstractPluginFactory;

public class ValidatingVisitorFactory extends AbstractPluginFactory<ValidatingVisitor> {
    @Override
    protected Class<ValidatingVisitor> getPluginClass() {
        return ValidatingVisitor.class;
    }

    @Override
    protected int getPriority(ValidatingVisitor obj, Object... args) {
        return obj.getPriority();
    }

    public ValidatingVisitor getValidatingVisitor() {
        return getPlugin();
    }
}
