package liquibase.resource

import liquibase.test.TestContext
import liquibase.util.StreamUtil
import spock.lang.Specification
import spock.lang.Unroll

class ClassLoaderResourceAccessorTest extends Specification {

    def testResourceAccessor = new ClassLoaderResourceAccessor(new URLClassLoader(
            [
                    Thread.currentThread().getContextClassLoader().getResource("simple-files.jar"),
                    Thread.currentThread().getContextClassLoader().getResource("simple-files.zip"),
            ] as URL[],
            new URLClassLoader([
                    new File(TestContext.getInstance().findCoreJvmProjectRoot(), "/target/classes").toURL(),
                    new File(TestContext.getInstance().findCoreJvmProjectRoot(), "/target/test-classes").toURL(),
            ] as URL[]
            )
    ))

//    @Unroll("#featureName: #relativeTo #streamPath")
//    def "getFinalPath"() {
//        expect:
//        new ClassLoaderResourceAccessor().getFinalPath(relativeTo, streamPath) == expected
//
//        where:
//        relativeTo                         | streamPath                       | expected
//        null                               | "com/example/test.sql"           | "com/example/test.sql"
//        null                               | "/com/example/test.sql"          | "com/example/test.sql"
//        null                               | "\\com\\example\\test.sql"       | "com/example/test.sql"
//        null                               | "/com////example//test.sql"      | "com/example/test.sql"
//        null                               | "classpath:com/example/test.sql" | "com/example/test.sql"
//        "com/example"                      | "test.sql"                       | "com/example/test.sql"
//        "/com/example/"                    | "test.sql"                       | "com/example/test.sql"
//        "com/example"                      | "/my/test.sql"                   | "com/example/my/test.sql"
//        "com/example"                      | "/my/test.sql"                   | "com/example/my/test.sql"
//        "com/example/other.file"           | "/my/test.sql"                   | "com/example/my/test.sql"
//        "classpath:com/example/other.file" | "my/test.sql"                    | "com/example/my/test.sql"
//        "changelog.xml"                    | "sql/function.sql"               | "sql/function.sql"
//        "db-change.log/changelog.xml"      | "data/file.csv"                  | "db-change.log/data/file.csv"
//    }

            @ Unroll("#featureName: #relativeTo #streamPath")

    def "openStreams, checking content"() {
        given:
        def streams = testResourceAccessor.openStreams(null, streamPath)

        expect:
        streams.size() == 1
        StreamUtil.readStreamAsString(streams.iterator().next()).split(/\r?\n/)[0] == expectedContent

        where:
        streamPath                        | expectedContent
        "liquibase.properties"            | "# This is a sample liquibase.properties file for use by core unit tests. Its main purpose if to test the"
        "liquibase/empty.changelog.xml"   | "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\">"
        "file-in-zip-root.txt"            | "File in root"
        "com/example/zip/file-in-zip.txt" | "File in zip"
        "file-in-jar-root.txt"            | "File in root"
        "com/example/jar/file-in-jar.txt" | "File in jar"
        "com/example/jar/file-in-jar.txt" | "File in jar"
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
        //have to resort them because different test runners may put classloader entries in different orders
        (testResourceAccessor.list(path, recursive)*.getPath()) as SortedSet == expectedValue as SortedSet

        where:
        [path, recursive, expectedValue] << [
                [
                        "com/example/jar", true,
                        [
                                "com/example/jar/file-in-jar.txt",
                        ]
                ],
                [
                        "com/example", true,
                        [
                                "com/example/directory/file-in-directory.txt",
                                "com/example/everywhere/file-everywhere.txt",
                                "com/example/everywhere/other-file-everywhere.txt",
                                "com/example/file with space.txt",
                                "com/example/file-in-jar.txt",
                                "com/example/jar/file-in-jar.txt",
                                "com/example/file-in-zip.txt",
                                "com/example/liquibase/change/ColumnConfig.class",
                                "com/example/liquibase/change/ComputedConfig.class",
                                "com/example/liquibase/change/CreateTableExampleChange.class",
                                "com/example/liquibase/change/DefaultConstraintConfig.class",
                                "com/example/liquibase/change/IdentityConfig.class",
                                "com/example/liquibase/change/KeyColumnConfig.class",
                                "com/example/liquibase/change/PrimaryKeyConfig.class",
                                "com/example/liquibase/change/UniqueConstraintConfig.class",
                                "com/example/my-logic.sql",
                                "com/example/shared/file-in-jar.txt",
                                "com/example/shared/file-in-zip.txt",
                                "com/example/users.csv",
                                "com/example/zip/file-in-zip.txt"
                        ]
                ],
                [
                        "com/example", false,
                        [
                                "com/example/file with space.txt",
                                "com/example/my-logic.sql",
                                "com/example/users.csv",
                                "com/example/file-in-jar.txt",
                                "com/example/file-in-zip.txt",
                        ]

                ],
                [
                        "com/example", false,
                        [
                                "com/example/file with space.txt",
                                "com/example/my-logic.sql",
                                "com/example/users.csv",
                                "com/example/file-in-jar.txt",
                                "com/example/file-in-zip.txt",
                        ]
                ]
        ]
    }
}
