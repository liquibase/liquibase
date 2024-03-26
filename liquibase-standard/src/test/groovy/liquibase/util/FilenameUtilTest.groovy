package liquibase.util

import org.junit.Assume
import spock.lang.Specification

class FilenameUtilTest extends Specification {

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

    def "normalize (windows)"() {
        setup:
        Assume.assumeTrue(SystemUtil.isWindows())

        expect:
        FilenameUtil.normalize(filename) == expected

        where:
        filename           | expected
        "C:\\foo\\..\\bar" | "C:/bar"
        "C:\\..\\bar"      | "C:/bar"
    }

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

    def "sanitizeFileName"() {
        expect:
        FilenameUtil.sanitizeFileName(filename) == expected

        where:
        filename                         | expected
        "normalString"                   | "normalString"
        "<B>o\\b|I/s|*?Yo\"u\\r?Uncle:/" | "_B_o_b_I_s___Yo_u_r_Uncle__"
        null                             | null
    }

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

    def "getFullPath (windows)"() {
        setup:
        Assume.assumeTrue(SystemUtil.isWindows())

        expect:
        FilenameUtil.getDirectory(filename) == expected

        where:
        filename          | expected
        "C:\\a\\b\\c.txt" | "C:/a/b"
        "C:\\"            | "C:/"
    }
}
