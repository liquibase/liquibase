package liquibase;

public class SingletonScopeManager extends ScopeManager {

    private Scope currentScope;

    @Override
    public synchronized Scope getCurrentScope() {
        return currentScope;
    }

    @Override
    protected Scope init(Scope scope) throws Exception {
        return scope;
    }

    @Override
    protected synchronized void setCurrentScope(Scope scope) {
        this.currentScope = scope;
    }
}
