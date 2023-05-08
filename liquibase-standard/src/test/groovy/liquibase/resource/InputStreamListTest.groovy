package liquibase.resource

import spock.lang.Specification

class InputStreamListTest extends Specification {

    def "close on empty list"() {
        given:
        InputStreamList list = new InputStreamList()

        when:
        list.close()

        then:
        list.size() == 0
    }

    def "close on populated list"() {
        given:
        def input1  = Mock(InputStream)
        def input2  = Mock(InputStream)

        InputStreamList list = new InputStreamList()
        list.add(URI.create("test:1"), input1)
        list.add(URI.create("test:2"), input2)

        when:
        list.close()

        then:
        1 * input1.close()
        1 * input2.close()

    }

    def "Adding duplicate streams"() {
        given:
        def input1  = Mock(InputStream)
        def input2  = Mock(InputStream)
        def input3  = Mock(InputStream)

        InputStreamList list = new InputStreamList()

        when:
        list.add(URI.create("test:same"), input1)
        list.add(URI.create("test:same"), input2)
        list.add(URI.create("test:different"), input3)

        then:
        0 * input1.close()
        1 * input2.close()
        0 * input3.close()

        list.size() == 2

    }

    def "alreadySaw"() {
        when:
        def list = new InputStreamList()
        list.add(new URI("file:///C:/test/path/liquibase-4.0.0-beta1-local-SNAPSHOT.jar!www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.6.xsd"), new ByteArrayInputStream())
        list.add(new URI("jar:file:/C:/test/path/liquibase-4.0.0-beta1-local-SNAPSHOT.jar!/www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd"), new ByteArrayInputStream())

        then:
        assert list.alreadySaw(new URI("file:///C:/test/path/liquibase-4.0.0-beta1-local-SNAPSHOT.jar!www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.6.xsd"))
        assert list.alreadySaw(new URI("jar:file:/C:/test/path/liquibase-4.0.0-beta1-local-SNAPSHOT.jar!/www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.6.xsd"))
        assert list.alreadySaw(new URI("file:///C:/test/path/liquibase-4.0.0-beta1-local-SNAPSHOT.jar!www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd"))
        assert list.alreadySaw(new URI("jar:file:/C:/test/path/liquibase-4.0.0-beta1-local-SNAPSHOT.jar!/www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd"))

        assert !list.alreadySaw(new URI("file:///C:/test/path/liquibase-4.0.0-beta1-local-SNAPSHOT.jar!www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.5.xsd"))
        assert !list.alreadySaw(new URI("jar:file:/C:/test/path/liquibase-4.0.0-beta1-local-SNAPSHOT.jar!/www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.5.xsd"))
    }
}
