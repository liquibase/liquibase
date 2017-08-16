package liquibase.integration.cdi;

import liquibase.integration.cdi.annotations.Liquibase;
import liquibase.integration.cdi.annotations.LiquibaseSchema;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.Logger;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.util.FileUtil;
import liquibase.util.StreamUtil;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * @author Nikita Lipatov (https://github.com/islonik)
 * @since 27/5/17.
 */
@Singleton
public class SchemesCDIConfigBuilder {

    private static final Logger log = LogService.getLog(SchemesCDIConfigBuilder.class);

    private static final String ROOT_PATH = System.getProperty("java.io.tmpdir");

    private static final String SCHEMA_NAME = "/schema.template.xml";
    private static final String TEMPLATE_NAME = "liquibase.cdi.schema.xml";
    private static final String INCLUDE_TPL = "\t<include file=\"%s\"/>%n";

    private static final Long FILE_LOCK_TIMEOUT = 50L;

    private final BeanManager bm;
    private final SchemesTreeBuilder treeBuilder;

    @Inject
    public SchemesCDIConfigBuilder(BeanManager bm, SchemesTreeBuilder treeBuilder) {
        this.bm = bm;
        this.treeBuilder = treeBuilder;
    }

    /**
     * API method.
     */
    public ResourceAccessor createResourceAccessor() {
        return new FileSystemResourceAccessor(ROOT_PATH);
    }

    /**
     * API method.
     */
    public CDILiquibaseConfig createCDILiquibaseConfig() {
        final String id = UUID.randomUUID().toString();
        log.debug(LogType.LOG, String.format("[id = %s] createConfig(). Date: '%s'", id, new Date()));

        final InputStream is = SchemesCDIConfigBuilder.class.getResourceAsStream(SCHEMA_NAME);
        try {
            return jvmLocked(id, new Callable<CDILiquibaseConfig>() {
                public CDILiquibaseConfig call() throws Exception {
                    return createCDILiquibaseConfig(id, is);
                }
            });
        } catch (Exception ex) {
            log.warning(LogType.LOG, String.format("[id = %s] Unable to initialize liquibase where '%s'.", id, ex.getMessage()), ex);
            return null;
        } finally {
            try {
                is.close();
            } catch (IOException ioe) {
                log.warning(LogType.LOG, String.format("[id = %s] IOException during closing an input stream '%s'.", id, ioe.getMessage()), ioe);
            }
        }
    }
    
    private CDILiquibaseConfig createCDILiquibaseConfig(final String id, final InputStream is) throws IOException {
        File liquibaseDir = new File(String.format("%s/liquibase/schemes", ROOT_PATH));
        if (!liquibaseDir.exists() && (!liquibaseDir.mkdirs())) {
            throw new RuntimeException(String.format("[id = %s] Cannot create [%s] dirs.", id, liquibaseDir));
        }
        log.debug(LogType.LOG, String.format("[id = %s] Includes directory: [path='%s']", id, liquibaseDir.getAbsolutePath()));

        String path = String.format("%s/%s", ROOT_PATH, TEMPLATE_NAME);
        File output = new File(path);

        if (output.exists()) {
            log.debug(LogType.LOG, String.format("[id = %s] File [path='%s'] already exists, deleting", id, path));
            if (output.delete()) {
                log.debug(LogType.LOG, String.format("[id = %s] File [path='%s'] already exists, deleted successfully.", id, path));
            } else {
                log.debug(LogType.LOG, String.format("[id = %s] File [path='%s'] already exists, failed to delete.", id, path));
            }
        }
        if (!output.createNewFile()) {
            throw new RuntimeException(String.format("[id = %s] Cannot create [%s] file.", id, output));
        }
        log.info(LogType.LOG, String.format("[id = %s] File %s was created.", id, output));
        log.debug(LogType.LOG, String.format("[id = %s] Root liquibase file [path='%s'] ready.", id, path));

        long start = System.currentTimeMillis();
        log.info(LogType.LOG, String.format("[id = %s] Scanning application for liquibase schemes.", id));

        Set<Bean<?>> beans = bm.getBeans(Object.class, new AnnotationLiteralDefault());

        Set<Class<?>> classesSet = new LinkedHashSet<>();
        for (Bean<?> bean : beans) {
            classesSet.add(bean.getBeanClass());
        }
        Set<Annotation> annotationsSet = new LinkedHashSet<>();
        for (Class clazz : classesSet) {
            annotationsSet.add(clazz.getAnnotation(LiquibaseSchema.class));
        }
        List<LiquibaseSchema> liquibaseSchemaList = new ArrayList<>();
        for (Annotation ann : annotationsSet) {
            liquibaseSchemaList.add((LiquibaseSchema) ann);
        }

        List<LiquibaseSchema> treeList = treeBuilder.build(id, liquibaseSchemaList);

        List<String[]> resourceList = new ArrayList<>();
        for (LiquibaseSchema liquibaseSchema : treeList) {
            resourceList.add(liquibaseSchema.resource());
        }

        List<String> schemaPaths = new ArrayList<>();
        for (String[] resources : resourceList) {
            for (String resource : resources) {
                schemaPaths.add(copyToFile(id, liquibaseDir.getAbsolutePath(), resource));
            }
        }

        StringBuilder schemes = new StringBuilder();
        for (String schema : schemaPaths) {
            schemes.append(String.format(INCLUDE_TPL, schema)).append("\n");
        }

        log.info(LogType.LOG, String.format("[id = %s] Scan complete [took=%s milliseconds].", id, System.currentTimeMillis() - start));
        log.debug(LogType.LOG, String.format("[id = %s] Resolved schemes: %n%s%n", id, schemes));
        log.debug(LogType.LOG, String.format("[id = %s] Generating root liquibase file...", id));

        String template = StreamUtil.getStreamContents(is); // schema.template.xml

        String xml = String.format(template, schemes);

        FileUtil.write(xml, output);

        log.info(LogType.LOG, String.format("[id = %s] File %s was written.", id, output));
        log.debug(LogType.LOG, String.format("[id = %s] Generation complete.", id));
        log.debug(LogType.LOG, String.format("[id = %s] Root liquibase xml: %n %s %n", id, xml));

        CDILiquibaseConfig config = new CDILiquibaseConfig();
        config.setChangeLog(TEMPLATE_NAME);
        return config;
    }

    synchronized CDILiquibaseConfig jvmLocked(final String id, Callable<CDILiquibaseConfig> action) throws Exception {
        return fileLocked(id, action);
    }

    /**
     * Synchronization among multiple JVM's.
     */
    CDILiquibaseConfig fileLocked(final String id, Callable<CDILiquibaseConfig> action) throws Exception {
        log.info(LogType.LOG, String.format("[id = %s] JVM lock acquired, acquiring file lock", id));
        String lockPath = String.format("%s/schema.liquibase.lock", ROOT_PATH);

        File lockFile = new File(lockPath);
        if (!lockFile.exists() && lockFile.createNewFile()) {
            log.info(LogType.LOG, String.format("[id = %s] Created lock file [path='%s'].", id, lockPath));
        }

        log.info(LogType.LOG, String.format("[id = %s] Trying to acquire the file lock [file='%s']...", id, lockPath));

        CDILiquibaseConfig actionResult = null;
        FileLock lock = null;
        try (
            FileOutputStream fileStream = new FileOutputStream(lockPath);
            FileChannel fileChannel = fileStream.getChannel();
        )
        {
            while (null == lock) {
                try {
                    lock = fileChannel.tryLock();
                } catch (OverlappingFileLockException e) {
                    log.debug(LogType.LOG, String.format("[id = %s] Lock already acquired, waiting for the lock...", id));
                }
                if (null == lock) {
                    log.debug(LogType.LOG, String.format("[id = %s] Waiting for the lock...", id));
                    Thread.sleep(FILE_LOCK_TIMEOUT);
                }
            }
            log.info(LogType.LOG, String.format("[id = %s] File lock acquired, running liquibase...", id));
            actionResult = action.call();
            lock.release();
        } catch (Exception e) {
            log.warning(LogType.LOG, e.getMessage(), e);
        }
        return actionResult;
    }

    private String copyToFile(final String id, final String liquibase, final String schema) {
        log.info(LogType.LOG, String.format("[id = %s] copyToFile(%s, %s)", id, liquibase, schema));

        InputStream is = null;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream(schema);

            log.info(LogType.LOG, String.format("[id = %s] Transferring schema [resource=%s] to directory [path=%s]...", id, schema, liquibase));
            String path = schema.startsWith("/") ? schema.substring(1) : schema;
            log.debug(LogType.LOG, String.format("[id = %s] LiquibaseSchema path is [path='%s'].", id, path));

            if (path.contains("/")) {

                String dirPath = String.format("%s/%s", liquibase, path.substring(0, path.lastIndexOf('/')));
                log.debug(LogType.LOG, String.format("[id = %s] LiquibaseSchema path contains intermediate directories [path='%s'], preparing its...", id, dirPath));

                File file = new File(dirPath);
                if (!file.exists() && file.mkdirs()) {
                    log.info(LogType.LOG, String.format("[id = %s] Directories for [path='%s'] file created.", id, file.getAbsolutePath()));
                }
            }

            File file = new File(String.format("%s/%s", liquibase, path));
            if (file.exists()) {
                log.info(LogType.LOG, String.format("[id = %s] LiquibaseSchema file [path='%s'] already exists, deleting...", id, file.getAbsolutePath()));
                if (file.delete()) {
                    log.info(LogType.LOG, String.format("[id = %s] File [path='%s'] deleted.", id, file.getAbsolutePath()));
                }
            }
            if (file.createNewFile()) {
                log.info(LogType.LOG, String.format("[id = %s] File [path='%s'] created.", id, file.getAbsolutePath()));
            }
            log.debug(LogType.LOG, String.format("[id = %s] LiquibaseSchema file [path='%s'] is ready, copying data...", id, file.getAbsolutePath()));

            FileUtil.write(StreamUtil.getStreamContents(is), file);

            String schemaPath = file.getAbsolutePath().replace(ROOT_PATH, "");
            log.info(LogType.LOG, String.format("[id = %s] Data copied, schema path is [path='%s'].", id, schemaPath));
            return schemaPath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                log.warning(LogType.LOG, String.format("IOException during closing an input stream '%s'.", ioe.getMessage()), ioe);
            }
        }
    }

    static class AnnotationLiteralDefault extends AnnotationLiteral<Liquibase> {
        private static final long serialVersionUID = -2878951947483191L;
    }
}
