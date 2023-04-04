package liquibase.io

import spock.lang.Specification
import spock.lang.Unroll

class EmptyLineAndCommentSkippingInputStreamTest extends Specification {


    public static final String COMMENT_CHAR = "#"

    @Unroll("#featureName: #message")
    def "should skip empty lines and comment lines with Win/Linux/Mac style line breaks"() {
        expect:
        inputStreamToString(new EmptyLineAndCommentSkippingInputStream(new ByteArrayInputStream(input.getBytes()), COMMENT_CHAR)) == inputStreamToString(new ByteArrayInputStream(expected.getBytes()))

        where:
        [message, input, expected] << [
                ["various line endings and comments inside",
                 "Line1\n" +
                         "line2\n" +
                         " 1\n" + //unix line break
                         " 2\r\n" + //windows line break
                         "#I am a comment\n" + //comment
                         "#I am a comment\n" + //comment
                         "#I am a comment\r\n" + //comment
                         " 3\n" +
                         " 4\n\r\n\r" + //all in one :)
                         "line3", "Line1\nline2\n 1\n 2\n 3\n 4\nline3"],

                ["comment at the end",
                 "Line1\n#I am a comment", "Line1"],

                ["comment at the beginning",
                 "#I am a comment\nLine1", "Line1"],

                ["surrounding newlines",
                 "\n\n\nLine1\n\n\n", "Line1"],

                ["just data",
                 "Line1\nLine2", "Line1\nLine2"],

                ["just data but lines contain comment character",
                 "Li#ne1\nLine2#", "Li#ne1\nLine2#"],

                ["just data in windows format",
                 "Line1\r\nLine2", "Line1\nLine2"],

                ["comments and newlines in the middle",
                 "Line1\n\n#\n###\n#\nLine2", "Line1\nLine2"],
        ]
    }

    def "files ready by LoadDataChangeTest are the same"() {
        expect:
        inputStreamToString(new EmptyLineAndCommentSkippingInputStream(getClass().getResourceAsStream("/liquibase/change/core/sample.data1-withComments.csv"), COMMENT_CHAR)) ==
        inputStreamToString(new EmptyLineAndCommentSkippingInputStream(getClass().getResourceAsStream("/liquibase/change/core/sample.data1-removedComments.csv"), COMMENT_CHAR))
    }

    def "Line endings in csv-files with comments"() {
        expect:
        inputStreamToString(new EmptyLineAndCommentSkippingInputStream(new ByteArrayInputStream("a#comment\r\nb\r\n\r\nc".getBytes()), COMMENT_CHAR)) ==
        inputStreamToString(new EmptyLineAndCommentSkippingInputStream(new ByteArrayInputStream("a#comment\nb\n\nc".getBytes()), COMMENT_CHAR))
    }

    private String inputStreamToString(InputStream inputStreamWithComments) {
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
