package liquibase.database.object;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Account database object.
 */
class AccountTest {

    @Test
    void shouldCreateBasicAccountObject() {
        Account account = new Account();
        account.setName("TEST_ACCOUNT");
        
        assertEquals("TEST_ACCOUNT", account.getName());
        assertEquals("TEST_ACCOUNT", account.getSnapshotId());
    }

    @Test
    void shouldConvertNameToUppercase() {
        Account account = new Account();
        account.setName("test_account");
        
        assertEquals("TEST_ACCOUNT", account.getName());
    }

    @Test
    void shouldThrowExceptionForNullName() {
        Account account = new Account();
        
        assertThrows(IllegalArgumentException.class, () -> {
            account.setName(null);
        });
    }

    @Test
    void shouldThrowExceptionForEmptyName() {
        Account account = new Account();
        
        assertThrows(IllegalArgumentException.class, () -> {
            account.setName("");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            account.setName("   ");
        });
    }

    @Test
    void shouldReturnEmptyContainingObjects() {
        Account account = new Account();
        account.setName("TEST_ACCOUNT");
        
        assertEquals(0, account.getContainingObjects().length);
    }

    @Test
    void shouldReturnNullSchema() {
        Account account = new Account();
        account.setName("TEST_ACCOUNT");
        
        assertNull(account.getSchema());
    }

    @Test
    void shouldSetAccountProperties() {
        Account account = new Account();
        account.setName("TEST_ACCOUNT");
        account.setRegion("us-west-2");
        account.setCloud("AWS");
        account.setAccountUrl("https://test-account.snowflakecomputing.com");
        
        assertEquals("TEST_ACCOUNT", account.getName());
        assertEquals("us-west-2", account.getRegion());
        assertEquals("AWS", account.getCloud());
        assertEquals("https://test-account.snowflakecomputing.com", account.getAccountUrl());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        Account account1 = new Account();
        account1.setName("TEST_ACCOUNT");
        account1.setRegion("us-west-2");
        account1.setCloud("AWS");
        
        Account account2 = new Account();
        account2.setName("TEST_ACCOUNT");
        account2.setRegion("us-west-2");
        account2.setCloud("AWS");
        
        Account account3 = new Account();
        account3.setName("DIFFERENT_ACCOUNT");
        account3.setRegion("us-west-2");
        account3.setCloud("AWS");
        
        // Test equals
        assertEquals(account1, account2);
        assertNotEquals(account1, account3);
        assertNotEquals(account1, null);
        assertNotEquals(account1, "string");
        
        // Test hashCode consistency
        assertEquals(account1.hashCode(), account2.hashCode());
        assertNotEquals(account1.hashCode(), account3.hashCode());
    }

    @Test
    void shouldHandleNullPropertiesInEquals() {
        Account account1 = new Account();
        account1.setName("TEST_ACCOUNT");
        // Leave other properties null
        
        Account account2 = new Account();
        account2.setName("TEST_ACCOUNT");
        // Leave other properties null
        
        Account account3 = new Account();
        account3.setName("TEST_ACCOUNT");
        account3.setRegion("us-west-2");
        
        assertEquals(account1, account2);
        assertNotEquals(account1, account3);
    }

    @Test
    void shouldGenerateToString() {
        Account account = new Account();
        account.setName("TEST_ACCOUNT");
        account.setRegion("us-west-2");
        account.setCloud("AWS");
        account.setAccountUrl("https://test-account.snowflakecomputing.com");
        
        String toString = account.toString();
        
        assertTrue(toString.contains("TEST_ACCOUNT"));
        assertTrue(toString.contains("us-west-2"));
        assertTrue(toString.contains("AWS"));
        assertTrue(toString.contains("https://test-account.snowflakecomputing.com"));
    }
}