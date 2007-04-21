package liquibase.migrator;

import junit.framework.TestCase;

public class MigratorSchemaResolverTest extends TestCase {

    public void testResolveEntity() throws Exception {
        MigratorSchemaResolver migratorSchemaResolver = new MigratorSchemaResolver();
        assertNotNull(migratorSchemaResolver.resolveEntity(null, "http://www.liquibase.org/xml/ns/migrator/dbchangelog-1.0.xsd"));

        assertNull(migratorSchemaResolver.resolveEntity(null, "http://www.liquibase.org/xml/ns/migrator/invalid.xsd"));
    }
}