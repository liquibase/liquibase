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
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;

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
        ChangeLogParserFactory.getInstance().unregisterAllParsers();
        assumeThat(ChangeLogParserFactory.getInstance().getParsers(), empty());

        ChangeLogParserFactory.getInstance().register(new MockChangeLogParser(".test"));

        assertEquals(1, ChangeLogParserFactory.getInstance().getParsers().size());
    }

    @Test
    public void unregister_instance() {
        ChangeLogParserFactory factory = ChangeLogParserFactory.getInstance();
        factory.unregisterAllParsers();
        assumeThat(ChangeLogParserFactory.getInstance().getParsers(), empty());

        ChangeLogParser mockChangeLogParser = new MockChangeLogParser(".test");

        factory.register(new XMLChangeLogSAXParser());
        factory.register(mockChangeLogParser);
        factory.register(new SqlChangeLogParser());

        assertEquals(3, factory.getParsers().size());

        factory.unregister(mockChangeLogParser);
        assertEquals(2, factory.getParsers().size());
    }

    @Test
    public void unregisterAllParsers_ShouldRemoveAllParsers() {
        ChangeLogParserFactory factory = ChangeLogParserFactory.getInstance();
        factory.register(new MockChangeLogParser());
        assumeThat(factory.getParsers(), not(empty()));

        factory.unregisterAllParsers();

        assertThat(factory.getParsers(), empty());
    }

    @Test
    public void getParser_byExtension() throws Exception {
        ChangeLogParserFactory.getInstance().unregisterAllParsers();
        assumeThat(ChangeLogParserFactory.getInstance().getParsers(), empty());

        XMLChangeLogSAXParser xmlChangeLogParser = new XMLChangeLogSAXParser();
        ChangeLogParserFactory.getInstance().register(xmlChangeLogParser);
        ChangeLogParserFactory.getInstance().register(new SqlChangeLogParser());

        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser("xml", new JUnitResourceAccessor());

        assertNotNull(parser);
        assertSame(xmlChangeLogParser, parser);
    }

    @Test
    public void getParser_byFile() throws Exception {
        ChangeLogParserFactory.getInstance().unregisterAllParsers();
        assumeThat(ChangeLogParserFactory.getInstance().getParsers(), empty());

        XMLChangeLogSAXParser xmlChangeLogParser = new XMLChangeLogSAXParser();
        ChangeLogParserFactory.getInstance().register(xmlChangeLogParser);
        ChangeLogParserFactory.getInstance().register(new SqlChangeLogParser());

        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser("path/to/a/file.xml", new JUnitResourceAccessor());

        assertNotNull(parser);
        assertSame(xmlChangeLogParser, parser);
    }

    @Test
    public void getParser_shouldAssumePriority() throws Exception {
        ChangeLogParserFactory factory = ChangeLogParserFactory.getInstance();

        MockChangeLogParser higherPriorityParser = new MockChangeLogParser("banana") {
            @Override
            public int getPriority() {
                return Integer.MAX_VALUE;
            }
        };
        factory.register(new MockChangeLogParser("banana"));
        factory.register(higherPriorityParser);

        assertEquals(higherPriorityParser, factory.getParser("banana", new JUnitResourceAccessor()));
    }

    @Test
    public void getParser_shouldNotGiveAbilityToChangeParsers() {
        ChangeLogParserFactory factory = ChangeLogParserFactory.getInstance();

        MockChangeLogParser mockChangeLogParser = new MockChangeLogParser();
        factory.getParsers().add(mockChangeLogParser);

        assertThat(factory.getParsers(), not(hasItem(mockChangeLogParser)));
    }

    @Test(expected = UnknownChangelogFormatException.class)
    public void getParser_noneMatching() throws Exception {
        ChangeLogParserFactory.getInstance().unregisterAllParsers();

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
