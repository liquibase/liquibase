package liquibase.changelog.parser.xml;

import liquibase.changelog.parser.xml.LiquibaseSchemaResolver;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for {@link LiquibaseSchemaResolver}
 */
public class LiquibaseSchemaResolverTest {

    @Test
    public void resolveEntity() throws Exception {
        LiquibaseSchemaResolver liquibaseSchemaResolver = new LiquibaseSchemaResolver();

        assertNotNull(liquibaseSchemaResolver.resolveEntity(null, "http://www.liquibase.org/xml/ns/migrator/dbchangelog-1.0.xsd"));
        assertNull(liquibaseSchemaResolver.resolveEntity(null, "http://www.liquibase.org/xml/ns/migrator/invalid.xsd"));
    }
}