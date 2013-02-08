package liquibase.change;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used by {@link AbstractChange } to declare {@link ChangeParameterMetaData} information.
 * This annotation should not be checked for outside AbstractChange, if any code is trying to determine the
 * metadata provided by this annotation, it should get it from {@link liquibase.change.Change#getChangeMetaData()}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface DatabaseChangeProperty {

    /**
     * If false, this field or method will not be included in {@ChangeParameterMetaData}
     */
    public boolean isChangeProperty() default true;

    /**
     * Value to put into {@link ChangeParameterMetaData#getRequiredForDatabase()}
     */
    public String[] requiredForDatabase() default "none";

    /**
     * Value to put into {@link liquibase.change.ChangeParameterMetaData#getMustEqualExisting()}
     */
    public String mustEqualExisting() default "";

    boolean isNestedProperty() default false;
}
