package liquibase.datatype;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.datatype.core.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test that our data types are correctly selected based on database and priority
 */
public class DataTypeSelectionTest {

    private SnowflakeDatabase snowflakeDb;
    private PostgresDatabase postgresDb;

    @Before
    public void setup() {
        snowflakeDb = new SnowflakeDatabase();
        postgresDb = new PostgresDatabase();
    }

    @Test
    public void testDataTypeSupportsCorrectDatabase() {
        VariantTypeSnowflake variant = new VariantTypeSnowflake();
        ArrayTypeSnowflake array = new ArrayTypeSnowflake();
        ObjectTypeSnowflake object = new ObjectTypeSnowflake();
        GeographyTypeSnowflake geography = new GeographyTypeSnowflake();
        GeometryTypeSnowflake geometry = new GeometryTypeSnowflake();

        // Should support Snowflake
        assertTrue("VARIANT should support Snowflake", variant.supports(snowflakeDb));
        assertTrue("ARRAY should support Snowflake", array.supports(snowflakeDb));
        assertTrue("OBJECT should support Snowflake", object.supports(snowflakeDb));
        assertTrue("GEOGRAPHY should support Snowflake", geography.supports(snowflakeDb));
        assertTrue("GEOMETRY should support Snowflake", geometry.supports(snowflakeDb));

        // Should NOT support other databases
        assertFalse("VARIANT should not support Postgres", variant.supports(postgresDb));
        assertFalse("ARRAY should not support Postgres", array.supports(postgresDb));
        assertFalse("OBJECT should not support Postgres", object.supports(postgresDb));
        assertFalse("GEOGRAPHY should not support Postgres", geography.supports(postgresDb));
        assertFalse("GEOMETRY should not support Postgres", geometry.supports(postgresDb));
    }

    @Test
    public void testDataTypePriority() {
        VariantTypeSnowflake variant = new VariantTypeSnowflake();
        ArrayTypeSnowflake array = new ArrayTypeSnowflake();
        
        // Our data types should have database priority (higher than default)
        assertEquals("VARIANT should have database priority", 
                     LiquibaseDataType.PRIORITY_DATABASE, variant.getPriority());
        assertEquals("ARRAY should have database priority", 
                     LiquibaseDataType.PRIORITY_DATABASE, array.getPriority());
    }

    @Test
    public void testDataTypeNames() {
        VariantTypeSnowflake variant = new VariantTypeSnowflake();
        ArrayTypeSnowflake array = new ArrayTypeSnowflake();
        ObjectTypeSnowflake object = new ObjectTypeSnowflake();
        GeographyTypeSnowflake geography = new GeographyTypeSnowflake();
        GeometryTypeSnowflake geometry = new GeometryTypeSnowflake();

        // Test that the @DataTypeInfo annotations have the right names
        // These names should match what users type in their changelogs
        assertTrue("VARIANT should respond to 'variant' name", variant.getName().equals("variant"));
        assertTrue("ARRAY should respond to 'array' name", array.getName().equals("array"));
        assertTrue("OBJECT should respond to 'object' name", object.getName().equals("object"));
        assertTrue("GEOGRAPHY should respond to 'geography' name", geography.getName().equals("geography"));
        assertTrue("GEOMETRY should respond to 'geometry' name", geometry.getName().equals("geometry"));
    }

    @Test
    public void testDataTypeAliases() {
        VariantTypeSnowflake variant = new VariantTypeSnowflake();
        ObjectTypeSnowflake object = new ObjectTypeSnowflake();

        // Test aliases from @DataTypeInfo
        String[] variantAliases = variant.getAliases();
        boolean hasJsonAlias = false;
        for (String alias : variantAliases) {
            if ("json".equals(alias)) hasJsonAlias = true;
        }
        assertTrue("VARIANT should have 'json' alias", hasJsonAlias);

        String[] objectAliases = object.getAliases();
        boolean hasMapAlias = false;
        for (String alias : objectAliases) {
            if ("map".equals(alias)) hasMapAlias = true;
        }
        assertTrue("OBJECT should have 'map' alias", hasMapAlias);
    }

    @Test
    public void testDatabaseTypeGeneration() {
        SnowflakeDatabase database = new SnowflakeDatabase();
        
        VariantTypeSnowflake variant = new VariantTypeSnowflake();
        ArrayTypeSnowflake array = new ArrayTypeSnowflake();
        
        // Test basic database type generation
        assertEquals("VARIANT", variant.toDatabaseDataType(database).getType());
        assertEquals("ARRAY", array.toDatabaseDataType(database).getType());
        
        // Test array with parameter
        array.addParameter("STRING");
        assertEquals("ARRAY(STRING)", array.toDatabaseDataType(database).getType());
    }
}