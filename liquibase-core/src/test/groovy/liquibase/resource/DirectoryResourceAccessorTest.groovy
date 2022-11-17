package liquibase.resource

import liquibase.util.StreamUtil
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
    def "openStreams and openStream"() {
        when:
        def accessor = new DirectoryResourceAccessor("src/main" as File)

        def streams = accessor.openStreams(relativeTo, path)
        def stream = accessor.openStream(relativeTo, path)

        then:
        streams.size() == 1
        StreamUtil.readStreamAsString(streams.iterator().next()).contains(expected)
        StreamUtil.readStreamAsString(stream).contains(expected)

        where:
        relativeTo                      | path                            | expected
        null                            | "java/liquibase/Liquibase.java" | "class Liquibase"
        "java/liquibase/Liquibase.java" | "Scope.java"                    | "class Scope"
        "java/liquibase/Liquibase.java" | "util/StringUtil.java"          | "class StringUtil"
    }

    @Unroll
    def "list non-recursive"() {
        when:
        def accessor = new DirectoryResourceAccessor("src/main" as File)

        def results = accessor.list(relativeTo, path, false, true, false)

        then:
        results.contains("java/liquibase/AbstractExtensibleObject.java")
        !results.contains("java/liquibase/util/StringUtil.java")

        where:
        relativeTo      | path
        null            | "java/liquibase"
        "java/file.txt" | "liquibase"
    }

    @Unroll
    def "list recursive"() {
        when:
        def accessor = new DirectoryResourceAccessor("src/main" as File)

        def results = accessor.list(relativeTo, path, true, true, false)

        then:
        results.contains("java/liquibase/AbstractExtensibleObject.java")
        results.contains("java/liquibase/util/StringUtil.java")

        where:
        relativeTo      | path
        null            | "java/liquibase"
        "java/file.txt" | "liquibase"
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
        path                                                           | expected
        "liquibase/resource/DirectoryResourceAccessorTest.class"       | ["liquibase/resource/DirectoryResourceAccessorTest.class"]
        "/liquibase/resource/DirectoryResourceAccessorTest.class"      | ["liquibase/resource/DirectoryResourceAccessorTest.class"]
        "liquibase\\resource\\DirectoryResourceAccessorTest.class"     | ["liquibase/resource/DirectoryResourceAccessorTest.class"]
        "\\liquibase\\resource\\DirectoryResourceAccessorTest.class"   | ["liquibase/resource/DirectoryResourceAccessorTest.class"]
        "com/example/file with space.txt"                              | ["com/example/file with space.txt"]
        "com\\example\\file with space.txt"                            | ["com/example/file with space.txt"]
        "com/example/file with space.txt"                              | ["com/example/file with space.txt"]
        "c:\\liquibase\\resource\\DirectoryResourceAccessorTest.class" | ["liquibase/resource/DirectoryResourceAccessorTest.class"]
    }

    def "getAll returns empty if nothing matches"() {
        expect:
        simpleTestAccessor.getAll("com/example/invalid.txt").size() == 0
    }

    @Unroll
    def "search fails with invalid values: #path"() {
        when:
        simpleTestAccessor.search(path, true)

        then:
        def e = thrown(Exception)
        e.message == expected

        where:
        path                                                     | expected
        null                                                     | "Path must not be null"
        "liquibase/resource/DirectoryResourceAccessorTest.class" | "'liquibase/resource/DirectoryResourceAccessorTest.class' is a file, not a directory"
    }

    @Unroll
    def "list"() {
        expect:
        simpleTestAccessor.search(path, recursive)*.getPath() as SortedSet == expected as SortedSet

        where:
        path          | recursive | expected
        "com/example" | false     | ["com/example/changelog.xml", "com/example/file with space.txt", "com/example/my-logic.sql", "com/example/users.csv"]
        "com/example" | true      | ["com/example/changelog.xml",
                                     "com/example/directory/file-in-directory.txt",
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
