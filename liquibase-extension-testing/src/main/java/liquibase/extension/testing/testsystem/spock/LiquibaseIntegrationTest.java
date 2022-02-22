package liquibase.extension.testing.testsystem.spock;

import org.spockframework.runtime.extension.ExtensionAnnotation;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtensionAnnotation(LiquibaseIntegrationTestExtension.class)
public @interface LiquibaseIntegrationTest {
}
