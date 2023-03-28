package liquibase.ui

import liquibase.Scope
import liquibase.logging.core.BufferedLogService
import spock.lang.Specification

import java.util.logging.Level

class CompositeUIServiceTest extends Specification {
    def "Should log console messages with composite service"(String message, boolean error, Level level) {
        when:
        BufferedLogService bufferLog = new BufferedLogService()
        def console = new ConsoleUIService()
        def uiServices = [console, new LoggerUIService()]
        def compositeUIService = new CompositeUIService(console, uiServices as Collection<UIService>);
        Scope.child(Scope.Attr.logService.name(), bufferLog, {
            if (error) {
                compositeUIService.sendErrorMessage(message)
            } else {
                compositeUIService.sendMessage(message)
            }
        })

        then:
        bufferLog.getLogAsString(level).contains(message)

        where:
        message              | error | level
        "Some info message"  | false | Level.INFO
        "Some error message" | true  | Level.SEVERE
    }
}
