package liquibase.action.visitor;

import liquibase.action.Action;
import liquibase.executor.ExecutionOptions;

public interface ActionVisitor {

    public void visit(Action action, ExecutionOptions options);
}
