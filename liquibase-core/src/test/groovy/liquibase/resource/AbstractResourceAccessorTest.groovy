package liquibase.resource

import spock.lang.Specification

class AbstractResourceAccessorTest extends Specification {


    protected AbstractResourceAccessor createResourceAccessor(List rootUrls, boolean caseSensitive) {
//        def rootUrlsSet = new ArrayList(rootUrls)
        new AbstractResourceAccessor() {

            @Override
            SortedSet<Resource> getAll(String path) throws IOException {
                return null
            }

            @Override
            List<Resource> list(String path, boolean recursive) throws IOException {
                return null
            }

            @Override
            SortedSet<String> describeLocations() {
                return null
            }

            @Override
            void close() throws Exception {

            }
        }
    }
}
