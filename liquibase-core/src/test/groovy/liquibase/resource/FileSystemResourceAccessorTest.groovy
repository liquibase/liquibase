package liquibase.resource

import spock.lang.Specification

public class FileSystemResourceAccessorTest extends Specification {

    def createResourceAccessor() {
        File thisClassFile = new File(new URI(this.getClass().getClassLoader().getResource("liquibase/resource/FileSystemResourceAccessor.class").toExternalForm()));
        String packageDirectory = thisClassFile.getParent();

        
        return new FileSystemResourceAccessor(packageDirectory);
    }
    
    def onlyAcceptsDirectories() {
        when:
        File thisClassFile = new File(new URI(this.getClass().getClassLoader().getResource("liquibase/resource/FileSystemResourceAccessor.class").toExternalForm()));

        ResourceAccessor o = new FileSystemResourceAccessor(thisClassFile.getAbsolutePath());

        then:
        thrown(IllegalArgumentException)
    }

    def singleFileTest() {
        expect:
        createResourceAccessor().getResourcesAsStream("FileSystemResourceAccessor.class") != null;
    }
    
    def multipleFileTest() throws IOException {
        expect:
        createResourceAccessor().list(null, ".", true, true, true).findAll({it.contains("FileSystemResourceAccessor")}).size() > 0
    }
    
    def alphabeticalOrderTest() throws IOException {
        expect:
    	def files = createResourceAccessor().list(null, ".", true, false, false);
    	boolean correct = false;
    	String lastFile = null;
        for (file in files) {
            if (lastFile != null) {
                assert lastFile.compareTo(file) > 0 : file+" should have come before "+lastFile;
            }
        }
    }

    def addRootPathTestDirectory() {
        expect:
        def directoryUrl = new URL("file:/home/user/liquibase/liquibase-core/target/test-classes/");
        def fileSystemResourceAccessor = createResourceAccessor();
        def count = fileSystemResourceAccessor.getRootPaths().size();

        fileSystemResourceAccessor.addRootPath(directoryUrl);

        assert (fileSystemResourceAccessor.getRootPaths().size() == count + 1) : "non-root directory not added";
    }

    def addRootPathTestMultiReleaseJar() {
        expect:
        def multiReleaseJarUrl = new URL("jar:file:/home/user/.m2/repository/javax/xml/bind/jaxb-api/2.3.0/jaxb-api-2.3.0.jar!/META-INF/versions/9/");
        def fileSystemResourceAccessor = createResourceAccessor();
        def count = fileSystemResourceAccessor.getRootPaths().size();

        fileSystemResourceAccessor.addRootPath(multiReleaseJarUrl);

        assert (fileSystemResourceAccessor.getRootPaths().size() == count + 1) : "multi-release jar not added";
    }
}
