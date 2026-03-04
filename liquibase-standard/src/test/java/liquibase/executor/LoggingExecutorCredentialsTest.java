package liquibase.executor;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.exception.DatabaseException;
import liquibase.statement.core.RawSqlStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Comprehensive tests for LoggingExecutor credential obfuscation functionality
 */
public class LoggingExecutorCredentialsTest {

    private LoggingExecutor loggingExecutor;
    private StringWriter outputWriter;
    private Database database;

    @BeforeEach
    void setUp() {
        outputWriter = new StringWriter();
        database = new H2Database();
        Executor mockDelegatedExecutor = mock(Executor.class);
        loggingExecutor = new LoggingExecutor(mockDelegatedExecutor, outputWriter, database);
    }

    // ===== BASIC CREDENTIAL OBFUSCATION TESTS =====

    @Test
    void testObfuscateAllCredentialTypes() throws DatabaseException {
        String sqlWithCredentials = "CREATE STAGE my_stage CREDENTIALS = ( " +
                                   "AWS_KEY_ID = 'AKIAIOSFODNN7EXAMPLE', " +
                                   "AWS_SECRET_KEY = 'wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY', " +
                                   "AWS_TOKEN = 'AQoDYXdzEJr...', " +
                                   "AZURE_SAS_TOKEN = 'sp=r&st=2021-01-01T00:00:00Z&se=2021-12-31T23:59:59Z' " +
                                   ");";
        
        loggingExecutor.execute(new RawSqlStatement(sqlWithCredentials));
        String output = outputWriter.toString();
        
        // Verify credentials are obfuscated
        assertFalse(output.contains("AKIAIOSFODNN7EXAMPLE"), "AWS_KEY_ID should be obfuscated");
        assertFalse(output.contains("wJalrXUtnFEMI/K7MDENG"), "AWS_SECRET_KEY should be obfuscated");
        assertFalse(output.contains("AQoDYXdzEJr"), "AWS_TOKEN should be obfuscated");
        assertFalse(output.contains("sp=r&st=2021-01-01T00:00:00Z"), "AZURE_SAS_TOKEN should be obfuscated");
        
        // Verify proper replacement
        assertTrue(output.contains("AWS_KEY_ID = '*****'"), "AWS_KEY_ID should be replaced with asterisks");
        assertTrue(output.contains("AWS_SECRET_KEY = '*****'"), "AWS_SECRET_KEY should be replaced with asterisks");
        assertTrue(output.contains("AWS_TOKEN = '*****'"), "AWS_TOKEN should be replaced with asterisks");
        assertTrue(output.contains("AZURE_SAS_TOKEN = '*****'"), "AZURE_SAS_TOKEN should be replaced with asterisks");
    }

    @Test
    void testCaseInsensitiveObfuscation() throws DatabaseException {
        String sqlWithCredentials = "create stage my_stage credentials = ( " +
                                   "aws_key_id = 'AKIAIOSFODNN7EXAMPLE', " +
                                   "AWS_SECRET_KEY = 'wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY' " +
                                   ");";
        
        loggingExecutor.execute(new RawSqlStatement(sqlWithCredentials));
        String output = outputWriter.toString();
        
        assertFalse(output.contains("AKIAIOSFODNN7EXAMPLE"), "Case insensitive AWS_KEY_ID should be obfuscated");
        assertFalse(output.contains("wJalrXUtnFEMI/K7MDENG"), "Case insensitive AWS_SECRET_KEY should be obfuscated");
        assertTrue(output.contains("aws_key_id = '*****'"), "Lowercase parameter should be preserved");
    }

    @Test
    void testQuoteHandling() throws DatabaseException {
        String sqlWithCredentials = "CREATE STAGE my_stage CREDENTIALS = ( " +
                                   "AWS_KEY_ID = 'single_quotes_credential', " +
                                   "AWS_SECRET_KEY = \"double_quotes_credential\" " +
                                   ");";
        
        loggingExecutor.execute(new RawSqlStatement(sqlWithCredentials));
        String output = outputWriter.toString();
        
        assertFalse(output.contains("single_quotes_credential"), "Single quoted credential should be obfuscated");
        assertFalse(output.contains("double_quotes_credential"), "Double quoted credential should be obfuscated");
        assertTrue(output.contains("AWS_KEY_ID = '*****'"), "Single quotes should be preserved");
        assertTrue(output.contains("AWS_SECRET_KEY = \"*****\""), "Double quotes should be preserved");
    }

    @Test
    void testWhitespaceVariations() throws DatabaseException {
        String sqlWithCredentials = "CREATE STAGE my_stage CREDENTIALS=(AWS_KEY_ID='AKIAIOSFODNN7EXAMPLE'  ,   AWS_SECRET_KEY  =  'wJalrXUtnFEMI/K7MDENG'   );";
        
        loggingExecutor.execute(new RawSqlStatement(sqlWithCredentials));
        String output = outputWriter.toString();
        
        assertFalse(output.contains("AKIAIOSFODNN7EXAMPLE"), "Credential with no spaces should be obfuscated");
        assertFalse(output.contains("wJalrXUtnFEMI/K7MDENG"), "Credential with extra spaces should be obfuscated");
    }

    @Test
    void testWordBoundaryPrecision() throws DatabaseException {
        String boundarySQL = "CREATE STAGE boundary_test " +
                            "CREDENTIALS = ( " +
                            "  PREFIX_AWS_KEY_ID = 'should_not_match', " +
                            "  AWS_KEY_ID = 'should_match', " +
                            "  AWS_KEY_ID_SUFFIX = 'should_not_match', " +
                            "  EMBEDDED_AWS_SECRET_KEY = 'should_not_match' " +
                            ");";
        
        loggingExecutor.execute(new RawSqlStatement(boundarySQL));
        String output = outputWriter.toString();
        
        // Only exact matches should be obfuscated
        assertTrue(output.contains("PREFIX_AWS_KEY_ID = 'should_not_match'"), "Prefixed variants should not match");
        assertTrue(output.contains("AWS_KEY_ID_SUFFIX = 'should_not_match'"), "Suffixed variants should not match");
        assertTrue(output.contains("EMBEDDED_AWS_SECRET_KEY = 'should_not_match'"), "Embedded variants should not match");
        
        // Exact match should be obfuscated
        assertFalse(output.contains("should_match"), "Exact match should be obfuscated");
        assertTrue(output.contains("AWS_KEY_ID = '*****'"), "Exact match should show obfuscation marker");
    }

    // ===== EDGE CASES AND SECURITY TESTS =====

    @Test
    void testMultipleCredentialsBlocks() throws DatabaseException {
        String multiBlockSQL = "CREATE STAGE stage1 CREDENTIALS = ( AWS_KEY_ID = 'secret1' ); " +
                              "CREATE STAGE stage2 CREDENTIALS = ( AWS_SECRET_KEY = 'secret2' ); " +
                              "ALTER STAGE stage3 SET CREDENTIALS = ( AWS_TOKEN = 'secret3' );";
        
        loggingExecutor.execute(new RawSqlStatement(multiBlockSQL));
        String output = outputWriter.toString();
        
        // All credentials should be obfuscated
        assertFalse(output.contains("secret1"), "First block credential should be obfuscated");
        assertFalse(output.contains("secret2"), "Second block credential should be obfuscated");
        assertFalse(output.contains("secret3"), "Third block credential should be obfuscated");
        
        // Should have multiple obfuscation markers
        long markerCount = output.chars().filter(ch -> ch == '*').count() / 5;
        assertTrue(markerCount >= 3, "Should have at least 3 obfuscation markers");
    }

    @Test
    void testInternationalCharacters() throws DatabaseException {
        String internationalSQL = "CREATE STAGE international_stage CREDENTIALS = ( " +
                                 "AWS_KEY_ID = 'ключ_密钥_café', " +
                                 "AWS_SECRET_KEY = 'pässwörd_naïve' " +
                                 ");";
        
        loggingExecutor.execute(new RawSqlStatement(internationalSQL));
        String output = outputWriter.toString();
        
        // International characters should be obfuscated
        assertFalse(output.contains("ключ"), "Cyrillic should be obfuscated");
        assertFalse(output.contains("密钥"), "Chinese should be obfuscated");
        assertFalse(output.contains("café"), "Accented characters should be obfuscated");
        assertFalse(output.contains("pässwörd"), "German umlauts should be obfuscated");
        assertFalse(output.contains("naïve"), "French accents should be obfuscated");
    }

    @Test
    void testPerformanceWithLargeCredentials() throws DatabaseException {
        StringBuilder largeCredential = new StringBuilder("CREATE STAGE perf_stage CREDENTIALS = ( AWS_KEY_ID = '");
        for (int i = 0; i < 1000; i++) {
            largeCredential.append("A");
        }
        largeCredential.append("' );");
        
        long startTime = System.currentTimeMillis();
        loggingExecutor.execute(new RawSqlStatement(largeCredential.toString()));
        long endTime = System.currentTimeMillis();
        
        long executionTime = endTime - startTime;
        assertTrue(executionTime < 500, "Should handle large credentials efficiently");
        
        String output = outputWriter.toString();
        assertFalse(output.contains("AAAAAAAAAA"), "Large credential should be obfuscated");
        assertTrue(output.contains("AWS_KEY_ID = '*****'"), "Should show obfuscation marker");
    }

    // ===== NON-CREDENTIALS CONTEXT TESTS =====

    @Test
    void testNoObfuscationOutsideCredentialsBlocks() throws DatabaseException {
        String nonCredentialsSQL = "INSERT INTO config VALUES ('AWS_KEY_ID', 'should_not_be_obfuscated'); " +
                                  "SELECT 'AWS_SECRET_KEY' as param; " +
                                  "CREATE TABLE test (aws_token VARCHAR(100));";
        
        loggingExecutor.execute(new RawSqlStatement(nonCredentialsSQL));
        String output = outputWriter.toString();
        
        // Should not obfuscate anything outside CREDENTIALS blocks
        assertTrue(output.contains("'should_not_be_obfuscated'"), "INSERT values should not be obfuscated");
        assertTrue(output.contains("'AWS_SECRET_KEY'"), "SELECT literals should not be obfuscated");
        assertTrue(output.contains("aws_token"), "Table columns should not be obfuscated");
        
        // Should not contain any obfuscation markers
        assertFalse(output.contains("*****"), "Should not contain obfuscation markers");
    }

    @Test
    void testNonCredentialSQL() throws DatabaseException {
        // Test SQL with no credentials
        loggingExecutor.execute(new RawSqlStatement("SELECT 1"));
        String output = outputWriter.toString();
        assertTrue(output.contains("SELECT 1"), "Non-credential SQL should pass through unchanged");
        assertFalse(output.contains("*****"), "No obfuscation markers should be present");
    }

    @Test
    void testComplexCredentialValues() throws DatabaseException {
        String complexSQL = "CREATE STAGE complex_stage CREDENTIALS = ( " +
                           "AWS_KEY_ID = 'AKIA_SPECIAL_CHARS_123', " +
                           "AWS_SECRET_KEY = 'secret_with_slashes_and_underscores', " +
                           "AZURE_SAS_TOKEN = 'token_with_query_params_values' " +
                           ");";
        
        loggingExecutor.execute(new RawSqlStatement(complexSQL));
        String output = outputWriter.toString();
        
        // Complex values should be obfuscated
        assertFalse(output.contains("AKIA_SPECIAL_CHARS_123"), "Special characters should be obfuscated");
        assertFalse(output.contains("secret_with_slashes"), "Complex values should be obfuscated");
        assertFalse(output.contains("token_with_query_params"), "Query-like parameters should be obfuscated");
        
        // Standard markers should be present
        assertTrue(output.contains("AWS_KEY_ID = '*****'"), "Should show obfuscation marker");
        assertTrue(output.contains("AWS_SECRET_KEY = '*****'"), "Should show obfuscation marker");
        assertTrue(output.contains("AZURE_SAS_TOKEN = '*****'"), "Should show obfuscation marker");
    }

    @Test
    void testEncryptionBlockObfuscation() throws DatabaseException {
        String sqlWithEncryption = "CREATE STAGE encrypted_stage " +
                                  "CREDENTIALS = ( AWS_KEY_ID = 'aws_credential' ) " +
                                  "ENCRYPTION = ( MASTER_KEY = 'super_secret_master_key_123' );";
        
        loggingExecutor.execute(new RawSqlStatement(sqlWithEncryption));
        String output = outputWriter.toString();
        
        // Both credentials and encryption should be obfuscated
        assertFalse(output.contains("aws_credential"), "AWS credential should be obfuscated");
        assertFalse(output.contains("super_secret_master_key_123"), "Master key should be obfuscated");
        
        // Proper obfuscation markers should be present
        assertTrue(output.contains("CREDENTIALS = ( AWS_KEY_ID = '*****' )"), "CREDENTIALS block should be obfuscated");
        assertTrue(output.contains("ENCRYPTION = ( MASTER_KEY = '*****' )"), "ENCRYPTION block should be obfuscated");
    }
}
