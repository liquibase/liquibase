package liquibase.util

import spock.lang.Specification
import spock.lang.Unroll

class FilenameUtilsTest extends Specification {

    @Unroll
    def "normalize"() {
        expect:
        FilenameUtils.normalize(filename) == expected
        where:
        filename              | expected
        "/foo//"              | "/foo"
        "/foo/./"             | "/foo"
        "/foo/../bar"         | "/bar"
        "/foo/../bar/"        | "/bar"
        "/foo/../bar/../baz"  | "/baz"
        "//foo//./bar"        | "/foo/bar"
        "../foo"              | "../foo"
        "foo/bar/.."          | "foo"
        "foo/../../bar"       | "../bar"
        "foo/../bar"          | "bar"
        "//server/foo/../bar" | "/server/bar"
        "//server/../bar"     | "/bar"
        "C:\\foo\\..\\bar"    | "C:/bar"
        "C:\\..\\bar"         | "C:/bar"
        null                  | null
        ""                    | ""
    }

    @Unroll
    def "concat"() {
        expect:
        FilenameUtils.concat(basePath, filename) == expected

        where:
        basePath       | filename                       | expected
        null           | null                           | null
        null           | "liquibase/delta-changelogs/"  | "liquibase/delta-changelogs"
        null           | "liquibase\\delta-changelogs/" | "liquibase/delta-changelogs"
        null           | "liquibase/delta-changelogs"   | "liquibase/delta-changelogs"
        "base/path"    | "liquibase/changelogs"         | "base/path/liquibase/changelogs"
        "\\base\\path" | "liquibase/changelogs"         | "/base/path/liquibase/changelogs"
    }

    @Unroll
    def "sanitizeFileName"() {
        expect:
        FilenameUtils.sanitizeFileName(filename) == expected

        where:
        filename                         | expected
        "normalString"                   | "normalString"
        "<B>o\\b|I/s|*?Yo\"u\\r?Uncle:/" | "_B_o_b_I_s___Yo_u_r_Uncle__"
        null                             | null
    }

    @Unroll
    def "getFullPath"() {
        expect:
        FilenameUtils.getDirectory(filename) == expected

        where:
        filename          | expected
        "C:\\a\\b\\c.txt" | "C:/a/b"
        "a.txt"           | ""
        "a/b/c"           | "a/b/c"
        "a/b/c/"          | "a/b/c"
        "C:\\"            | "C:/"
        null              | null
    }
}
