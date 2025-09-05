package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.object.Account;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.DatabaseObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AccountSnapshotGeneratorSnowflake.
 * Target: Achieve 95%+ code coverage for all methods.
 */
class AccountSnapshotGeneratorSnowflakeTest {

    private AccountSnapshotGeneratorSnowflake generator;

    @Mock
    private SnowflakeDatabase snowflakeDatabase;

    @Mock
    private Database nonSnowflakeDatabase;

    @Mock
    private DatabaseSnapshot databaseSnapshot;

    @Mock
    private JdbcConnection jdbcConnection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private static final String TEST_ACCOUNT = "TEST_ACCOUNT";
    private static final String TEST_REGION = "US_EAST_1";
    private static final String SNOWFLAKE_URL = "jdbc:snowflake://testaccount.snowflakecomputing.com/";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new AccountSnapshotGeneratorSnowflake();
    }

    @Test
    void shouldHaveHighPriorityForAccountOnSnowflake() {
        int priority = generator.getPriority(Account.class, snowflakeDatabase);
        assertEquals(AccountSnapshotGeneratorSnowflake.PRIORITY_DATABASE, priority);
    }

    @Test
    void shouldHaveNoPriorityForAccountOnNonSnowflake() {
        int priority = generator.getPriority(Account.class, nonSnowflakeDatabase);
        assertEquals(AccountSnapshotGeneratorSnowflake.PRIORITY_NONE, priority);
    }

    @Test
    void shouldHaveNoPriorityForNonAccountOnSnowflake() {
        int priority = generator.getPriority(DatabaseObject.class, snowflakeDatabase);
        assertEquals(AccountSnapshotGeneratorSnowflake.PRIORITY_NONE, priority);
    }

    @Test
    void shouldBeRootLevelObject() {
        Class<? extends DatabaseObject>[] addsTo = generator.addsTo();
        
        assertNotNull(addsTo);
        assertEquals(0, addsTo.length, "Account should be a root-level object, peer to Catalog, not added to other objects");
    }

    @Test
    void shouldNotReplaceAnyGenerators() {
        assertNotNull(generator.replaces());
        assertEquals(0, generator.replaces().length);
    }

    @Test
    void shouldReturnNullForNullExample() throws Exception {
        DatabaseObject result = generator.snapshotObject(null, null);
        assertNull(result);
    }

    @Test
    void shouldReturnNullForNullSnapshot() throws Exception {
        // Test with Account object but no snapshot - should return null safely
        Account testAccount = new Account();
        testAccount.setName("TEST");
        
        // This test verifies that the method handles null snapshot gracefully
        // The method should check for null snapshot before calling snapshot.getDatabase()
        DatabaseObject result = generator.snapshotObject(testAccount, null);
        
        // Will return null because no snapshot provided
        assertNull(result);
    }

    @Test
    void shouldReturnNullForAccountWithNullName() throws Exception {
        Account account = new Account();
        // Name is null
        
        DatabaseObject result = generator.snapshotObject(account, null);
        assertNull(result);
    }

    // ==================== snapshotObject() Tests ====================

    @Test
    void snapshotObject_WithValidAccountName_ReturnsEnrichedAccount() throws Exception {
        // Given: Valid account example and mocked database interactions
        Account exampleAccount = new Account();
        exampleAccount.setName(TEST_ACCOUNT);
        
        when(databaseSnapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("ACCOUNT_NAME")).thenReturn(TEST_ACCOUNT);
        when(resultSet.getString("REGION")).thenReturn(TEST_REGION);
        
        // When: Snapshotting the account
        DatabaseObject result = generator.snapshotObject(exampleAccount, databaseSnapshot);
        
        // Then: Should return Account with enriched metadata
        assertNotNull(result, "Should return a valid Account object");
        assertTrue(result instanceof Account, "Should return Account instance");
        Account account = (Account) result;
        assertEquals(TEST_ACCOUNT, account.getName(), "Should have correct account name");
        assertEquals(TEST_REGION, account.getRegion(), "Should have enriched region");
        
        verify(preparedStatement).executeQuery();
        verify(resultSet).close();
        verify(preparedStatement).close();
    }

    @Test
    void snapshotObject_WithNullAccountName_CreatesAccountFromConnection() throws Exception {
        // Given: Account example with null name (triggers account creation from connection)
        Account exampleAccount = new Account();
        // name is null
        
        when(databaseSnapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.getURL()).thenReturn(SNOWFLAKE_URL);
        when(jdbcConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("ACCOUNT_NAME")).thenReturn("TESTACCOUNT");
        when(resultSet.getString("REGION")).thenReturn(TEST_REGION);
        
        // When: Snapshotting account with null name
        DatabaseObject result = generator.snapshotObject(exampleAccount, databaseSnapshot);
        
        // Then: Should create account from connection URL
        assertNotNull(result, "Should create account from connection");
        assertTrue(result instanceof Account, "Should return Account instance");
        Account account = (Account) result;
        assertEquals("TESTACCOUNT", account.getName(), "Should extract account from connection");
        assertEquals(TEST_REGION, account.getRegion(), "Should have enriched region");
        
        verify(jdbcConnection).getURL();
        verify(preparedStatement).executeQuery();
    }

    @Test
    void snapshotObject_WithNonSnowflakeDatabase_ReturnsNull() throws Exception {
        // Given: Account example but non-Snowflake database
        Account exampleAccount = new Account();
        exampleAccount.setName(TEST_ACCOUNT);
        
        when(databaseSnapshot.getDatabase()).thenReturn(nonSnowflakeDatabase);
        
        // When: Snapshotting with non-Snowflake database
        DatabaseObject result = generator.snapshotObject(exampleAccount, databaseSnapshot);
        
        // Then: Should return null
        assertNull(result, "Should return null for non-Snowflake database");
    }

    @Test
    void snapshotObject_WithNonAccountExample_ReturnsNull() throws Exception {
        // Given: Non-Account example
        DatabaseObject nonAccountExample = mock(DatabaseObject.class);
        
        when(databaseSnapshot.getDatabase()).thenReturn(snowflakeDatabase);
        
        // When: Snapshotting non-Account object
        DatabaseObject result = generator.snapshotObject(nonAccountExample, databaseSnapshot);
        
        // Then: Should return null
        assertNull(result, "Should return null for non-Account examples");
    }

    @Test
    void snapshotObject_WithSQLException_ReturnsAccountWithoutMetadata() throws Exception {
        // Given: Account example that will cause SQL exception during metadata enrichment
        Account exampleAccount = new Account();
        exampleAccount.setName(TEST_ACCOUNT);
        
        when(databaseSnapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenThrow(new SQLException("Connection failed"));
        
        // When: Snapshotting account (SQL exception during metadata enrichment should be caught)
        DatabaseObject result = generator.snapshotObject(exampleAccount, databaseSnapshot);
        
        // Then: Should return account without metadata (exceptions during enrichment are caught and ignored)
        assertNotNull(result, "Should return account even with SQL errors during metadata enrichment");
        Account account = (Account) result;
        assertEquals(TEST_ACCOUNT, account.getName(), "Should have basic account name");
        // Metadata fields should be null/default since enrichment failed
    }

    // ==================== addTo() Method Tests ====================

    @Test
    void addTo_WithAnyObject_DoesNothing() throws Exception {
        // Given: Any database object
        Account foundAccount = new Account();
        foundAccount.setName(TEST_ACCOUNT);
        
        // When: Calling addTo (should do nothing for root-level objects)
        assertDoesNotThrow(() -> generator.addTo(foundAccount, databaseSnapshot),
                          "Should handle addTo operation gracefully");
        
        // Then: Method should complete without errors (accounts are root-level objects)
        // No assertions needed as this method intentionally does nothing
    }

    @Test
    void addTo_WithNullObject_DoesNothing() throws Exception {
        // When: Calling addTo with null object
        assertDoesNotThrow(() -> generator.addTo(null, databaseSnapshot),
                           "Should handle null object gracefully");
        
        // Then: Method should complete without errors
    }

    // ==================== URL Parsing Edge Cases ====================

    @Test
    void snapshotObject_WithMalformedURL_UsesDefaultAccountName() throws Exception {
        // Given: Account with null name and malformed connection URL
        Account exampleAccount = new Account();
        
        when(databaseSnapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.getURL()).thenReturn("malformed-url");
        when(jdbcConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("ACCOUNT_NAME")).thenReturn("FALLBACK_ACCOUNT");
        when(resultSet.getString("REGION")).thenReturn(TEST_REGION);
        
        // When: Snapshotting with malformed URL
        DatabaseObject result = generator.snapshotObject(exampleAccount, databaseSnapshot);
        
        // Then: Should use fallback account name from SQL query
        assertNotNull(result, "Should create account even with malformed URL");
        Account account = (Account) result;
        assertEquals("FALLBACK_ACCOUNT", account.getName(), "Should use account name from SQL query");
    }

    @Test
    void snapshotObject_WithNullURL_UsesDefaultAccountName() throws Exception {
        // Given: Account with null name and null connection URL
        Account exampleAccount = new Account();
        
        when(databaseSnapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.getURL()).thenReturn(null);
        when(jdbcConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("ACCOUNT_NAME")).thenReturn("DEFAULT_ACCOUNT");
        when(resultSet.getString("REGION")).thenReturn(null);
        
        // When: Snapshotting with null URL
        DatabaseObject result = generator.snapshotObject(exampleAccount, databaseSnapshot);
        
        // Then: Should use account name from SQL query
        assertNotNull(result, "Should create account even with null URL");
        Account account = (Account) result;
        assertEquals("DEFAULT_ACCOUNT", account.getName(), "Should use account name from SQL query");
        assertNull(account.getRegion(), "Should handle null region gracefully");
    }

    // ==================== Cloud Provider Detection Tests ====================

    @Test
    void snapshotObject_WithAWSRegion_SetsCloudToAWS() throws Exception {
        // Given: Account with AWS region
        Account exampleAccount = new Account();
        exampleAccount.setName(TEST_ACCOUNT);
        
        when(databaseSnapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("ACCOUNT_NAME")).thenReturn(TEST_ACCOUNT);
        when(resultSet.getString("REGION")).thenReturn("aws-us-east-1");
        
        // When: Snapshotting account
        DatabaseObject result = generator.snapshotObject(exampleAccount, databaseSnapshot);
        
        // Then: Should detect AWS cloud provider
        Account account = (Account) result;
        assertEquals("AWS", account.getCloud(), "Should detect AWS from region");
    }

    @Test
    void snapshotObject_WithAzureRegion_SetsCloudToAzure() throws Exception {
        // Given: Account with Azure region
        Account exampleAccount = new Account();
        exampleAccount.setName(TEST_ACCOUNT);
        
        when(databaseSnapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("ACCOUNT_NAME")).thenReturn(TEST_ACCOUNT);
        when(resultSet.getString("REGION")).thenReturn("azure-east-us-2");
        
        // When: Snapshotting account
        DatabaseObject result = generator.snapshotObject(exampleAccount, databaseSnapshot);
        
        // Then: Should detect Azure cloud provider
        Account account = (Account) result;
        assertEquals("AZURE", account.getCloud(), "Should detect AZURE from region");
    }

    @Test
    void snapshotObject_WithGCPRegion_SetsCloudToGCP() throws Exception {
        // Given: Account with GCP region
        Account exampleAccount = new Account();
        exampleAccount.setName(TEST_ACCOUNT);
        
        when(databaseSnapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("ACCOUNT_NAME")).thenReturn(TEST_ACCOUNT);
        when(resultSet.getString("REGION")).thenReturn("gcp-us-central1");
        
        // When: Snapshotting account
        DatabaseObject result = generator.snapshotObject(exampleAccount, databaseSnapshot);
        
        // Then: Should detect GCP cloud provider
        Account account = (Account) result;
        assertEquals("GCP", account.getCloud(), "Should detect GCP from region");
    }

    @Test
    void snapshotObject_WithUnknownRegion_LeavesCloudNull() throws Exception {
        // Given: Account with unknown region
        Account exampleAccount = new Account();
        exampleAccount.setName(TEST_ACCOUNT);
        
        when(databaseSnapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("ACCOUNT_NAME")).thenReturn(TEST_ACCOUNT);
        when(resultSet.getString("REGION")).thenReturn("unknown-region-123");
        
        // When: Snapshotting account
        DatabaseObject result = generator.snapshotObject(exampleAccount, databaseSnapshot);
        
        // Then: Should leave cloud provider null
        Account account = (Account) result;
        assertNull(account.getCloud(), "Should not set cloud for unknown regions");
    }

    // ==================== Error Handling Edge Cases ====================

    @Test
    void snapshotObject_WithMetadataQueryFailure_ContinuesWithoutMetadata() throws Exception {
        // Given: Account that fails metadata enrichment but should continue
        Account exampleAccount = new Account();
        exampleAccount.setName(TEST_ACCOUNT);
        
        when(databaseSnapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenThrow(new SQLException("Permission denied"));
        
        // When: Snapshotting account (metadata failure should be caught and ignored)
        DatabaseObject result = generator.snapshotObject(exampleAccount, databaseSnapshot);
        
        // Then: Should return account without metadata (failure is tolerated)
        assertNotNull(result, "Should return account even with metadata failures");
        Account account = (Account) result;
        assertEquals(TEST_ACCOUNT, account.getName(), "Should have basic account name");
        // Region and cloud should be null since metadata enrichment failed
    }

    @Test
    void snapshotObject_WithCreateAccountConnectionFailure_ReturnsAccountWithoutMetadata() throws Exception {
        // Given: Account with null name that will fail during metadata enrichment in createAccountContainer
        Account exampleAccount = new Account();
        
        when(databaseSnapshot.getDatabase()).thenReturn(snowflakeDatabase);
        when(snowflakeDatabase.getConnection()).thenReturn(jdbcConnection);
        when(jdbcConnection.getURL()).thenReturn(SNOWFLAKE_URL);
        when(jdbcConnection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenThrow(new SQLException("Connection timeout"));
        
        // When: Snapshotting account (connection failure during metadata enrichment should be caught)
        DatabaseObject result = generator.snapshotObject(exampleAccount, databaseSnapshot);
        
        // Then: Should return account without metadata (enrichment failures are caught and ignored)
        assertNotNull(result, "Should create account even with metadata enrichment failures");
        Account account = (Account) result;
        assertEquals("TESTACCOUNT", account.getName(), "Should extract account from URL");
        // Metadata fields should be null since enrichment failed
    }

    // Note: Full database integration tests for complex queries and real Snowflake connections
    // are covered in AccountSnapshotIntegrationTest since they require actual database connections
}