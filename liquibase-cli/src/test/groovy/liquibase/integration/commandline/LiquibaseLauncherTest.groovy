package liquibase.integration.commandline

import liquibase.Scope
import liquibase.util.StringUtil
import org.xml.sax.InputSource
import spock.lang.Specification
import spock.lang.Unroll

class LiquibaseLauncherTest extends Specification {

    @Unroll
    def "duplicate jars are detected properly"(List<URL> jars, String message) {
        when:
        def uiService = new LiquibaseCommandLineTest.TestConsoleUIService()
        Scope.child([
                (Scope.Attr.ui.name()): uiService
        ], {
            LiquibaseLauncher.checkForDuplicatedJars(jars)
        } as Scope.ScopedRunnerWithReturn<InputSource>) != null

        then:
        StringUtil.standardizeLineEndings(uiService.getMessages().get(0)) == StringUtil.standardizeLineEndings(message)

        where:
        jars                                                                                  | message
        [new URL("file:///liquibase-core.jar"), new URL("file:///liquibase-core-4.23.1.jar")] | """WARNING: *** Duplicate Liquibase Core JAR files ***
/liquibase-core.jar
/liquibase-core-4.23.1.jar"""
        [new URL("file:///liquibase-commercial.jar"), new URL("file:///liquibase-commercial-4.23.1.jar")] | """WARNING: *** Duplicate Liquibase Commercial JAR files ***
/liquibase-commercial.jar
/liquibase-commercial-4.23.1.jar"""
        [new URL("file:///snakeyaml-1.33.0.jar"), new URL("file:///snakeyaml-2.0.jar")] | """WARNING: *** Duplicate snakeyaml JAR files ***
/snakeyaml-1.33.0.jar
/snakeyaml-2.0.jar"""
        [new URL("file:///snakeyaml-1.33.0.jar"), new URL("file:///snakeyaml.jar")] | """WARNING: *** Duplicate snakeyaml JAR files ***
/snakeyaml-1.33.0.jar
/snakeyaml.jar"""


    }
}
