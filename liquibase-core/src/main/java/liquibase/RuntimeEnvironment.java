package liquibase;

import liquibase.database.Database;

public class RuntimeEnvironment {
    private Database targetDatabase;
    private Contexts contexts;
    private LabelExpression labels;

    public RuntimeEnvironment(Database targetDatabase) {
        this.targetDatabase = targetDatabase;
    }

    public RuntimeEnvironment(Database targetDatabase, Contexts contexts, LabelExpression labelExpression) {
        this.targetDatabase = targetDatabase;
        this.contexts = contexts;
        this.labels = labelExpression;
    }

    public Database getTargetDatabase() {
        return targetDatabase;
    }

    public Contexts getContexts() {
        return contexts;
    }

    public LabelExpression getLabels() {
        return labels;
    }
}
