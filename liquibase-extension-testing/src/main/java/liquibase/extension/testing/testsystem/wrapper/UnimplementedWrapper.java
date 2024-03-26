package liquibase.extension.testing.testsystem.wrapper;

import liquibase.exception.UnexpectedLiquibaseException;

/**
 * Wrapper for databases that are not yet implemented.
 *
 * @deprecated will remove when all TestSystems are created.
 */
@Deprecated
public class UnimplementedWrapper extends DatabaseWrapper {
    @Override
    public void start() throws Exception {
        throw new UnexpectedLiquibaseException("Unimplemented");
    }

    @Override
    public void stop() throws Exception {
        throw new UnexpectedLiquibaseException("Unimplemented");

    }

    @Override
    public String describe() {
        throw new UnexpectedLiquibaseException("Unimplemented");
    }

    @Override
    public String getUsername() {
        throw new UnexpectedLiquibaseException("Unimplemented");
    }

    @Override
    public String getUrl() {
        throw new UnexpectedLiquibaseException("Unimplemented");
    }
}
