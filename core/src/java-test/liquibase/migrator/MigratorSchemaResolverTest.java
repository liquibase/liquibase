package liquibase.migrator;

import liquibase.parser.MigratorSchemaResolver;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for {@link MigratorSchemaResolver}
 */
public class MigratorSchemaResolverTest {

    @Test
    public void resolveEntity() throws Exception {
        MigratorSchemaResolver migratorSchemaResolver = new MigratorSchemaResolver();

        assertNotNull(migratorSchemaResolver.resolveEntity(null, "http://www.liquibase.org/xml/ns/migrator/dbchangelog-1.0.xsd"));
        assertNull(migratorSchemaResolver.resolveEntity(null, "http://www.liquibase.org/xml/ns/migrator/invalid.xsd"));
    }
}