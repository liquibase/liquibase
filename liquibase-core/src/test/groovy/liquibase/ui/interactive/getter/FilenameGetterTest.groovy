package liquibase.ui.interactive.getter

import spock.lang.Specification
import spock.lang.Unroll

class FilenameGetterTest extends Specification {
    def "Validate"() {
        expect:
        new FilenameGetter().validate("hello.txt")
    }

    def "Validate non-files are not allowed" () {
        when:
        new FilenameGetter().validate("hello")

        then:
        thrown(IllegalArgumentException)
    }

    def "Validate files with illegal characters are not allowed" () {
        when:
        new FilenameGetter().validate("hello/.txt")

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "Can't have path elements #dir"(String dir) {
        when:
        new FilenameGetter().validate(dir)

        then:
        thrown(IllegalArgumentException)

        where:
        dir | _
        "path/hello.txt" | _
    }
}
