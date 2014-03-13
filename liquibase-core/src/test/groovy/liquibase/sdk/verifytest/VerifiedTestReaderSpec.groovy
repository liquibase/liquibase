package liquibase.sdk.verifytest

import spock.lang.Specification

class VerifiedTestReaderSpec extends Specification {

    def "read empty test"() {
        when:
        def test = new VerifiedTest("com.example.Test", "my test name")
        def reader = new StringReader(write(test))
        def readTest = new VerifiedTestReader().read(reader)

        then:
        assertTestsSame(test, readTest)

        cleanup:
        reader.close()
    }

    def "read complex test"() {
        when:
        def test = new VerifiedTestWriterSpec().createComplexTest()
        def testAsString = write(test)
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

    def assertTestsSame(expected, actual) {
        def writer = new VerifiedTestWriter()
        def expectedString = new StringWriter()
        def actualString = new StringWriter()

        writer.write(expected, expectedString)
        writer.write(actual, actualString)

        try {
            assert expectedString.toString() == actualString.toString()
        } finally {
            expectedString.close()
            actualString.close()
        }

        true
    }

    def write(test) {
        def out = new StringWriter()
        new VerifiedTestWriter().write(test, out)
        out.flush()
        out.close()
        return out.toString()

    }
}
