package liquibase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be added to fields in {@link AbstractExtensibleObject} to configure {@link liquibase.ObjectMetaData.Attribute} information about it.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExtensibleObjectAttribute {

    String description();
    boolean required() default false;

}
