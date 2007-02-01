package liquibase.migrator;

public class DatabaseChangeLog {
    private Migrator migrator;


    public DatabaseChangeLog(Migrator migrator) {
        this.migrator = migrator;
    }

    public Migrator getMigrator() {
        return migrator;
    }
}
