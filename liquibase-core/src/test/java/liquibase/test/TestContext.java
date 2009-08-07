package liquibase.test;

import liquibase.database.*;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.MockDatabase;
import liquibase.exception.DatabaseException;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.*;
import java.io.File;
import java.net.*;

/**
 * Controls the database connections for running tests.
 * For times we aren't wanting to run the database-hitting tests, set the "test.databases" system property
 * to be a comma-separated list of the databses we want to test against.  The string is checked against the database url.
 */
public class TestContext {
    private static TestContext instance = new TestContext();

    private Set<Database> allDatabases;
    private ResourceAccessor resourceAccessor;

    public static TestContext getInstance() {
        return instance;
    }
    
    public Set<Database> getAllDatabases() {
        if (allDatabases == null) {
            allDatabases = new HashSet<Database>();

            allDatabases.addAll(DatabaseFactory.getInstance().getImplementedDatabases());

            List<Database> toRemove = new ArrayList<Database>();
            for (Database database : allDatabases) {
                if (database instanceof SQLiteDatabase //todo: re-enable sqlite testing
                        || database instanceof MockDatabase) {
                    toRemove.add(database);
                }
            }
            allDatabases.removeAll(toRemove);
        }
        return allDatabases;
    }

    public File findCoreJvmProjectRoot() throws URISyntaxException {
        return new File(findCoreProjectRoot().getParentFile(), "liquibase-core-jvm");
    }

    public File findIntegrationTestProjectRoot() throws URISyntaxException {
        return new File(findCoreProjectRoot().getParentFile(), "liquibase-integration-tests");
    }

    public File findCoreProjectRoot() throws URISyntaxException {
        File thisClassFile = new File(new URI(this.getClass().getClassLoader().getResource("liquibase/test/TestContext.class").toExternalForm()));
        return thisClassFile.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
    }

    public ResourceAccessor getTestResourceAccessor() throws URISyntaxException, MalformedURLException {
        if (resourceAccessor == null) {
            File samples1 = new File(TestContext.getInstance().findCoreJvmProjectRoot(), "/lib-test/liquibase-sample1.jar");
            File samples2 = new File(TestContext.getInstance().findCoreJvmProjectRoot(), "/lib-test/liquibase-sample2.jar");
            if (!samples2.exists()) {
                throw new RuntimeException("Could not find "+samples2.getAbsolutePath());
            }
            resourceAccessor = new CompositeResourceAccessor(new ClassLoaderResourceAccessor(), new ClassLoaderResourceAccessor(new URLClassLoader(new URL[]{
// sample1 does not actually run                    samples1.toURL(),
                    samples2.toURL(),
                    new File(TestContext.getInstance().findCoreJvmProjectRoot(), "/build").toURL(),
                    new File(TestContext.getInstance().findCoreProjectRoot(), "/build").toURL()
            })));
        }

        return resourceAccessor;
    }


}
