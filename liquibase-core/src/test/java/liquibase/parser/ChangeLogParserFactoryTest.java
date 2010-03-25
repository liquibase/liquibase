package liquibase.parser;

import liquibase.parser.core.sql.SqlChangeLogParser;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.SortedSet;

public class ChangeLogParserFactoryTest {

    @Before
    public void setup() {
        ChangeLogParserFactory.reset();

    }

    @Test
    public void getInstance() {
        assertNotNull(ChangeLogParserFactory.getInstance());
        
        assertTrue(ChangeLogParserFactory.getInstance() == ChangeLogParserFactory.getInstance());
    }

    @Test
    public void register() {
        ChangeLogParserFactory.getInstance().getParsers().clear();

        assertEquals(0, ChangeLogParserFactory.getInstance().getParsers().size());

        ChangeLogParserFactory.getInstance().register(new MockChangeLogParser("mock"));

        assertEquals(1, ChangeLogParserFactory.getInstance().getParsers().size());
    }

    @Test
    public void unregister_instance() {
        ChangeLogParserFactory factory = ChangeLogParserFactory.getInstance();

        factory.getParsers().clear();

        assertEquals(0, factory.getParsers().size());

        XMLChangeLogSAXParser changeLogParser = new XMLChangeLogSAXParser();

        factory.register(new SqlChangeLogParser());
        factory.register(changeLogParser);

        assertEquals(2, factory.getParsers().size());

        factory.unregister(changeLogParser);
        assertEquals(1, factory.getParsers().size());
    }

    @Test
    public void reset() {
        ChangeLogParserFactory instance1 = ChangeLogParserFactory.getInstance();
        ChangeLogParserFactory.reset();
        assertFalse(instance1 == ChangeLogParserFactory.getInstance());
    }

    @SuppressWarnings("unchecked")
	@Test
    public void builtInGeneratorsAreFound() {
        Map<String, SortedSet<ChangeLogParser>> generators = ChangeLogParserFactory.getInstance().getParsers();
        assertEquals(2, generators.size());
    }

    @Test
    public void getParsers() {
        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser("xml");

        assertNotNull(parser);
        assertTrue(parser instanceof XMLChangeLogSAXParser);
    }

    @Test
    public void getExtensionParser() {
        ChangeLogParserFactory parserFactory = ChangeLogParserFactory.getInstance();
        ChangeLogParser defaultParser = parserFactory.getParser("xml");

        assertNotNull(defaultParser);
        assertTrue(defaultParser instanceof XMLChangeLogSAXParser);

        ChangeLogParser otherXmlParser = new XMLChangeLogSAXParser() {
            @Override
            public int getPriority() {
                return 100;
            }
        };
        parserFactory.register(otherXmlParser);

        try {
            assertTrue(otherXmlParser == parserFactory.getParser("xml"));
            assertFalse(defaultParser == parserFactory.getParser("xml"));
        } finally {
            parserFactory.unregister(otherXmlParser);
        }
    }

}