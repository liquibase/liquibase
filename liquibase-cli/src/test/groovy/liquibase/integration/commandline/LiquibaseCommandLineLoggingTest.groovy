package liquibase.integration.commandline

import liquibase.logging.LogService
import liquibase.logging.core.JavaLogService
import spock.lang.Specification

import java.util.logging.ConsoleHandler
import java.util.logging.Formatter
import java.util.logging.SimpleFormatter
import java.util.logging.StreamHandler

/**
 * A-3: Verifies per-sink formatter routing:
 *  - ConsoleHandler receives the console-specific formatter from getConsoleFormatter()
 *  - Non-console handlers (FileHandler / StreamHandler) stay on the plain formatter path
 *
 * RED phase: fails until A-4 wires setConsoleFormatterOnHandler into the handler loop
 * in LiquibaseCommandLine.configureLogging().
 *
 * NOTE: A-3 also covers the static helper setConsoleFormatterOnHandler introduced in A-2,
 * which is a prerequisite for the A-4 wiring test below.
 */
class LiquibaseCommandLineLoggingTest extends Specification {

    // ── Helpers ─────────────────────────────────────────────────────────────────

    /** A JavaLogService subclass that returns a distinct console formatter. */
    static class ColorLogService extends JavaLogService {
        final Formatter consoleFormatter = new SimpleFormatter() {
            @Override String toString() { return "CONSOLE_FORMATTER" }
        }

        @Override
        Formatter getConsoleFormatter() { return consoleFormatter }

        // getCustomFormatter() stays null (file handler must stay plain)
        @Override
        Formatter getCustomFormatter() { return null }
    }

    // ── setConsoleFormatterOnHandler unit tests (A-2 helper, exercised here) ──

    def "setConsoleFormatterOnHandler applies console formatter to ConsoleHandler"() {
        given:
        def logService = new ColorLogService()
        def handler = new ConsoleHandler()

        when:
        JavaLogService.setConsoleFormatterOnHandler(logService, handler)

        then:
        handler.formatter.is(logService.consoleFormatter)
    }

    def "setConsoleFormatterOnHandler does NOT change a non-console handler when getCustomFormatter is null"() {
        given:
        def logService = new ColorLogService() // getCustomFormatter == null
        def outputStream = new ByteArrayOutputStream()
        def fileHandler = new StreamHandler(outputStream, new SimpleFormatter())
        def originalFormatter = fileHandler.formatter

        when:
        // Simulate what configureLogging does for file handlers: use setFormatterOnHandler
        JavaLogService.setFormatterOnHandler(logService, fileHandler)

        then: "file handler formatter is unchanged because getCustomFormatter() returns null"
        fileHandler.formatter.is(originalFormatter)
    }

    def "setConsoleFormatterOnHandler falls back to getCustomFormatter when getConsoleFormatter is null"() {
        given: "a log service where getConsoleFormatter returns null but getCustomFormatter is non-null"
        def customFmt = new SimpleFormatter()
        def logService = new JavaLogService() {
            @Override Formatter getConsoleFormatter() { return null }
            @Override Formatter getCustomFormatter() { return customFmt }
        }
        def handler = new ConsoleHandler()

        when:
        JavaLogService.setConsoleFormatterOnHandler(logService, handler)

        then: "falls back to getCustomFormatter"
        handler.formatter.is(customFmt)
    }

    def "setConsoleFormatterOnHandler is a no-op for null logService"() {
        given:
        def handler = new ConsoleHandler()
        def original = handler.formatter

        when:
        JavaLogService.setConsoleFormatterOnHandler(null, handler)

        then:
        noExceptionThrown()
        handler.formatter.is(original)
    }

    def "setConsoleFormatterOnHandler is a no-op for null handler"() {
        given:
        def logService = new ColorLogService()

        when:
        JavaLogService.setConsoleFormatterOnHandler(logService, null)

        then:
        noExceptionThrown()
    }

    // ── configureLogging handler-loop integration test (A-4 gate) ──────────────

    /**
     * Exercises the actual LiquibaseCommandLine.configureLogging handler loop via reflection to
     * verify that AFTER A-4:
     *  - ConsoleHandler receives the colour formatter from getConsoleFormatter()
     *  - A non-console StreamHandler keeps its original plain formatter
     *
     * BEFORE A-4 (current state) the loop calls setFormatterOnHandler() for ALL handlers —
     * so ConsoleHandler does NOT get the colour formatter (getConsoleFormatter returns non-null
     * but is ignored).  This test will go RED until A-4 patches the loop.
     */
    def "configureLogging loop applies console formatter to ConsoleHandler but not to file handler"() {
        given: "a LiquibaseCommandLine instance and a ColorLogService in Scope"
        def cli = new LiquibaseCommandLine()
        def colorLogService = new ColorLogService()

        def rootLogger = java.util.logging.Logger.getLogger("")
        def consoleHandler = new ConsoleHandler()
        def plainFormatter = new SimpleFormatter()
        def fileHandler = new StreamHandler(new ByteArrayOutputStream(), plainFormatter)

        // Remove all existing root handlers to have a clean slate; restore in cleanup
        def savedHandlers = rootLogger.handlers.toList()
        savedHandlers.each { rootLogger.removeHandler(it) }
        rootLogger.addHandler(consoleHandler)
        rootLogger.addHandler(fileHandler)

        when: "configureLogging runs under the ColorLogService scope (via reflection)"
        def invokeError = null
        try {
            liquibase.Scope.child(
                [(liquibase.Scope.Attr.logService.name()): colorLogService],
                { ->
                    def method = LiquibaseCommandLine.class.getDeclaredMethod(
                        "configureLogging",
                        java.util.logging.Level.class, String.class, boolean.class)
                    method.accessible = true
                    method.invoke(cli, java.util.logging.Level.INFO, null, false)
                } as liquibase.Scope.ScopedRunner
            )
        } catch (Throwable t) {
            invokeError = t
        } finally {
            rootLogger.removeHandler(consoleHandler)
            rootLogger.removeHandler(fileHandler)
            savedHandlers.each { rootLogger.addHandler(it) }
        }

        then: "no exception during configureLogging"
        invokeError == null

        and: "ConsoleHandler gets the colour formatter (A-4 patched loop)"
        consoleHandler.formatter.is(colorLogService.consoleFormatter)

        and: "file-style StreamHandler keeps the plain formatter (getCustomFormatter is null → unchanged)"
        fileHandler.formatter.is(plainFormatter)
    }
}
