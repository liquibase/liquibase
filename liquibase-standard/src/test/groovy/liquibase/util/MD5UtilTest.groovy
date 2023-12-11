package liquibase.util

import spock.lang.Specification
import spock.lang.Unroll

class MD5UtilTest extends Specification {

    @Unroll
    def computeMD5() throws Exception {
        when:
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes())

        then:
        MD5Util.computeMD5(input as String) == expected
        MD5Util.computeMD5(inputStream) == expected

        where:
        input               | expected
        "foo"               | "acbd18db4cc2f85cedef654fccc4a4d8"
        "abc"               | "900150983cd24fb0d6963f7d28e17f72"
        "bbb"               | "08f8e0260c64418510cefb2b06eee5cd"
        "foo".multiply(500) | "7d66efd792402b14bf5a1dd4fa7d5417"
    }
}
