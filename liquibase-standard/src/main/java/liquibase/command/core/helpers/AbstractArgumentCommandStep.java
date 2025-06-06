package liquibase.command.core.helpers;

import liquibase.command.AbstractCommandStep;

import java.util.List;

public abstract class AbstractArgumentCommandStep extends AbstractCommandStep {

    @Override
    public abstract List<Class<?>> providedDependencies();

    @Override
    public boolean isInternal() {
        return true;
    }
}
