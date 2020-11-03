package liquibase.util


import spock.lang.Specification
import spock.lang.Unroll

class StreamUtilTest extends Specification {


    @Unroll
    def readStreamAsString() throws IOException {
        when:
        ByteArrayInputStream stream = new ByteArrayInputStream(contents);
        String result = StreamUtil.readStreamAsString(stream);

        then:
        result == expected;

        where:
        contents                                                                                                   | expected | notes
        "".bytes                                                                                                   | ""       | "empty"
        "TEST2".bytes                                                                                              | "TEST2"  | "Simple string"
        [(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 0x61, 0x62, 0x63] as byte[]                                        | "abc"    | "No BOM encoding given"
        [(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 0x61, 0x62, 0x63] as byte[]                                        | "abc"    | "Correct BOM encoding given"
        [0x61, 0x62, 0x63] as byte[]                                                                               | "abc"    | "No BOM encoding given"
        [0xEF, 0xBB, 0xBF, 0x61, 0x62, 0x63] as byte[]                                                             | "abc"    | "UTF-8 encoding given"
        [0xEF, 0xBB, 0xBF] as byte[]                                                                               | ""       | ""
        [0xFE, 0xFF] as byte[]                                                                                     | ""       | ""
        [0xFF, 0xFE] as byte[]                                                                                     | ""       | ""
        [0xFF, 0xFE, 0x00, 0x00] as byte[]                                                                         | ""       | ""
        [0x00, 0x00, 0xFE, 0xFF] as byte[]                                                                         | ""       | ""
        [0xEF, 0xBB, 0xBF, 0x61] as byte[]                                                                         | "a"       | "Empty UTF-8 with 4 bytes"
        [0xFE, 0xFF, 0x00, 0x61] as byte[]                                                                         | "a"       | "Empty UTF-16BE with 4 bytes"
        [0xFF, 0xFE, 0x61, 0x00] as byte[]                                                                         | "a"       | "Empty UTF-1LBE with 4 bytes"
        [0xFE, 0xFF, 0x00, 0x61, 0x00, 0x62, 0x00, 0x63] as byte[]                                                 | "abc"    | ""
        [0xFF, 0xFE, 0x61, 0x00, 0x62, 0x00, 0x63, 0x00] as byte[]                                                 | "abc"    | ""
        [0xFF, 0xFE, 0x00, 0x00, 0x61, 0x00, 0x00, 0x00, 0x62, 0x00, 0x00, 0x00, 0x63, 0x00, 0x00, 0x00] as byte[] | "abc"    | ""
        [0x00, 0x00, 0xFE, 0xFF, 0x00, 0x00, 0x00, 0x61, 0x00, 0x00, 0x00, 0x62, 0x00, 0x00, 0x00, 0x63] as byte[] | "abc"    | ""
    }

//
//
//    public void testWithBomCorrectEncodingGiven() throws IOException {
//        String contents = "abc";
//        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{
//                );
//
//        assertEquals(contents, StreamUtil.readStreamAsString(bais, "UTF8"));
//    }
//
//
//    public void testWithoutBomUtf8() throws IOException {
//        String contents = "abc";
//        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{0x61,
//                0x62, 0x63});
//
//        assertEquals(contents, StreamUtil.readStreamAsString(bais, "UTF8"));
//    }
//
//
//    public void testWithoutBomLatin1() throws IOException {
//        String contents = "abc";
//        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{0x61,
//                0x62, 0x63});
//
//        assertEquals(contents, StreamUtil.readStreamAsString(bais, "Latin1"));
//    }
//
//
//    public void testWithBomWrongEncodingGiven() throws IOException {
//        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[]{
//                (byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 0x61, 0x62, 0x63});
//
//        try {
//            StreamUtil.readStreamAsString(bais, "UTF-16BE");
//            fail("Should have thrown an IllegalArgumentException");
//        } catch (IllegalArgumentException e) {
//            assertTrue(e.getMessage().contains("UTF-16BE"));
//        }
//    }
}
