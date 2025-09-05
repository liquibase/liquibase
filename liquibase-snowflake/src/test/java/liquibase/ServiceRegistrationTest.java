package liquibase;

import liquibase.change.core.*;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.parser.NamespaceDetails;
import liquibase.parser.NamespaceDetailsFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for service registration
 */
@DisplayName("Service Registration")
public class ServiceRegistrationTest {
    
    @Test
    @DisplayName("Should instantiate Schema change types")
    public void testSchemaChangeInstantiation() {
        // Test that change classes can be instantiated (basic service registration test)
        CreateSchemaChange createChange = new CreateSchemaChange();
        assertNotNull(createChange);
        assertTrue(createChange.supports(new SnowflakeDatabase()));
        
        DropSchemaChange dropChange = new DropSchemaChange();
        assertNotNull(dropChange);
        assertTrue(dropChange.supports(new SnowflakeDatabase()));
        
        AlterSchemaChange alterChange = new AlterSchemaChange();
        assertNotNull(alterChange);
        assertTrue(alterChange.supports(new SnowflakeDatabase()));
    }
    
    @Test
    @DisplayName("Should instantiate Database change types")
    public void testDatabaseChangeInstantiation() {
        CreateDatabaseChange createChange = new CreateDatabaseChange();
        assertNotNull(createChange);
        assertTrue(createChange.supports(new SnowflakeDatabase()));
        
        DropDatabaseChange dropChange = new DropDatabaseChange();
        assertNotNull(dropChange);
        assertTrue(dropChange.supports(new SnowflakeDatabase()));
        
        AlterDatabaseChange alterChange = new AlterDatabaseChange();
        assertNotNull(alterChange);
        assertTrue(alterChange.supports(new SnowflakeDatabase()));
    }
    
    @Test
    @DisplayName("Should instantiate Warehouse change types")
    public void testWarehouseChangeInstantiation() {
        CreateWarehouseChange createChange = new CreateWarehouseChange();
        assertNotNull(createChange);
        assertTrue(createChange.supports(new SnowflakeDatabase()));
        
        DropWarehouseChange dropChange = new DropWarehouseChange();
        assertNotNull(dropChange);
        assertTrue(dropChange.supports(new SnowflakeDatabase()));
        
        AlterWarehouseChange alterChange = new AlterWarehouseChange();
        assertNotNull(alterChange);
        assertTrue(alterChange.supports(new SnowflakeDatabase()));
    }
    
    @Test
    @DisplayName("Should register Snowflake namespace details")
    public void testNamespaceRegistration() {
        NamespaceDetailsFactory factory = NamespaceDetailsFactory.getInstance();
        
        boolean foundSnowflake = false;
        for (NamespaceDetails details : factory.getNamespaceDetails()) {
            for (String namespace : details.getNamespaces()) {
                if ("http://www.liquibase.org/xml/ns/snowflake".equals(namespace)) {
                    foundSnowflake = true;
                    
                    // Verify namespace details
                    assertEquals("snowflake", details.getShortName(namespace));
                    assertTrue(details.getSchemaUrl(namespace).contains("liquibase-snowflake"));
                    break;
                }
            }
        }
        
        assertTrue(foundSnowflake, "Snowflake namespace should be registered");
    }
}