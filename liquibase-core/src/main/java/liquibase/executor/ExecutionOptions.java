package liquibase.executor;

import liquibase.RuntimeEnvironment;
import liquibase.action.visitor.ActionVisitor;

import java.util.List;

/**
 * Options that can affect SQL ran against a database.
 */
public class ExecutionOptions {
    private RuntimeEnvironment runtimeEnvironment;
    private List<ActionVisitor> actionVisitors;

    public ExecutionOptions(RuntimeEnvironment runtimeEnvironment) {
        this.runtimeEnvironment = runtimeEnvironment;
    }

    public ExecutionOptions(List<ActionVisitor> actionVisitors, RuntimeEnvironment runtimeEnvironment) {
        this.actionVisitors = actionVisitors;
        this.runtimeEnvironment = runtimeEnvironment;
    }

    public List<ActionVisitor> getActionVisitors() {
        return actionVisitors;
    }

    public RuntimeEnvironment getRuntimeEnvironment() {
        return runtimeEnvironment;
    }
}
