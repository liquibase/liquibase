package liquibase.changelog.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for a class with  contains changeSets
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChangeLog {
    /**
     * Sequence that provide an order for changelog classes.
     * If not set, then canonical name of the class is taken and sorted alphabetically, ascending.
     * @return order
     */
    String order() default "";
}
