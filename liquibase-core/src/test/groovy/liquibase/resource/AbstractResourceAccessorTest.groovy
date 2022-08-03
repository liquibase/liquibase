package liquibase.resource

import spock.lang.Specification

class AbstractResourceAccessorTest extends Specification {


    protected AbstractResourceAccessor createResourceAccessor(List rootUrls, boolean caseSensitive) {
//        def rootUrlsSet = new ArrayList(rootUrls)
        new AbstractResourceAccessor() {

            @Override
            List<Resource> getAll(String path) throws IOException {
                return null
            }

            @Override
            List<Resource> search(String path, boolean recursive) throws IOException {
                return null
            }

            @Override
            List<String> describeLocations() {
                return null
            }

            @Override
            void close() throws Exception {

            }
        }
    }
}
