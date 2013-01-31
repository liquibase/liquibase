package liquibase.change;

import liquibase.structure.DatabaseObject;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface DatabaseChange {
    String name();
    String description();
    int priority();
    String[] appliesTo() default {};

}
