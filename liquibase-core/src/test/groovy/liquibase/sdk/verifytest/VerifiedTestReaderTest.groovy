package liquibase.sdk.verifytest

import spock.lang.Specification

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
}
