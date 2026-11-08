package liquibase.logging

import liquibase.logging.core.AbstractLogService
import liquibase.logging.core.JavaLogService
import spock.lang.Specification

/**
 * A-1: Verifies the getConsoleFormatter() seam on LogService and JavaLogService.
 *
 * RED phase: these tests fail until A-2 adds the method to LogService / JavaLogService.
 */
class LogServiceConsoleFormatterTest extends Specification {

    /** Minimal concrete LogService that does NOT override getConsoleFormatter — gets the interface default. */
    static class MinimalLogService extends AbstractLogService {
        @Override int getPriority() { return 0 }
        @Override liquibase.logging.Logger getLog(Class clazz) { return null }
    }

    def "LogService default getConsoleFormatter returns null"() {
        given: "a LogService that does not override getConsoleFormatter"
        def svc = new MinimalLogService()

        expect:
        svc.getConsoleFormatter() == null
    }

    def "JavaLogService getConsoleFormatter returns same value as getCustomFormatter"() {
        given:
        def svc = new JavaLogService()

        expect: "both return null when no custom formatter is set"
        svc.getConsoleFormatter() == svc.getCustomFormatter()
    }

    def "JavaLogService getConsoleFormatter still matches getCustomFormatter after a subclass override"() {
        given: "a subclass that overrides getCustomFormatter to return a custom formatter"
        def formatter = new java.util.logging.SimpleFormatter()
        def svc = new JavaLogService() {
            @Override
            java.util.logging.Formatter getCustomFormatter() { return formatter }
        }

        expect:
        svc.getConsoleFormatter() == formatter
        svc.getConsoleFormatter() == svc.getCustomFormatter()
    }
}
