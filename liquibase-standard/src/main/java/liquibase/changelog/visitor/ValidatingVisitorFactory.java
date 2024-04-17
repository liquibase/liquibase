package liquibase.changelog.visitor;

import liquibase.plugin.AbstractPluginFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
