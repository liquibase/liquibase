package liquibase.resource

import liquibase.GlobalConfiguration
import liquibase.Scope
import spock.lang.Specification
import spock.lang.Unroll

class ZipResourceAccessorTest extends Specification {

    ZipResourceAccessor simpleTestAccessor

    def setup() {
        simpleTestAccessor = new ZipResourceAccessor(new File(this.getClass().getClassLoader().getResource("simple-files.jar").toURI()));

    }

    @Unroll
    def "Cannot construct invalid values"() {
        when:
        new ZipResourceAccessor(testFile);

        then:
        def e = thrown(IllegalArgumentException)
        assert e.message.startsWith(expected)

        where:
        testFile                                                                                                             | expected
        null                                                                                                                 | "File must not be null"
        new File("/invalid/file.zip")                                                                                        | "Non-existent file: "
        new File("/invalid/file.jar")                                                                                        | "Non-existent file: "
        new File(this.getClass().getClassLoader().getResource("liquibase/resource/DirectoryResourceAccessor.class").toURI()) | "Not a jar or zip file: "
    }


    @Unroll("#featureName: #path")
    def "getAll"() {
        expect:
        simpleTestAccessor.getAll(path)*.getPath() == expected

        where:
        path                                 | expected
        "file-in-jar-root.txt"               | ["file-in-jar-root.txt"]
        "/file-in-jar-root.txt"              | ["file-in-jar-root.txt"]
        "\\file-in-jar-root.txt"             | ["file-in-jar-root.txt"]
        "com/example/jar/file-in-jar.txt"    | ["com/example/jar/file-in-jar.txt"]
        "com\\example\\jar\\file-in-jar.txt" | ["com/example/jar/file-in-jar.txt"]
        "/com/example/jar/file-in-jar.txt"   | ["com/example/jar/file-in-jar.txt"]
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
        path                     | recursive | expected
        "com/example"            | false     | ["com/example/file-in-jar.txt"]
        "com/example/everywhere" | false     | ["com/example/everywhere/other-file-everywhere.txt",
                                                "com/example/everywhere/file-everywhere.txt",]
        "com/example"            | true      | ["com/example/shared/file-in-jar.txt",
                                                "com/example/jar/file-in-jar.txt",
                                                "com/example/file-in-jar.txt",
                                                "com/example/everywhere/other-file-everywhere.txt",
                                                "com/example/everywhere/file-everywhere.txt",
        ]
    }

}
