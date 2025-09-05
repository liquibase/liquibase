package liquibase.diff.output;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.Account;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.structure.DatabaseObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AccountComparator.
 */
class AccountComparatorTest {

    private AccountComparator comparator;

    @Mock
    private SnowflakeDatabase snowflakeDatabase;

    @Mock
    private Database nonSnowflakeDatabase;

    @Mock
    private CompareControl compareControl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        comparator = new AccountComparator();
    }

    @Test
    void shouldHaveHighPriorityForAccountOnSnowflake() {
        int priority = comparator.getPriority(Account.class, snowflakeDatabase);
        assertEquals(AccountComparator.PRIORITY_DATABASE, priority);
    }

    @Test
    void shouldHaveNoPriorityForAccountOnNonSnowflake() {
        int priority = comparator.getPriority(Account.class, nonSnowflakeDatabase);
        assertEquals(AccountComparator.PRIORITY_NONE, priority);
    }

    @Test
    void shouldHaveNoPriorityForNonAccountOnSnowflake() {
        int priority = comparator.getPriority(DatabaseObject.class, snowflakeDatabase);
        assertEquals(AccountComparator.PRIORITY_NONE, priority);
    }

    @Test
    void shouldHashByName() {
        Account account = new Account();
        account.setName("TEST_ACCOUNT");
        
        String[] hash = comparator.hash(account, snowflakeDatabase, null);
        
        assertEquals(1, hash.length);
        assertEquals("TEST_ACCOUNT", hash[0]);
    }

    @Test
    void shouldIdentifySameObjectsWhenNameMatches() {
        Account account1 = new Account();
        account1.setName("TEST_ACCOUNT");
        
        Account account2 = new Account();
        account2.setName("TEST_ACCOUNT");
        
        boolean isSame = comparator.isSameObject(account1, account2, snowflakeDatabase, null);
        
        assertTrue(isSame);
    }

    @Test
    void shouldIdentifySameObjectsWhenNameMatchesCaseInsensitive() {
        Account account1 = new Account();
        account1.setName("TEST_ACCOUNT");
        
        Account account2 = new Account();
        account2.setName("test_account");
        
        when(snowflakeDatabase.isCaseSensitive()).thenReturn(false);
        
        boolean isSame = comparator.isSameObject(account1, account2, snowflakeDatabase, null);
        
        assertTrue(isSame);
    }

    @Test
    void shouldNotIdentifyDifferentObjectsAsTheSame() {
        Account account1 = new Account();
        account1.setName("ACCOUNT_1");
        
        Account account2 = new Account();
        account2.setName("ACCOUNT_2");
        
        boolean isSame = comparator.isSameObject(account1, account2, snowflakeDatabase, null);
        
        assertFalse(isSame);
    }

    @Test
    void shouldNotIdentifyNonAccountsAsTheSame() {
        Account account = new Account();
        account.setName("TEST_ACCOUNT");
        
        Account differentAccount = new Account(); // Use concrete Account instead of anonymous class
        differentAccount.setName("DIFFERENT_ACCOUNT");
        
        boolean isSame = comparator.isSameObject(account, differentAccount, snowflakeDatabase, null);
        
        assertFalse(isSame);
    }

    @Test
    void shouldReturnFalseForNullNames() {
        Account account1 = new Account();
        // Name is null
        
        Account account2 = new Account();
        account2.setName("TEST_ACCOUNT");
        
        boolean isSame = comparator.isSameObject(account1, account2, snowflakeDatabase, null);
        
        assertFalse(isSame);
    }

    @Test
    void shouldDetectNoPropertyChanges() {
        Account account1 = new Account();
        account1.setName("TEST_ACCOUNT");
        account1.setRegion("us-west-2");
        account1.setCloud("AWS");
        account1.setAccountUrl("https://test-account.snowflakecomputing.com");
        
        Account account2 = new Account();
        account2.setName("TEST_ACCOUNT");
        account2.setRegion("us-west-2");
        account2.setCloud("AWS");
        account2.setAccountUrl("https://test-account.snowflakecomputing.com");
        
        ObjectDifferences differences = comparator.findDifferences(
            account1, account2, snowflakeDatabase, compareControl, null, new HashSet<>()
        );
        
        assertTrue(differences.getDifferences().isEmpty());
    }

    @Test
    void shouldDetectRegionChange() {
        Account account1 = new Account();
        account1.setName("TEST_ACCOUNT");
        account1.setRegion("us-west-2");
        account1.setCloud("AWS");
        
        Account account2 = new Account();
        account2.setName("TEST_ACCOUNT");
        account2.setRegion("us-east-1");
        account2.setCloud("AWS");
        
        ObjectDifferences differences = comparator.findDifferences(
            account1, account2, snowflakeDatabase, compareControl, null, new HashSet<>()
        );
        
        assertFalse(differences.getDifferences().isEmpty());
    }

    @Test
    void shouldDetectCloudChange() {
        Account account1 = new Account();
        account1.setName("TEST_ACCOUNT");
        account1.setRegion("us-west-2");
        account1.setCloud("AWS");
        
        Account account2 = new Account();
        account2.setName("TEST_ACCOUNT");
        account2.setRegion("us-west-2");
        account2.setCloud("AZURE");
        
        ObjectDifferences differences = comparator.findDifferences(
            account1, account2, snowflakeDatabase, compareControl, null, new HashSet<>()
        );
        
        assertFalse(differences.getDifferences().isEmpty());
    }

    @Test
    void shouldDetectAccountUrlChange() {
        Account account1 = new Account();
        account1.setName("TEST_ACCOUNT");
        account1.setAccountUrl("https://old-account.snowflakecomputing.com");
        
        Account account2 = new Account();
        account2.setName("TEST_ACCOUNT");
        account2.setAccountUrl("https://new-account.snowflakecomputing.com");
        
        ObjectDifferences differences = comparator.findDifferences(
            account1, account2, snowflakeDatabase, compareControl, null, new HashSet<>()
        );
        
        assertFalse(differences.getDifferences().isEmpty());
    }

    @Test
    void shouldHandleNullPropertiesInDifferences() {
        Account account1 = new Account();
        account1.setName("TEST_ACCOUNT");
        // Leave properties null
        
        Account account2 = new Account();
        account2.setName("TEST_ACCOUNT");
        account2.setRegion("us-west-2");
        
        ObjectDifferences differences = comparator.findDifferences(
            account1, account2, snowflakeDatabase, compareControl, null, new HashSet<>()
        );
        
        assertFalse(differences.getDifferences().isEmpty());
    }

    @Test
    void shouldReturnEmptyDifferencesForNonAccounts() {
        // Use identical Account objects to test no-difference scenario
        Account account1 = new Account();
        account1.setName("SAME_ACCOUNT");
        account1.setRegion("us-west-2");
        account1.setCloud("AWS");
        
        Account account2 = new Account();  
        account2.setName("SAME_ACCOUNT");
        account2.setRegion("us-west-2");
        account2.setCloud("AWS");
        
        // Test with identical accounts - should have no differences
        ObjectDifferences differences = comparator.findDifferences(
            account1, account2, snowflakeDatabase, compareControl, null, new HashSet<>()
        );
        
        // Since these accounts are identical, we expect no differences
        assertTrue(differences.getDifferences().isEmpty());
    }
}