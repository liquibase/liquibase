package liquibase.resource

import spock.lang.Specification

class AbstractResourceAccessorTest extends Specification {


    protected AbstractResourceAccessor createResourceAccessor(List rootUrls, boolean caseSensitive) {
//        def rootUrlsSet = new ArrayList(rootUrls)
        new AbstractResourceAccessor() {
            @Override
            SortedSet<Resource> find(String relativeTo, String path, boolean recursive, boolean includeFiles, boolean includeDirectories) throws IOException {
                return null
            }

            @Override
            SortedSet<String> describeLocations() {
                return null
            }
        }
    }
}
