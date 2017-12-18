package liquibase.integration.cdi;

import liquibase.integration.cdi.annotations.Liquibase;
import liquibase.integration.cdi.annotations.LiquibaseSchema;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Nikita Lipatov (https://github.com/islonik),
 * @since 27/5/17.
 */
public class SchemesCDIConfigBuilderTest {

    private static final Long FILE_LOCK_TIMEOUT = 5L;
    private static final String BEFORE_KEY = "Before";
    private static final String AFTER_KEY = "After";
    private static AtomicLong COUNTER;
    private static Logger log;

    private SchemesCDIConfigBuilder schemesCDIConfigBuilder;

    private BeanManager bm;
    private SchemesTreeBuilder treeBuilder;

    /**
     * Suppress annoying log messages
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        log = LogService.getLog(SchemesCDIConfigBuilder.class);
//        log.setLogLevel(LogLevel.WARNING); // you can change it to INFO or DEBUG level if you want to see them

        Class c1 = SchemesCDIConfigBuilder.class;
        final Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);

        final Field field1 = c1.getDeclaredField("FILE_LOCK_TIMEOUT");
        field1.setAccessible(true);
        modifiersField.setInt(field1, field1.getModifiers() & ~Modifier.FINAL);
        field1.set(null, FILE_LOCK_TIMEOUT);

        final Field field2 = c1.getDeclaredField("ROOT_PATH");
        field2.setAccessible(true);
        modifiersField.setInt(field2, field2.getModifiers() & ~Modifier.FINAL);

        field2.set(null, getRootPath());
    }

    private static String getRootPath() {
        String rootPath = "target/tempTestDir";
        File tempRootDir = new File(rootPath);
        tempRootDir.mkdirs();
        return tempRootDir.getAbsolutePath();
    }

    @Before
    public void setUp() {
        COUNTER = new AtomicLong(0L);

        bm = mock(BeanManager.class);
        treeBuilder = new SchemesTreeBuilder();
        schemesCDIConfigBuilder = new SchemesCDIConfigBuilder(bm, treeBuilder);
    }

    /**
     * General execution.
     */
    @Test
    public void testCreateCDILiquibaseConfig() throws Exception {
        Set<Bean<?>> beans = new LinkedHashSet<Bean<?>>();
        beans.add(mockBean(new A1()));
        beans.add(mockBean(new B2()));

        when(bm.getBeans(eq(Object.class), eq(new SchemesCDIConfigBuilder.AnnotationLiteralDefault()))).thenReturn(beans);

        CDILiquibaseConfig config = schemesCDIConfigBuilder.createCDILiquibaseConfig();

        Assert.assertNotNull(config);
        Assert.assertEquals("liquibase.cdi.schema.xml", config.getChangeLog());
    }

    private Bean mockBean(final Object object) {
        return new Bean() {
            @Override
            public Set<Type> getTypes() {
                return null;
            }

            @Override
            public Set<Annotation> getQualifiers() {
                return null;
            }

            @Override
            public Class<? extends Annotation> getScope() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public Set<Class<? extends Annotation>> getStereotypes() {
                return null;
            }

            @Override
            public Class<?> getBeanClass() {
                return object.getClass();
            }

            @Override
            public boolean isAlternative() {
                return false;
            }

            @Override
            public boolean isNullable() {
                return false;
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return null;
            }

            @Override
            public Object create(CreationalContext creationalContext) {
                return null;
            }

            @Override
            public void destroy(Object o, CreationalContext creationalContext) {

            }
        };
    }

    /**
     * Emulating concurrency migrations inside one JVM
     * <p>
     * We use only 1 monitor here, synchronized block inside jvmLocked should prevent multiple access, wait() isn't fired
     */
    @Test
    public void testJvmLocked() throws Exception {
        final long jobTimeout = 10L;
        final int n = 100;

        final ExecutorService executors = Executors.newFixedThreadPool(n);
        try {
            List<Future<CDILiquibaseConfig>> futures = new ArrayList<Future<CDILiquibaseConfig>>();
            for (int i = 1; i <= n; i++) {
                final Random random = new Random();
                final String tempId = String.format("id-%s", i);
                Callable<CDILiquibaseConfig> callable = new Callable<CDILiquibaseConfig>() {
                    @Override
                    public CDILiquibaseConfig call() throws Exception {
                        Thread.sleep(random.nextInt(2) + 1);
                        return schemesCDIConfigBuilder.jvmLocked(tempId, getAction(jobTimeout));
                    }
                };
                futures.add(executors.submit(callable));
            }

            validateFutures(futures);
        } catch (Exception e) {
            log.warning(LogType.LOG, e.getMessage(), e);
        } finally {
            executors.shutdown();
        }
    }

    @Test
    public void testFileLocked() throws Exception {
        final long jobTimeout = 10L;
        final int n = 100;

        final ExecutorService executors = Executors.newFixedThreadPool(n);
        try {
            List<Future<CDILiquibaseConfig>> futures = new ArrayList<Future<CDILiquibaseConfig>>();
            for (int i = 1; i <= n; i++) {
                final Random random = new Random();
                final String tempId = String.format("id-%s", i);
                Callable<CDILiquibaseConfig> callable = new Callable<CDILiquibaseConfig>() {
                    @Override
                    public CDILiquibaseConfig call() throws Exception {
                        Thread.sleep(random.nextInt(2) + 1);
                        return schemesCDIConfigBuilder.fileLocked(tempId, getAction(jobTimeout));
                    }
                };
                futures.add(executors.submit(callable));
            }

            validateFutures(futures);
        } catch (Exception e) {
            log.warning(LogType.LOG, e.getMessage(), e);
        } finally {
            executors.shutdown();
        }
    }

    private void validateFutures(List<Future<CDILiquibaseConfig>> futures) throws Exception {
        // an infinite loop
        while (true) {
            Iterator<Future<CDILiquibaseConfig>> futuresIterator = futures.iterator();
            while (futuresIterator.hasNext()) {
                Future<CDILiquibaseConfig> future = futuresIterator.next();
                Assert.assertFalse(future.isCancelled());
                if (future.isDone()) {
                    CDILiquibaseConfig config = future.get();
                    // we got a response from a thread
                    Assert.assertEquals("1", config.getParameters().get(BEFORE_KEY));
                    Assert.assertEquals("0", config.getParameters().get(AFTER_KEY));
                    futuresIterator.remove();
                }
            }
            // exit from the infinite loop
            if (futures.isEmpty()) {
                break;
            }
        }
    }

    private Callable<CDILiquibaseConfig> getAction(final long timeout) {
        return new Callable<CDILiquibaseConfig>() {
            @Override
            public CDILiquibaseConfig call() throws Exception {
                Map<String, String> map = new LinkedHashMap<String, String>();
                CDILiquibaseConfig config = new CDILiquibaseConfig();
                map.put(BEFORE_KEY, Long.toString(COUNTER.incrementAndGet()));
                Thread.sleep(timeout);
                map.put(AFTER_KEY, Long.toString(COUNTER.decrementAndGet()));
                config.setParameters(map);
                return config;
            }
        };
    }

}


@ApplicationScoped
@Default
@Liquibase
@LiquibaseSchema(name = "1", depends = "",  resource = "liquibase/parser/core/xml/changeLog1.xml")
class A1 {
}

@ApplicationScoped
@Default
@Liquibase
@LiquibaseSchema(name = "2", depends = "1", resource = "liquibase/parser/core/xml/changeLog2.xml")
class B2 {
}