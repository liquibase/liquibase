package liquibase.util


import spock.lang.Specification
import spock.lang.Unroll

class SpringBootFatJarTest extends Specification {

    @Unroll
    void getPathForResource() {
        expect:
        SpringBootFatJar.getPathForResource(input) == expected

        where:
        input                                                          | expected
        "some/path!/that/has!/two/bangs"                               | "that/has/two/bangs"
        "some/path!/that/has/one/bang"                                 | "that/has/one/bang"
        "some/simple/path"                                             | "some/simple/path"
        "jar:file:/some/fat.jar!/BOOT-INF/lib/some.jar!/db/changelogs" | "BOOT-INF/lib/some.jar"
        "jar:file:/some/fat.jar!/BOOT-INF/classes!/db/changelogs"      | "BOOT-INF/classes/db/changelogs"
    }

    @Unroll
    void getSimplePathForResources() {
        SpringBootFatJar.getSimplePathForResources(entryName, path) == expected

        where:
        entryName                       | path                                                           | expected
        "/that/has/two/bangs/entryname" | "some/path!/that/has!/two/bangs"                               | "two/bangs/entryname"
        "/that/has/one/bang"            | "some/path!/that/has/one/bang"                                 | "/that/has/one/bang"
        "BOOT-INF/lib/some.jar"         | "jar:file:/some/fat.jar!/BOOT-INF/lib/some.jar!/db/changelogs" | "db/changelogs"
    }
}
