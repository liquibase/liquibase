package liquibase.changelog.visitor;

import liquibase.plugin.AbstractPluginFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;

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

    /**
     * Clear all cached instances of the validating visitor.
     */
    public void reset() {
        Collection<ValidatingVisitor> allInstances = this.findAllInstances();
        for (ValidatingVisitor instance : allInstances) {
            removeInstance(instance);
        }
    }
}
