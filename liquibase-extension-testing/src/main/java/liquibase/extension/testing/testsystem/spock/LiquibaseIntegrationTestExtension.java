package liquibase.extension.testing.testsystem.spock;

import org.spockframework.runtime.AbstractRunListener;
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.ErrorInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.util.ArrayList;
import java.util.List;

public class LiquibaseIntegrationTestExtension extends AbstractAnnotationDrivenExtension<LiquibaseIntegrationTest> {
    @Override
    public void visitSpecAnnotation(LiquibaseIntegrationTest annotation, SpecInfo spec) {
        final ErrorListener listener = new ErrorListener();

        LiquibaseIntegrationMethodInterceptor interceptor = new LiquibaseIntegrationMethodInterceptor(spec, listener);
        spec.addSetupSpecInterceptor(interceptor);
        spec.addCleanupSpecInterceptor(interceptor);
        spec.addSetupInterceptor(interceptor);
        spec.addCleanupInterceptor(interceptor);

        spec.addListener(listener);

    }

    public static class ErrorListener extends AbstractRunListener {

        private final List<ErrorInfo> errors = new ArrayList<>();

        @Override
        public void error(ErrorInfo error) {
            errors.add(error);
        }

        public List<ErrorInfo> getErrors() {
            return errors;
        }
    }
}
