package liquibase.executor;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.exception.DatabaseException;
import liquibase.statement.core.RawSqlStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Tests for LoggingExecutor credential obfuscation functionality
 */
public class LoggingExecutorTest {

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

    @Test
    void testObfuscateAWSKeyId() throws DatabaseException {
        String sqlWithCredentials = "CREATE STAGE my_stage CREDENTIALS = ( AWS_KEY_ID = 'AKIAIOSFODNN7EXAMPLE' AWS_SECRET_KEY = 'wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY' );";
        
        loggingExecutor.execute(new RawSqlStatement(sqlWithCredentials));
        
        String output = outputWriter.toString();
        assertFalse(output.contains("AKIAIOSFODNN7EXAMPLE"), "AWS_KEY_ID should be obfuscated");
        assertFalse(output.contains("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"), "AWS_SECRET_KEY should be obfuscated");
        assertTrue(output.contains("AWS_KEY_ID = '*****'"), "AWS_KEY_ID should be replaced with asterisks");
        assertTrue(output.contains("AWS_SECRET_KEY = '*****'"), "AWS_SECRET_KEY should be replaced with asterisks");
    }

    @Test
    void testObfuscateAWSToken() throws DatabaseException {
        String sqlWithCredentials = "ALTER STAGE my_stage SET CREDENTIALS = ( AWS_KEY_ID = 'AKIAIOSFODNN7EXAMPLE' AWS_SECRET_KEY = 'wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY' AWS_TOKEN = 'AQoDYXdzEJr...' );";
        
        loggingExecutor.execute(new RawSqlStatement(sqlWithCredentials));
        
        String output = outputWriter.toString();
        assertFalse(output.contains("AQoDYXdzEJr..."), "AWS_TOKEN should be obfuscated");
        assertTrue(output.contains("AWS_TOKEN = '*****'"), "AWS_TOKEN should be replaced with asterisks");
    }

    @Test
    void testObfuscateAzureSasToken() throws DatabaseException {
        String sqlWithCredentials = "CREATE STAGE my_azure_stage CREDENTIALS = ( AZURE_SAS_TOKEN = 'sp=r&st=2021-01-01T00:00:00Z&se=2021-12-31T23:59:59Z&spr=https&sv=2020-08-04&sr=c&sig=example123' );";
        
        loggingExecutor.execute(new RawSqlStatement(sqlWithCredentials));
        
        String output = outputWriter.toString();
        assertFalse(output.contains("sp=r&st=2021-01-01T00:00:00Z&se=2021-12-31T23:59:59Z&spr=https&sv=2020-08-04&sr=c&sig=example123"), "AZURE_SAS_TOKEN should be obfuscated");
        assertTrue(output.contains("AZURE_SAS_TOKEN = '*****'"), "AZURE_SAS_TOKEN should be replaced with asterisks");
    }

    @Test
    void testCaseInsensitiveObfuscation() throws DatabaseException {
        String sqlWithCredentials = "CREATE STAGE my_stage credentials = ( aws_key_id = 'AKIAIOSFODNN7EXAMPLE' aws_secret_key = 'wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY' );";
        
        loggingExecutor.execute(new RawSqlStatement(sqlWithCredentials));
        
        String output = outputWriter.toString();
        assertFalse(output.contains("AKIAIOSFODNN7EXAMPLE"), "AWS_KEY_ID should be obfuscated regardless of case");
        assertFalse(output.contains("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"), "AWS_SECRET_KEY should be obfuscated regardless of case");
        assertTrue(output.contains("aws_key_id = '*****'"), "Lowercase credentials should be replaced with asterisks");
        assertTrue(output.contains("aws_secret_key = '*****'"), "Lowercase credentials should be replaced with asterisks");
    }

    @Test
    void testMixedQuotesObfuscation() throws DatabaseException {
        String sqlWithCredentials = "CREATE STAGE my_stage CREDENTIALS = ( AWS_KEY_ID = \"AKIAIOSFODNN7EXAMPLE\" AWS_SECRET_KEY = 'wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY' );";
        
        loggingExecutor.execute(new RawSqlStatement(sqlWithCredentials));
        
        String output = outputWriter.toString();
        assertFalse(output.contains("AKIAIOSFODNN7EXAMPLE"), "AWS_KEY_ID with double quotes should be obfuscated");
        assertFalse(output.contains("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"), "AWS_SECRET_KEY with single quotes should be obfuscated");
        assertTrue(output.contains("AWS_KEY_ID = \"*****\""), "Double-quoted credentials should preserve quote type");
        assertTrue(output.contains("AWS_SECRET_KEY = '*****'"), "Single-quoted credentials should preserve quote type");
    }

    @Test
    void testNonStageStatementUnmodified() throws DatabaseException {
        String sqlWithoutCredentials = "CREATE TABLE users (id INT, name VARCHAR(50));";
        
        loggingExecutor.execute(new RawSqlStatement(sqlWithoutCredentials));
        
        String output = outputWriter.toString();
        assertTrue(output.contains("CREATE TABLE users (id INT, name VARCHAR(50));"), "Non-STAGE statements should not be modified");
    }

    @Test
    void testWhitespaceHandling() throws DatabaseException {
        String sqlWithCredentials = "CREATE STAGE my_stage CREDENTIALS = ( AWS_KEY_ID  =  'AKIAIOSFODNN7EXAMPLE' AWS_SECRET_KEY=  'wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY' );";
        
        loggingExecutor.execute(new RawSqlStatement(sqlWithCredentials));
        
        String output = outputWriter.toString();
        assertFalse(output.contains("AKIAIOSFODNN7EXAMPLE"), "AWS_KEY_ID with extra whitespace should be obfuscated");
        assertFalse(output.contains("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"), "AWS_SECRET_KEY with extra whitespace should be obfuscated");
    }

    @Test
    void testNullStatementHandling() throws DatabaseException {
        // Test that null statements don't cause exceptions
        loggingExecutor.execute(new RawSqlStatement(null));
        
        String output = outputWriter.toString();
        // Should not throw exception and output should be handled gracefully
    }

    @Test
    void testEmptyCredentialValues() throws DatabaseException {
        String sqlWithEmptyCredentials = "CREATE STAGE my_stage CREDENTIALS = ( AWS_KEY_ID = '' AWS_SECRET_KEY = \"\" );";
        
        loggingExecutor.execute(new RawSqlStatement(sqlWithEmptyCredentials));
        
        String output = outputWriter.toString();
        assertTrue(output.contains("AWS_KEY_ID = '*****'"), "Empty AWS_KEY_ID should still be obfuscated");
        assertTrue(output.contains("AWS_SECRET_KEY = \"*****\""), "Empty AWS_SECRET_KEY should still be obfuscated");
    }

    @Test
    void testMultilineStatements() throws DatabaseException {
        String multilineSQL = "CREATE STAGE my_stage\n" +
                             "CREDENTIALS = (\n" +
                             "    AWS_KEY_ID = 'AKIAIOSFODNN7EXAMPLE'\n" +
                             "    AWS_SECRET_KEY = 'wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY'\n" +
                             ");";
        
        loggingExecutor.execute(new RawSqlStatement(multilineSQL));
        
        String output = outputWriter.toString();
        assertFalse(output.contains("AKIAIOSFODNN7EXAMPLE"), "Multiline AWS_KEY_ID should be obfuscated");
        assertFalse(output.contains("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"), "Multiline AWS_SECRET_KEY should be obfuscated");
    }

    @Test
    void testMixedCredentialTypes() throws DatabaseException {
        String sqlWithMixedCredentials = "ALTER STAGE my_stage SET CREDENTIALS = ( AWS_KEY_ID = 'aws123' AZURE_SAS_TOKEN = 'azure456' AWS_TOKEN = 'token789' );";
        
        loggingExecutor.execute(new RawSqlStatement(sqlWithMixedCredentials));
        
        String output = outputWriter.toString();
        assertFalse(output.contains("aws123"), "AWS_KEY_ID in mixed statement should be obfuscated");
        assertFalse(output.contains("azure456"), "AZURE_SAS_TOKEN in mixed statement should be obfuscated");
        assertFalse(output.contains("token789"), "AWS_TOKEN in mixed statement should be obfuscated");
        assertTrue(output.contains("AWS_KEY_ID = '*****'"), "AWS_KEY_ID should be replaced with asterisks");
        assertTrue(output.contains("AZURE_SAS_TOKEN = '*****'"), "AZURE_SAS_TOKEN should be replaced with asterisks");
        assertTrue(output.contains("AWS_TOKEN = '*****'"), "AWS_TOKEN should be replaced with asterisks");
    }

    @Test
    void testSpecialCharactersInCredentials() throws DatabaseException {
        String sqlWithSpecialChars = "CREATE STAGE my_stage CREDENTIALS = ( AWS_KEY_ID = 'KEY/+==ABC123' AWS_SECRET_KEY = 'SECRET/+==XYZ789' );";
        
        loggingExecutor.execute(new RawSqlStatement(sqlWithSpecialChars));
        
        String output = outputWriter.toString();
        assertFalse(output.contains("KEY/+==ABC123"), "AWS_KEY_ID with special characters should be obfuscated");
        assertFalse(output.contains("SECRET/+==XYZ789"), "AWS_SECRET_KEY with special characters should be obfuscated");
    }

    @Test
    void testCredentialsInComments() throws DatabaseException {
        String sqlWithCommentedCredentials = "-- Example: AWS_KEY_ID = 'AKIAIOSFODNN7EXAMPLE'\nCREATE STAGE my_stage;";
        
        loggingExecutor.execute(new RawSqlStatement(sqlWithCommentedCredentials));
        
        String output = outputWriter.toString();
        assertTrue(output.contains("AKIAIOSFODNN7EXAMPLE"), "Credentials in comments should NOT be obfuscated");
    }

    @Test
    void testPartialCredentialMatches() throws DatabaseException {
        // These should NOT be obfuscated as they don't match exact patterns
        String sqlWithPartialMatches = "CREATE STAGE my_stage SET MY_AWS_KEY_ID = 'should_not_obfuscate' AWS_KEY_ID_SUFFIX = 'also_not_obfuscate';";
        
        loggingExecutor.execute(new RawSqlStatement(sqlWithPartialMatches));
        
        String output = outputWriter.toString();
        assertTrue(output.contains("should_not_obfuscate"), "Partial matches should NOT be obfuscated");
        assertTrue(output.contains("also_not_obfuscate"), "Partial matches should NOT be obfuscated");
    }

    @Test
    void testMalformedCredentialPatterns() throws DatabaseException {
        // Test credentials without proper quotes or formatting
        String malformedSQL = "CREATE STAGE my_stage CREDENTIALS = ( AWS_KEY_ID = AKIAIOSFODNN7EXAMPLE AWS_SECRET_KEY = wJalrXUtnFEMI );";
        
        loggingExecutor.execute(new RawSqlStatement(malformedSQL));
        
        String output = outputWriter.toString();
        // Malformed credentials without quotes should not be obfuscated by our patterns
        assertTrue(output.contains("AKIAIOSFODNN7EXAMPLE"), "Unquoted credentials should not be obfuscated");
        assertTrue(output.contains("wJalrXUtnFEMI"), "Unquoted credentials should not be obfuscated");
    }

    @Test
    void testMultipleCredentialBlocks() throws DatabaseException {
        String sqlWithMultipleBlocks = "CREATE STAGE stage1 CREDENTIALS = ( AWS_KEY_ID = 'key1' ); CREATE STAGE stage2 CREDENTIALS = ( AWS_SECRET_KEY = 'secret2' );";
        
        loggingExecutor.execute(new RawSqlStatement(sqlWithMultipleBlocks));
        
        String output = outputWriter.toString();
        assertFalse(output.contains("key1"), "First credential block should be obfuscated");
        assertFalse(output.contains("secret2"), "Second credential block should be obfuscated");
        assertTrue(output.contains("AWS_KEY_ID = '*****'"), "First AWS_KEY_ID should be replaced");
        assertTrue(output.contains("AWS_SECRET_KEY = '*****'"), "Second AWS_SECRET_KEY should be replaced");
    }

    @Test
    void testMultiLineComments() throws DatabaseException {
        String sqlWithMultiLineComments = "/* This is a multi-line comment\n" +
                                         "   AWS_KEY_ID = 'should_not_be_obfuscated'\n" +
                                         "   AWS_SECRET_KEY = 'also_should_not_be_obfuscated'\n" +
                                         "*/\n" +
                                         "CREATE STAGE my_stage CREDENTIALS = ( AWS_KEY_ID = 'should_be_obfuscated' );";
        
        loggingExecutor.execute(new RawSqlStatement(sqlWithMultiLineComments));
        
        String output = outputWriter.toString();
        assertTrue(output.contains("should_not_be_obfuscated"), "Credentials in multi-line comments should NOT be obfuscated");
        assertTrue(output.contains("also_should_not_be_obfuscated"), "Credentials in multi-line comments should NOT be obfuscated");
        assertFalse(output.contains("should_be_obfuscated"), "Actual credentials should be obfuscated");
        assertTrue(output.contains("AWS_KEY_ID = '*****'"), "Actual AWS_KEY_ID should be replaced with asterisks");
    }
}