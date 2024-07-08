package liquibase.integration.jakarta.cdi;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionPoint;
import liquibase.Scope;
import liquibase.integration.jakarta.cdi.annotations.Liquibase;
import liquibase.integration.jakarta.cdi.annotations.LiquibaseSchema;
import liquibase.logging.Logger;


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
        log = Scope.getCurrentScope().getLog(SchemesCDIConfigBuilder.class);
//        log.setLogLevel(LogLevel.WARNING); // you can change it to INFO or DEBUG level if you want to see them

        try {
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
        } catch (NoSuchFieldException e) {
            //newer JDK version's don't have the internal fields
        }
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

        treeBuilder = new SchemesTreeBuilder();
        schemesCDIConfigBuilder = new SchemesCDIConfigBuilder(bm, treeBuilder);
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
            log.warning(e.getMessage(), e);
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
            log.warning(e.getMessage(), e);
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
        return () -> {
            Map<String, String> map = new LinkedHashMap<String, String>();
            CDILiquibaseConfig config = new CDILiquibaseConfig();
            map.put(BEFORE_KEY, Long.toString(COUNTER.incrementAndGet()));
            Thread.sleep(timeout);
            map.put(AFTER_KEY, Long.toString(COUNTER.decrementAndGet()));
            config.setParameters(map);
            return config;
        };
    }

}
