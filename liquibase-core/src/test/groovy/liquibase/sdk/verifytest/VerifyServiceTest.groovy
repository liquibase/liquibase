package liquibase.sdk.verifytest

import spock.lang.Specification

class VerifyServiceTest extends Specification {

    def "getFileName"() {
        when:
        def testName = "testing something"
        def instance = VerifyService.getInstance("com.example.ExampleClass", testName)
        def baseDirectory = instance.getBaseDirectory().absolutePath

        then:
        instance.getFile().absolutePath.replace(baseDirectory, "").replace("\\", "/") == "/com/example/ExampleClass.testing_something.accepted.md"
    }

    def "getBaseDirectory"() {
        expect:
        VerifyService.getInstance("liquibase.sdk.verifytest.VerifiedTestFactory", "test something").getBaseDirectory().absolutePath.endsWith("src"+File.separator+"test"+File.separator+"resources")
    }
}
