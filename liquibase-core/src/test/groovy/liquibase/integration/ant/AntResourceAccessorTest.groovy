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
        def streams = createResourceAccessor().getResourcesAsStream("liquibase/integration/ant/AntResourceAccessorTest.class")
        streams.size() > 0

        for (stream in streams) {
            stream.close()
        }

    }

    def "getResourceAsStream when file does not exist"() throws Exception {
        expect:
        createResourceAccessor().getResourcesAsStream("non/existant/file.txt") == null
    }

    def list() throws Exception {
        when:
        def resources = createResourceAccessor().list(null, "liquibase/change", true, true, false);
        then:
        resources.size() > 0
    }
}
