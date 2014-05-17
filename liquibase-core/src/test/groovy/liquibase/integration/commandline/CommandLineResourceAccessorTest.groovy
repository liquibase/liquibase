package liquibase.integration.commandline;


import liquibase.resource.ResourceAccessor
import spock.lang.Specification

public class CommandLineResourceAccessorTest extends Specification {

    def ResourceAccessor createFileOpener() {
        return new CommandLineResourceAccessor(Thread.currentThread().getContextClassLoader())
    }

    def getResourcesAsStream() throws Exception {
        when:
        def resourcesAsStream = createFileOpener().getResourcesAsStream("liquibase/integration/ant/AntResourceAccessorTest.class");

        then:
        resourcesAsStream.size() == 1

        resourcesAsStream.iterator().next().close()
    }

    def "getResourcesAsStream when file does not exist"() throws Exception {
        expect:
        createFileOpener().getResourcesAsStream("non/existant/file.txt") == null
    }

    def "getContents"() throws Exception {
        when:
        def contents = createFileOpener().list(null, "liquibase/change", true, true, true);

        then:
        contents != null
    }
}
