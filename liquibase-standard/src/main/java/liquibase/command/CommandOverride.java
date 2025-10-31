package liquibase.command;

import liquibase.Beta;
import liquibase.database.Database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to indicate that this CommandStep is intentionally overriding another CommandStep.
 * Multiple overrides are allowed - they will be filtered at runtime based on database support.
 * CommandStep with this annotation will execute only if the current database matches supportedDatabases,
 * or if supportedDatabases is empty (acts as default fallback).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Beta
public @interface CommandOverride {
    Class<? extends CommandStep> override();

    /**
     * Specifies which Database types this override supports.
     * If empty (default), this override acts as a "default" fallback for all databases.
     * At runtime, the override will only execute if the current database matches one of these types.
     *
     * @return array of Database classes this override supports
     */
    Class<? extends Database>[] supportedDatabases() default {};
}
