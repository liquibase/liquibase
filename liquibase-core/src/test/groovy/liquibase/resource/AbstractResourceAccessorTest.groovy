package liquibase.resource

import spock.lang.Specification

class AbstractResourceAccessorTest extends Specification {


    protected AbstractResourceAccessor createResourceAccessor(List rootUrls, boolean caseSensitive) {
//        def rootUrlsSet = new ArrayList(rootUrls)
        new AbstractResourceAccessor() {
            @Override
            InputStreamList openStreams(String relativeTo, String streamPath) throws IOException {
                return null
            }

            @Override
            SortedSet<String> list(String relativeTo, String path, boolean recursive, boolean includeFiles, boolean includeDirectories) throws IOException {
                return null
            }
        }
    }
}
