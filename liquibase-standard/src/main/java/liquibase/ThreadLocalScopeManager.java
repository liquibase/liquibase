package liquibase;

import java.util.Optional;

/**
 * An alternative to {@link SingletonScopeManager} which manages a separate Scope per thread.<br><br>
 * Integrations that would prefer to use this scope manager can call <pre>Scope.setScopeManager(new ThreadLocalScopeManager())</pre>.
 * <br><br>
 * The value of Scope.getCurrentScope() at the time of the ThreadLocalScopeManger's creation will be the basis of all scopes created after setScopeManager() is changed,
 * so you will generally want to setScopeManager as soon as possible.
 */
@SuppressWarnings("java:S5164")
public class ThreadLocalScopeManager extends ScopeManager {

    private final Scope rootScope;
    private final ThreadLocal<Scope> threadLocalScopes = new ThreadLocal<>();

    public ThreadLocalScopeManager() {
        this(Scope.getCurrentScope());
    }

    public ThreadLocalScopeManager(Scope rootScope) {
        this.rootScope = rootScope;
    }

    @Override
    public Scope getCurrentScope() {
        return Optional.ofNullable(threadLocalScopes.get()).orElse(rootScope);
    }

    @Override
    protected void setCurrentScope(Scope scope) {
        if (scope == rootScope) {
            threadLocalScopes.remove();
        } else {
            threadLocalScopes.set(scope);
        }
    }
    @Override
    protected Scope init(Scope scope) throws Exception {
        return rootScope;
    }


}
