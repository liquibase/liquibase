package liquibase.resource

import spock.lang.Specification

import static org.junit.Assert.assertNull

public class CompositeResourceAccessorTest extends Specification {
    
    def ResourceAccessor first;
    def ResourceAccessor second;
    def CompositeResourceAccessor composite;
    def InputStream validStream;
    def Set<String> empty = new HashSet<>()
    def Set<String> hasElements;
    
    def setup() {
        first = Mock(ResourceAccessor.class);
        second = Mock(ResourceAccessor.class);
        composite = new CompositeResourceAccessor(first,second);
        validStream = this.getClass().getClassLoader().getResourceAsStream("liquibase/resource/CompositeResourceAccessorTest.class");

        hasElements = new HashSet<>()
        def resources = this.getClass().getClassLoader().getResources("liquibase")
        while (resources.hasMoreElements()) {
            hasElements.add(resources.nextElement().toExternalForm())
        }

    }
    
    def cleanup() {
        if (validStream != null) {
            validStream.close();
        }
        
    }
    
    def streamFirstHas() {
        when:
        1 * first.getResourcesAsStream("file") >> new HashSet<InputStream>(Arrays.asList(validStream))
        def is = composite.getResourcesAsStream("file");
        
        then:
        validStream == is.iterator().next();
    }
    
    def streamSecondHas() {
        when:
        first.getResourcesAsStream("file") >> null
        second.getResourcesAsStream("file") >> new HashSet<InputStream>(Arrays.asList(validStream))
        def is = composite.getResourcesAsStream("file");

        then:
        validStream == is.iterator().next()
    }
    
    def streamNeitherHas() {
        when:
        first.getResourcesAsStream("file") >> null
        second.getResourcesAsStream("file") >> null
        def is = composite.getResourcesAsStream("file");
        
        then:
        is == null
        assertNull(is);
    }
    
    def resourcesFirstHas() {
        when:
        first.list(null, "file", true, true, true) >> hasElements
        def urls = composite.list(null, "file", true, true, true);

        then:
        urls == hasElements
    }
    
    def resourcesSecondHas() {
        when:
        first.list(null, "file", true, true, true) >> empty
        second.list(null, "file", true, true, true) >> hasElements
        def urls = composite.list(null, "file", true, true, true)

        then:
        hasElements == urls
    }
    
    def resourcesNeitherHas() {
        when:
        first.list(null, "file", true, true, true) >> empty
        second.list(null, "file", true, true, true) >> empty

        def urls = composite.list(null, "file", true, true, true)

        then:
        urls == null
    }
}