package liquibase;

public abstract class ScopeManager {


    public abstract Scope getCurrentScope();

    protected abstract void setCurrentScope(Scope scope);
}
