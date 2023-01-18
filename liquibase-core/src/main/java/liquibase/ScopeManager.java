package liquibase;

public abstract class ScopeManager {

    public abstract Scope getCurrentScope();

    protected abstract void setCurrentScope(Scope scope);

    /**
     * Modify or wrap a Scope to work with this Manager.
     *
     * @param scope that is supposed to be current so far.
     * @return a scope to use as the current one.
     * @throws Exception which won't stop us from using this Manager.
     */
    protected abstract Scope init(Scope scope) throws Exception;
}
