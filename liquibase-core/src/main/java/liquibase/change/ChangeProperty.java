package liquibase.change;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface ChangeProperty {
    public boolean includeInSerialization() default true;
    public boolean includeInMetaData() default true;
}
