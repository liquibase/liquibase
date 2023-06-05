package liquibase.command;

import liquibase.Beta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to indicate that this CommandStep is intentionally overriding another CommandStep.
 * A CommandStep may have AT MOST one override and usage of this annotation will verify
 * there are no other CommandSteps that also override the given CommandStep. CommandStep
 * with this annotation will completely replace execution of the overridden CommandStep
 * during pipeline execution.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Beta
public @interface CommandOverride {
    Class<? extends CommandStep> override();
}
