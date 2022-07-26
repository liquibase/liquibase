package liquibase.resource

import liquibase.GlobalConfiguration
import liquibase.Scope
import spock.lang.Specification
import spock.lang.Unroll

class DirectoryResourceAccessorTest extends Specification {

    DirectoryResourceAccessor simpleTestAccessor

    def setup() {
        def testClasspathRoot = new File(this.getClass().getClassLoader().getResource("liquibase/resource/DirectoryResourceAccessorTest.class").toURI())
                .getParentFile()
                .getParentFile()
                .getParentFile()
        simpleTestAccessor = new DirectoryResourceAccessor(testClasspathRoot);

    }

    @Unroll
    def "Cannot construct invalid values"() {
        when:
        new DirectoryResourceAccessor(testDir);

        then:
        def e = thrown(IllegalArgumentException)
        assert e.message.startsWith(expected)

        where:
        testDir                                                                                                              | expected
        null                                                                                                                 | "Directory must not be null"
        new File(this.getClass().getClassLoader().getResource("liquibase/resource/DirectoryResourceAccessor.class").toURI()) | "Not a directory: "
    }

    @Unroll("#featureName: #path")
    def "getAll"() {
        expect:
        simpleTestAccessor.getAll(path)*.getPath() == expected

        where:
        path                                                         | expected
        "liquibase/resource/DirectoryResourceAccessorTest.class"     | ["liquibase/resource/DirectoryResourceAccessorTest.class"]
        "/liquibase/resource/DirectoryResourceAccessorTest.class"    | ["liquibase/resource/DirectoryResourceAccessorTest.class"]
        "liquibase\\resource\\DirectoryResourceAccessorTest.class"   | ["liquibase/resource/DirectoryResourceAccessorTest.class"]
        "\\liquibase\\resource\\DirectoryResourceAccessorTest.class" | ["liquibase/resource/DirectoryResourceAccessorTest.class"]
        "com/example/file with space.txt"                            | ["com/example/file with space.txt"]
        "com\\example\\file with space.txt"                          | ["com/example/file with space.txt"]
        "com/example/file with space.txt"                            | ["com/example/file with space.txt"]
    }

    def "getAll returns empty if nothing matches"() {
        expect:
        simpleTestAccessor.getAll("com/example/invalid.txt").size() == 0
    }

    @Unroll
    def "list"() {
        expect:
        simpleTestAccessor.list(path, recursive)*.getPath() == expected

        where:
        path          | recursive | expected
        "com/example" | false     | ["com/example/file with space.txt", "com/example/my-logic.sql", "com/example/users.csv"]
        "com/example" | true      | ["com/example/directory/file-in-directory.txt",
                                     "com/example/everywhere/file-everywhere.txt",
                                     "com/example/everywhere/other-file-everywhere.txt",
                                     "com/example/file with space.txt",
                                     "com/example/liquibase/change/ColumnConfig.class",
                                     "com/example/liquibase/change/ComputedConfig.class",
                                     "com/example/liquibase/change/CreateTableExampleChange.class",
                                     "com/example/liquibase/change/DefaultConstraintConfig.class",
                                     "com/example/liquibase/change/IdentityConfig.class",
                                     "com/example/liquibase/change/KeyColumnConfig.class",
                                     "com/example/liquibase/change/PrimaryKeyConfig.class",
                                     "com/example/liquibase/change/UniqueConstraintConfig.class",
                                     "com/example/my-logic.sql",
                                     "com/example/users.csv"]
    }

}
