package liquibase.resource

import spock.lang.Specification
import spock.lang.Unroll

class AbstractResourceAccessorTest extends Specification {


    protected AbstractResourceAccessor createResourceAccessor(List rootUrls, boolean caseSensitive) {
//        def rootUrlsSet = new ArrayList(rootUrls)
        new AbstractResourceAccessor() {
            @Override
            InputStreamList openStreams(String path) throws IOException {
                return null
            }

            @Override
            SortedSet<String> list(String path, boolean recursive, boolean includeFiles, boolean includeDirectories) throws IOException {
                return null
            }

            @Override
            String getCanonicalPath(String relativeTo, String path) throws IOException {
                return null
            }
        }
    }
}
