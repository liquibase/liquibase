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

    def "classLoader.getResource"() {
        when:
        CompositeResourceAccessor composite = new CompositeResourceAccessor(
                new ClassLoaderResourceAccessor(new URLClassLoader([new File("./src/main/java/liquibase/resource").toURI().toURL()] as URL[])),
                new ClassLoaderResourceAccessor(new URLClassLoader([new File("./src/main/java/liquibase/precondition").toURI().toURL()] as URL[])),
        )

        then:
        composite.toClassLoader().getResource("CompositeResourceAccessor.java").toExternalForm().endsWith("src/main/java/liquibase/resource/CompositeResourceAccessor.java")
        composite.toClassLoader().getResource("Precondition.java").toExternalForm().endsWith("src/main/java/liquibase/precondition/Precondition.java")

        //can find from context classloader which is also included
        composite.toClassLoader().getResource("liquibase/precondition/Precondition.class").toExternalForm().endsWith("target/classes/liquibase/precondition/Precondition.class")
    }


    def "classLoader.getResources"() {
        when:
        CompositeResourceAccessor composite = new CompositeResourceAccessor(
                new ClassLoaderResourceAccessor(new URLClassLoader([new File("./src/main/java/liquibase/resource").toURI().toURL()] as URL[])),
                new ClassLoaderResourceAccessor(new URLClassLoader([new File("./src/main/java/liquibase/precondition").toURI().toURL()] as URL[])),
        )

        then:
        composite.toClassLoader().getResources("CompositeResourceAccessor.java")*.toExternalForm()*.endsWith("src/main/java/liquibase/resource/CompositeResourceAccessor.java")
        composite.toClassLoader().getResources("Precondition.java")*.toExternalForm()*.endsWith("src/main/java/liquibase/precondition/Precondition.java")

        assert "Did not find resource from context classloader which is also included", composite.toClassLoader().getResources("liquibase/precondition/Precondition.class")*.toExternalForm()*.endsWith("target/classes/liquibase/precondition/Precondition.class")

        assert "Did not return resources across nested classloaders", composite.toClassLoader().getResources("META-INF/MANIFEST.MF").toList().size() > 2
    }
}
