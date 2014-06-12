package liquibase.sdk.verifytest

import liquibase.exception.UnexpectedLiquibaseException
import spock.lang.Specification

class TestPermutationTest extends Specification {

    def setupRunCount
    def executeRunCount
    def cleanupRunCount
    TestPermutation permutation
    TestPermutation previousRun

    def setup() {
        setupRunCount = 0
        executeRunCount = 0
        cleanupRunCount = 0

        permutation = new TestPermutation([a: 1, b: 2])
                .data("out", 100)
                .setup({ setupRunCount++; new TestPermutation.OkResult() } as TestPermutation.Setup)
                .expect({ executeRunCount++ } as TestPermutation.Verification)
                .cleanup({ cleanupRunCount++ } as TestPermutation.Cleanup)

        previousRun = new TestPermutation([a: 1, b: 2])
                .data("out", 100)
                .setVerified(true)
    }

    def "getKey with no definition is empty string"() {
        expect:
        new TestPermutation(new HashMap<String, Object>()).getKey() == ""
    }

    def "test() when no previous run"() {
        when:
        permutation.setPreviousRun(null)
        permutation.test()

        then:
        setupRunCount == 1
        executeRunCount == 1
        cleanupRunCount == 1
        assert permutation.isValid()
        assert permutation.getVerified()
        assert permutation.canSave

    }

    def "test() when previous previous run was verified"() {
        when:
        permutation.setPreviousRun(previousRun)
        previousRun.setVerified(true)
        permutation.test()

        then:
        setupRunCount == 0
        executeRunCount == 0
        cleanupRunCount == 0

        assert permutation.isValid()
        assert permutation.getVerified()
        assert permutation.canSave
    }

    def "test() when previous previous run was not verified"() {
        when:
        permutation.setPreviousRun(previousRun)
        previousRun.setVerified(false)
        permutation.test()

        then:
        setupRunCount == 1
        executeRunCount == 1
        cleanupRunCount == 1

        assert permutation.isValid()
        assert permutation.getVerified()
        assert permutation.canSave
    }

    def "test() when previous previous run was verified but had a different output"() {
        when:
        permutation.setPreviousRun(previousRun)
        previousRun.data("out", 1000)
        previousRun.setVerified(true)
        permutation.test()

        then:
        setupRunCount == 1
        executeRunCount == 1
        cleanupRunCount == 1

        assert permutation.isValid()
        assert permutation.getVerified()
        assert permutation.canSave
    }

    def "test() when setup throws exception"() {
        when:
        permutation.setup({throw new RuntimeException("Testing exception")} as TestPermutation.Setup)
        permutation.test()

        then:
        def e = thrown(UnexpectedLiquibaseException)
        assert e.message.startsWith("Error executing setup")
        assert e.cause.message == "Testing exception"
        assert !permutation.getVerified()
        assert !permutation.isValid()
        permutation.getNotRanMessage() == "Testing exception"
        assert !permutation.canSave
    }

    def "test() when setup returns CannotVerify"() {
        when:
        permutation.setup({setupRunCount++; return new TestPermutation.CannotVerify("cannot verify message")} as TestPermutation.Setup)
        permutation.test()

        then:
        setupRunCount == 1
        executeRunCount == 0
        cleanupRunCount == 0
        assert !permutation.getVerified()
        assert permutation.isValid()
        permutation.getNotRanMessage() == "cannot verify message"
        assert permutation.canSave
    }

    def "test() when setup returns Invalid"() {
        when:
        permutation.setup({setupRunCount++; return new TestPermutation.Invalid("invalid message")} as TestPermutation.Setup)
        permutation.test()

        then:
        setupRunCount == 1
        executeRunCount == 0
        cleanupRunCount == 0
        assert !permutation.getVerified()
        assert !permutation.isValid()
        permutation.getNotRanMessage() == "invalid message"
        assert permutation.canSave
    }
}
