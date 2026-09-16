package liquibase.logging.core;

import liquibase.Scope;
import liquibase.logging.Logger;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link JavaLogService}.
 */
class JavaLogServiceTest {

    @Test
    void getLog_doesNotModifyUseParentHandlers() {
        JavaLogService service = new JavaLogService();
        java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger("liquibase.logging");
        boolean originalValue = julLogger.getUseParentHandlers();

        // Explicitly set to false to verify getLog does not force it to true
        julLogger.setUseParentHandlers(false);

        service.getLog(JavaLogService.class);

        assertFalse(julLogger.getUseParentHandlers(),
                "getLog should not modify useParentHandlers on the underlying JUL logger");

        // Restore original value
        julLogger.setUseParentHandlers(originalValue);
    }

    @Test
    void getLog_returnsNonNullLogger() {
        JavaLogService service = new JavaLogService();
        Logger logger = service.getLog(JavaLogServiceTest.class);
        assertNotNull(logger);
    }

    @Test
    void getLog_returnsCachedLoggerOnSecondCall() {
        JavaLogService service = new JavaLogService();
        Logger first = service.getLog(JavaLogServiceTest.class);
        Logger second = service.getLog(JavaLogServiceTest.class);
        assertSame(first, second);
    }

    @Test
    void getLogName_returnsSecondLevelPackage() {
        JavaLogService service = new JavaLogService();
        assertEquals("liquibase.logging", service.getLogName(JavaLogService.class));
    }

    @Test
    void getLogName_returnsLiquibaseForTopLevelPackage() {
        JavaLogService service = new JavaLogService();
        assertEquals("liquibase", service.getLogName(liquibase.Scope.class));
    }

    @Test
    void getLogName_returnsUnknownForNull() {
        JavaLogService service = new JavaLogService();
        assertEquals("unknown", service.getLogName(null));
    }
}
