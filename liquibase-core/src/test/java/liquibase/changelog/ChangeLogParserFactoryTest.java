package liquibase.changelog;

import liquibase.exception.ChangeLogParseException;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.parser.core.sql.SqlChangeLogParser;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.ResourceAccessor;
import liquibase.test.JUnitResourceAccessor;
import org.junit.After;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ChangeLogParserFactoryTest {
    @After
    public void teardown() {
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

        ChangeLogParserFactory.getInstance().register(new MockChangeLogParser());

        assertEquals(1, ChangeLogParserFactory.getInstance().getParsers().size());
    }

    @Test
    public void unregister_instance() {
        ChangeLogParserFactory factory = ChangeLogParserFactory.getInstance();

        factory.getParsers().clear();

        assertEquals(0, factory.getParsers().size());

        ChangeLogParser mockChangeLogParser = new MockChangeLogParser();

        factory.register(new XMLChangeLogSAXParser());
        factory.register(mockChangeLogParser);
        factory.register(new SqlChangeLogParser());

        assertEquals(3, factory.getParsers().size());

        factory.unregister(mockChangeLogParser);
        assertEquals(2, factory.getParsers().size());
    }

    @Test
    public void getParser_byExtension() throws Exception {
        ChangeLogParserFactory.getInstance().getParsers().clear();

        XMLChangeLogSAXParser xmlChangeLogParser = new XMLChangeLogSAXParser();
        ChangeLogParserFactory.getInstance().register(xmlChangeLogParser);
        ChangeLogParserFactory.getInstance().register(new SqlChangeLogParser());

        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser("xml", new JUnitResourceAccessor());

        assertNotNull(parser);
        assertTrue(xmlChangeLogParser == parser);
    }

    @Test
    public void getParser_byFile() throws Exception {
        ChangeLogParserFactory.getInstance().getParsers().clear();

        XMLChangeLogSAXParser xmlChangeLogParser = new XMLChangeLogSAXParser();
        ChangeLogParserFactory.getInstance().register(xmlChangeLogParser);
        ChangeLogParserFactory.getInstance().register(new SqlChangeLogParser());

        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser("path/to/a/file.xml", new JUnitResourceAccessor());

        assertNotNull(parser);
        assertTrue(xmlChangeLogParser == parser);
    }

    @Test
    public void getParser_noneMatching() throws Exception {
        ChangeLogParserFactory.getInstance().getParsers().clear();

        ChangeLogParserFactory.getInstance().getParsers().clear();

        XMLChangeLogSAXParser xmlChangeLogParser = new XMLChangeLogSAXParser();
        ChangeLogParserFactory.getInstance().register(xmlChangeLogParser);
        ChangeLogParserFactory.getInstance().register(new SqlChangeLogParser());

        try {
            ChangeLogParserFactory.getInstance().getParser("badextension", new JUnitResourceAccessor());
            fail("Did not throw an exception");
        } catch (Exception e) {
            //what we want
        }
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
        List parsers = ChangeLogParserFactory.getInstance().getParsers();
        assertTrue(!parsers.isEmpty());
    }

    private static class MockChangeLogParser implements ChangeLogParser {

        @Override
        public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
            return null;
        }

        @Override
        public boolean supports(String changeLogFile, ResourceAccessor resourceAccessor) {
            return changeLogFile.endsWith(".test");
        }

        @Override
        public int getPriority() {
            return PRIORITY_DEFAULT;
        }        
    }
}
