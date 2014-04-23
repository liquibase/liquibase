package liquibase.sdk.verifytest

import liquibase.change.AddColumnConfig
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

## Permutation: d4b53d3a50a2359fe867c6601320a64e ##

- _VERIFIED:_ true
- **Add Column Parameter 1:** name=col 1

#### Data ####

- **String data:** No notes, just one data


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


## Permutation: 2ee12a0f494678ca48ec84ac97d911f5 ##

- _VERIFIED:_ true
- **Multiline Parameter =>**
    A Longer param with
    this on a second line
- **String Parameter 1:** Short Parameter

#### Data ####

- **String data:** No notes, just one data


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

    def "output complex test with table"() {
        when:
        def test = createComplexTestWithTable()

        then:
        def out = new StringWriter()
        new VerifiedTestWriter().write(test, out)

        out.toString().trim() == '''
# Test: com.example.Test "complex test with table" #

NOTE: This output is generated when the test is ran. DO NOT EDIT MANUALLY

## Permutation Group for Table Param: d4b53d3a50a2359fe867c6601320a64e ##

- **Add Column Parameter 1:** name=col 1

| Permutation                      | Verified | table param 2 | DETAILS
| f6fbc90d15003817e508ee6d1cb61068 | true     | tp2 is b      | **String data**: No notes, just one data


## Permutation Group for Table Param: 2ee12a0f494678ca48ec84ac97d911f5 ##

- **Multiline Parameter =>**
    A Longer param with
    this on a second line
- **String Parameter 1:** Short Parameter

| Permutation                      | Verified | table param 2 | DETAILS
| 8caa0b27bd1db4b3cced303fa7b61aa9 | true     | tp2 is b      | **String data**: No notes, just one data


## Permutation Group for Table Param: b5cb63edb30386a18168a95c0ab25dd5 ##

- **Param 1:** param 1 is a
- **Param 2:** param 2 is a

| Permutation                      | Verified | table param 1 | table param 2 | DETAILS
| f74f75555062a663e1a4b5e0da9b3a0c | true     | tp1 is a      | tp2 is a      | **out data**: Permutation 1


## Permutation Group for Table Param: f24df6e8a26c4ffd549d0fe2a30a5d55 ##

- **Param 1:** param 1 is b
- **Param 2:** param 2 is b

| Permutation                      | Verified | table param 1 | table param 2 | table param 3 | DETAILS
| d6cd3f835d6b38943590bc1364c56291 | false    |               | tp2 is b      |               | **out data**: Permutation 2
| 4733eb00327d488a1800d0880a9f99d6 | false    |               | tp2 is e      |               | **out data**: Permutation 5
| 6e57bf0c5d8da7e9cdfae8dd00691a11 | false    | tp1 is d      | tp2 is d      | tp3 is d      | __more info__: Some notes for this
|                                  |          |               |               |               | __yet more info__: Even more notes for this
|                                  |          |               |               |               | **more out data**: Permutation 4 extra data
|                                  |          |               |               |               | **out data**: Permutation 4


## Permutation Group for Table Param: 93856bc3d12defde2112420e969d3893 ##

- **Param 1:** param 1 is c
- **Param 2:** param 2 is c

| Permutation                      | Verified | table param 1 | table param 2 | table param 3 | DETAILS
| 84666008d474dbf4e353570e19d6a7fb | true     | tp1 is c      | tp2 is c      | tp3 is c      | __more info__: Some notes for permutation 3
|                                  |          |               |               |               | **out data**: Permutation 3


## Permutation Group for Another Param Set: 4e9b7a0b2822d5b32cf08e1cbcef5c1e ##

- **Param 1:** param 1 is d
- **Param 2:** param 2 is d

| Permutation                      | Verified | table param x1 | DETAILS
| f5ed4e1856f7d668583cc40fb0ece552 | false    | tpx1 is a      | **out data**: Permutation 6
| 3e7cadb79572013c3b0b823d79e65b51 | false    | tpx1 is b      | **out data**: Permutation 7<br>With a second line with \\| chars<br>And another with \\| chars
'''.trim()

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
        permutation.notRanMessage = "Was too lazy"
        permutation.describe("String Parameter 1", "param 1 on permutation 2")
        permutation.describe("String Parameter 2", "param 2 on permutation 2")
        permutation.data("String data", "No notes, just one data")

        permutation = new TestPermutation(test)
        permutation.setVerified(false)
        permutation.setValid(false)
        permutation.notRanMessage = "Invalid: Something was wrong with the parameters"
        permutation.describe("String Parameter 1", "param 1 on permutation 3")
        permutation.describe("String Parameter 2", "param 2 on permutation 3")
        permutation.data("String data", "No notes, just one data")


        permutation = new TestPermutation(test)
        permutation.setVerified(true)
        permutation.describe("String Parameter 1", "Just param 1 on permutation 3")
        permutation.data("String data", "No notes, just one data")

        permutation = new TestPermutation(test)
        permutation.setVerified(true)
        permutation.describe("Add Column Parameter 1", new AddColumnConfig().setName("col 1"))
        permutation.data("String data", "No notes, just one data")

        permutation = new TestPermutation(test)
        permutation.setVerified(true)
        permutation.describe("String Parameter 1", "Short Parameter")
        permutation.describe("Multiline Parameter", "A Longer param with\nthis on a second line")
        permutation.data("String data", "No notes, just one data")

        return test
    }

    def createComplexTestWithTable() {
        def test = new VerifiedTest("com.example.Test", "complex test with table")

        def permutation = new TestPermutation(test)
        permutation.setVerified(true)
        permutation.describe("Param 1", "param 1 is a")
        permutation.describe("Param 2", "param 2 is a")
        permutation.describeAsTable("Table Param", ["table param 1":"tp1 is a", "table param 2":"tp2 is a"])
        permutation.data("out data", "Permutation 1")

        permutation = new TestPermutation(test)
        permutation.setVerified(false)
        permutation.describe("Param 1", "param 1 is b")
        permutation.describe("Param 2", "param 2 is b")
        permutation.describeAsTable("Table Param", ["table param 2":"tp2 is b"])
        permutation.data("out data", "Permutation 2")

        permutation = new TestPermutation(test)
        permutation.setVerified(true)
        permutation.describe("Param 1", "param 1 is c")
        permutation.describe("Param 2", "param 2 is c")
        permutation.describeAsTable("Table Param", ["table param 1":"tp1 is c", "table param 2":"tp2 is c", "table param 3":"tp3 is c"])
        permutation.data("out data", "Permutation 3")
        permutation.note("more info", "Some notes for permutation 3")

        permutation = new TestPermutation(test)
        permutation.setVerified(false)
        permutation.describe("Param 1", "param 1 is b")
        permutation.describe("Param 2", "param 2 is b")
        permutation.describeAsTable("Table Param", ["table param 1":"tp1 is d", "table param 2":"tp2 is d", "table param 3":"tp3 is d"])
        permutation.data("out data", "Permutation 4")
        permutation.data("more out data", "Permutation 4 extra data")
        permutation.note("more info", "Some notes for this")
        permutation.note("yet more info", "Even more notes for this")

        permutation = new TestPermutation(test)
        permutation.setVerified(false)
        permutation.describe("Param 1", "param 1 is b")
        permutation.describe("Param 2", "param 2 is b")
        permutation.describeAsTable("Table Param", ["table param 2":"tp2 is e"])
        permutation.data("out data", "Permutation 5")

        permutation = new TestPermutation(test)
        permutation.setVerified(false)
        permutation.describe("Param 1", "param 1 is d")
        permutation.describe("Param 2", "param 2 is d")
        permutation.describeAsTable("Another Param Set", ["table param x1":"tpx1 is a"])
        permutation.data("out data", "Permutation 6")

        permutation = new TestPermutation(test)
        permutation.setVerified(false)
        permutation.describe("Param 1", "param 1 is d")
        permutation.describe("Param 2", "param 2 is d")
        permutation.describeAsTable("Another Param Set", ["table param x1":"tpx1 is b"])
        permutation.data("out data", "Permutation 7\nWith a second line with | chars\nAnd another with | chars")

        permutation = new TestPermutation(test)
        permutation.setVerified(true)
        permutation.describe("Add Column Parameter 1", new AddColumnConfig().setName("col 1"))
        permutation.describeAsTable("Table Param", ["table param 2":"tp2 is b"])
        permutation.data("String data", "No notes, just one data")

        permutation = new TestPermutation(test)
        permutation.setVerified(true)
        permutation.describe("String Parameter 1", "Short Parameter")
        permutation.describe("Multiline Parameter", "A Longer param with\nthis on a second line")
        permutation.describeAsTable("Table Param", ["table param 2":"tp2 is b"])
        permutation.data("String data", "No notes, just one data")


        return test

    }
}
