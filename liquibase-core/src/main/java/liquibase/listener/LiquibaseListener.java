package liquibase.listener;

import liquibase.Scope;

/**
 * Base interface for all the different listeners available in liquibase.
 * There are no common methods defined, since each listener can be very different, but the common base class allows them all to be registered through {@link liquibase.Scope#child(LiquibaseListener, Scope.ScopedRunner)}.
 * To find all the listeners of a type, use {@link Scope#getListeners(Class)}.
 * <br><br>
 * Listener implementations should use a naming convention of "will" in methods that will be called before something happens such as {@link SqlListener#readSqlWillRun(String)} vs. methods in the past tense for something that already did happen.
 */
public interface LiquibaseListener {
}
