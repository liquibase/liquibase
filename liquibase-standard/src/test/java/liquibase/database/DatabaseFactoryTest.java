package liquibase.database;

import liquibase.exception.DatabaseException;
import liquibase.resource.ResourceAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class DatabaseFactoryTest {

    @TempDir
    public File temporaryFolder;

    private ResourceAccessor resourceAccessor;
    private DatabaseFactory databaseFactory;

    @BeforeEach
    public void setUp() {
        resourceAccessor = mock(ResourceAccessor.class);
        DatabaseFactory.reset();
        databaseFactory = DatabaseFactory.getInstance();
    }

    @Test
    public void getInstance() {
        assertNotNull(DatabaseFactory.getInstance());
    }

    // openConnection() method tests
    @Test
    public void openConnectionReturnsOfflineConnectionWhenUrlPrefixMatches() throws Exception {
        String username = "sa";
        DatabaseConnection dbConnection = databaseFactory.openConnection("offline:h2?param1=value1&aparam2=value2", username, "", null, resourceAccessor);
        assertThat(dbConnection, notNullValue());
        assertThat(dbConnection, instanceOf(OfflineConnection.class));
        assertThat(dbConnection.getConnectionUserName(), equalTo(username));
    }

    @Test
    public void openConnectionUsesDriverArgument() throws Exception {
        DatabaseConnection dbConnection = databaseFactory.openConnection("jdbc:h2:mem:DatabaseFactoryTest", "sa", "", "org.h2.Driver", null, null, null, resourceAccessor);
        assertThat(dbConnection, notNullValue());
        assertThat(dbConnection.getDatabaseProductName(), equalTo("H2"));
    }

    @Test
    public void openConnectionThrowsExceptionWhenDriverCannotBeFoundByUrl() throws Exception {
        DatabaseException expectedException = assertThrows(DatabaseException.class, () -> databaseFactory.openConnection("not:a:driver", "", "", null, resourceAccessor));
        assertThat(expectedException.getCause(), instanceOf(RuntimeException.class));
        assertThat(expectedException.getMessage(), containsString("Driver class was not specified and could not be determined from the url"));
    }

    @Test
    public void openConnectionExceptionMessageDoesNotLeakPasswordWhenDriverCannotBeFoundByUrl() throws Exception {
        // CWE-209 regression: when the URL prefix is not recognized, the resulting
        // RuntimeException message must not surface credentials embedded in the URL.
        // findDriverClass() now routes the URL through JdbcConnection.sanitizeUrl()
        // before concatenating it into the message. The "cosmosdb://" URL is matched
        // by sanitizeUrl's registered ConnectionPatterns (see JdbcConnectionTest) but
        // is NOT recognised by findDefaultDriver (which only matches jdbc:-prefixed
        // schemes), so this URL exercises both branches: sanitisation kicks in and
        // the unrecognised-driver exception is the one that fires.
        String url = "cosmosdb://liquibaseuser:SuperSecretPwd123@host:443/db";
        DatabaseException expectedException = assertThrows(DatabaseException.class,
                () -> databaseFactory.openConnection(url, "", "", null, resourceAccessor));
        assertThat(expectedException.getCause(), instanceOf(RuntimeException.class));
        assertThat(expectedException.getMessage(), containsString("Driver class was not specified and could not be determined from the url"));
        // The credential value must not appear anywhere in the exception chain.
        assertThat(expectedException.getMessage(), not(containsString("SuperSecretPwd123")));
        Throwable cause = expectedException.getCause();
        while (cause != null) {
            String msg = cause.getMessage();
            if (msg != null) {
                assertThat(msg, not(containsString("SuperSecretPwd123")));
            }
            cause = cause.getCause();
        }
    }

    @Test
    public void openConnectionLoadsGivenDatabaseClass() throws Exception {
        DatabaseConnection dbConnection = databaseFactory.openConnection("jdbc:h2:mem:DatabaseFactoryTest", "sa", "", null, "liquibase.database.core.H2Database", null, null, resourceAccessor);
        assertThat(dbConnection, notNullValue());
        assertThat(dbConnection.getDatabaseProductName(), equalTo("H2"));
    }

    @Test
    public void openConnectionLoadsDriverPropertiesFromGivenFile() throws Exception {
        File propsFile = new File(temporaryFolder, "db-factory-test-connection-props.properties");
        Properties expectedProps = new Properties();
        expectedProps.setProperty("param1", "value1");
        expectedProps.setProperty("param2", "value2");
        try (FileWriter writer = new FileWriter(propsFile)) {
            expectedProps.store(writer, "connection properties");
        }
        String propsFilePath = propsFile.getAbsolutePath();

        DatabaseConnection dbConnection = databaseFactory.openConnection("jdbc:h2:mem:DatabaseFactoryTest", "sa", "", null, null, propsFilePath, null, resourceAccessor);
        assertThat(dbConnection, notNullValue());
        assertThat(dbConnection.getDatabaseProductName(), equalTo("H2"));
        // TODO: Figure out how to assert the properties are loaded
    }

    @Test
    public void openConnectionCreatesCustomPropertyProviderClassWhenGiven() throws Exception {
        DatabaseConnection dbConnection = databaseFactory.openConnection("jdbc:h2:mem:DatabaseFactoryTest", "sa", "", "liquibase.database.DatabaseFactoryTest$CustomProperties", resourceAccessor);
        assertThat(dbConnection, notNullValue());
        assertThat(dbConnection.getDatabaseProductName(), equalTo("H2"));
    }

    @Test
    public void openConnectionThrowsRuntimeExceptionWhenDriverPropertiesFileNotFound() throws Exception {
        DatabaseException expectedException = assertThrows(DatabaseException.class, () -> databaseFactory.openConnection("jdbc:h2:mem:DatabaseFactoryTest", "sa", "", null, null, "unknown file", null, resourceAccessor));
        assertThat(expectedException.getCause(), instanceOf(RuntimeException.class));
        assertThat(expectedException.getMessage(), containsString("Can't open JDBC Driver specific properties from the file"));
    }

    @Test
    public void openConnectionReturnsAConnection() throws Exception {
        DatabaseConnection dbConnection = databaseFactory.openConnection("jdbc:h2:mem:DatabaseFactoryTest", "sa", "", null, resourceAccessor);
        assertThat(dbConnection, notNullValue());
        assertThat(dbConnection.getDatabaseProductName(), equalTo("H2"));
    }

    /**
     * Simple type to test the propertyProviderClass
     */
    @SuppressWarnings({"unused", "RedundantSuppression"})
    private static class CustomProperties extends Properties {
        // Default constructor needed for reflective construction
        public CustomProperties() {
            super();
        }
    }
}
