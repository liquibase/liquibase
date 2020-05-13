package liquibase.util.csv.opencsv.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for fields to mark if they are required or not.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CsvBind {
    /**
     * @return if the field is required to contain information.
     */
    boolean required() default false;
}
