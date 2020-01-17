package liquibase.command;

import liquibase.exception.LiquibaseException;

import java.util.Map;

public abstract class AbstractSelfConfiguratingCommand<T extends CommandResult> extends AbstractCommand<T> {
    public void configure(Map<String, Object> argsMap) throws LiquibaseException {

    }
}
