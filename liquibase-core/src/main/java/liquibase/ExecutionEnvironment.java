package liquibase;

import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import  liquibase.ExecutionEnvironment;

public class ExecutionEnvironment {

    private Database targetDatabase;
    private Contexts contexts;
    private LabelExpression labelExpression;
    private ChangeSet changeSet;

    public ExecutionEnvironment() {
    }

    public ExecutionEnvironment(Database targetDatabase) {
        this.targetDatabase = targetDatabase;
    }

    public Database getTargetDatabase() {
        return targetDatabase;
    }

    public ExecutionEnvironment setTargetDatabase(Database targetDatabase) {
        this.targetDatabase = targetDatabase;
        return this;
    }

    public Contexts getContexts() {
        return contexts;
    }

    public ExecutionEnvironment setContexts(Contexts contexts) {
        this.contexts = contexts;
        return this;
    }

    public LabelExpression getLabelExpression() {
        return labelExpression;
    }

    public ExecutionEnvironment setLabelExpression(LabelExpression labelExpression) {
        this.labelExpression = labelExpression;
        return this;
    }

    public ChangeSet getCurrentChangeSet() {
        return changeSet;
    }

    public ExecutionEnvironment setChangeSet(ChangeSet changeSet) {
        this.changeSet = changeSet;
        return this;
    }
}
