package liquibase.sdk.verifytest

import liquibase.change.AddColumnConfig
import liquibase.change.core.CreateTableChange
import spock.lang.Specification

class VerifiedTestWriterTest extends Specification {
    def "output empty test"() {
        when:
        def out = new StringWriter()

        then:
        new VerifiedTestWriter().write("com.example.Test", "my test name", [], out)

        out.toString().trim() == '''
# Test: com.example.Test "my test name" #

NOTE: This output is generated when the test is ran. DO NOT EDIT MANUALLY'''.trim()

        cleanup:
        out.close()
    }

    def "output complex test"() {
        when:
        def permutations = createComplexPermutations()

        then:
        def out = new StringWriter()
        new VerifiedTestWriter().write("com.example.Test", "complex test", permutations, out)

        out.toString().trim() == '''
# Test: com.example.Test "complex test" #

NOTE: This output is generated when the test is ran. DO NOT EDIT MANUALLY

## Permutation: 1c9080 ##

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


## Permutation: 4930c7 ##

- _VERIFIED:_ Was too lazy
- **Group Param:** b
- **String Parameter 1:** param 1 on permutation 2
- **String Parameter 2:** param 2 on permutation 2

#### Data ####

- **String data:** No notes, just one data


## Permutation: 8af3fc ##

- _VERIFIED:_ true
- **Add Column Parameter 1:** name=col 1
- **Group Param:** b

#### Data ####

- **String data:** No notes, just one data


## Permutation: 955bca ##

- _VERIFIED:_ true
- **Group Param:** a
- **String Parameter 1:** Just param 1 on permutation 3

#### Data ####

- **String data:** No notes, just one data


## Permutation: c8bd89 ##

- _VERIFIED:_ true
- **Group Param:** b
- **Multiline Parameter =>**
    A Longer param with
    this on a second line
- **String Parameter 1:** Short Parameter

#### Data ####

- **String data:** No notes, just one data'''.trim()

        cleanup:
        out && out.close()
    }

    def "output complex test with table"() {
        when:
        def test = createComplexPermutationsWithTable()

        then:
        def out = new StringWriter()
        new VerifiedTestWriter().write("com.example.Test", "complex test with table", test, out)

        out.toString().trim() == '''
# Test: com.example.Test "complex test with table" #

NOTE: This output is generated when the test is ran. DO NOT EDIT MANUALLY

## Permutations ##

- **Group Param:** b
- **Param 1:** param 1 is c
- **Param 2:** param 2 is c

| Permutation | Verified | table param 1 | table param 2 | table param 3 | table param x1 | DETAILS
| 64837e      | true     | tp1 is c      | tp2 is c      | tp3 is c      |                | __more info__: Some notes for permutation 3
|             |          |               |               |               |                | **out data**: Permutation 3


## Permutations ##

- **Group Param:** a
- **Multiline Parameter =>**
    A Longer param with
    this on a second line
- **String Parameter 1:** Short Parameter

| Permutation | Verified | table param 1 | table param 2 | table param 3 | table param x1 | DETAILS
| a68130      | true     |               | tp2 is b      |               |                | **String data**: No notes, just one data


## Permutations ##

- **Group Param:** b
- **Param 1:** param 1 is b
- **Param 2:** param 2 is b

| Permutation | Verified | table param 1 | table param 2 | table param 3 | table param x1 | DETAILS
| 4d89b3      | false    |               | tp2 is b      |               |                | **out data**: Permutation 2
| 1560a2      | false    |               | tp2 is e      |               |                | **out data**: Permutation 5
| d2cce7      | false    | tp1 is d      | tp2 is d      | tp3 is d      |                | __more info__: Some notes for this
|             |          |               |               |               |                | __yet more info__: Even more notes for this
|             |          |               |               |               |                | **more out data**: Permutation 4 extra data
|             |          |               |               |               |                | **out data**: Permutation 4


## Permutations ##

- **Group Param:** a
- **Param 1:** param 1 is a
- **Param 2:** param 2 is a

| Permutation | Verified | table param 1 | table param 2 | table param 3 | table param x1 | DETAILS
| 060a77      | true     | tp1 is a      | tp2 is a      |               |                | **out data**: Permutation 1


## Permutations ##

- **Add Column Parameter 1:** name=col 1
- **Group Param:** b

| Permutation | Verified | table param 1 | table param 2 | table param 3 | table param x1 | DETAILS
| f9b7e8      | true     |               | tp2 is b      |               |                | **String data**: No notes, just one data


## Permutations ##

- **Group Param:** b
- **Param 1:** param 1 is d
- **Param 2:** param 2 is d

| Permutation | Verified | table param 1 | table param 2 | table param 3 | table param x1 | DETAILS
| e1aa99      | false    |               |               |               | tpx1 is a      | **out data**: Permutation 6
| 28cb50      | false    |               |               |               | tpx1 is b      | **out data**: Permutation 7<br>With a second line with \\| chars<br>And another with \\| chars
'''.trim()

        cleanup:
        out && out.close()
    }

    def createComplexPermutations() {
        def permutations = new ArrayList<TestPermutation>()

        def testName = "complex test"
        permutations.add(new TestPermutation([
                "String Parameter 1" : "param 1",
                "String Parameter 2" : "param 2",
                "Int Parameter"      : 4,
                "Group Param"        : "a",
                "Integer Parameter"  : new Integer(42),
                "Class Parameter"    : CreateTableChange.class,
                "Multiline Parameter": "I have a line\nAnd another line\nAnd a third line\nThis  one  has  double  spaces"])

                .note("String note", "note goes here")
                .note("Int Note", 838)
                .data("String data", "I see data here")
                .data("int data", 3838)
                .setVerified(true)
        )

        permutations.add(new TestPermutation([
                "String Parameter 1": "param 1 on permutation 2",
                "String Parameter 2": "param 2 on permutation 2",
                "Group Param"       : "b"])
                .data("String data", "No notes, just one data")
                .setNotRanMessage("Was too lazy")
                .setVerified(false)
        )

        permutations.add(new TestPermutation([
                "String Parameter 1": "param 1 on permutation 3",
                "String Parameter 2": "param 2 on permutation 3",
                "Group Param"       : "a"])
                .data("String data", "No notes, just one data")
                .setNotRanMessage("Invalid: Something was wrong with the parameters")
                .setVerified(false)
                .setValid(false))

        permutations.add(new TestPermutation([
                "String Parameter 1": "Just param 1 on permutation 3",
                "Group Param"       : "a"])
                .data("String data", "No notes, just one data")
                .setVerified(true))

        permutations.add(new TestPermutation([
                "Add Column Parameter 1": new AddColumnConfig().setName("col 1"),
                "Group Param"           : "b"])
                .data("String data", "No notes, just one data")
                .setVerified(true))

        permutations.add(new TestPermutation([
                "String Parameter 1" : "Short Parameter",
                "Group Param"        : "b",
                "Multiline Parameter": "A Longer param with\nthis on a second line"])
                .data("String data", "No notes, just one data")
                .setVerified(true))

        return permutations
    }

    def createComplexPermutationsWithTable() {
        def testName = "complex test with table"
        def permutations = new ArrayList<TestPermutation>()

        def tableColumns = ["table param 1", "table param 2", "table param 3", "table param x1"]

        permutations.add(new TestPermutation([
                "Param 1"      : "param 1 is a",
                "Param 2"      : "param 2 is a",
                "Group Param"  : "a",
                "table param 1": "tp1 is a",
                "table param 2": "tp2 is a"])
                .asTable(tableColumns)
                .data("out data", "Permutation 1")
                .setVerified(true))

        permutations.add(new TestPermutation([
                "Param 1"      : "param 1 is b",
                "Group Param"  : "b",
                "Param 2"      : "param 2 is b",
                "table param 2": "tp2 is b"])
                .asTable(tableColumns)
                .data("out data", "Permutation 2")
                .setVerified(false))

        permutations.add(new TestPermutation([
                "Param 1"      : "param 1 is c",
                "Param 2"      : "param 2 is c",
                "table param 1": "tp1 is c",
                "table param 2": "tp2 is c",
                "table param 3": "tp3 is c",
                "Group Param"  : "b"])
                .asTable(tableColumns)
                .data("out data", "Permutation 3")
                .note("more info", "Some notes for permutation 3")
                .setVerified(true))

        permutations.add(new TestPermutation([
                "Param 1"      : "param 1 is b",
                "Param 2"      : "param 2 is b",
                "table param 1": "tp1 is d",
                "table param 2": "tp2 is d",
                "table param 3": "tp3 is d",
                "Group Param"  : "b"])
                .asTable(tableColumns)
                .data("out data", "Permutation 4")
                .data("more out data", "Permutation 4 extra data")
                .note("more info", "Some notes for this")
                .note("yet more info", "Even more notes for this")
                .setVerified(false))

        permutations.add(new TestPermutation([
                "Param 1"      : "param 1 is b",
                "Param 2"      : "param 2 is b",
                "Group Param"  : "b",
                "table param 2": "tp2 is e"])
                .asTable(tableColumns)
                .data("out data", "Permutation 5")
                .setVerified(false))

        permutations.add(new TestPermutation([
                "Param 1"       : "param 1 is d",
                "Param 2"       : "param 2 is d",
                "Group Param"   : "b",
                "table param x1": "tpx1 is a"])
                .asTable(tableColumns)
                .data("out data", "Permutation 6")
                .setVerified(false))

        permutations.add(new TestPermutation([
                "Param 1"       : "param 1 is d",
                "Param 2"       : "param 2 is d",
                "Group Param"   : "b",
                "table param x1": "tpx1 is b"])
                .asTable(tableColumns)
                .data("out data", "Permutation 7\nWith a second line with | chars\nAnd another with | chars")
                .setVerified(false))

        permutations.add(new TestPermutation([
                "Add Column Parameter 1": new AddColumnConfig().setName("col 1"),
                "table param 2"         : "tp2 is b",
                "Group Param"           : "b"])
                .asTable(tableColumns)
                .data("String data", "No notes, just one data")
                .setVerified(true))

        permutations.add(new TestPermutation([
                "String Parameter 1" : "Short Parameter",
                "Group Param"        : "a",
                "Multiline Parameter": "A Longer param with\nthis on a second line",
                "table param 2"      : "tp2 is b"])
                .asTable(tableColumns)
                .data("String data", "No notes, just one data")
                .setVerified(true))


        return permutations

    }
}
