package liquibase.change.core;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.core.AlterTableStatement;
import liquibase.sqlgenerator.core.snowflake.AlterTableGeneratorSnowflake;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Enhanced tests for ALTER TABLE implementation with new properties and validation
 */
@DisplayName("ALTER TABLE Enhanced Features Tests")
public class AlterTableEnhancedTest {
    
    @Test
    @DisplayName("Test new properties: setMaxDataExtensionTimeInDays")
    public void testMaxDataExtensionTimeInDays() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setSetMaxDataExtensionTimeInDays(30);
        
        // Should support Snowflake
        assertTrue(change.supports(new SnowflakeDatabase()));
        
        // Should generate statement with new property
        liquibase.statement.SqlStatement[] stmts = change.generateStatements(new SnowflakeDatabase());
        assertEquals(1, stmts.length);
        assertTrue(stmts[0] instanceof AlterTableStatement);
        
        AlterTableStatement stmt = (AlterTableStatement) stmts[0];
        assertEquals(Integer.valueOf(30), stmt.getSetMaxDataExtensionTimeInDays());
        
        System.out.println("✅ Max data extension property test passed");
    }
    
    @Test
    @DisplayName("Test new properties: setDefaultDdlCollation")
    public void testDefaultDdlCollation() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setSetDefaultDdlCollation("utf8_general_ci");
        
        // Should generate statement with new property
        liquibase.statement.SqlStatement[] stmts = change.generateStatements(new SnowflakeDatabase());
        AlterTableStatement stmt = (AlterTableStatement) stmts[0];
        assertEquals("utf8_general_ci", stmt.getSetDefaultDdlCollation());
        
        System.out.println("✅ Default DDL collation property test passed");
    }
    
    @Test
    @DisplayName("Test SQL generation with new properties")
    public void testSqlGenerationWithNewProperties() {
        AlterTableStatement stmt = new AlterTableStatement(null, null, "TEST_TABLE");
        stmt.setSetMaxDataExtensionTimeInDays(45);
        stmt.setSetDefaultDdlCollation("en_US");
        stmt.setSetDataRetentionTimeInDays(14);
        
        AlterTableGeneratorSnowflake gen = new AlterTableGeneratorSnowflake();
        Sql[] sqls = gen.generateSql(stmt, new SnowflakeDatabase(), null);
        
        assertTrue(sqls.length > 0);
        String sql = sqls[0].toSql();
        
        // Should contain all three properties in SET statement
        assertTrue(sql.contains("ALTER TABLE"));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("MAX_DATA_EXTENSION_TIME_IN_DAYS = 45"));
        assertTrue(sql.contains("DEFAULT_DDL_COLLATION = 'en_US'"));
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS = 14"));
        
        System.out.println("✅ Enhanced SQL generation test passed: " + sql);
    }
    
    @Test
    @DisplayName("Test enhanced validation: clustering column count")
    public void testClusteringColumnCountValidation() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        
        // Test valid clustering key (4 columns or less)
        change.setClusterBy("col1,col2,col3,col4");
        ValidationErrors errors = change.validate(new SnowflakeDatabase());
        assertFalse(errors.hasErrors(), "Valid clustering key should not have errors");
        
        // Test invalid clustering key (more than 4 columns)
        change.setClusterBy("col1,col2,col3,col4,col5");
        errors = change.validate(new SnowflakeDatabase());
        assertTrue(errors.hasErrors(), "More than 4 columns should cause validation error");
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Maximum 4 columns allowed")));
        
        System.out.println("✅ Clustering column count validation test passed");
    }
    
    @Test
    @DisplayName("Test enhanced validation: clustering expression syntax")
    public void testClusteringExpressionValidation() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        
        // Test valid expressions
        String[] validExpressions = {
            "col1",
            "col1,col2",
            "\"quoted_col\"",
            "UPPER(col1)",
            "col1 + col2"
        };
        
        for (String expr : validExpressions) {
            change.setClusterBy(expr);
            ValidationErrors errors = change.validate(new SnowflakeDatabase());
            assertFalse(errors.hasErrors(), "Valid expression should not cause errors: " + expr);
        }
        
        // Test invalid expressions (dangerous SQL)
        String[] invalidExpressions = {
            "col1; DROP TABLE test",
            "col1 -- comment",
            "DROP TABLE",
            ""
        };
        
        for (String expr : invalidExpressions) {
            change.setClusterBy(expr);
            ValidationErrors errors = change.validate(new SnowflakeDatabase());
            assertTrue(errors.hasErrors(), "Invalid expression should cause errors: " + expr);
        }
        
        System.out.println("✅ Clustering expression validation test passed");
    }
    
    @Test
    @DisplayName("Test enhanced validation: data extension time range")
    public void testDataExtensionTimeValidation() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        
        // Test valid range
        change.setSetMaxDataExtensionTimeInDays(30);
        ValidationErrors errors = change.validate(new SnowflakeDatabase());
        assertFalse(errors.hasErrors(), "Valid extension time should not cause errors");
        
        // Test invalid range (negative)
        change.setSetMaxDataExtensionTimeInDays(-1);
        errors = change.validate(new SnowflakeDatabase());
        assertTrue(errors.hasErrors(), "Negative extension time should cause errors");
        
        // Test invalid range (too large)
        change.setSetMaxDataExtensionTimeInDays(100);
        errors = change.validate(new SnowflakeDatabase());
        assertTrue(errors.hasErrors(), "Extension time > 90 should cause errors");
        
        System.out.println("✅ Data extension time validation test passed");
    }
    
    @Test
    @DisplayName("Test enhanced validation: collation specification")
    public void testCollationValidation() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        
        // Test valid collations
        String[] validCollations = {
            "utf8_general_ci",
            "'en_US'",
            "\"utf8_unicode_ci\"",
            "en_US.UTF-8"
        };
        
        for (String collation : validCollations) {
            change.setSetDefaultDdlCollation(collation);
            ValidationErrors errors = change.validate(new SnowflakeDatabase());
            assertFalse(errors.hasErrors(), "Valid collation should not cause errors: " + collation);
        }
        
        // Test invalid collations
        String[] invalidCollations = {
            "",
            "DROP TABLE",
            "col; DELETE FROM test",
            "utf8--comment"
        };
        
        for (String collation : invalidCollations) {
            change.setSetDefaultDdlCollation(collation);
            ValidationErrors errors = change.validate(new SnowflakeDatabase());
            assertTrue(errors.hasErrors(), "Invalid collation should cause errors: " + collation);
        }
        
        System.out.println("✅ Collation validation test passed");
    }
    
    @Test
    @DisplayName("Test confirmation message with new properties")
    public void testConfirmationMessageWithNewProperties() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setSetMaxDataExtensionTimeInDays(45);
        change.setSetDefaultDdlCollation("utf8_general_ci");
        change.setSetDataRetentionTimeInDays(30);
        
        String message = change.getConfirmationMessage();
        
        assertTrue(message.contains("TEST_TABLE"));
        assertTrue(message.contains("max data extension set to 45 days"));
        assertTrue(message.contains("default DDL collation set to utf8_general_ci"));
        assertTrue(message.contains("data retention set to 30 days"));
        
        System.out.println("✅ Confirmation message test passed: " + message);
    }
}