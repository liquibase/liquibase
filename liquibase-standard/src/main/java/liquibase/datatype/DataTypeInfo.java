package liquibase.datatype;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface DataTypeInfo {
    String name();
    int minParameters();
    int maxParameters();
    String[] aliases() default {};

    String description() default "##default";
    int priority();

}
