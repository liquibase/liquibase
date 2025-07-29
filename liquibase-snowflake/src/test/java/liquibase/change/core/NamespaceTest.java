package liquibase.change.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NamespaceTest {
    
    @Test
    public void testCreateWarehouseNamespace() {
        CreateWarehouseChange change = new CreateWarehouseChange();
        assertEquals("http://www.liquibase.org/xml/ns/snowflake", 
                     change.getSerializedObjectNamespace(),
                     "CreateWarehouseChange should return Snowflake namespace");
    }
    
    @Test
    public void testAlterWarehouseNamespace() {
        AlterWarehouseChange change = new AlterWarehouseChange();
        assertEquals("http://www.liquibase.org/xml/ns/snowflake", 
                     change.getSerializedObjectNamespace(),
                     "AlterWarehouseChange should return Snowflake namespace");
    }
    
    @Test
    public void testDropWarehouseNamespace() {
        DropWarehouseChange change = new DropWarehouseChange();
        assertEquals("http://www.liquibase.org/xml/ns/snowflake", 
                     change.getSerializedObjectNamespace(),
                     "DropWarehouseChange should return Snowflake namespace");
    }
    
    @Test
    public void testCreateDatabaseNamespace() {
        CreateDatabaseChange change = new CreateDatabaseChange();
        assertEquals("http://www.liquibase.org/xml/ns/snowflake", 
                     change.getSerializedObjectNamespace(),
                     "CreateDatabaseChange should return Snowflake namespace");
    }
    
    @Test
    public void testAlterDatabaseNamespace() {
        AlterDatabaseChange change = new AlterDatabaseChange();
        assertEquals("http://www.liquibase.org/xml/ns/snowflake", 
                     change.getSerializedObjectNamespace(),
                     "AlterDatabaseChange should return Snowflake namespace");
    }
    
    @Test
    public void testDropDatabaseNamespace() {
        DropDatabaseChange change = new DropDatabaseChange();
        assertEquals("http://www.liquibase.org/xml/ns/snowflake", 
                     change.getSerializedObjectNamespace(),
                     "DropDatabaseChange should return Snowflake namespace");
    }
    
    @Test
    public void testCreateSchemaNamespace() {
        CreateSchemaChange change = new CreateSchemaChange();
        assertEquals("http://www.liquibase.org/xml/ns/snowflake", 
                     change.getSerializedObjectNamespace(),
                     "CreateSchemaChange should return Snowflake namespace");
    }
    
    @Test
    public void testAlterSchemaNamespace() {
        AlterSchemaChange change = new AlterSchemaChange();
        assertEquals("http://www.liquibase.org/xml/ns/snowflake", 
                     change.getSerializedObjectNamespace(),
                     "AlterSchemaChange should return Snowflake namespace");
    }
    
    @Test
    public void testDropSchemaNamespace() {
        DropSchemaChange change = new DropSchemaChange();
        assertEquals("http://www.liquibase.org/xml/ns/snowflake", 
                     change.getSerializedObjectNamespace(),
                     "DropSchemaChange should return Snowflake namespace");
    }
}