package liquibase.integration.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.util.AnnotationLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * CDI Bootstrap
 * <p/>
 * Observes CDI container startup events and triggers the Liquibase update
 * process via @PostConstruct on CDILiquibase
 *
 * @author Aaron Walker (http://github.com/aaronwalker)
 */
public class CDIBootstrap implements Extension {
    
    private Bean<CDILiquibase> instance;

    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        AnnotatedType<CDILiquibase> at = bm.createAnnotatedType(CDILiquibase.class);
        final InjectionTarget<CDILiquibase> it = bm.createInjectionTarget(at);
        instance = new Bean<CDILiquibase>() {

            public Set<Type> getTypes() {
                Set<Type> types = new HashSet<Type>();
                types.add(CDILiquibase.class);
                types.add(Object.class);
                return types;
            }

            public Set<Annotation> getQualifiers() {
                Set<Annotation> qualifiers = new HashSet<Annotation>();
                qualifiers.add( new AnnotationLiteral<Default>() {} );
                qualifiers.add( new AnnotationLiteral<Any>() {} );
                return qualifiers;
            }

            public Class<? extends Annotation> getScope() {
                return ApplicationScoped.class;
            }

            public String getName() {
                return "cdiLiquibase";
            }

            public Set<Class<? extends Annotation>> getStereotypes() {
                return Collections.emptySet();
            }

            public Class<?> getBeanClass() {
                return CDILiquibase.class;
            }

            public boolean isAlternative() {
                return false;
            }

            public boolean isNullable() {
                return false;
            }

            public Set<InjectionPoint> getInjectionPoints() {
                return it.getInjectionPoints();
            }

            public CDILiquibase create(CreationalContext<CDILiquibase> ctx) {
                CDILiquibase instance = it.produce(ctx);
                it.inject(instance, ctx);
                it.postConstruct(instance);
                return instance;
            }

            public void destroy(CDILiquibase instance, CreationalContext<CDILiquibase> ctx) {
                it.preDestroy(instance);
                it.dispose(instance);
                ctx.release();
            }
        };
        abd.addBean(instance);
    }

    void afterDeploymentValidation(@Observes AfterDeploymentValidation event, BeanManager manager) {
        try {
            manager.getReference(instance, instance.getBeanClass(), manager.createCreationalContext(instance)).toString();
        } catch (Exception ex) {
            event.addDeploymentProblem(ex);
        }
    }

}
