package liquibase.datatype;

import liquibase.Scope;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.datatype.core.*;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Test that our new data types are actually discoverable by Liquibase's service loader
 */
public class DataTypeDiscoveryTest {

    @Test
    public void testDataTypesAreDiscoverable() {
        Collection<LiquibaseDataType> dataTypes = Scope.getCurrentScope().getServiceLocator().findInstances(LiquibaseDataType.class);
        
        // Check that our new data types are found
        boolean foundVariant = false;
        boolean foundArray = false;
        boolean foundObject = false;
        boolean foundGeography = false;
        boolean foundGeometry = false;
        
        for (LiquibaseDataType dataType : dataTypes) {
            if (dataType instanceof VariantTypeSnowflake) foundVariant = true;
            if (dataType instanceof ArrayTypeSnowflake) foundArray = true;
            if (dataType instanceof ObjectTypeSnowflake) foundObject = true;
            if (dataType instanceof GeographyTypeSnowflake) foundGeography = true;
            if (dataType instanceof GeometryTypeSnowflake) foundGeometry = true;
        }
        
        assertTrue("VariantTypeSnowflake should be discoverable", foundVariant);
        assertTrue("ArrayTypeSnowflake should be discoverable", foundArray);
        assertTrue("ObjectTypeSnowflake should be discoverable", foundObject);
        assertTrue("GeographyTypeSnowflake should be discoverable", foundGeography);
        assertTrue("GeometryTypeSnowflake should be discoverable", foundGeometry);
    }

    @Test
    public void testDataTypesWorkWithSnowflakeDatabase() {
        SnowflakeDatabase database = new SnowflakeDatabase();
        
        VariantTypeSnowflake variantType = new VariantTypeSnowflake();
        ArrayTypeSnowflake arrayType = new ArrayTypeSnowflake();
        ObjectTypeSnowflake objectType = new ObjectTypeSnowflake();
        GeographyTypeSnowflake geographyType = new GeographyTypeSnowflake();
        GeometryTypeSnowflake geometryType = new GeometryTypeSnowflake();
        
        // Test that they all support Snowflake database
        assertTrue("VARIANT should support Snowflake", variantType.supports(database));
        assertTrue("ARRAY should support Snowflake", arrayType.supports(database));
        assertTrue("OBJECT should support Snowflake", objectType.supports(database));
        assertTrue("GEOGRAPHY should support Snowflake", geographyType.supports(database));
        assertTrue("GEOMETRY should support Snowflake", geometryType.supports(database));
        
        // Test SQL generation
        assertEquals("VARIANT", variantType.toDatabaseDataType(database).getType());
        assertEquals("ARRAY", arrayType.toDatabaseDataType(database).getType());
        assertEquals("OBJECT", objectType.toDatabaseDataType(database).getType());
        assertEquals("GEOGRAPHY", geographyType.toDatabaseDataType(database).getType());
        assertEquals("GEOMETRY", geometryType.toDatabaseDataType(database).getType());
    }
}