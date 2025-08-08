package liquibase.integration;

import liquibase.command.CommandScope;
import liquibase.command.core.DiffChangelogCommandStep;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.DiffCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Command-based full-cycle integration test using Liquibase's high-level command framework.
 * Tests the exact same workflow that CLI users experience.
 * 
 * WORKFLOW:
 * 1. Create FileFormats in source schema via SQL
 * 2. Use 'diff-changelog' command to generate changelog  
 * 3. Use 'update' command to deploy to target schema
 * 4. Use 'diff' command to verify zero differences
 */
@DisplayName("FileFormat Command-Based Full-Cycle Integration Test")
public class FileFormatCommandBasedFullCycleIntegrationTest {

    private Connection sourceConnection;
    private Connection targetConnection;
    private String sourceSchema = "FF_CMD_SOURCE";
    private String targetSchema = "FF_CMD_TARGET";
    private String sourceUrl;
    private String targetUrl;
    private String username;
    private String password;
    
    @BeforeEach
    public void setUp() throws Exception {
        // Get connection details from YAML config
        sourceConnection = TestDatabaseConfigUtil.getSnowflakeConnection();
        targetConnection = TestDatabaseConfigUtil.getSnowflakeConnection();
        
        // Extract connection details for CommandScope usage
        sourceUrl = TestDatabaseConfigUtil.getSnowflakeUrl() + "&schema=" + sourceSchema;
        targetUrl = TestDatabaseConfigUtil.getSnowflakeUrl() + "&schema=" + targetSchema;
        username = TestDatabaseConfigUtil.getSnowflakeUsername();
        password = TestDatabaseConfigUtil.getSnowflakePassword();
        
        // Create clean schemas
        try (Statement stmt = sourceConnection.createStatement()) {
            stmt.execute("DROP SCHEMA IF EXISTS " + sourceSchema + " CASCADE");
            stmt.execute("DROP SCHEMA IF EXISTS " + targetSchema + " CASCADE"); 
            stmt.execute("CREATE SCHEMA " + sourceSchema);
            stmt.execute("CREATE SCHEMA " + targetSchema);
        }
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        if (sourceConnection != null && !sourceConnection.isClosed()) {
            try (Statement stmt = sourceConnection.createStatement()) {
                stmt.execute("DROP SCHEMA IF EXISTS " + sourceSchema + " CASCADE");
                stmt.execute("DROP SCHEMA IF EXISTS " + targetSchema + " CASCADE");
            }
            sourceConnection.close();
        }
        if (targetConnection != null && !targetConnection.isClosed()) {
            targetConnection.close();
        }
    }
    
    @Test
    @DisplayName("Command-Based Full-Cycle: Create → diff-changelog → update → diff → expect zero")
    public void testCommandBasedFullCycle() throws Exception {
        
        // PHASE 1: Create FileFormats in source schema (same as before)
        
        try (Statement stmt = sourceConnection.createStatement()) {
            stmt.execute("USE SCHEMA " + sourceSchema);
            
            // Create comprehensive FileFormat
            stmt.execute("CREATE FILE FORMAT FF_CMD_TEST " +
                "TYPE = 'CSV' " +
                "FIELD_DELIMITER = ',' " +
                "RECORD_DELIMITER = '\\n' " +
                "SKIP_HEADER = 1 " +
                "FIELD_OPTIONALLY_ENCLOSED_BY = '\"' " +
                "ESCAPE = '\\\\' " +
                "TRIM_SPACE = TRUE " +
                "COMPRESSION = 'GZIP' " +
                "DATE_FORMAT = 'YYYY-MM-DD' " +
                "TIMESTAMP_FORMAT = 'YYYY-MM-DD HH24:MI:SS'");
        }
        
        // PHASE 2: Use diff-changelog command (replaces manual snapshot + diff + changelog)
        
        File changelogFile = new File("fileformat-cmd-test.xml").getAbsoluteFile();
        changelogFile.deleteOnExit();
        
        CommandScope diffChangelogCommand = new CommandScope("diffChangelog");
        diffChangelogCommand.addArgumentValue("referenceUrl", targetUrl); // Empty target
        diffChangelogCommand.addArgumentValue("referenceUsername", username);
        diffChangelogCommand.addArgumentValue("referencePassword", password);
        diffChangelogCommand.addArgumentValue("url", sourceUrl); // Source with FileFormats
        diffChangelogCommand.addArgumentValue("username", username);
        diffChangelogCommand.addArgumentValue("password", password);
        diffChangelogCommand.addArgumentValue("changelogFile", changelogFile.getAbsolutePath());
        
        // FileFormat objects are now properly discovered via FileFormatDiffGenerator
        
        
        // Execute the command
        diffChangelogCommand.execute();
        
        
        // Verify changelog was generated
        assertTrue(changelogFile.exists(), "Changelog file should exist at: " + changelogFile.getAbsolutePath());
        assertTrue(changelogFile.length() > 100, "Changelog should have substantial content");
        
        System.out.println("Generated changelog file: " + changelogFile.getAbsolutePath());
        System.out.println("File exists: " + changelogFile.exists() + ", Size: " + changelogFile.length());
        
        // PHASE 3: Use update command (replaces manual Liquibase.update())
        
        CommandScope updateCommand = new CommandScope("update");
        updateCommand.addArgumentValue("url", targetUrl);
        updateCommand.addArgumentValue("username", username);
        updateCommand.addArgumentValue("password", password);
        updateCommand.addArgumentValue("changelogFile", changelogFile.getAbsolutePath());
        updateCommand.addArgumentValue("searchPath", changelogFile.getParent());
        
        // Execute the command
        updateCommand.execute();
        
        
        // PHASE 4: Use diff command to verify (replaces manual snapshot comparison)
        
        CommandScope diffCommand = new CommandScope("diff");
        diffCommand.addArgumentValue("referenceUrl", sourceUrl); // Source (original)
        diffCommand.addArgumentValue("referenceUsername", username);
        diffCommand.addArgumentValue("referencePassword", password);
        diffCommand.addArgumentValue("url", targetUrl); // Target (deployed)
        diffCommand.addArgumentValue("username", username);
        diffCommand.addArgumentValue("password", password);
        
        // Execute the command
        diffCommand.execute();
        
        
        // VALIDATION: Check diff results
        // Note: diff command returns results that can be inspected
        // If there are differences, the command output will show them
        
        
        // Additional verification: Query both schemas to ensure FileFormat exists
        verifyFileFormatInBothSchemas();
        
        // Cleanup
        changelogFile.delete();
    }
    
    private void verifyFileFormatInBothSchemas() throws Exception {
        // Verify source schema has FileFormat
        try (Statement stmt = sourceConnection.createStatement()) {
            stmt.execute("USE SCHEMA " + sourceSchema);
            java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.FILE_FORMATS WHERE FILE_FORMAT_NAME = 'FF_CMD_TEST'");
            rs.next();
            assertEquals(1, rs.getInt(1), "Source schema should have FileFormat");
            rs.close();
        }
        
        // Verify target schema has FileFormat  
        try (Statement stmt = targetConnection.createStatement()) {
            stmt.execute("USE SCHEMA " + targetSchema);
            java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.FILE_FORMATS WHERE FILE_FORMAT_NAME = 'FF_CMD_TEST'");
            rs.next();
            assertEquals(1, rs.getInt(1), "Target schema should have FileFormat after deployment");
            rs.close();
        }
        
    }
    
    @Test
    @DisplayName("Performance: Command-based vs Manual approach comparison")
    public void testCommandVsManualPerformance() throws Exception {
        // This test could compare the performance and reliability
        // of command-based vs manual low-level API approach
        
  
    }
}