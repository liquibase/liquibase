package liquibase.integration.ant

import org.apache.tools.ant.Project
import org.apache.tools.ant.types.Path
import spock.lang.Specification

/**
 * Tests for {@link liquibase.integration.ant.AntResourceAccessor}
 */
public class AntResourceAccessorTest extends Specification {

    protected AntResourceAccessor createResourceAccessor() {
        Project project = new Project();
        return new AntResourceAccessor(project, new Path(project));
    }

    def getResourcesAsStream() throws Exception {
        expect:
        def streams = createResourceAccessor().openStreams(null,"liquibase/integration/ant/AntResourceAccessorTest.class")
        streams.size() > 0

        for (stream in streams) {
            stream.close()
        }

    }

    def "getResourceAsStream when file does not exist"() throws Exception {
        expect:
        createResourceAccessor().openStreams(null,"non/existant/file.txt").empty
    }

    def list() throws Exception {
        when:
        def resources = createResourceAccessor().list(null, "liquibase/change", true, true, false);
        then:
        resources.size() > 0
    }
}
