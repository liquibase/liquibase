package liquibase.resource

import spock.lang.Specification
import spock.lang.Unroll

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
        simpleTestAccessor.openStream(null, path) != null

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
        String osName = System.getProperty("os.name");
        if ((osName != null) && !osName.toLowerCase().contains("windows")) {
            return
        }

        simpleTestAccessor.openStream(null, path) != null

        where:
        path << [
                new File(getClass().getResource("/liquibase/resource/FileSystemResourceAccessor.class").toURI()).getCanonicalPath(),
                "/" + new File(getClass().getResource("/liquibase/resource/FileSystemResourceAccessor.class").toURI()).getCanonicalPath(),
        ]
    }

    def "openStream throws an error if multiple files match"() {
        when:
        simpleTestAccessor.openStream(null, "com/example/everywhere/file-everywhere.txt",)

        then:
        def e = thrown(IOException)
        e.message.startsWith("Found 3 files that match com/example/everywhere/file-everywhere.txt: file:")
        e.message.contains("file-everywhere.txt")
        e.message.contains("test-classes")
        e.message.contains("simple-files.jar")
        e.message.contains("simple-files.zip")
    }

    def "openStream returns null if nothing matches"() {
        expect:
        simpleTestAccessor.openStream(null, "com/example/invalid.txt") == null
    }

    @Unroll("#featureName: #path relative to #relativeTo")
    def "openStreams can open files"() throws IOException {
        expect:
        simpleTestAccessor.openStreams(relativeTo, path).size() == size

        where:
        relativeTo                                   | path                                            | size | notes
        null                                         | "file-in-root.txt"                              | 3    | null
        null                                         | "com/example/everywhere/file-everywhere.txt"    | 3    | null
        null                                         | "com\\example\\everywhere\\file-everywhere.txt" | 3    | null
        "com/example"                                | "everywhere/file-everywhere.txt"                | 3    | null
        "/com/example/"                              | "/everywhere/file-everywhere.txt"               | 3    | null
        "com\\example"                               | "everywhere\\file-everywhere.txt"               | 3    | null
        "com/example/everywhere/file-everywhere.txt" | "other-file-everywhere.txt"                     | 3    | null
        "com/example/everywhere/file-everywhere.txt" | "../everywhere/other-file-everywhere.txt"       | 3    | null
        "com\\example\\users.csv"                    | "everywhere\\file-everywhere.txt"               | 1    | "users.csv is only on file system"
        "com/example/everywhere/file-everywhere.txt" | "../jar/file-in-jar.txt"                        | 1    | "lookup file in jar based on file available everywhere"
        "file-in-root.txt"                           | "com/example/everywhere/file-everywhere.txt"    | 3    | null
        "/file-in-root.txt"                          | "com/example/everywhere/file-everywhere.txt"    | 3    | null
        "\\file-in-root.txt"                         | "com/example/everywhere/file-everywhere.txt"    | 3    | null
        "file-in-root.txt"                           | "com/example/zip/file-in-zip.txt"               | 1    | null
        null                                         | "com/example/zip/file-in-zip.txt"               | 1    | null
        null                                         | "com/example/jar/file-in-jar.txt"               | 1    | null
        null                                         | "com/example/invalid.txt"                       | 0    | null
    }

    @Unroll
    def "list"() {
        expect:
        simpleTestAccessor.list(relativeTo, path, recursive, includeFiles, includeDirectories) == expected as SortedSet

        where:
        relativeTo              | path          | recursive | includeFiles | includeDirectories | expected
        null                    | "com/example" | false     | true         | false              | ["com/example/file-in-jar.txt", "com/example/file-in-zip.txt", "com/example/my-logic.sql", "com/example/users.csv"]
        "com"                   | "example"     | false     | true         | false              | ["com/example/file-in-jar.txt", "com/example/file-in-zip.txt", "com/example/my-logic.sql", "com/example/users.csv"]
        "com/example/users.csv" | "everywhere"  | false     | true         | false              | ["com/example/everywhere/file-everywhere.txt", "com/example/everywhere/other-file-everywhere.txt"]
        null                    | "com/example" | false     | true         | true               | ["com/example/everywhere", "com/example/file-in-jar.txt", "com/example/file-in-zip.txt", "com/example/jar", "com/example/liquibase", "com/example/my-logic.sql", "com/example/shared", "com/example/users.csv", "com/example/zip"]
        null                    | "com/example" | true      | true         | true               | ["com/example/everywhere", "com/example/everywhere/file-everywhere.txt", "com/example/everywhere/other-file-everywhere.txt", "com/example/file-in-jar.txt", "com/example/file-in-zip.txt", "com/example/jar", "com/example/jar/file-in-jar.txt", "com/example/liquibase", "com/example/liquibase/change", "com/example/liquibase/change/ColumnConfig.class", "com/example/liquibase/change/ComputedConfig.class", "com/example/liquibase/change/CreateTableExampleChange.class", "com/example/liquibase/change/DefaultConstraintConfig.class", "com/example/liquibase/change/IdentityConfig.class", "com/example/liquibase/change/KeyColumnConfig.class", "com/example/liquibase/change/PrimaryKeyConfig.class", "com/example/liquibase/change/UniqueConstraintConfig.class", "com/example/my-logic.sql", "com/example/shared", "com/example/shared/file-in-jar.txt", "com/example/shared/file-in-zip.txt", "com/example/users.csv", "com/example/zip", "com/example/zip/file-in-zip.txt"]
        null                    | "com/example" | true      | true         | false              | ["com/example/everywhere/file-everywhere.txt", "com/example/everywhere/other-file-everywhere.txt", "com/example/file-in-jar.txt", "com/example/file-in-zip.txt", "com/example/jar/file-in-jar.txt", "com/example/liquibase/change/ColumnConfig.class", "com/example/liquibase/change/ComputedConfig.class", "com/example/liquibase/change/CreateTableExampleChange.class", "com/example/liquibase/change/DefaultConstraintConfig.class", "com/example/liquibase/change/IdentityConfig.class", "com/example/liquibase/change/KeyColumnConfig.class", "com/example/liquibase/change/PrimaryKeyConfig.class", "com/example/liquibase/change/UniqueConstraintConfig.class", "com/example/my-logic.sql", "com/example/shared/file-in-jar.txt", "com/example/shared/file-in-zip.txt", "com/example/users.csv", "com/example/zip/file-in-zip.txt"]
        null                    | "com/example" | true      | false        | true               | ["com/example/everywhere", "com/example/jar", "com/example/liquibase", "com/example/liquibase/change", "com/example/shared", "com/example/zip"]
        null                    | "com/example" | true      | false        | false              | []
        null                    | "com/example" | false     | false        | false              | []
    }

}
