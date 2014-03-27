package liquibase.sdk.verifytest

import spock.lang.Specification

class VerifiedTestReaderTest extends Specification {

    def "read empty test"() {
        when:
        def test = new VerifiedTest("com.example.Test", "my test name")
        def reader = new StringReader(write(test, null))
        def readTest = new VerifiedTestReader().read(reader)

        then:
        assertTestsSame(test, readTest)

        cleanup:
        reader.close()
    }

    def "read complex test"() {
        when:
        def test = new VerifiedTestWriterTest().createComplexTest()
        def testAsString = write(test, null)
        def reader = new StringReader(testAsString)
        def readTest
        try {
            readTest = new VerifiedTestReader().read(reader)
        } catch (Throwable e) {
            println testAsString
            throw e
        }

        then:
        assertTestsSame(test, readTest)

        cleanup:
        reader && reader.close()
    }

    def "read complex test with groups"() {
        when:
        def test = new VerifiedTestWriterTest().createComplexTest()
        def testAsStringA = write(test, "Group Param: a")
        def testAsStringB = write(test, "Group Param: b")
        def readerA = new StringReader(testAsStringA)
        def readerB = new StringReader(testAsStringB)
        def readTest
        try {
            readTest = new VerifiedTestReader().read(readerA, readerB)
        } catch (Throwable e) {
            println testAsStringA
            println "---------"
            println testAsStringB
            throw e
        }

        then:
        assertTestsSame(test, readTest)

        cleanup:
        readerA && readerA.close()
        readerB && readerB.close()
    }


    def "read complex test with table"() {
        when:
        def test = new VerifiedTestWriterTest().createComplexTestWithTable()
        def testAsString = write(test, null)
        def reader = new StringReader(testAsString)
        def readTest
        try {
            readTest = new VerifiedTestReader().read(reader)
        } catch (Throwable e) {
            println testAsString
            throw e
        }

        then:
        assertTestsSame(test, readTest)

        cleanup:
        reader && reader.close()
    }

    def "read complex test with table and groups"() {
        when:
        def test = new VerifiedTestWriterTest().createComplexTestWithTable()
        def testAsStringA = write(test, "Group Param: a")
        def testAsStringB = write(test, "Group Param: b")
        def readerA = new StringReader(testAsStringA)
        def readerB = new StringReader(testAsStringB)
        def readTest
        try {
            readTest = new VerifiedTestReader().read(readerA, readerB)
        } catch (Throwable e) {
            println testAsStringA
            println "------"
            println testAsStringB
            throw e
        }

        then:
        assertTestsSame(test, readTest)

        cleanup:
        readerA && readerA.close()
        readerB && readerB.close()
    }

    def assertTestsSame(expected, actual) {
        def writer = new VerifiedTestWriter()
        def expectedString = new StringWriter()
        def actualString = new StringWriter()

        writer.write(expected, expectedString, null)
        writer.write(actual, actualString, null)

        try {
            assert expectedString.toString() == actualString.toString()
        } finally {
            expectedString.close()
            actualString.close()
        }

        true
    }

    def write(test, group) {
        def out = new StringWriter()
        new VerifiedTestWriter().write(test, out, group)
        out.flush()
        out.close()
        return out.toString()

    }
}
