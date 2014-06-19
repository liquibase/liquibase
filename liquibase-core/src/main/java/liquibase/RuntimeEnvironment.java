package liquibase;

import liquibase.database.Database;

public class RuntimeEnvironment {
    private Database targetDatabase;
    private Contexts contexts;

    public RuntimeEnvironment(Database targetDatabase) {
        this.targetDatabase = targetDatabase;
    }

    public RuntimeEnvironment(Database targetDatabase, Contexts contexts) {
        this.targetDatabase = targetDatabase;
        this.contexts = contexts;
    }

    public Database getTargetDatabase() {
        return targetDatabase;
    }

    public Contexts getContexts() {
        return contexts;
    }
}
