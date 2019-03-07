package liquibase.changelog;

import liquibase.exception.UnknownChangelogFormatException;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.parser.MockChangeLogParser;
import liquibase.parser.core.sql.SqlChangeLogParser;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.test.JUnitResourceAccessor;
import org.junit.After;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

public class ChangeLogParserFactoryTest {

    @After
    public void teardown() {
        ChangeLogParserFactory.reset();
    }

    @Test
    public void getInstance() {
        assertNotNull(ChangeLogParserFactory.getInstance());

        assertSame(ChangeLogParserFactory.getInstance(), ChangeLogParserFactory.getInstance());
    }

    @Test
    public void register() {
        ChangeLogParserFactory.getInstance().getParsers().clear();

        assertEquals(0, ChangeLogParserFactory.getInstance().getParsers().size());

        ChangeLogParserFactory.getInstance().register(new MockChangeLogParser(".test"));

        assertEquals(1, ChangeLogParserFactory.getInstance().getParsers().size());
    }

    @Test
    public void unregister_instance() {
        ChangeLogParserFactory factory = ChangeLogParserFactory.getInstance();

        factory.getParsers().clear();

        assertEquals(0, factory.getParsers().size());

        ChangeLogParser mockChangeLogParser = new MockChangeLogParser(".test");

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
        assertSame(xmlChangeLogParser, parser);
    }

    @Test
    public void getParser_byFile() throws Exception {
        ChangeLogParserFactory.getInstance().getParsers().clear();

        XMLChangeLogSAXParser xmlChangeLogParser = new XMLChangeLogSAXParser();
        ChangeLogParserFactory.getInstance().register(xmlChangeLogParser);
        ChangeLogParserFactory.getInstance().register(new SqlChangeLogParser());

        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser("path/to/a/file.xml", new JUnitResourceAccessor());

        assertNotNull(parser);
        assertSame(xmlChangeLogParser, parser);
    }

    @Test(expected = UnknownChangelogFormatException.class)
    public void getParser_noneMatching() throws Exception {
        ChangeLogParserFactory.getInstance().getParsers().clear();

        ChangeLogParserFactory.getInstance().getParsers().clear();

        XMLChangeLogSAXParser xmlChangeLogParser = new XMLChangeLogSAXParser();
        ChangeLogParserFactory.getInstance().register(xmlChangeLogParser);
        ChangeLogParserFactory.getInstance().register(new SqlChangeLogParser());

        ChangeLogParserFactory.getInstance().getParser("badextension", new JUnitResourceAccessor());
    }

    @Test
    public void reset() {
        ChangeLogParserFactory instance1 = ChangeLogParserFactory.getInstance();
        ChangeLogParserFactory.reset();
        assertNotSame(instance1, ChangeLogParserFactory.getInstance());
    }

    @Test
    public void builtInGeneratorsAreFound() {
        List<ChangeLogParser> parsers = ChangeLogParserFactory.getInstance().getParsers();
        assertThat(parsers, not(empty()));
    }
}
