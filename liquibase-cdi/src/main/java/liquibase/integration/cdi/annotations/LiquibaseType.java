package liquibase.integration.cdi.annotations;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * Qualifier Annotation
 *
 *  @author Aaron Walker (http://github.com/aaronwalker)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD,METHOD,PARAMETER,TYPE})
@Qualifier
public @interface LiquibaseType {
}
