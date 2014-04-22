package liquibase.sdk.verifytest

import spock.lang.Specification

class VerifiedTestTest extends Specification {

    def "remove permutation"() {
        given:
        def test = new VerifiedTest("com.example.TestClass", "a test")

        and:
        def permutation1 = new TestPermutation(test)
        permutation1.describe("param 1", "a")

        and:
        def permutation2 = new TestPermutation(test)
        permutation2.describe("param 2", "b")

        and:
        def permutation3 = new TestPermutation(test)
        permutation3.describe("param 3", "c")

        expect:
        test.permutations.size() == 3
        test.getPermutation(permutation1.key) != null
        test.getPermutation(permutation2.key) != null
        test.getPermutation(permutation3.key) != null

        test.removePermutation("bad key") //should do nothing
        test.permutations.size() == 3

        test.removePermutation(permutation2.key)
        test.permutations.size() == 2
        test.getPermutation(permutation1.key) != null
        test.getPermutation(permutation3.key) != null
        test.getPermutation(permutation2.key) == null

        test.removePermutation(permutation3.key)
        test.permutations.size() == 1
        test.getPermutation(permutation3.key) == null

        test.removePermutation(permutation1.key)
        test.permutations.size() == 0
        test.getPermutation(permutation1.key) == null
    }
}
