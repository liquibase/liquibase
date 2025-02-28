package liquibase.changelog.visitor;

import liquibase.plugin.AbstractPluginFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidatingVisitorGeneratorFactory extends AbstractPluginFactory<ValidatingVisitorGenerator> {
    @Override
    protected Class<ValidatingVisitorGenerator> getPluginClass() {
        return ValidatingVisitorGenerator.class;
    }

    @Override
    protected int getPriority(ValidatingVisitorGenerator obj, Object... args) {
        return obj.getPriority();
    }

    public ValidatingVisitorGenerator getValidatingVisitorGenerator() {
        return getPlugin();
    }
}
