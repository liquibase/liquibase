package liquibase.resource

import liquibase.Scope
import liquibase.util.StreamUtil
import spock.lang.IgnoreIf
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Path

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
                                     "com/example/liquibase/change/ChangeWithPrimitiveFields.class",
                                     "com/example/liquibase/change/ColumnConfigExample.class",
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

    @Unroll("getAll rejects path traversal under strict mode: #payload")
    def "getAll rejects path traversal payloads that escape the accessor root when allowParentDirectoryReferences=false"() {
        // PR #7729 introduced the CWE-22 syntactic containment check. With the deprecation flag
        // liquibase.allowParentDirectoryReferences defaulting to true, the check is opt-in; this
        // spec exercises the opt-in (strict) path explicitly via Scope.child.
        given:
        IOException caught = null

        when:
        Scope.child(["liquibase.allowParentDirectoryReferences": "false"] as Map, {
            try {
                simpleTestAccessor.getAll(payload)
            } catch (IOException e) {
                caught = e
            }
        } as Scope.ScopedRunner)

        then:
        caught != null
        caught.message.contains("resolves outside accessor root")

        where:
        payload << [
                "../../etc/passwd",
                "../../../etc/passwd",
                "./../../etc/passwd",
                "subdir/../../escape.txt",
                "..\\..\\..\\etc\\passwd",
        ]
    }

    def "getAll allows paths that traverse via .. but stay inside the root"() {
        // subdir/.. collapses pre-resolve and the resulting path stays under root.
        // Behaviour is identical under both flag values — included in the default suite
        // (flag=true) since that's the deprecation-window default.
        expect:
        simpleTestAccessor.getAll("liquibase/resource/foo/../DirectoryResourceAccessorTest.class")*.getPath() ==
                ["liquibase/resource/DirectoryResourceAccessorTest.class"]
    }

    def "getAll allows ../ payloads that resolve outside the root when allowParentDirectoryReferences=true (deprecation default)"() {
        // Wesley/Filipe regression scenario: a customer with a 'shared dbarepo at the project
        // root, per-environment changelogs underneath' layout references '../shared/foo' from
        // within the accessor root. With the deprecation-default flag=true, the resolution
        // succeeds (matching pre-CWE-22-fix behaviour); with flag=false (the future default),
        // the same payload would be rejected.
        given:
        Path rootDir = Files.createTempDirectory("accessor-root-")
        Path outsideDir = Files.createTempDirectory("outside-sibling-")
        Path outsideFile = outsideDir.resolve("legit.txt")
        Files.writeString(outsideFile, "legitimate sibling content")
        def accessor = new DirectoryResourceAccessor(rootDir)
        String payload = "../" + outsideDir.fileName.toString() + "/legit.txt"
        List<Resource> result = null

        when:
        Scope.child(["liquibase.allowParentDirectoryReferences": "true"] as Map, {
            result = accessor.getAll(payload)
        } as Scope.ScopedRunner)

        then:
        result != null
        result.size() == 1
        result[0].getPath() == payload

        cleanup:
        Files.deleteIfExists(outsideFile)
        Files.deleteIfExists(outsideDir)
        Files.deleteIfExists(rootDir)
    }

    def "search rejects ../ startPath when allowParentDirectoryReferences=false"() {
        // Strict-mode counterpart to the search() default-mode spec below. With the deprecation
        // flag defaulting to true, this strict-rejection assertion runs inside Scope.child.
        given:
        IOException caught = null

        when:
        Scope.child(["liquibase.allowParentDirectoryReferences": "false"] as Map, {
            try {
                simpleTestAccessor.search("../../etc", true)
            } catch (IOException e) {
                caught = e
            }
        } as Scope.ScopedRunner)

        then:
        caught != null
        caught.message.contains("resolves outside accessor root")
    }

    def "search allows ../ startPath that resolves outside the root when allowParentDirectoryReferences=true (deprecation default)"() {
        // Mirrors the getAll() deprecation-default spec: a sibling directory referenced via
        // '..' from the accessor root is reachable under flag=true. Wesley flagged Flow
        // <conditions> and policy-check SCRIPT_PATH as call sites that depend on this.
        given:
        Path rootDir = Files.createTempDirectory("accessor-root-")
        Path outsideDir = Files.createTempDirectory("outside-sibling-")
        Path outsideFile = outsideDir.resolve("foo.txt")
        Files.writeString(outsideFile, "content")
        def accessor = new DirectoryResourceAccessor(rootDir)
        String startPath = "../" + outsideDir.fileName.toString()
        List<Resource> result = null

        when:
        Scope.child(["liquibase.allowParentDirectoryReferences": "true"] as Map, {
            result = accessor.search(startPath, true)
        } as Scope.ScopedRunner)

        then:
        result != null
        result.size() == 1
        result[0].getPath().endsWith("foo.txt")

        cleanup:
        Files.deleteIfExists(outsideFile)
        Files.deleteIfExists(outsideDir)
        Files.deleteIfExists(rootDir)
    }

    // Skipped on Windows because Files.createSymbolicLink requires the
    // SeCreateSymbolicLinkPrivilege (developer mode or admin).
    @IgnoreIf({ os.windows })
    def "getAll rejects a symlink that escapes the accessor root when allowParentDirectoryReferences=false"() {
        // Strict-mode behaviour: a symlink whose canonical real path escapes the canonical
        // root is rejected. With the deprecation flag defaulting to true, this assertion runs
        // inside Scope.child.
        given:
        Path rootDir = Files.createTempDirectory("accessor-root-")
        Path outsideDir = Files.createTempDirectory("outside-secret-")
        Path outsideFile = outsideDir.resolve("secret.txt")
        Files.writeString(outsideFile, "SECRET")
        Path symlinkInside = rootDir.resolve("escape-link")
        Files.createSymbolicLink(symlinkInside, outsideFile)
        def accessor = new DirectoryResourceAccessor(rootDir)
        IOException caught = null

        when:
        Scope.child(["liquibase.allowParentDirectoryReferences": "false"] as Map, {
            try {
                accessor.getAll("escape-link")
            } catch (IOException e) {
                caught = e
            }
        } as Scope.ScopedRunner)

        then:
        caught != null
        caught.message.contains("symlink") || caught.message.contains("outside")

        cleanup:
        Files.deleteIfExists(symlinkInside)
        Files.deleteIfExists(outsideFile)
        Files.deleteIfExists(outsideDir)
        Files.deleteIfExists(rootDir)
    }

    @IgnoreIf({ os.windows })
    def "getAll follows a symlink that escapes the accessor root when allowParentDirectoryReferences=true (deprecation default)"() {
        // Default-mode counterpart: legitimate symlink (e.g. customers using filesystem
        // symlinks to share resources across projects) is followed without rejection.
        given:
        Path rootDir = Files.createTempDirectory("accessor-root-")
        Path outsideDir = Files.createTempDirectory("outside-content-")
        Path outsideFile = outsideDir.resolve("legit.txt")
        Files.writeString(outsideFile, "legitimate symlinked content")
        Path symlinkInside = rootDir.resolve("legit-link")
        Files.createSymbolicLink(symlinkInside, outsideFile)
        def accessor = new DirectoryResourceAccessor(rootDir)
        List<Resource> result = null

        when:
        Scope.child(["liquibase.allowParentDirectoryReferences": "true"] as Map, {
            result = accessor.getAll("legit-link")
        } as Scope.ScopedRunner)

        then:
        result != null
        result.size() == 1
        result[0].getPath() == "legit-link"

        cleanup:
        Files.deleteIfExists(symlinkInside)
        Files.deleteIfExists(outsideFile)
        Files.deleteIfExists(outsideDir)
        Files.deleteIfExists(rootDir)
    }

}
