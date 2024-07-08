package liquibase.integration.commandline

import spock.lang.Specification
import spock.lang.Unroll

class CommandLineArgumentValueProviderTest extends Specification {

    @Unroll
    def keyMatches() {
        expect:
        new CommandLineArgumentValueProvider(null).keyMatches(wantedKey, storedKey) == expected

        where:
        wantedKey                 | storedKey             | expected
        "liquibase.logLevel"      | "log-level"           | true
        "liquibase.logLevel"      | "logLevel"            | true
        "liquibase.logLevel"      | "LOG-LEVEL"           | true
        "liquibase.logLevel"      | "LOGLEVEL"            | true
        "liquibase.logLevel"      | "liquibase-log-level" | true
        "liquibase.logLevelX"     | "log-level"           | false
        "liquibase.hub.mode"      | "hub-mode"            | true
        "liquibase.hub.mode"      | "hubmode"             | true
        "liquibase.hub.mode"      | "liquibase-hub-mode"  | true
        "other.config"            | "config"              | false
        "other.config"            | "other-config"        | true
        "other.config"            | "otherConfig"         | true
        "other.long.config"       | "otherLongConfig"     | true
        "other.long.config"       | "other-long-config"   | true
        "liquibase.command.myArg" | "my-arg"              | true
        "liquibase.command.myArg" | "myArg"               | true
        "liquibase.command.myArg" | "wrong-arg"           | false
    }
}
