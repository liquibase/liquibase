package liquibase.io

import spock.lang.Specification

class EmptyLineAndCommentSkippingInputStreamTest extends Specification {


    public static final String COMMENT_CHAR = "#"

    def "should skip empty lines and comment lines with Win/Linux/Mac style line breaks"() {
        given:
        byte[] bytesWithSeparators = "Line1\n" +
                "line2\n" +
                " 1\n" + //unix line break
                " 2\r\n" + //windows line break
                "#I am a comment\r" + //comment
                "#I am a comment\n" + //comment
                "#I am a comment\r\n" + //comment
                " 3\r" + //mac line break
                " 4\n\r\n\r" + //all in one :)
                "line3"

        byte[] bytesWithoutSeparators = "Line1" +
                "line2 1 2 3 4" +
                "line3"


        ByteArrayInputStream byteArrayInputStreamWithCommentsAndNewLines = new ByteArrayInputStream(bytesWithSeparators)
        ByteArrayInputStream byteArrayInputStreamWithoutCommentsAndNewLines = new ByteArrayInputStream(bytesWithoutSeparators)

        when:
        EmptyLineAndCommentSkippingInputStream inputStreamWithComments = new EmptyLineAndCommentSkippingInputStream(byteArrayInputStreamWithCommentsAndNewLines, COMMENT_CHAR)
        EmptyLineAndCommentSkippingInputStream inputStreamWithoutComments = new EmptyLineAndCommentSkippingInputStream(byteArrayInputStreamWithoutCommentsAndNewLines, /*disableComments*/ "")

        String stringFromByteArrayWithComments = inputStreamToString(inputStreamWithComments)
        String stringFromByteArrayWithoutComments = inputStreamToString(inputStreamWithoutComments)


        then:
        stringFromByteArrayWithComments == stringFromByteArrayWithoutComments
    }

    private String inputStreamToString(EmptyLineAndCommentSkippingInputStream inputStreamWithComments) {
        StringBuilder stringBuilder = new StringBuilder()
        int lastChar;
        while (true) {
            lastChar = inputStreamWithComments.read()
            if (lastChar == -1) break

            stringBuilder.append((char) lastChar)
        }

        stringBuilder.toString()
    }

}
