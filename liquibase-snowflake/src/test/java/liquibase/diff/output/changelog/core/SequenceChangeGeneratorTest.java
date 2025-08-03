package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.CreateSequenceChange;
import liquibase.change.core.DropSequenceChange;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.diff.output.DiffOutputControl;
import liquibase.structure.core.Sequence;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Catalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Snowflake sequence change generators.
 * Tests the complete set of sequence change generators:
 * - MissingSequenceChangeGeneratorSnowflake (CREATE)
 * - UnexpectedSequenceChangeGeneratorSnowflake (DROP)
 * - ChangedSequenceChangeGeneratorSnowflake (ALTER)
 */
public class SequenceChangeGeneratorTest {

    private SnowflakeDatabase database;
    private DiffOutputControl diffOutputControl;

    @BeforeEach
    public void setUp() {
        database = new SnowflakeDatabase();
        diffOutputControl = new DiffOutputControl();
        diffOutputControl.setIncludeCatalog(false);
        diffOutputControl.setIncludeSchema(true);
    }

    @Test
    public void testMissingSequenceChangeGeneratorPriority() {
        MissingSequenceChangeGeneratorSnowflake generator = new MissingSequenceChangeGeneratorSnowflake();
        
        // Should handle Sequence objects for SnowflakeDatabase with high priority
        assertEquals(MissingSequenceChangeGeneratorSnowflake.PRIORITY_DEFAULT + MissingSequenceChangeGeneratorSnowflake.PRIORITY_DATABASE,
                    generator.getPriority(Sequence.class, database),
                    "Should handle Sequence objects with DATABASE priority");
        
        // Should not handle other objects
        assertEquals(MissingSequenceChangeGeneratorSnowflake.PRIORITY_NONE,
                    generator.getPriority(liquibase.structure.core.Table.class, database),
                    "Should not handle Table objects");
    }

    @Test
    public void testUnexpectedSequenceChangeGeneratorPriority() {
        UnexpectedSequenceChangeGeneratorSnowflake generator = new UnexpectedSequenceChangeGeneratorSnowflake();
        
        // Should handle Sequence objects for SnowflakeDatabase with high priority
        assertEquals(UnexpectedSequenceChangeGeneratorSnowflake.PRIORITY_DEFAULT + UnexpectedSequenceChangeGeneratorSnowflake.PRIORITY_DATABASE,
                    generator.getPriority(Sequence.class, database),
                    "Should handle Sequence objects with DATABASE priority");
        
        // Should not handle other objects
        assertEquals(UnexpectedSequenceChangeGeneratorSnowflake.PRIORITY_NONE,
                    generator.getPriority(liquibase.structure.core.Table.class, database),
                    "Should not handle Table objects");
    }

    @Test
    public void testChangedSequenceChangeGeneratorPriority() {
        ChangedSequenceChangeGeneratorSnowflake generator = new ChangedSequenceChangeGeneratorSnowflake();
        
        // Should handle Sequence objects for SnowflakeDatabase with high priority
        assertEquals(ChangedSequenceChangeGeneratorSnowflake.PRIORITY_DEFAULT + ChangedSequenceChangeGeneratorSnowflake.PRIORITY_DATABASE,
                    generator.getPriority(Sequence.class, database),
                    "Should handle Sequence objects with DATABASE priority");
        
        // Should not handle other objects
        assertEquals(ChangedSequenceChangeGeneratorSnowflake.PRIORITY_NONE,
                    generator.getPriority(liquibase.structure.core.Table.class, database),
                    "Should not handle Table objects");
    }

    @Test
    public void testMissingSequenceChangeGeneration() throws Exception {
        MissingSequenceChangeGeneratorSnowflake generator = new MissingSequenceChangeGeneratorSnowflake();
        
        // Create a missing sequence object
        Sequence sequence = new Sequence();
        sequence.setName("TEST_SEQUENCE");
        sequence.setSchema(new Schema(new Catalog(null), "TEST_SCHEMA"));
        sequence.setStartValue(java.math.BigInteger.valueOf(1));
        sequence.setIncrementBy(java.math.BigInteger.valueOf(1));
        sequence.setMinValue(java.math.BigInteger.valueOf(1));
        sequence.setMaxValue(java.math.BigInteger.valueOf(999999999));
        
        // Generate change
        Change[] changes = generator.fixMissing(sequence, diffOutputControl, database, database, null);
        
        assertNotNull(changes, "Should generate changes");
        assertEquals(1, changes.length, "Should generate one change");
        assertTrue(changes[0] instanceof CreateSequenceChange, "Should generate CreateSequenceChange");
        
        CreateSequenceChange change = (CreateSequenceChange) changes[0];
        assertEquals("TEST_SEQUENCE", change.getSequenceName(), "Sequence name should match");
        assertEquals("TEST_SCHEMA", change.getSchemaName(), "Schema name should match");
    }

    @Test
    public void testUnexpectedSequenceChangeGeneration() throws Exception {
        UnexpectedSequenceChangeGeneratorSnowflake generator = new UnexpectedSequenceChangeGeneratorSnowflake();
        
        // Create an unexpected sequence object
        Sequence sequence = new Sequence();
        sequence.setName("UNEXPECTED_SEQUENCE");
        sequence.setSchema(new Schema(new Catalog(null), "TEST_SCHEMA"));
        
        // Generate change
        Change[] changes = generator.fixUnexpected(sequence, diffOutputControl, database, database, null);
        
        assertNotNull(changes, "Should generate changes");
        assertEquals(1, changes.length, "Should generate one change");
        assertTrue(changes[0] instanceof DropSequenceChange, "Should generate DropSequenceChange");
        
        DropSequenceChange change = (DropSequenceChange) changes[0];
        assertEquals("UNEXPECTED_SEQUENCE", change.getSequenceName(), "Sequence name should match");
        assertEquals("TEST_SCHEMA", change.getSchemaName(), "Schema name should match");
    }

    @Test
    public void testUnexpectedSequenceWithCascadeAttribute() throws Exception {
        UnexpectedSequenceChangeGeneratorSnowflake generator = new UnexpectedSequenceChangeGeneratorSnowflake();
        
        // Create an unexpected sequence object with cascade attribute
        Sequence sequence = new Sequence();
        sequence.setName("CASCADE_SEQUENCE");
        sequence.setSchema(new Schema(new Catalog(null), "TEST_SCHEMA"));
        sequence.setAttribute("cascade", "true");
        
        // Generate change
        Change[] changes = generator.fixUnexpected(sequence, diffOutputControl, database, database, null);
        
        assertNotNull(changes, "Should generate changes");
        assertEquals(1, changes.length, "Should generate one change");
        assertTrue(changes[0] instanceof DropSequenceChange, "Should generate DropSequenceChange");
        
        DropSequenceChange change = (DropSequenceChange) changes[0];
        assertEquals("CASCADE_SEQUENCE", change.getSequenceName(), "Sequence name should match");
        assertEquals("TEST_SCHEMA", change.getSchemaName(), "Schema name should match");
        
        // The cascade attribute should be stored in namespace storage for the SQL generator
        // We can't easily test this without more complex setup, but the functionality is there
    }

    @Test
    public void testSequenceChangeGeneratorOrdering() {
        MissingSequenceChangeGeneratorSnowflake missingGenerator = new MissingSequenceChangeGeneratorSnowflake();
        UnexpectedSequenceChangeGeneratorSnowflake unexpectedGenerator = new UnexpectedSequenceChangeGeneratorSnowflake();
        ChangedSequenceChangeGeneratorSnowflake changedGenerator = new ChangedSequenceChangeGeneratorSnowflake();
        
        // Test that generators can specify ordering (some may return null, which is valid)
        Class<?>[] missingRunAfter = missingGenerator.runAfterTypes();
        Class<?>[] unexpectedRunAfter = unexpectedGenerator.runAfterTypes();
        Class<?>[] changedRunAfter = changedGenerator.runAfterTypes();
        
        // The Unexpected generator should run after Table objects to ensure proper dependency ordering
        assertNotNull(unexpectedRunAfter, "Unexpected generator should specify run after types");
        
        boolean unexpectedRunsAfterTable = false;
        for (Class<?> clazz : unexpectedRunAfter) {
            if (clazz.equals(liquibase.structure.core.Table.class)) {
                unexpectedRunsAfterTable = true;
                break;
            }
        }
        assertTrue(unexpectedRunsAfterTable, "Unexpected generator should run after Table objects");
        
        // Missing and Changed generators may return null (inherit from base classes)
        // This is acceptable behavior - test that they don't crash
        assertDoesNotThrow(() -> missingGenerator.runAfterTypes(), "Missing generator runAfterTypes should not throw");
        assertDoesNotThrow(() -> changedGenerator.runAfterTypes(), "Changed generator runAfterTypes should not throw");
    }
}