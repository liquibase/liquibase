package liquibase.sdk.verifytest

import liquibase.change.core.CreateTableChange
import spock.lang.Specification

class VerifiedTestWriterSpec extends Specification {
    def "output empty test"() {
        when:
        def test = new VerifiedTest("com.example.Test", "my test name")
        def out = new StringWriter()

        then:
        new VerifiedTestWriter().write(test, out)

        out.toString().trim() == '''
# Test: com.example.Test "my test name" #

NOTE: This output is generated when the test is ran. DO NOT EDIT MANUALLY'''.trim()

        cleanup:
        out.close()
    }

    def "output complex test"() {
        when:
        def test = createComplexTest()

        then:
        def out = new StringWriter()
        new VerifiedTestWriter().write(test, out)

        out.toString().trim() == '''
# Test: com.example.Test "complex test" #

NOTE: This output is generated when the test is ran. DO NOT EDIT MANUALLY

## Permutation: f4d5aa7adf0ae5b77cae7615b8720396 ##

- _VERIFIED:_ true
- **Class Parameter:** liquibase.change.core.CreateTableChange
- **Int Parameter:** 4
- **Integer Parameter:** 42
- **Multiline Parameter =>**
    I have a line
    And another line
    And a third line
    This  one  has  double  spaces
- **String Parameter 1:** param 1
- **String Parameter 2:** param 2

#### Notes ####

- **Int Note:** 838
- **String note:** note goes here

#### Data ####

- **String data:** I see data here
- **int data:** 3838


## Permutation: d9b39bdebc8f1f3fb45bb9eb7bf463a3 ##

- _VERIFIED:_ true
- **String Parameter 1:** Just param 1 on permutation 3

#### Data ####

- **String data:** No notes, just one data


## Permutation: 7bf38f65be1d554e5784987489cf5791 ##

- _VERIFIED:_ false Was too lazy
- **String Parameter 1:** param 1 on permutation 2
- **String Parameter 2:** param 2 on permutation 2

#### Data ####

- **String data:** No notes, just one data'''.trim()

        cleanup:
        out && out.close()
    }

    def createComplexTest() {
        def test = new VerifiedTest("com.example.Test", "complex test")

        def permutation = new TestPermutation(test)
        permutation.setVerified(true)
        permutation.describe("String Parameter 1", "param 1")
        permutation.describe("String Parameter 2", "param 2")
        permutation.describe("Int Parameter", 4)
        permutation.describe("Integer Parameter", new Integer(42))
        permutation.describe("Class Parameter", CreateTableChange.class)
        permutation.describe("Multiline Parameter", "I have a line\nAnd another line\nAnd a third line\nThis  one  has  double  spaces")

        permutation.note("String note", "note goes here")
        permutation.note("Int Note", 838)

        permutation.data("String data", "I see data here")
        permutation.data("int data", 3838)


        permutation = new TestPermutation(test)
        permutation.setVerified(false)
        permutation.notVerifiedMessage = "Was too lazy"
        permutation.describe("String Parameter 1", "param 1 on permutation 2")
        permutation.describe("String Parameter 2", "param 2 on permutation 2")
        permutation.data("String data", "No notes, just one data")


        permutation = new TestPermutation(test)
        permutation.setVerified(true)
        permutation.describe("String Parameter 1", "Just param 1 on permutation 3")
        permutation.data("String data", "No notes, just one data")

        return test
    }
}
