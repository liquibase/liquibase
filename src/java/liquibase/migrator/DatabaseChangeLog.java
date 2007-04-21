package liquibase.migrator;

import liquibase.migrator.preconditions.PreconditionSet;

public class DatabaseChangeLog {
    private Migrator migrator;
    private PreconditionSet preconditions;

    public DatabaseChangeLog(Migrator migrator) {
        this.migrator = migrator;
    }

    public Migrator getMigrator() {
        return migrator;
    }

    public PreconditionSet getPreconditions() {
        return preconditions;
    }

    public void setPreconditions(PreconditionSet precond) {
        preconditions = precond;
    }
}
