package liquibase.parser.xml;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 * Tests for {@link liquibase.parser.xml.LiquibaseSchemaResolver}
 */
public class LiquibaseSchemaResolverTest {

    @Test
    public void resolveEntity() throws Exception {
        LiquibaseSchemaResolver liquibaseSchemaResolver = new LiquibaseSchemaResolver();

        assertNotNull(liquibaseSchemaResolver.resolveEntity(null, "http://www.liquibase.org/xml/ns/migrator/dbchangelog-1.0.xsd"));
        assertNull(liquibaseSchemaResolver.resolveEntity(null, "http://www.liquibase.org/xml/ns/migrator/invalid.xsd"));
    }
}