package liquibase.resource


import liquibase.util.StreamUtil
import spock.lang.Specification

class SearchPathResourceAccessorTest extends Specification {

    def "can construct"() {
        when:
        ResourceAccessor accessor = new SearchPathsResourceAccessor(new File(".", "target/test-classes").getAbsolutePath() + ", " + new File(".", "target/classes").getAbsolutePath())

        then:
        StreamUtil.readStreamAsString(accessor.openStream(null, "file-in-root.txt")) == "File in root"
        accessor.list(null, "www.liquibase.org/xml/ns/dbchangelog", false, true, false).contains("www.liquibase.org/xml/ns/dbchangelog/dbchangelog-1.0.xsd")

        accessor.describeLocations()[0].endsWith("classes")
        accessor.describeLocations()[1].endsWith("test-classes")
    }
}
