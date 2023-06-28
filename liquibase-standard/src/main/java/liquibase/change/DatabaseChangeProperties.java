package liquibase.change;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container object which allows multiple {@link DatabaseChangeProperty} annotations to be used on a single property.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface DatabaseChangeProperties {
    DatabaseChangeProperty[] value();
}
