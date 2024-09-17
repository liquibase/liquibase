package liquibase.command

import liquibase.command.core.HistoryCommandStep
import liquibase.util.StringUtil
import spock.lang.Specification

class CommandResultsTest extends Specification {

    def "can get and set value"() {
        when:
        def commandResults = new CommandResults(new TreeMap<>([
                "a"                                              : "value from a",
                "bool"                                           : true,
                (HistoryCommandStep.DEPLOYMENTS_RESULT.getName()): new HistoryCommandStep.DeploymentHistory()
        ]), new CommandScope("history"))

        then:
        commandResults.getCommandScope().getCommand().getName() == ["history"] as String[]
        commandResults.getResult("a") == "value from a"
        commandResults.getResult("bool") == true
        commandResults.getResult("invalid") == null

        commandResults.getResult(HistoryCommandStep.DEPLOYMENTS_RESULT).toString() == "0 past deployments"

        StringUtil.join(commandResults.getResults(), ", ") == "a=value from a, bool=true, deployments=0 past deployments"
    }
}
