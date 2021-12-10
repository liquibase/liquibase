package liquibase.ui.interactive.getter

import jdk.nashorn.internal.objects.annotations.Where
import liquibase.util.StringUtil
import spock.lang.Specification
import spock.lang.Unroll

class DirectoryGetterTest extends Specification {
    def "Validate absolute paths allowed for existing path"() {
        expect:
        new DirectoryGetter().validate(new File(".").getAbsolutePath())
    }

    def "Validate absolute paths allowed for nonexistant path"() {
        expect:
        new DirectoryGetter().validate(new File(StringUtil.randomIdentifer(10)).getAbsolutePath())
    }

    @Unroll
    def "Validate current path allowed"(String dir) {
        expect:
        new DirectoryGetter().validate(new File(dir).getAbsolutePath())

        where:
        dir | _
        "." | _
        "./" | _
        ".\\" | _
    }

    def "Existing file not allowed" () {
        given:
        def file = new File(StringUtil.randomIdentifer(10))
        file.write("helloworld")
        file.isFile()
        file.deleteOnExit()

        when:
        new DirectoryGetter().validate(file.getName())

        then:
        thrown(IllegalArgumentException)
    }
}
