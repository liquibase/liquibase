package liquibase.util


import spock.lang.Requires
import spock.lang.Specification
import spock.lang.Unroll

class FilenameUtilTest extends Specification {

    @Unroll
    def "normalize"() {
        expect:
        FilenameUtil.normalize(filename) == expected

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
        null                  | null
        ""                    | ""
    }

    @Requires({ System.getProperty("os.name").toLowerCase().contains("win") })
    @Unroll
    def "normalize (windows)"() {
        expect:
        FilenameUtil.normalize(filename) == expected

        where:
        filename           | expected
        "C:\\foo\\..\\bar" | "C:/bar"
        "C:\\..\\bar"      | "C:/bar"
    }

    @Unroll
    def "concat"() {
        expect:
        FilenameUtil.concat(basePath, filename) == expected

        where:
        basePath       | filename                       | expected
        null           | null                           | null
        null           | "liquibase/delta-changelogs/"  | "liquibase/delta-changelogs"
        null           | "liquibase\\delta-changelogs/" | "liquibase/delta-changelogs"
        null           | "liquibase/delta-changelogs"   | "liquibase/delta-changelogs"
        "base/path"    | "liquibase/changelogs"         | "base/path/liquibase/changelogs"
        "base/path"    | "/liquibase/changelogs"        | "base/path/liquibase/changelogs"
        "\\base\\path" | "liquibase/changelogs"         | "/base/path/liquibase/changelogs"
        "\\base\\path" | "\\liquibase\\changelogs"         | "/base/path/liquibase/changelogs"
    }

    @Unroll
    def "sanitizeFileName"() {
        expect:
        FilenameUtil.sanitizeFileName(filename) == expected

        where:
        filename                         | expected
        "normalString"                   | "normalString"
        "<B>o\\b|I/s|*?Yo\"u\\r?Uncle:/" | "_B_o_b_I_s___Yo_u_r_Uncle__"
        "this@that.com"                  | "this_that.com"
        null                             | null
    }

    @Unroll
    def "getFullPath"() {
        expect:
        FilenameUtil.getDirectory(filename) == expected

        where:
        filename | expected
        "a.txt"  | ""
        "a/b/c"  | "a/b/c"
        "a/b/c/" | "a/b/c"
        null     | null
    }

    @Requires({ System.getProperty("os.name").toLowerCase().contains("win") })
    @Unroll
    def "getFullPath (windows)"() {
        expect:
        FilenameUtil.getDirectory(filename) == expected

        where:
        filename          | expected
        "C:\\a\\b\\c.txt" | "C:/a/b"
        "C:\\"            | "C:/"
    }
}
