package liquibase.sdk.verifytest

import spock.lang.Specification
import static org.hamcrest.Matchers.*
import static spock.util.matcher.HamcrestSupport.that

class VerifiedTestReaderTest extends Specification {

    def "read empty test"() {
        when:
        def reader = new StringReader(write([]))
        def read = new VerifiedTestReader().read(reader)

        then:
        assertResultsSame([], read)

        cleanup:
        reader.close()
    }

    def "read complex test"() {
        when:
        def permutations = new VerifiedTestWriterTest().createComplexPermutations()
        def testAsString = write(permutations)
        def reader = new StringReader(testAsString)
        def readTest
        try {
            readTest = new VerifiedTestReader().read(reader)
        } catch (Throwable e) {
            println testAsString
            throw e
        }

        then:
        assertResultsSame(permutations, readTest)

        cleanup:
        reader && reader.close()
    }

    def "read complex test with table"() {
        when:
        def testPermutations = new VerifiedTestWriterTest().createComplexPermutationsWithTable()
        def testAsString = write(testPermutations)
        def reader = new StringReader(testAsString)
        def readPermutations
        try {
            readPermutations = new VerifiedTestReader().read(reader)
        } catch (Throwable e) {
            println testAsString
            throw e
        }

        then:
        assertResultsSame(testPermutations, readPermutations)

        cleanup:
        reader && reader.close()
    }

    def assertResultsSame(expected, actual) {
        def writer = new VerifiedTestWriter()
        def expectedString = new StringWriter()
        def actualString = new StringWriter()

        writer.write("com.example.Test", "assert same", expected, expectedString)
        writer.write("com.example.Test", "assert same", actual, actualString)

        try {
            assert expectedString.toString() == actualString.toString()
        } finally {
            expectedString.close()
            actualString.close()
        }

        true
    }

    def write(permutations) {
        def out = new StringWriter()
        new VerifiedTestWriter().write("com.example.Test", "my test name", permutations, out)
        out.flush()
        out.close()
        return out.toString()

    }

    def "table format correctly resets values from one row to the next"() {
        when:
        def contents = """# Test: liquibase.statementlogic.core.SelectTablesLogicTest "emptyDatabase" #

NOTE: This output is generated when the test is ran. DO NOT EDIT MANUALLY

## Permutations ##

- **connection:** Standard MS SqlServer connection

| Permutation | Verified | catalogName | schemaName | tableName  | DETAILS
| 2724b9      | true     |             |            | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=dbo, tableName=TABLE_NAME)
| b3e0f1      | true     |             | LBSCHEMA   | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=lbschema, tableName=TABLE_NAME)
| aac6ec      | true     |             | LBSCHEMA2  | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=lbschema2, tableName=TABLE_NAME)
| 0183ff      | true     | LBCAT       |            | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=dbo, tableName=TABLE_NAME)
| a87c62      | true     | LBCAT       | LBSCHEMA   | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=lbschema, tableName=TABLE_NAME)
| e9a23a      | true     | LBCAT       | LBSCHEMA2  | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=lbschema2, tableName=TABLE_NAME)
"""
        def readPermutations = new VerifiedTestReader().read(new StringReader(contents))

        then:
        readPermutations.size() == 6
        that readPermutations.collect({it.getKey()}), containsInAnyOrder(["2724b9", "b3e0f1", "aac6ec", "0183ff", "a87c62", "e9a23a"] as String[])
    }
}
