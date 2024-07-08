package liquibase.integration.cdi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
  * @author <a href="https://github.com/dikeert">antoermo</a>
 * @since 31/07/2015
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface LiquibaseSchema {
	String name();
	String depends() default "";
	String[] resource();
}
