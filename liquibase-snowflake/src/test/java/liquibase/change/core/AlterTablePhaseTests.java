package liquibase.change.core;

import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.AlterTableStatement;
import liquibase.sqlgenerator.core.snowflake.AlterTableGeneratorSnowflake;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase tests for AlterTable implementation to verify each component works
 */
@DisplayName("AlterTable Phase Tests")
public class AlterTablePhaseTests {
    
    @Test
    @DisplayName("Phase 1: Change Class")
    public void testPhase1_ChangeClass() {
        AlterTableChange change = new AlterTableChange();
        change.setTableName("TEST_TABLE");
        change.setClusterBy("col1,col2");
        
        // Must support Snowflake
        assertTrue(change.supports(new SnowflakeDatabase()));
        
        // Must generate statement
        liquibase.statement.SqlStatement[] stmts = change.generateStatements(new SnowflakeDatabase());
        assertEquals(1, stmts.length);
        assertTrue(stmts[0] instanceof AlterTableStatement);
        
        System.out.println("✅ Phase 1 PASS - Change class works");
    }
    
    @Test
    @DisplayName("Phase 2: Statement")
    public void testPhase2_Statement() {
        AlterTableStatement stmt = new AlterTableStatement(null, null, "TEST_TABLE");
        stmt.setClusterBy("col1,col2");
        assertEquals("col1,col2", stmt.getClusterBy());
        assertEquals("TEST_TABLE", stmt.getTableName());
        System.out.println("✅ Phase 2 PASS - Statement class works");
    }
    
    @Test
    @DisplayName("Phase 3: SQL Generator")
    public void testPhase3_SqlGenerator() {
        AlterTableStatement stmt = new AlterTableStatement(null, null, "TEST_TABLE");
        stmt.setClusterBy("col1,col2");
        
        AlterTableGeneratorSnowflake gen = new AlterTableGeneratorSnowflake();
        Sql[] sqls = gen.generateSql(stmt, new SnowflakeDatabase(), null);
        
        assertTrue(sqls.length > 0);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER TABLE"));
        assertTrue(sql.contains("CLUSTER BY"));
        System.out.println("✅ Phase 3 PASS - SQL: " + sql);
    }
    
    @Test
    @DisplayName("Phase 4: Service Registration")
    public void testPhase4_ServiceRegistration() {
        Change change = ChangeFactory.getInstance().create("alterTable");
        assertNotNull(change);
        assertTrue(change instanceof AlterTableChange);
        System.out.println("✅ Phase 4 PASS - Services registered");
    }
    
    @Test
    @DisplayName("Phase 5: XSD Parsing")
    public void testPhase5_XsdParsing() {
        // Just verify XSD exists - full XML parsing tested later
        java.io.InputStream xsd = getClass().getResourceAsStream(
            "/www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd");
        assertNotNull(xsd);
        System.out.println("✅ Phase 5 PASS - XSD exists");
    }
}