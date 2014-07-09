package liquibase;

import liquibase.changelog.ChangeSet;
import liquibase.database.Database;

public class ExecutionEnvironment extends AbstractExtensibleObject {

    private static String TARGET_DATABASE = "targetDatabase";
    private static String CONTEXTS = "contexts";
    private static String LABEL_EXPRESSION = "labelExpression";
    private static String CHANGE_SET = "changeSet";

    public ExecutionEnvironment() {
    }

    public ExecutionEnvironment(Database targetDatabase) {
        setTargetDatabase(targetDatabase);
    }

    public Database getTargetDatabase() {
        return getAttribute(TARGET_DATABASE, Database.class);
    }

    public ExecutionEnvironment setTargetDatabase(Database targetDatabase) {
        return (ExecutionEnvironment) setAttribute(TARGET_DATABASE, targetDatabase);
    }

    public Contexts getContexts() {
        return getAttribute(CONTEXTS, Contexts.class);
    }

    public ExecutionEnvironment setContexts(Contexts contexts) {
        return (ExecutionEnvironment) setAttribute(CONTEXTS, contexts);
    }

    public LabelExpression getLabelExpression() {
        return getAttribute(LABEL_EXPRESSION, LabelExpression.class);
    }

    public ExecutionEnvironment setLabelExpression(LabelExpression labelExpression) {
        return (ExecutionEnvironment) setAttribute(LABEL_EXPRESSION, labelExpression);
    }

    public ChangeSet getCurrentChangeSet() {
        return getAttribute(CHANGE_SET, ChangeSet.class);
    }

    public ExecutionEnvironment setChangeSet(ChangeSet changeSet) {
        return (ExecutionEnvironment) setAttribute(CHANGE_SET, changeSet);
    }
}
