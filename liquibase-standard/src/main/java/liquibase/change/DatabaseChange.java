package liquibase.change;

import java.lang.annotation.*;

/**
 * Annotation used by {@link AbstractChange } to declare {@link ChangeMetaData} information.
 * This annotation should not be checked for outside AbstractChange, if any code is trying to determine the
 * metadata provided by this annotation, it should get it from
 * {@link liquibase.change.ChangeFactory#getChangeMetaData(Change)}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface DatabaseChange {

    /**
     * Value to put into {@link liquibase.change.ChangeMetaData#getName()}
     */
    String name();

    /**
     * Value to put into {@link ChangeMetaData#getDescription()}
     */
    String description();

    /**
     * Value to put into {@link ChangeMetaData#getPriority()} ()}
     */
    int priority();

    /**
     * Value to put into {@link liquibase.change.ChangeMetaData#getAppliesTo()}
     */
    String[] appliesTo() default {};

    DatabaseChangeNote[] databaseNotes() default {};

    String since() default "";

}
