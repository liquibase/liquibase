package org.liquibase.maven.property;

import java.lang.annotation.*;

/**
 * Annotation used at the field level that indicates that field is liquibase property element
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PropertyElement {

    /**
     * Specify key if field name is not corresponded to liquibase property key name
     */
    String key() default "";
}
