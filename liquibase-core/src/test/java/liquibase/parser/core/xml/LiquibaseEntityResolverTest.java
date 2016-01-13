package liquibase.parser.core.xml;

import static org.junit.Assert.*;

import java.io.IOException;

import liquibase.test.JUnitResourceAccessor;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class LiquibaseEntityResolverTest {

    private static final String SYSTEM_ID_WITH_MIGRATOR = "http://www.liquibase.org/xml/ns/migrator/dbchangelog-1.0.xsd";
    private static final String VALID_SYSTEM_ID = "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd";
    private static final String VALID_SYSTEM_ID_WITH_MIGRATOR_PATH = "http://www.liquibase.org/xml/ns/migrator/dbchangelog-1.0.xsd";
    private static final String INVALID_SYSTEM_ID = "http://www.liquibase.org/xml/ns/migrator/invalid.xsd";
    private LiquibaseEntityResolver liquibaseSchemaResolver;
    private XMLChangeLogSAXParser parser;

    @Before
    public void setUp() {
        parser = new XMLChangeLogSAXParser();
        liquibaseSchemaResolver = new LiquibaseEntityResolver(parser);
    }

    @Test
    public void resolveSchemas() throws Exception {
        liquibaseSchemaResolver.useResoureAccessor(new JUnitResourceAccessor(), "");

        assertNotNull(liquibaseSchemaResolver.resolveEntity(null, null, null, VALID_SYSTEM_ID_WITH_MIGRATOR_PATH));
        assertNotNull(liquibaseSchemaResolver.resolveEntity(null, null, null, VALID_SYSTEM_ID));
        assertNull(liquibaseSchemaResolver.resolveEntity(null,null,null, INVALID_SYSTEM_ID));
    }

    @Test
    public void shouldReturnNullWhen() throws IOException, SAXException {
        assertNotNull(liquibaseSchemaResolver.resolveEntity(null, null, null, VALID_SYSTEM_ID));
    }
}