package liquibase.logging.core

import com.example.liquibase.change.CreateTableExampleChange
import liquibase.Scope
import liquibase.change.Change
import liquibase.change.core.CreateTableChange
import liquibase.util.StringUtil
import spock.lang.Specification
import spock.lang.Unroll

import java.util.logging.Level

class BufferedLogServiceTest extends Specification {
    def "getLogAsString truncates"() {
        when:
        String startMessage = "Test message"
        for (int i=0; i < 1000; i++) {
            startMessage += "-Test Message"
        }
        String message = ""
        for (int i=0; i < 1000; i++) {
            message += startMessage
        }
        BufferedLogService bufferedLogService = new BufferedLogService()
        BufferedLogService.BufferedLogMessage log =
           new BufferedLogService.BufferedLogMessage(Level.FINE, BufferedLogServiceTest.class, message, null)
        bufferedLogService.addLog(log)
        String logOutput = bufferedLogService.getLogAsString(Level.FINE)

        then:
        logOutput != null
        logOutput.length() == 10000000
    }
}
