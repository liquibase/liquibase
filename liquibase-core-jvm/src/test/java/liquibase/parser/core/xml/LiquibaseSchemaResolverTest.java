package liquibase.parser.core.xml;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for {@link liquibase.parser.core.xml.LiquibaseSchemaResolver}
 */
public class LiquibaseSchemaResolverTest {

    @Test
    public void resolveEntity() throws Exception {
        LiquibaseSchemaResolver liquibaseSchemaResolver = new LiquibaseSchemaResolver();

        assertNotNull(liquibaseSchemaResolver.resolveEntity(null, "http://www.liquibase.org/xml/ns/migrator/dbchangelog-1.0.xsd"));
        assertNull(liquibaseSchemaResolver.resolveEntity(null, "http://www.liquibase.org/xml/ns/migrator/invalid.xsd"));
    }
}