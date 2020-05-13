package liquibase.resource


import liquibase.util.StreamUtil
import spock.lang.Specification
import spock.lang.Unroll

class ClassLoaderResourceAccessorTest extends Specification {

    def testResourceAccessor = new ClassLoaderResourceAccessor(new URLClassLoader(
            [
                    Thread.currentThread().getContextClassLoader().getResource("simple-files.jar"),
                    Thread.currentThread().getContextClassLoader().getResource("simple-files.zip"),
            ] as URL[],
            Thread.currentThread().getContextClassLoader()))

    @Unroll("#featureName: #relativeTo #streamPath")
    def "getFinalPath"() {
        expect:
        new ClassLoaderResourceAccessor().getFinalPath(relativeTo, streamPath) == expected

        where:
        relativeTo                         | streamPath                       | expected
        null                               | "com/example/test.sql"           | "com/example/test.sql"
        null                               | "/com/example/test.sql"          | "com/example/test.sql"
        null                               | "\\com\\example\\test.sql"       | "com/example/test.sql"
        null                               | "/com////example//test.sql"      | "com/example/test.sql"
        null                               | "classpath:com/example/test.sql" | "com/example/test.sql"
        "com/example"                      | "test.sql"                       | "com/example/test.sql"
        "/com/example/"                    | "test.sql"                       | "com/example/test.sql"
        "com/example"                      | "/my/test.sql"                   | "com/example/my/test.sql"
        "com/example"                      | "/my/test.sql"                   | "com/example/my/test.sql"
        "com/example/other.file"           | "/my/test.sql"                   | "com/example/my/test.sql"
        "classpath:com/example/other.file" | "my/test.sql"                    | "com/example/my/test.sql"
        "changelog.xml"                    | "sql/function.sql"               | "sql/function.sql"
    }

    @Unroll("#featureName: #relativeTo #streamPath")
    def "openStreams, checking content"() {
        given:
        def streams = testResourceAccessor.openStreams(relativeTo, streamPath)

        expect:
        streams.size() == 1
        StreamUtil.readStreamAsString(streams.iterator().next()).split(/\r?\n/)[0] == expectedContent

        where:
        relativeTo                    | streamPath                        | expectedContent
        null                          | "liquibase.properties"            | "# This is a sample liquibase.properties file for use by core unit tests. Its main purpose if to test the"
        null                          | "liquibase/empty.changelog.xml"   | "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\">"
        null                          | "file-in-zip-root.txt"            | "File in root"
        null                          | "com/example/zip/file-in-zip.txt" | "File in zip"
        null                          | "file-in-jar-root.txt"            | "File in root"
        "com/example"                 | "jar/file-in-jar.txt"             | "File in jar"
        "com/example/file-in-jar.txt" | "jar/file-in-jar.txt"             | "File in jar"
    }

    @Unroll("#featureName: #relativeTo #streamPath")
    def "openStreams, can't check content"() {
        expect:
        testResourceAccessor.openStreams(relativeTo, streamPath).size() == expectedSize

        where:
        relativeTo | streamPath                                   | expectedSize
        null       | "invalid_file"                               | 0
        "/path/to" | "/another/invalid_file"                      | 0
        null       | "com/example/everywhere/file-everywhere.txt" | 3

    }

    @Unroll
    def "list"() {
        expect:
        testResourceAccessor.list(relativeTo, path, recursive, includeFiles, includeDirectories).toString() == expectedValue.toString()

        where:
        [relativeTo, path, recursive, includeFiles, includeDirectories, expectedValue] << [
                [
                        null, "com/example/jar", true, true, true,
                        [
                                "com/example/jar/file-in-jar.txt",
                        ]
                ],
                [
                        null, "com/example", true, true, true,
                        [
                                "com/example/everywhere",
                                "com/example/everywhere/file-everywhere.txt",
                                "com/example/everywhere/other-file-everywhere.txt",
                                "com/example/file-in-jar.txt",
                                "com/example/file-in-zip.txt",
                                "com/example/jar",
                                "com/example/jar/file-in-jar.txt",
                                "com/example/liquibase",
                                "com/example/liquibase/change",
                                "com/example/liquibase/change/ColumnConfig.class",
                                "com/example/liquibase/change/ComputedConfig.class",
                                "com/example/liquibase/change/CreateTableExampleChange.class",
                                "com/example/liquibase/change/DefaultConstraintConfig.class",
                                "com/example/liquibase/change/IdentityConfig.class",
                                "com/example/liquibase/change/KeyColumnConfig.class",
                                "com/example/liquibase/change/PrimaryKeyConfig.class",
                                "com/example/liquibase/change/UniqueConstraintConfig.class",
                                "com/example/my-logic.sql",
                                "com/example/shared",
                                "com/example/shared/file-in-jar.txt",
                                "com/example/shared/file-in-zip.txt",
                                "com/example/users.csv",
                                "com/example/zip",
                                "com/example/zip/file-in-zip.txt",
                        ]
                ],
                [
                        null, "com/example", false, true, true,
                        [
                                "com/example/everywhere",
                                "com/example/file-in-jar.txt",
                                "com/example/file-in-zip.txt",
                                "com/example/jar",
                                "com/example/liquibase",
                                "com/example/my-logic.sql",
                                "com/example/shared",
                                "com/example/users.csv",
                                "com/example/zip",
                        ]
                ],
                [
                        null, "com/example", false, true, false,
                        [
                                "com/example/file-in-jar.txt",
                                "com/example/file-in-zip.txt",
                                "com/example/my-logic.sql",
                                "com/example/users.csv",
                        ]
                ],
                [
                        null, "com/example", false, false, true,
                        [
                                "com/example/everywhere",
                                "com/example/jar",
                                "com/example/liquibase",
                                "com/example/shared",
                                "com/example/zip",
                        ]
                ],
        ]
    }
}
