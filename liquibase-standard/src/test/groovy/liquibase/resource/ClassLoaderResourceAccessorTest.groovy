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

    @Unroll
    def "getAll, checking content: #path"() {
        given:
        def resources = testResourceAccessor.getAll(path)

        expect:
        resources.size() == 1
        StreamUtil.readStreamAsString(resources.iterator().next().openInputStream()).split(/\r?\n/)[0] == expectedContent

        where:
        path                        | expectedContent
        "liquibase.properties"            | "# This is a sample liquibase.properties file for use by core unit tests. Its main purpose if to test the"
        "liquibase/empty.changelog.xml"   | "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\">"
        "file-in-zip-root.txt"            | "File in root"
        "com/example/zip/file-in-zip.txt" | "File in zip"
        "file-in-jar-root.txt"            | "File in root"
        "com/example/jar/file-in-jar.txt" | "File in jar"
        "com/example/jar/file-in-jar.txt" | "File in jar"
    }

    @Unroll
    def "getAll, doesn't exist: #path"() {
        expect:
        testResourceAccessor.getAll(path) == null

        where:
        path << ["invalid_file", "/invalid_file"]
    }

    def "getAll, multiple locations"() {
        expect:
        testResourceAccessor.getAll("com/example/everywhere/file-everywhere.txt").size() == 3
    }


    @Unroll
    def "search"() {
        expect:
        //have to resort them because different test runners may put classloader entries in different orders
        (testResourceAccessor.search(path, recursive)*.getPath()) as SortedSet == expectedValue as SortedSet

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
                                "com/example/changelog.xml",
                                "com/example/directory/file-in-directory.txt",
                                "com/example/everywhere/file-everywhere.txt",
                                "com/example/everywhere/other-file-everywhere.txt",
                                "com/example/file with space.txt",
                                "com/example/file-in-jar.txt",
                                "com/example/jar/file-in-jar.txt",
                                "com/example/liquibase/change/ChangeWithPrimitiveFields.class",
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
                                "com/example/changelog.xml",
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
                                "com/example/changelog.xml",
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
