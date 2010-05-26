package liquibase.parser.core.xml;

import static org.junit.Assert.*;
import org.junit.Test;

public class LiquibaseEntityResolverTest {

    @Test
    public void resolveSchemas() throws Exception {
        LiquibaseEntityResolver liquibaseSchemaResolver = new LiquibaseEntityResolver();

        assertNotNull(liquibaseSchemaResolver.resolveEntity(null, null, null, "http://www.liquibase.org/xml/ns/migrator/dbchangelog-1.0.xsd"));
        assertNull(liquibaseSchemaResolver.resolveEntity(null,null,null, "http://www.liquibase.org/xml/ns/migrator/invalid.xsd"));
    }
}