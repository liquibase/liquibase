package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.Stage;
import liquibase.snapshot.jvm.JdbcSnapshotGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StageSnapshotGeneratorSnowflake.
 * ✅ CORRECT APPROACH: Tests business logic only - no database interactions mocked.
 * Database introspection is tested via integration tests with real Snowflake connections.
 */
@DisplayName("StageSnapshotGeneratorSnowflake - Unit Tests")
public class StageSnapshotGeneratorSnowflakeTest {

    private StageSnapshotGeneratorSnowflake generator;
    private SnowflakeDatabase database;
    private Database nonSnowflakeDatabase;

    @BeforeEach
    void setUp() {
        generator = new StageSnapshotGeneratorSnowflake();
        database = new SnowflakeDatabase(); // Real database object for business logic tests
        nonSnowflakeDatabase = new liquibase.database.core.H2Database(); // Different DB type
    }

    // ==================== Priority Tests (Business Logic) ====================
    
    @Test
    @DisplayName("Should return PRIORITY_DATABASE for Stage objects with Snowflake database")
    void shouldReturnPriorityDatabaseForStageObjectsWithSnowflakeDatabase() {
        int priority = generator.getPriority(Stage.class, database);
        assertEquals(JdbcSnapshotGenerator.PRIORITY_DATABASE, priority);
    }
    
    @Test
    @DisplayName("Should return PRIORITY_ADDITIONAL for Schema objects with Snowflake database")
    void shouldReturnPriorityAdditionalForSchemaObjectsWithSnowflakeDatabase() {
        int priority = generator.getPriority(Schema.class, database);
        assertEquals(JdbcSnapshotGenerator.PRIORITY_ADDITIONAL, priority);
    }
    
    @Test
    @DisplayName("Should return PRIORITY_NONE for non-Snowflake database")
    void shouldReturnPriorityNoneForNonSnowflakeDatabase() {
        int priority = generator.getPriority(Stage.class, nonSnowflakeDatabase);
        assertEquals(JdbcSnapshotGenerator.PRIORITY_NONE, priority);
    }
    
    @Test
    @DisplayName("Should return PRIORITY_NONE for unsupported object type")
    void shouldReturnPriorityNoneForUnsupportedObjectType() {
        int priority = generator.getPriority(Catalog.class, database);
        assertEquals(JdbcSnapshotGenerator.PRIORITY_NONE, priority);
    }

    // ==================== Object Type Validation (Business Logic) ====================
    
    @Test
    @DisplayName("Should validate Stage class as supported type")
    void shouldValidateStageClassAsSupportedType() {
        // Test that generator recognizes Stage class as supported
        assertTrue(generator.getPriority(Stage.class, database) > JdbcSnapshotGenerator.PRIORITY_NONE);
    }
    
    @Test
    @DisplayName("Should validate Schema class has additional priority")
    void shouldValidateSchemaClassHasAdditionalPriority() {
        // Test that generator provides Schema support for bulk discovery
        assertEquals(JdbcSnapshotGenerator.PRIORITY_ADDITIONAL, 
                    generator.getPriority(Schema.class, database));
    }
    
    @Test
    @DisplayName("Should reject unsupported object types")
    void shouldRejectUnsupportedObjectTypes() {
        // Test business logic for type validation
        assertEquals(JdbcSnapshotGenerator.PRIORITY_NONE, 
                    generator.getPriority(Catalog.class, database));
    }

    // ==================== Database Type Validation (Business Logic) ====================
    
    @Test
    @DisplayName("Should only work with Snowflake database instances")
    void shouldOnlyWorkWithSnowflakeDatabaseInstances() {
        // Test database type checking logic
        assertTrue(generator.getPriority(Stage.class, database) > JdbcSnapshotGenerator.PRIORITY_NONE);
        assertEquals(JdbcSnapshotGenerator.PRIORITY_NONE, 
                    generator.getPriority(Stage.class, nonSnowflakeDatabase));
    }

    // ==================== replaces() Method Test ====================
    
    @Test
    @DisplayName("Should return empty array for replaces method")
    void shouldReturnEmptyArrayForReplacesMethod() {
        Class<?>[] replaces = generator.replaces();
        assertNotNull(replaces);
        assertEquals(0, replaces.length);
    }

    // ==================== Constructor Test ====================
    
    @Test
    @DisplayName("Should initialize with correct supported types")
    void shouldInitializeWithCorrectSupportedTypes() {
        // Test that constructor sets up the generator correctly
        assertNotNull(generator);
        
        // Verify priority behavior matches constructor setup
        assertEquals(JdbcSnapshotGenerator.PRIORITY_DATABASE, 
                    generator.getPriority(Stage.class, database));
        assertEquals(JdbcSnapshotGenerator.PRIORITY_ADDITIONAL, 
                    generator.getPriority(Schema.class, database));
    }

    // ==================== Helper Methods ====================
    
    // Note: DatabaseSnapshot creation removed from unit tests
    // Unit tests focus on business logic only
    // Database snapshot operations are tested in integration tests
    
    // ==================== NOTE: Database Integration Tests ====================
    
    /*
     * ✅ CORRECT APPROACH NOTE:
     * 
     * Database interactions (snapshotObject with real DB access, addTo with real queries)
     * are tested in separate integration tests that use actual Snowflake connections:
     * 
     * - StageSnapshotGeneratorIntegrationTest.java
     * - StageFullCycleIntegrationTest.java
     * 
     * These integration tests verify:
     * - Actual INFORMATION_SCHEMA.STAGES queries work correctly
     * - Real result set processing handles Snowflake data properly  
     * - SHOW STAGES enhancement works with real database
     * - Bulk discovery works with actual schemas
     * - Error handling works with real database exceptions
     * 
     * This separation ensures:
     * - Unit tests are fast and don't require database setup
     * - Integration tests validate real database interactions  
     * - Business logic is tested independently from database connectivity
     */
}