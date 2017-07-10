package liquibase.parser;

import liquibase.parser.core.sql.SqlChangeLogParser;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.test.JUnitResourceAccessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ChangeLogParserFactoryTest {

    @Before
    public void setup() {
        ChangeLogParserFactory.reset();
    }

    @After
    public void after() {
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
        List<ChangeLogParser> generators = ChangeLogParserFactory.getInstance().getParsers();
        assertEquals(5, generators.size());
    }

    @Test
    public void getParsers() throws Exception {
        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser("asdf.xml", new JUnitResourceAccessor());

        assertNotNull(parser);
        assertTrue(parser instanceof XMLChangeLogSAXParser);
    }

    @Test
    public void getExtensionParser() throws Exception {
        ChangeLogParserFactory parserFactory = ChangeLogParserFactory.getInstance();
        ChangeLogParser defaultParser = parserFactory.getParser("asdf.xml", new JUnitResourceAccessor());

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
            assertTrue(otherXmlParser == parserFactory.getParser("asdf.xml", new JUnitResourceAccessor()));
            assertFalse(defaultParser == parserFactory.getParser("asdf.xml", new JUnitResourceAccessor()));
        } finally {
            parserFactory.unregister(otherXmlParser);
        }
    }

}