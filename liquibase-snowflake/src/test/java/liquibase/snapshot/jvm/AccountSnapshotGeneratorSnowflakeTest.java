package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.Account;
import liquibase.structure.DatabaseObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AccountSnapshotGeneratorSnowflake.
 */
class AccountSnapshotGeneratorSnowflakeTest {

    private AccountSnapshotGeneratorSnowflake generator;

    @Mock
    private SnowflakeDatabase snowflakeDatabase;

    @Mock
    private Database nonSnowflakeDatabase;

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

    // Note: Full database integration tests for snapshotObject() and addTo() methods
    // are covered in AccountSnapshotIntegrationTest since they require actual database connections
}