package liquibase.database.jvm;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SnowflakeOAuthConnectionTest {

    @Test
    public void testOAuthConnectionWithNullUsername() throws Exception {
        Connection mockConnection = mock(Connection.class);
        DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getUserName()).thenReturn(null);
        when(mockMetaData.getURL()).thenReturn("jdbc:snowflake://account.snowflakecomputing.com/?authenticator=oauth&token=xyz");
        
        SnowflakeOAuthConnection connection = new SnowflakeOAuthConnection(mockConnection);
        
        String username = connection.getConnectionUserName();
        assertNotNull("Username should not be null", username);
        assertNotEquals("Username should not be 'null'", "null", username);
        assertEquals("Should return the default OAuth user", "oauth-authenticated-user", username);
    }

    @Test
    public void testOAuthConnectionWithClientId() throws Exception {
        Connection mockConnection = mock(Connection.class);
        DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getUserName()).thenReturn(null);
        when(mockMetaData.getURL()).thenReturn("jdbc:snowflake://account.snowflakecomputing.com/?authenticator=oauth&client_id=my-client-id&token=xyz");
        
        SnowflakeOAuthConnection connection = new SnowflakeOAuthConnection(mockConnection);
        
        String username = connection.getConnectionUserName();
        assertNotNull("Username should not be null", username);
        assertEquals("Should return the client_id as username", "my-client-id", username);
    }

    @Test
    public void testStandardUsernameIsReturned() throws Exception {
        Connection mockConnection = mock(Connection.class);
        DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getUserName()).thenReturn("standard-user");
        when(mockMetaData.getURL()).thenReturn("jdbc:snowflake://account.snowflakecomputing.com/?authenticator=oauth&token=xyz");
        
        SnowflakeOAuthConnection connection = new SnowflakeOAuthConnection(mockConnection);
        
        String username = connection.getConnectionUserName();
        assertEquals("Should return the original username", "standard-user", username);
    }

    @Test
    public void testNonOAuthAuthenticator() throws Exception {
        Connection mockConnection = mock(Connection.class);
        DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getUserName()).thenReturn(null);
        when(mockMetaData.getURL()).thenReturn("jdbc:snowflake://account.snowflakecomputing.com/?authenticator=externalbrowser");
        
        SnowflakeOAuthConnection connection = new SnowflakeOAuthConnection(mockConnection);
        
        String username = connection.getConnectionUserName();
        assertNotNull("Username should not be null", username);
        assertEquals("Should use authenticator type", "externalbrowser-authenticated-user", username);
    }

    @Test
    public void testFallbackUsername() throws Exception {
        Connection mockConnection = mock(Connection.class);
        DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getUserName()).thenReturn(null);
        when(mockMetaData.getURL()).thenReturn("jdbc:snowflake://account.snowflakecomputing.com/");
        
        SnowflakeOAuthConnection connection = new SnowflakeOAuthConnection(mockConnection);
        
        String username = connection.getConnectionUserName();
        assertNotNull("Username should not be null", username);
        assertEquals("Should use default username", "snowflake-user", username);
    }

    @Test
    public void testHandlesNullUrl() throws Exception {
        Connection mockConnection = mock(Connection.class);
        DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getUserName()).thenReturn(null);
        when(mockMetaData.getURL()).thenReturn(null);
        
        SnowflakeOAuthConnection connection = new SnowflakeOAuthConnection(mockConnection);
        
        String username = connection.getConnectionUserName();
        assertNotNull("Username should not be null even with null URL", username);
        assertEquals("Should use default username with null URL", "snowflake-user", username);
    }
}