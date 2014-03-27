package liquibase.sdk.verifytest

import spock.lang.Specification

class VerifiedTestFactoryTest extends Specification {

    def "getFileName"() {
        when:
        def test = new VerifiedTest("com.example.ExampleClass", "testing something")

        then:
        VerifiedTestFactory.instance.getFile(test).absolutePath.replace(VerifiedTestFactory.instance.getBaseDirectory(test).absolutePath, "").replace("\\", "/") == "/com/example/ExampleClass.testing_something.accepted.md"
    }

    def "getBaseDirectory"() {
        when:
        def test = new VerifiedTest("liquibase.sdk.verifytest.VerifiedTestFactory", "testing something")

        then:
        VerifiedTestFactory.instance.getBaseDirectory(test).absolutePath.endsWith("src"+File.separator+"test"+File.separator+"resources")

    }
}
