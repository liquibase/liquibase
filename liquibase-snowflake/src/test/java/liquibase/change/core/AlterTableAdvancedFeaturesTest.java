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
 * Unit tests for advanced ALTER TABLE features including search optimization,
 * row access policies, aggregation policies, projection policies, and tag management.
 */
@DisplayName("ALTER TABLE Advanced Features Tests")
public class AlterTableAdvancedFeaturesTest {
    
    @Test
    @DisplayName("Test search optimization: addSearchOptimization")
    public void testAddSearchOptimization() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setAddSearchOptimization("GEO");
        
        assertTrue(change.supports(new SnowflakeDatabase()));
        
        liquibase.statement.SqlStatement[] stmts = change.generateStatements(new SnowflakeDatabase());
        assertEquals(1, stmts.length);
        assertTrue(stmts[0] instanceof AlterTableStatement);
        
        AlterTableStatement stmt = (AlterTableStatement) stmts[0];
        assertEquals("GEO", stmt.getAddSearchOptimization());
        
    }
    
    @Test
    @DisplayName("Test search optimization: dropSearchOptimization")
    public void testDropSearchOptimization() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setDropSearchOptimization(true);
        
        liquibase.statement.SqlStatement[] stmts = change.generateStatements(new SnowflakeDatabase());
        AlterTableStatement stmt = (AlterTableStatement) stmts[0];
        assertEquals(Boolean.TRUE, stmt.getDropSearchOptimization());
        
    }
    
    @Test
    @DisplayName("Test row access policies: addRowAccessPolicy")
    public void testAddRowAccessPolicy() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setAddRowAccessPolicy("my_policy ON (user_id, department)");
        
        liquibase.statement.SqlStatement[] stmts = change.generateStatements(new SnowflakeDatabase());
        AlterTableStatement stmt = (AlterTableStatement) stmts[0];
        assertEquals("my_policy ON (user_id, department)", stmt.getAddRowAccessPolicy());
        
    }
    
    @Test
    @DisplayName("Test row access policies: dropRowAccessPolicy")
    public void testDropRowAccessPolicy() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setDropRowAccessPolicy("my_policy");
        
        liquibase.statement.SqlStatement[] stmts = change.generateStatements(new SnowflakeDatabase());
        AlterTableStatement stmt = (AlterTableStatement) stmts[0];
        assertEquals("my_policy", stmt.getDropRowAccessPolicy());
        
    }
    
    @Test
    @DisplayName("Test aggregation policies: setAggregationPolicy")
    public void testSetAggregationPolicy() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setSetAggregationPolicy("my_agg_policy");
        
        liquibase.statement.SqlStatement[] stmts = change.generateStatements(new SnowflakeDatabase());
        AlterTableStatement stmt = (AlterTableStatement) stmts[0];
        assertEquals("my_agg_policy", stmt.getSetAggregationPolicy());
        
    }
    
    @Test
    @DisplayName("Test aggregation policies: setAggregationPolicy with force")
    public void testSetAggregationPolicyWithForce() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setSetAggregationPolicy("my_agg_policy");
        change.setForceAggregationPolicy(true);
        
        liquibase.statement.SqlStatement[] stmts = change.generateStatements(new SnowflakeDatabase());
        AlterTableStatement stmt = (AlterTableStatement) stmts[0];
        assertEquals("my_agg_policy", stmt.getSetAggregationPolicy());
        assertEquals(Boolean.TRUE, stmt.getForceAggregationPolicy());
        
    }
    
    @Test
    @DisplayName("Test aggregation policies: unsetAggregationPolicy")
    public void testUnsetAggregationPolicy() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setUnsetAggregationPolicy(true);
        
        liquibase.statement.SqlStatement[] stmts = change.generateStatements(new SnowflakeDatabase());
        AlterTableStatement stmt = (AlterTableStatement) stmts[0];
        assertEquals(Boolean.TRUE, stmt.getUnsetAggregationPolicy());
        
    }
    
    @Test
    @DisplayName("Test projection policies: setProjectionPolicy")
    public void testSetProjectionPolicy() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setSetProjectionPolicy("my_proj_policy");
        
        liquibase.statement.SqlStatement[] stmts = change.generateStatements(new SnowflakeDatabase());
        AlterTableStatement stmt = (AlterTableStatement) stmts[0];
        assertEquals("my_proj_policy", stmt.getSetProjectionPolicy());
        
    }
    
    @Test
    @DisplayName("Test projection policies: setProjectionPolicy with force")
    public void testSetProjectionPolicyWithForce() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setSetProjectionPolicy("my_proj_policy");
        change.setForceProjectionPolicy(true);
        
        liquibase.statement.SqlStatement[] stmts = change.generateStatements(new SnowflakeDatabase());
        AlterTableStatement stmt = (AlterTableStatement) stmts[0];
        assertEquals("my_proj_policy", stmt.getSetProjectionPolicy());
        assertEquals(Boolean.TRUE, stmt.getForceProjectionPolicy());
        
    }
    
    @Test
    @DisplayName("Test projection policies: unsetProjectionPolicy")
    public void testUnsetProjectionPolicy() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setUnsetProjectionPolicy(true);
        
        liquibase.statement.SqlStatement[] stmts = change.generateStatements(new SnowflakeDatabase());
        AlterTableStatement stmt = (AlterTableStatement) stmts[0];
        assertEquals(Boolean.TRUE, stmt.getUnsetProjectionPolicy());
        
    }
    
    @Test
    @DisplayName("Test tag management: setTag")
    public void testSetTag() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setSetTag("env = 'production', department = 'engineering'");
        
        liquibase.statement.SqlStatement[] stmts = change.generateStatements(new SnowflakeDatabase());
        AlterTableStatement stmt = (AlterTableStatement) stmts[0];
        assertEquals("env = 'production', department = 'engineering'", stmt.getSetTag());
        
    }
    
    @Test
    @DisplayName("Test tag management: unsetTag")
    public void testUnsetTag() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setUnsetTag("env, department, owner");
        
        liquibase.statement.SqlStatement[] stmts = change.generateStatements(new SnowflakeDatabase());
        AlterTableStatement stmt = (AlterTableStatement) stmts[0];
        assertEquals("env, department, owner", stmt.getUnsetTag());
        
    }
    
    @Test
    @DisplayName("Test SQL generation with search optimization")
    public void testSqlGenerationSearchOptimization() {
        AlterTableStatement stmt = new AlterTableStatement(null, null, "TEST_TABLE");
        stmt.setAddSearchOptimization("GEO");
        
        AlterTableGeneratorSnowflake gen = new AlterTableGeneratorSnowflake();
        Sql[] sqls = gen.generateSql(stmt, new SnowflakeDatabase(), null);
        
        assertTrue(sqls.length > 0);
        String sql = sqls[0].toSql();
        
        assertTrue(sql.startsWith("ALTER TABLE"), "SQL should start with ALTER TABLE: " + sql);
        assertTrue(sql.contains("ADD SEARCH OPTIMIZATION ON GEO"), "SQL should contain search optimization clause: " + sql);
        
    }
    
    @Test
    @DisplayName("Test SQL generation with row access policy")
    public void testSqlGenerationRowAccessPolicy() {
        AlterTableStatement stmt = new AlterTableStatement(null, null, "TEST_TABLE");
        stmt.setAddRowAccessPolicy("security_policy ON (user_role)");
        
        AlterTableGeneratorSnowflake gen = new AlterTableGeneratorSnowflake();
        Sql[] sqls = gen.generateSql(stmt, new SnowflakeDatabase(), null);
        
        assertTrue(sqls.length > 0);
        String sql = sqls[0].toSql();
        
        assertTrue(sql.startsWith("ALTER TABLE"), "SQL should start with ALTER TABLE: " + sql);
        assertTrue(sql.contains("ADD ROW ACCESS POLICY security_policy ON (user_role)"), "SQL should contain row access policy clause: " + sql);
        
    }
    
    @Test
    @DisplayName("Test SQL generation with aggregation policy and force")
    public void testSqlGenerationAggregationPolicyWithForce() {
        AlterTableStatement stmt = new AlterTableStatement(null, null, "TEST_TABLE");
        stmt.setSetAggregationPolicy("data_policy");
        stmt.setForceAggregationPolicy(true);
        
        AlterTableGeneratorSnowflake gen = new AlterTableGeneratorSnowflake();
        Sql[] sqls = gen.generateSql(stmt, new SnowflakeDatabase(), null);
        
        assertTrue(sqls.length > 0);
        String sql = sqls[0].toSql();
        
        assertTrue(sql.startsWith("ALTER TABLE"), "SQL should start with ALTER TABLE: " + sql);
        assertTrue(sql.contains("SET AGGREGATION POLICY data_policy FORCE"), "SQL should contain forced aggregation policy clause: " + sql);
        
    }
    
    @Test
    @DisplayName("Test SQL generation with tag operations")
    public void testSqlGenerationTagOperations() {
        AlterTableStatement stmt = new AlterTableStatement(null, null, "TEST_TABLE");
        stmt.setSetTag("cost_center = 'engineering', env = 'prod'");
        
        AlterTableGeneratorSnowflake gen = new AlterTableGeneratorSnowflake();
        Sql[] sqls = gen.generateSql(stmt, new SnowflakeDatabase(), null);
        
        assertTrue(sqls.length > 0);
        String sql = sqls[0].toSql();
        
        assertTrue(sql.startsWith("ALTER TABLE"), "SQL should start with ALTER TABLE: " + sql);
        // Check for TAG SET operations with specific content
        assertTrue(sql.contains("TAG") && sql.contains("cost_center") && sql.contains("engineering"), 
                   "SQL should contain TAG operations with cost_center and engineering");
        
    }
    
    @Test
    @DisplayName("Test validation with advanced features")
    public void testValidationWithAdvancedFeatures() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setAddSearchOptimization("EQUALITY");
        
        ValidationErrors errors = change.validate(new SnowflakeDatabase());
        assertFalse(errors.hasErrors(), "Valid search optimization should not have errors");
        
    }
    
    @Test
    @DisplayName("Test confirmation message with advanced features")
    public void testConfirmationMessageWithAdvancedFeatures() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setAddSearchOptimization("GEO");
        change.setSetTag("env = 'test'");
        change.setSetAggregationPolicy("my_policy");
        change.setForceAggregationPolicy(true);
        
        String message = change.getConfirmationMessage();
        
        assertTrue(message.contains("TEST_TABLE"));
        assertTrue(message.contains("search optimization added"));
        assertTrue(message.contains("tags set: env = 'test'"));
        assertTrue(message.contains("aggregation policy set to my_policy (forced)"));
        
    }
}