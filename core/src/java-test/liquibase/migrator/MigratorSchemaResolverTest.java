package liquibase.migrator;

import liquibase.parser.LiquibaseSchemaResolver;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for {@link liquibase.parser.LiquibaseSchemaResolver}
 */
public class MigratorSchemaResolverTest {

    @Test
    public void resolveEntity() throws Exception {
        LiquibaseSchemaResolver liquibaseSchemaResolver = new LiquibaseSchemaResolver();

        assertNotNull(liquibaseSchemaResolver.resolveEntity(null, "http://www.liquibase.org/xml/ns/migrator/dbchangelog-1.0.xsd"));
        assertNull(liquibaseSchemaResolver.resolveEntity(null, "http://www.liquibase.org/xml/ns/migrator/invalid.xsd"));
    }
}