package liquibase.test;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.sdk.database.MockDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

import java.io.File;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

                database.setCanCacheLiquibaseTableInfo(false);
            }
            allDatabases.removeAll(toRemove);
        }
        return allDatabases;
    }

    public File findCoreJvmProjectRoot() throws URISyntaxException {
        return new File(findCoreProjectRoot().getParentFile(), "liquibase-core");
    }

    public File findIntegrationTestProjectRoot() throws URISyntaxException {
        return new File(findCoreProjectRoot().getParentFile(), "liquibase-integration-tests");
    }

    public File findCoreProjectRoot() throws URISyntaxException {
        URI uri = new URI(this.getClass().getClassLoader().getResource("liquibase/test/TestContext.class").toExternalForm());
        if(!uri.isOpaque()) {
            File thisClassFile = new File(uri);
            return thisClassFile.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
        }
        uri = new URI(this.getClass().getClassLoader().getResource("liquibase/integration/commandline/Main.class").toExternalForm());
        if(!uri.isOpaque()) {
            File thisClassFile = new File(uri);
            return new File(thisClassFile.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile(), "liquibase-core");
        }
        uri = new URI(this.getClass().getClassLoader().getResource("liquibase/test/DatabaseTest.class").toExternalForm());
        if(!uri.isOpaque()) {
            File thisClassFile = new File(uri);
            return new File(thisClassFile.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile(), "liquibase-core");
        }
        throw new IllegalStateException("Cannot find liquibase-core project root");
    }

    public ResourceAccessor getTestResourceAccessor() throws URISyntaxException, MalformedURLException {
        if (resourceAccessor == null) {
            File integrationJarsDir = new File(TestContext.getInstance().findIntegrationTestProjectRoot(), "src/test/resources/ext/jars");

            /*File samples1 = new File(integrationJarsDir, "liquibase-samplesqlgenerator.jar");
            File samples2 = new File(integrationJarsDir, "liquibase-changesample.jar");

            if (!samples2.exists()) {
                throw new RuntimeException("Could not find "+samples2.getAbsolutePath());
            }*/
            resourceAccessor = new ClassLoaderResourceAccessor(new URLClassLoader(new URL[]{
                    //samples1.toURL(),
                    //samples2.toURL(),
                    new File(TestContext.getInstance().findCoreJvmProjectRoot(), "/target/classes").toURL(),
                    new File(TestContext.getInstance().findCoreProjectRoot(), "/target/classes").toURL()
            }));
        }

        return resourceAccessor;
    }


}
