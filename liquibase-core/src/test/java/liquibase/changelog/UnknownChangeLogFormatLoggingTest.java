package liquibase.changelog;

import liquibase.logging.*;
import liquibase.logging.core.NoOpLoggerContext;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.test.JUnitResourceAccessor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * CORE-3020 No warning when included file doesn't exist (missing extension)
 *
 * This test verifies, that there will be a log message produced if no parser is detected from an included file name.
 * Typical situation is when you include a file and forgot the extension. Then there should be at least a log message.
 * <pre>{@code
 * <databaseChangeLog ...>
 *  <include file="liquibase/parser/core/xml/simpleChangeLog.xml"/> <!-- OK -->
 *  <include file="liquibase/parser/core/xml/secondChangeLog"/> <!-- here is an extension missing -->
 * </databaseChangeLog>
 * }</pre>
 * <p>
 * See also https://liquibase.jira.com/browse/CORE-2520, which introduced the logging filtering
 */
public class UnknownChangeLogFormatLoggingTest {

    private Logger mockLogger;

    @Before
    public void setUp() throws Exception {
        mockLogger = Mockito.mock(Logger.class);
        LogService.setLoggerFactory(new LoggerFactory() {
            @Override
            public Logger getLog(Class clazz) {
                return mockLogger;
            }

            @Override
            public LoggerContext pushContext(String key, Object object) {
                return new NoOpLoggerContext();
            }

            @Override
            public void close() {

            }
        });
    }

    @Test
    public void testUnknownFileTypeWarning() throws Exception {
        ArgumentCaptor<String> loggerCaptor = ArgumentCaptor.forClass(String.class);
        new XMLChangeLogSAXParser().parse("liquibase/parser/core/xml/unknownIncludedFileChangeLog.xml", new ChangeLogParameters(), new JUnitResourceAccessor());
        Mockito.verify(mockLogger, Mockito.atLeastOnce()).warning(Matchers.eq(LogType.LOG), loggerCaptor.capture());
        final String recordedValue = loggerCaptor.getValue();
        Assert.assertTrue("The warning should contain a path to the unrecognized file", recordedValue.contains("liquibase/parser/core/xml/preconditionsChangeLog"));
    }

}
