package liquibase.sdk.verifytest

import spock.lang.Specification

class TestPermutationTest extends Specification {

    def "getKey with no definition is empty string"() {
        given:
        def test = new VerifiedTest("com.example.ExampleTest", "testing example")
        def permutation1 = new TestPermutation(test)

        expect:
        permutation1.getKey() == ""
    }
}
