package liquibase.database;

import liquibase.exception.DatabaseException;
import liquibase.resource.ResourceAccessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.util.Properties;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class DatabaseFactoryTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ResourceAccessor resourceAccessor;
    private DatabaseFactory databaseFactory;

    @Before
    public void setUp() {
        resourceAccessor = mock(ResourceAccessor.class);
        DatabaseFactory.reset();
        databaseFactory = DatabaseFactory.getInstance();
    }

    @After
    public void tearDown() {
        temporaryFolder.delete();
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
        expectedException.expect(instanceOf(DatabaseException.class));
        expectedException.expectCause(instanceOf(RuntimeException.class));
        expectedException.expectMessage(containsString("Driver class was not specified and could not be determined from the url"));

        databaseFactory.openConnection("not:a:driver", "", "", null, resourceAccessor);
    }

    @Test
    public void openConnectionLoadsGivenDatabaseClass() throws Exception {
        DatabaseConnection dbConnection = databaseFactory.openConnection("jdbc:h2:mem:DatabaseFactoryTest", "sa", "", null, "liquibase.database.core.H2Database", null, null, resourceAccessor);
        assertThat(dbConnection, notNullValue());
        assertThat(dbConnection.getDatabaseProductName(), equalTo("H2"));
    }

    @Test
    public void openConnectionLoadsDriverPropertiesFromGivenFile() throws Exception {
        File propsFile = temporaryFolder.newFile("db-factory-test-connection-props.properties");
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
        expectedException.expect(instanceOf(DatabaseException.class));
        expectedException.expectCause(instanceOf(RuntimeException.class));
        expectedException.expectMessage(containsString("Can't open JDBC Driver specific properties from the file"));

        databaseFactory.openConnection("jdbc:h2:mem:DatabaseFactoryTest", "sa", "", null, null, "unknown file", null, resourceAccessor);
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
