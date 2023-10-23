package liquibase.integration.jakarta.cdi;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.*;
import jakarta.enterprise.util.AnnotationLiteral;

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
 * @author Aaron Walker (http://github.com/aaronwalker), Jeroen Peschier (https://github.com/xazap)
 */
public class CDIBootstrap implements Extension {
    
    private Bean<CDILiquibase> instance;

    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        
        instance = new Bean<CDILiquibase>() {
        	final AnnotatedType<CDILiquibase> at = bm.createAnnotatedType(CDILiquibase.class);	
        	final InjectionTarget<CDILiquibase> it = bm.getInjectionTargetFactory(at).createInjectionTarget(this);
        	
            @Override
            public Set<Type> getTypes() {
                Set<Type> types = new HashSet<>();
                types.add(CDILiquibase.class);
                types.add(Object.class);
                return types;
            }

            @Override
            public Set<Annotation> getQualifiers() {
                Set<Annotation> qualifiers = new HashSet<>();
                qualifiers.add( new AnnotationLiteral<Default>() {
                    private static final long serialVersionUID = 6919382612875193843L;
                } );
                qualifiers.add( new AnnotationLiteral<Any>() {
                    private static final long serialVersionUID = 972067042069411460L;
                } );
                return qualifiers;
            }

            @Override
            public Class<? extends Annotation> getScope() {
                return ApplicationScoped.class;
            }

            @Override
            public String getName() {
                return "cdiLiquibase";
            }

            @Override
            public Set<Class<? extends Annotation>> getStereotypes() {
                return Collections.emptySet();
            }

            @Override
            public Class<?> getBeanClass() {
                return CDILiquibase.class;
            }

            @Override
            public boolean isAlternative() {
                return false;
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return it.getInjectionPoints();
            }

            @Override
            public CDILiquibase create(CreationalContext<CDILiquibase> ctx) {
                CDILiquibase instance = it.produce(ctx);
                it.inject(instance, ctx);
                it.postConstruct(instance);
                return instance;
            }

            @Override
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
