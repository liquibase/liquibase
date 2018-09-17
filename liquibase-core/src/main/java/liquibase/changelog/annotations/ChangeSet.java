package liquibase.changelog.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Set of changes to be added to the DB. Many changesets are included in one changelog.
 *
 * @see ChangeLog
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ChangeSet {

    /**
     * Author of the changeset.
     * Obligatory
     *
     * @return author
     */
    String author();  // must be set

    /**
     * Unique ID of the changeset.
     * Obligatory
     *
     * @return unique id
     */
    String id();      // must be set

    /**
     * Sequence that provide correct order for changesets. Sorted alphabetically, ascending.
     * Obligatory.
     *
     * @return ordering
     */
    String order();   // must be set

    /**
     * Executes the change set on every liquibase execution, even if it has been run before.
     * Optional (default is false)
     *
     * @return should run always?
     */
    boolean runAlways() default false;


    /**
     * If set to true, the changeSet will be ignored (skipped)
     */
    String ignore() default "false";


    /**
     * If false, do not stop liquibase update execution if an error is thrown executing the changeSet.  Defaults to true
     */
    String failOnError() default "true";


    /**
     * ChangeSet comments defined in changeLog file
     */
    String comments() default "";

}
