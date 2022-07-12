package liquibase.util


import spock.lang.Specification
import spock.lang.Unroll

class BomAwareInputStreamTest extends Specification {

    @Unroll
    def "reads string and detects encoding"() {
        when:
        BomAwareInputStream stream = new BomAwareInputStream(new ByteArrayInputStream(contents));
        byte[] result = StreamUtil.readStream(stream);

        then:
        stream.detectedCharset?.name() == expectedCharsetName
        new String(result,  ObjectUtil.defaultIfNull(expectedCharsetName,"UTF-8")) == expectedContent

        where:
        contents                                                                                                   | expectedContent | expectedCharsetName | notes
        "".bytes                                                                                                   | ""              | null                | "empty"
        "TEST2".bytes                                                                                              | "TEST2"         | null                | "Simple string"
        [0x61, 0x62, 0x63] as byte[]                                                                               | "abc"           | null                | "No BOM encoding given"
        [0xEF, 0xBB, 0xBF, 0x61, 0x62, 0x63] as byte[]                                                             | "abc"           | "UTF-8"             | "UTF-8 encoding given"
        [0xEF, 0xBB, 0xBF] as byte[]                                                                               | ""              | "UTF-8"             | ""
        [0xFE, 0xFF] as byte[]                                                                                     | ""              | "UTF-16BE"          | ""
        [0xFF, 0xFE] as byte[]                                                                                     | ""              | "UTF-16LE"          | ""
        [0xFF, 0xFE, 0x00, 0x00] as byte[]                                                                         | ""              | "UTF-32LE"          | ""
        [0x00, 0x00, 0xFE, 0xFF] as byte[]                                                                         | ""              | "UTF-32BE"          | ""
        [0xEF, 0xBB, 0xBF, 0x61] as byte[]                                                                         | "a"              | "UTF-8"             | "Empty UTF-8 with 4 bytes"
        [0xFE, 0xFF, 0x00, 0x61] as byte[]                                                                         | "a"              | "UTF-16BE"          | "Empty UTF-16BE with 4 bytes"
        [0xFF, 0xFE, 0x61, 0x00] as byte[]                                                                         | "a"              | "UTF-16LE"          | "Empty UTF-1LBE with 4 bytes"
        [0xFE, 0xFF, 0x00, 0x61, 0x00, 0x62, 0x00, 0x63] as byte[]                                                 | "abc"           | "UTF-16BE"          | ""
        [0xFF, 0xFE, 0x61, 0x00, 0x62, 0x00, 0x63, 0x00] as byte[]                                                 | "abc"           | "UTF-16LE"          | ""
        [0xFF, 0xFE, 0x00, 0x00, 0x61, 0x00, 0x00, 0x00, 0x62, 0x00, 0x00, 0x00, 0x63, 0x00, 0x00, 0x00] as byte[] | "abc"           | "UTF-32LE"          | ""
        [0x00, 0x00, 0xFE, 0xFF, 0x00, 0x00, 0x00, 0x61, 0x00, 0x00, 0x00, 0x62, 0x00, 0x00, 0x00, 0x63] as byte[] | "abc"           | "UTF-32BE"          | ""
    }
}
