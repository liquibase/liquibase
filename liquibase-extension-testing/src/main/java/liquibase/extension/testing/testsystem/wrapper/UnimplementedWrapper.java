package liquibase.extension.testing.testsystem.wrapper;

import liquibase.exception.UnexpectedLiquibaseException;

public class UnimplementedWrapper extends DatabaseWrapper {
    @Override
    public void start(boolean keepRunning) throws Exception {
        throw new UnexpectedLiquibaseException("Unimplemented");
    }

    @Override
    public void stop() throws Exception {
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
