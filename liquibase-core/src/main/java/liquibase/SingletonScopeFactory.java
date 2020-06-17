package liquibase;

public class SingletonScopeFactory extends ScopeManager {

    private Scope currentScope = new Scope();

    @Override
    public synchronized Scope getCurrentScope() {
        return currentScope;
    }

    @Override
    protected synchronized void setCurrentScope(Scope scope) {
        this.currentScope = scope;

        if (this.currentScope == null) {
            this.currentScope = new Scope();
        }
    }
}
