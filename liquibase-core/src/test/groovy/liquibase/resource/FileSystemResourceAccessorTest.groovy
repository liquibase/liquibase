package liquibase.resource

import liquibase.util.SystemUtils
import org.junit.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.Assume.assumeTrue

class FileSystemResourceAccessorTest extends Specification {

    FileSystemResourceAccessor simpleTestAccessor
    File classpathRoot

    def setup() {
        classpathRoot = new File(this.getClass().getClassLoader().getResource("liquibase/resource/FileSystemResourceAccessor.class").toURI())
                .getParentFile()
                .getParentFile()
                .getParentFile()

        def testClasspathRoot = new File(this.getClass().getClassLoader().getResource("liquibase/resource/FileSystemResourceAccessorTest.class").toURI())
                .getParentFile()
                .getParentFile()
                .getParentFile()

        def simpleFilesJar = new File(this.getClass().getClassLoader().getResource("simple-files.jar").toURI())
        def simpleFilesZip = new File(this.getClass().getClassLoader().getResource("simple-files.zip").toURI())

        simpleTestAccessor = new FileSystemResourceAccessor(this.classpathRoot, testClasspathRoot, simpleFilesJar, simpleFilesZip);

    }

    def "Cannot construct with standard files"() {
        when:
        def baseFile = new File(this.getClass().getClassLoader().getResource("liquibase/resource/FileSystemResourceAccessor.class").toURI());

        def ignored = new FileSystemResourceAccessor(baseFile);

        then:
        def e = thrown(IllegalArgumentException)
        assert e.message.endsWith("must be a directory, jar or zip")
    }

    def "Can construct with directories and zip/jar files"() {
        when:
        def baseFile = new File(this.getClass().getClassLoader().getResource(base).toURI());


        def accessor = new FileSystemResourceAccessor(baseFile);

        then:
        accessor.getRootPaths().size() == 1

        where:
        base << ["com/example", "simple-files.zip", "simple-files.jar"]
    }

    @Unroll("#featureName: #path")
    def "openStream can open a single file"() {
        expect:
        simpleTestAccessor.openStream(path) != null

        where:
        path << [
                "liquibase/resource/FileSystemResourceAccessor.class",
                "liquibase/resource/FileSystemResourceAccessorTest.class",
                "/liquibase/resource/FileSystemResourceAccessor.class",
                "liquibase\\resource\\FileSystemResourceAccessor.class",
                "\\liquibase\\resource\\FileSystemResourceAccessor.class",
                "file-in-jar-root.txt",
                "file-in-zip-root.txt",
                "com/example/shared/file-in-zip.txt",
                "com/example/shared/file-in-jar.txt",
                "com/example/zip/file-in-zip.txt",
                "com/example/jar/file-in-jar.txt",
                "/com/example/zip/file-in-zip.txt",
                "/com/example/jar/file-in-jar.txt",
                "com\\example\\zip\\file-in-zip.txt",
                "com\\example\\jar\\file-in-jar.txt",
        ]
    }

//    def "openStream can detect that a single file is in multiple roots"() {
//        when:
//        simpleTestAccessor.addRootPath(Paths.get(classpathRoot.toPath().toString()+"/../liquibase"))
//
//        then:
//        simpleTestAccessor.openStream("liquibase/resource/FileSystemResourceAccessor.class") != null
//    }

    @Unroll("#featureName: #path")
    def "openStream can open a single file using an absolute path (windows)"() {
        expect:
        if (!SystemUtils.isWindows()) {
            return
        }

        simpleTestAccessor.openStream(path) != null

        where:
        path << [
                new File(getClass().getResource("/liquibase/resource/FileSystemResourceAccessor.class").toURI()).getCanonicalPath(),
                "/" + new File(getClass().getResource("/liquibase/resource/FileSystemResourceAccessor.class").toURI()).getCanonicalPath(),
        ]
    }

    def "openStream throws an error if multiple files match"() {
        when:
        simpleTestAccessor.openStream("com/example/everywhere/file-everywhere.txt")

        then:
        def e = thrown(IOException)
        e.message == "Found 2 files that match com/example/everywhere/file-everywhere.txt"

    }

    def "openStream returns null if nothing matches"() {
        expect:
        simpleTestAccessor.openStream("com/example/invalid.txt") == null
    }

    @Unroll("#featureName: #path")
    def "openStreams can open files"() throws IOException {
        expect:
        simpleTestAccessor.openStreams(path).size() == size

        where:
        path                                            | size
        "file-in-root.txt"                              | 2
        "com/example/everywhere/file-everywhere.txt"    | 2
        "com\\example\\everywhere\\file-everywhere.txt" | 2
        "com/example/zip/file-in-zip.txt"               | 1
        "com/example/jar/file-in-jar.txt"               | 1
        "com/example/invalid.txt"                       | 0
    }


    @Unroll("#featureName: #path -> #expected")
    def "getCanonicalPath creates correct paths"() {
        when:
        def accessor = new FileSystemResourceAccessor()

        then:
        accessor.getCanonicalPath(null, path) == expected

        where:
        relativeTo | path                          | expected
        null       | "short.file"                  | "short.file"
        null       | "/short.file"                 | "/short.file"
        null       | "\\short.file"                | "/short.file"
        null       | "some/path/Here.file"         | "some/path/Here.file"
        null       | "some\\path\\Here.file"       | "some/path/Here.file"
        null       | "some//path///Here.file"      | "some/path/Here.file"
        null       | "some\\\\path\\\\\\Here.file" | "some/path/Here.file"
        null       | "path/./with/./dirs"          | "path/with/dirs"
        null       | "path/./with/.dirs"           | "path/with/.dirs"
    }

    @Unroll
    def "list"() {
        expect:
        simpleTestAccessor.list(path, recursive, includeFiles, includeDirectories) == expected as SortedSet

        where:
        path          | recursive | includeFiles | includeDirectories | expected
        "com/example" | false     | true         | false              | ["com/example/file-in-jar.txt", "com/example/file-in-zip.txt", "com/example/my-logic.sql", "com/example/users.csv"]
        "com/example" | false     | true         | true               | ["com/example/everywhere", "com/example/file-in-jar.txt", "com/example/file-in-zip.txt", "com/example/jar", "com/example/liquibase", "com/example/my-logic.sql", "com/example/shared", "com/example/users.csv", "com/example/zip"]
        "com/example" | true      | true         | true               | ["com/example/everywhere", "com/example/everywhere/file-everywhere.txt", "com/example/everywhere/other-file-everywhere.txt", "com/example/file-in-jar.txt", "com/example/file-in-zip.txt", "com/example/jar", "com/example/jar/file-in-jar.txt", "com/example/liquibase", "com/example/liquibase/change", "com/example/liquibase/change/ColumnConfig.class", "com/example/liquibase/change/ComputedConfig.class", "com/example/liquibase/change/CreateTableExampleChange.class", "com/example/liquibase/change/DefaultConstraintConfig.class", "com/example/liquibase/change/IdentityConfig.class", "com/example/liquibase/change/KeyColumnConfig.class", "com/example/liquibase/change/PrimaryKeyConfig.class", "com/example/liquibase/change/UniqueConstraintConfig.class", "com/example/my-logic.sql", "com/example/shared", "com/example/shared/file-in-jar.txt", "com/example/shared/file-in-zip.txt", "com/example/users.csv", "com/example/zip", "com/example/zip/file-in-zip.txt"]
        "com/example" | true      | true         | false              | ["com/example/everywhere/file-everywhere.txt", "com/example/everywhere/other-file-everywhere.txt", "com/example/file-in-jar.txt", "com/example/file-in-zip.txt", "com/example/jar/file-in-jar.txt", "com/example/liquibase/change/ColumnConfig.class", "com/example/liquibase/change/ComputedConfig.class", "com/example/liquibase/change/CreateTableExampleChange.class", "com/example/liquibase/change/DefaultConstraintConfig.class", "com/example/liquibase/change/IdentityConfig.class", "com/example/liquibase/change/KeyColumnConfig.class", "com/example/liquibase/change/PrimaryKeyConfig.class", "com/example/liquibase/change/UniqueConstraintConfig.class", "com/example/my-logic.sql", "com/example/shared/file-in-jar.txt", "com/example/shared/file-in-zip.txt", "com/example/users.csv", "com/example/zip/file-in-zip.txt"]
        "com/example" | true      | false        | true               | ["com/example/everywhere", "com/example/jar", "com/example/liquibase", "com/example/liquibase/change", "com/example/shared", "com/example/zip"]
        "com/example" | true      | false        | false              | []
        "com/example" | false     | false        | false              | []
    }

}
