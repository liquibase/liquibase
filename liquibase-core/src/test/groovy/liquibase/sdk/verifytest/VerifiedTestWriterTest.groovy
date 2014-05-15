package liquibase.sdk.verifytest

import liquibase.change.AddColumnConfig
import liquibase.change.core.CreateTableChange
import spock.lang.Specification

class VerifiedTestWriterTest extends Specification {
    def "output empty test"() {
        when:
        def test = new VerifiedTest("com.example.Test", "my test name")
        def out = new StringWriter()

        then:
        new VerifiedTestWriter().write(test, out, null)

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
        new VerifiedTestWriter().write(test, out, null)

        out.toString().trim() == '''
# Test: com.example.Test "complex test" #

NOTE: This output is generated when the test is ran. DO NOT EDIT MANUALLY

## Permutation: 8af3fcf180bb7335 ##

- _VERIFIED:_ true
- **Add Column Parameter 1:** name=col 1
- **Group Param:** b

#### Data ####

- **String data:** No notes, just one data


## Permutation: 1c90803416ec5a06 ##

- _VERIFIED:_ true
- **Class Parameter:** liquibase.change.core.CreateTableChange
- **Group Param:** a
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


## Permutation: 955bcaf822e25d2f ##

- _VERIFIED:_ true
- **Group Param:** a
- **String Parameter 1:** Just param 1 on permutation 3

#### Data ####

- **String data:** No notes, just one data


## Permutation: c8bd894d4bbc320b ##

- _VERIFIED:_ true
- **Group Param:** b
- **Multiline Parameter =>**
    A Longer param with
    this on a second line
- **String Parameter 1:** Short Parameter

#### Data ####

- **String data:** No notes, just one data


## Permutation: 4930c7f23f3d9876 ##

- _VERIFIED:_ false Was too lazy
- **Group Param:** b
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
        new VerifiedTestWriter().write(test, out, null)

        out.toString().trim() == '''
# Test: com.example.Test "complex test with table" #

NOTE: This output is generated when the test is ran. DO NOT EDIT MANUALLY

## Permutation Group for Table Param: 8af3fcf180bb7335 ##

- **Add Column Parameter 1:** name=col 1
- **Group Param:** b

| Permutation      | Verified | table param 2 | DETAILS
| f9b7e83ad5bc4b1d | true     | tp2 is b      | **String data**: No notes, just one data


## Permutation Group for Table Param: 18d9d1387a32c2fc ##

- **Group Param:** a
- **Multiline Parameter =>**
    A Longer param with
    this on a second line
- **String Parameter 1:** Short Parameter

| Permutation      | Verified | table param 2 | DETAILS
| a68130beea635bec | true     | tp2 is b      | **String data**: No notes, just one data


## Permutation Group for Table Param: 7d75ae643ea143ae ##

- **Group Param:** a
- **Param 1:** param 1 is a
- **Param 2:** param 2 is a

| Permutation      | Verified | table param 1 | table param 2 | DETAILS
| 060a77fd1acc5f4a | true     | tp1 is a      | tp2 is a      | **out data**: Permutation 1


## Permutation Group for Table Param: 24076f6d9d1e3902 ##

- **Group Param:** b
- **Param 1:** param 1 is b
- **Param 2:** param 2 is b

| Permutation      | Verified | table param 1 | table param 2 | table param 3 | DETAILS
| 4d89b3a53901f034 | false    |               | tp2 is b      |               | **out data**: Permutation 2
| 1560a26a8a7692d2 | false    |               | tp2 is e      |               | **out data**: Permutation 5
| d2cce7457ad1c274 | false    | tp1 is d      | tp2 is d      | tp3 is d      | __more info__: Some notes for this
|                  |          |               |               |               | __yet more info__: Even more notes for this
|                  |          |               |               |               | **more out data**: Permutation 4 extra data
|                  |          |               |               |               | **out data**: Permutation 4


## Permutation Group for Table Param: 14e2a171fa3e1eb9 ##

- **Group Param:** b
- **Param 1:** param 1 is c
- **Param 2:** param 2 is c

| Permutation      | Verified | table param 1 | table param 2 | table param 3 | DETAILS
| 64837e3558e4ad73 | true     | tp1 is c      | tp2 is c      | tp3 is c      | __more info__: Some notes for permutation 3
|                  |          |               |               |               | **out data**: Permutation 3


## Permutation Group for Another Param Set: fd46209776fe3cb2 ##

- **Group Param:** b
- **Param 1:** param 1 is d
- **Param 2:** param 2 is d

| Permutation      | Verified | table param x1 | DETAILS
| e1aa992cb6b2145c | false    | tpx1 is a      | **out data**: Permutation 6
| 28cb50a95c64c5f5 | false    | tpx1 is b      | **out data**: Permutation 7<br>With a second line with \\| chars<br>And another with \\| chars
'''.trim()

        cleanup:
        out && out.close()
    }


    def "output complex test using groups"() {
        when:
        def test = createComplexTest()

        then:
        def out = new StringWriter()
        new VerifiedTestWriter().write(test, out, "Group Param: invalid")
        out.toString().trim() == '''
# Test: com.example.Test "complex test" Group "Group Param: invalid" #

NOTE: This output is generated when the test is ran. DO NOT EDIT MANUALLY
'''.trim()

        def out2 = new StringWriter()
        new VerifiedTestWriter().write(test, out2, "Group Param: b")
        out2.toString().trim() == '''
# Test: com.example.Test "complex test" Group "Group Param: b" #

NOTE: This output is generated when the test is ran. DO NOT EDIT MANUALLY

## Permutation: 8af3fcf180bb7335 ##

- _VERIFIED:_ true
- **Add Column Parameter 1:** name=col 1
- **Group Param:** b

#### Data ####

- **String data:** No notes, just one data


## Permutation: c8bd894d4bbc320b ##

- _VERIFIED:_ true
- **Group Param:** b
- **Multiline Parameter =>**
    A Longer param with
    this on a second line
- **String Parameter 1:** Short Parameter

#### Data ####

- **String data:** No notes, just one data


## Permutation: 4930c7f23f3d9876 ##

- _VERIFIED:_ false Was too lazy
- **Group Param:** b
- **String Parameter 1:** param 1 on permutation 2
- **String Parameter 2:** param 2 on permutation 2

#### Data ####

- **String data:** No notes, just one data
'''.trim()

        def out3 = new StringWriter()
        new VerifiedTestWriter().write(test, out3, "Group Param: a")
        out3.toString().trim() == '''
# Test: com.example.Test "complex test" Group "Group Param: a" #

NOTE: This output is generated when the test is ran. DO NOT EDIT MANUALLY

## Permutation: 1c90803416ec5a06 ##

- _VERIFIED:_ true
- **Class Parameter:** liquibase.change.core.CreateTableChange
- **Group Param:** a
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


## Permutation: 955bcaf822e25d2f ##

- _VERIFIED:_ true
- **Group Param:** a
- **String Parameter 1:** Just param 1 on permutation 3

#### Data ####

- **String data:** No notes, just one data'''.trim()

        cleanup:
        out && out.close()
        out2 && out2.close()
        out2 && out2.close()

    }

    def "output complex test with tables using groups"() {
        when:
        def test = createComplexTestWithTable()

        then:
        def out = new StringWriter()
        new VerifiedTestWriter().write(test, out, "Group Param: invalid")
        out.toString().trim() == '''
# Test: com.example.Test "complex test with table" Group "Group Param: invalid" #

NOTE: This output is generated when the test is ran. DO NOT EDIT MANUALLY
'''.trim()

        def out2 = new StringWriter()
        new VerifiedTestWriter().write(test, out2, "Group Param: b")
        out2.toString().trim() == '''
# Test: com.example.Test "complex test with table" Group "Group Param: b" #

NOTE: This output is generated when the test is ran. DO NOT EDIT MANUALLY

## Permutation Group for Table Param: 8af3fcf180bb7335 ##

- **Add Column Parameter 1:** name=col 1
- **Group Param:** b

| Permutation      | Verified | table param 2 | DETAILS
| f9b7e83ad5bc4b1d | true     | tp2 is b      | **String data**: No notes, just one data


## Permutation Group for Table Param: 24076f6d9d1e3902 ##

- **Group Param:** b
- **Param 1:** param 1 is b
- **Param 2:** param 2 is b

| Permutation      | Verified | table param 1 | table param 2 | table param 3 | DETAILS
| 4d89b3a53901f034 | false    |               | tp2 is b      |               | **out data**: Permutation 2
| 1560a26a8a7692d2 | false    |               | tp2 is e      |               | **out data**: Permutation 5
| d2cce7457ad1c274 | false    | tp1 is d      | tp2 is d      | tp3 is d      | __more info__: Some notes for this
|                  |          |               |               |               | __yet more info__: Even more notes for this
|                  |          |               |               |               | **more out data**: Permutation 4 extra data
|                  |          |               |               |               | **out data**: Permutation 4


## Permutation Group for Table Param: 14e2a171fa3e1eb9 ##

- **Group Param:** b
- **Param 1:** param 1 is c
- **Param 2:** param 2 is c

| Permutation      | Verified | table param 1 | table param 2 | table param 3 | DETAILS
| 64837e3558e4ad73 | true     | tp1 is c      | tp2 is c      | tp3 is c      | __more info__: Some notes for permutation 3
|                  |          |               |               |               | **out data**: Permutation 3


## Permutation Group for Another Param Set: fd46209776fe3cb2 ##

- **Group Param:** b
- **Param 1:** param 1 is d
- **Param 2:** param 2 is d

| Permutation      | Verified | table param x1 | DETAILS
| e1aa992cb6b2145c | false    | tpx1 is a      | **out data**: Permutation 6
| 28cb50a95c64c5f5 | false    | tpx1 is b      | **out data**: Permutation 7<br>With a second line with \\| chars<br>And another with \\| chars
'''.trim()

        def out3 = new StringWriter()
        new VerifiedTestWriter().write(test, out3, "Group Param: a")
        out3.toString().trim() == '''
# Test: com.example.Test "complex test with table" Group "Group Param: a" #

NOTE: This output is generated when the test is ran. DO NOT EDIT MANUALLY

## Permutation Group for Table Param: 18d9d1387a32c2fc ##

- **Group Param:** a
- **Multiline Parameter =>**
    A Longer param with
    this on a second line
- **String Parameter 1:** Short Parameter

| Permutation      | Verified | table param 2 | DETAILS
| a68130beea635bec | true     | tp2 is b      | **String data**: No notes, just one data


## Permutation Group for Table Param: 7d75ae643ea143ae ##

- **Group Param:** a
- **Param 1:** param 1 is a
- **Param 2:** param 2 is a

| Permutation      | Verified | table param 1 | table param 2 | DETAILS
| 060a77fd1acc5f4a | true     | tp1 is a      | tp2 is a      | **out data**: Permutation 1'''.trim()

        cleanup:
        out && out.close()
        out2 && out2.close()
        out2 && out2.close()

    }

    def createComplexTest() {
        def test = new VerifiedTest("com.example.Test", "complex test")

        def permutation = new TestPermutation(test)
        permutation.setVerified(true)
        permutation.describe("String Parameter 1", "param 1")
        permutation.describe("String Parameter 2", "param 2")
        permutation.describe("Int Parameter", 4)
        permutation.describeAsGroup("Group Param", "a")
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
        permutation.describeAsGroup("Group Param", "b")
        permutation.data("String data", "No notes, just one data")

        permutation = new TestPermutation(test)
        permutation.setVerified(false)
        permutation.setValid(false)
        permutation.notRanMessage = "Invalid: Something was wrong with the parameters"
        permutation.describe("String Parameter 1", "param 1 on permutation 3")
        permutation.describe("String Parameter 2", "param 2 on permutation 3")
        permutation.describeAsGroup("Group Param", "a")
        permutation.data("String data", "No notes, just one data")


        permutation = new TestPermutation(test)
        permutation.setVerified(true)
        permutation.describe("String Parameter 1", "Just param 1 on permutation 3")
        permutation.describeAsGroup("Group Param", "a")
        permutation.data("String data", "No notes, just one data")

        permutation = new TestPermutation(test)
        permutation.setVerified(true)
        permutation.describe("Add Column Parameter 1", new AddColumnConfig().setName("col 1"))
        permutation.describeAsGroup("Group Param", "b")
        permutation.data("String data", "No notes, just one data")

        permutation = new TestPermutation(test)
        permutation.setVerified(true)
        permutation.describe("String Parameter 1", "Short Parameter")
        permutation.describeAsGroup("Group Param", "b")
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
        permutation.describeAsGroup("Group Param", "a")
        permutation.describeAsTable("Table Param", ["table param 1":"tp1 is a", "table param 2":"tp2 is a"])
        permutation.data("out data", "Permutation 1")

        permutation = new TestPermutation(test)
        permutation.setVerified(false)
        permutation.describe("Param 1", "param 1 is b")
        permutation.describeAsGroup("Group Param", "b")
        permutation.describe("Param 2", "param 2 is b")
        permutation.describeAsTable("Table Param", ["table param 2":"tp2 is b"])
        permutation.data("out data", "Permutation 2")

        permutation = new TestPermutation(test)
        permutation.setVerified(true)
        permutation.describe("Param 1", "param 1 is c")
        permutation.describe("Param 2", "param 2 is c")
        permutation.describeAsTable("Table Param", ["table param 1":"tp1 is c", "table param 2":"tp2 is c", "table param 3":"tp3 is c"])
        permutation.describeAsGroup("Group Param", "b")
        permutation.data("out data", "Permutation 3")
        permutation.note("more info", "Some notes for permutation 3")

        permutation = new TestPermutation(test)
        permutation.setVerified(false)
        permutation.describe("Param 1", "param 1 is b")
        permutation.describe("Param 2", "param 2 is b")
        permutation.describeAsTable("Table Param", ["table param 1":"tp1 is d", "table param 2":"tp2 is d", "table param 3":"tp3 is d"])
        permutation.describeAsGroup("Group Param", "b")
        permutation.data("out data", "Permutation 4")
        permutation.data("more out data", "Permutation 4 extra data")
        permutation.note("more info", "Some notes for this")
        permutation.note("yet more info", "Even more notes for this")

        permutation = new TestPermutation(test)
        permutation.setVerified(false)
        permutation.describe("Param 1", "param 1 is b")
        permutation.describe("Param 2", "param 2 is b")
        permutation.describeAsGroup("Group Param", "b")
        permutation.describeAsTable("Table Param", ["table param 2":"tp2 is e"])
        permutation.data("out data", "Permutation 5")

        permutation = new TestPermutation(test)
        permutation.setVerified(false)
        permutation.describe("Param 1", "param 1 is d")
        permutation.describe("Param 2", "param 2 is d")
        permutation.describeAsGroup("Group Param", "b")
        permutation.describeAsTable("Another Param Set", ["table param x1":"tpx1 is a"])
        permutation.data("out data", "Permutation 6")

        permutation = new TestPermutation(test)
        permutation.setVerified(false)
        permutation.describe("Param 1", "param 1 is d")
        permutation.describe("Param 2", "param 2 is d")
        permutation.describeAsGroup("Group Param", "b")
        permutation.describeAsTable("Another Param Set", ["table param x1":"tpx1 is b"])
        permutation.data("out data", "Permutation 7\nWith a second line with | chars\nAnd another with | chars")

        permutation = new TestPermutation(test)
        permutation.setVerified(true)
        permutation.describe("Add Column Parameter 1", new AddColumnConfig().setName("col 1"))
        permutation.describeAsTable("Table Param", ["table param 2":"tp2 is b"])
        permutation.describeAsGroup("Group Param", "b")
        permutation.data("String data", "No notes, just one data")

        permutation = new TestPermutation(test)
        permutation.setVerified(true)
        permutation.describe("String Parameter 1", "Short Parameter")
        permutation.describeAsGroup("Group Param", "a")
        permutation.describe("Multiline Parameter", "A Longer param with\nthis on a second line")
        permutation.describeAsTable("Table Param", ["table param 2":"tp2 is b"])
        permutation.data("String data", "No notes, just one data")


        return test

    }
}
