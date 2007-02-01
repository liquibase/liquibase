package liquibase.migrator;

import junit.framework.TestCase;

public class MigratorSchemaResolverTest extends TestCase {

    public void testResolveEntity() throws Exception {
        MigratorSchemaResolver migratorSchemaResolver = new MigratorSchemaResolver();
        assertNotNull(migratorSchemaResolver.resolveEntity(null, "http://www.sundog.net/xml/ns/migrator/migrator-1.0.xsd"));

        assertNull(migratorSchemaResolver.resolveEntity(null, "http://www.sundog.net/xml/ns/migrator/invalid.xsd"));
    }
}