package liquibase;

import liquibase.database.Database;

import java.util.HashMap;

public class RuntimeEnvironment {
    public static final String MAIN_DB_KEY = "";
    private HashMap<String, Database> targetDatabases = new HashMap<String, Database>();
    private Contexts contexts;
    private final LabelExpression labels;

    /**
     * @deprecated use version with LabelExpression
     */
    public RuntimeEnvironment(Database targetDatabase, Contexts contexts) {
        this(targetDatabase, contexts, new LabelExpression());
    }

    public RuntimeEnvironment(Database targetDatabase, Contexts contexts, LabelExpression labelExpression) {
        this.targetDatabases.put(MAIN_DB_KEY, targetDatabase);
        this.contexts = contexts;
        this.labels = labelExpression;
    }

    public RuntimeEnvironment(HashMap<String, Database> targetDatabases, Contexts contexts, LabelExpression labelExpression) {
        this.targetDatabases = targetDatabases;
        this.contexts = contexts;
        this.labels = labelExpression;
    }

    public Database getTargetDatabase() {
        Database targetDatabase = targetDatabases.get(MAIN_DB_KEY);
        return targetDatabase;
    }

    public Database getTargetDatabase(String dbConnection) {
        Database targetDatabase = targetDatabases.get(dbConnection != null ? dbConnection : MAIN_DB_KEY);
        return targetDatabase;
    }

    public Contexts getContexts() {
        return contexts;
    }

    public LabelExpression getLabels() {
        return labels;
    }
}
