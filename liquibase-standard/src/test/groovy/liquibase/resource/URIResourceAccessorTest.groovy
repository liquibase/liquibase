package liquibase.resource

import spock.lang.Specification
import spock.lang.Unroll

class URIResourceAccessorTest extends Specification {

    @Unroll
    def resolveSibling() {
        when:
        def newResource = new URIResource(path, URI.create(uri)).resolveSibling(input)

        then:
        newResource.uri.toString() == expectedUri
        newResource.path == expectedPath

        where:
        path                                             | uri                                                                                                                                     | input                                      | expectedUri                                                                                                                             | expectedPath
        "my/file.xml"                                    | "file:/local/my/file.xml"                                                                                                               | "other.csv"                                | "file:/local/my/other.csv"                                                                                                              | "my/other.csv"
        "liquibase/harness/data/changelogs/loadData.xml" | "jar:file:/C:/Users/example/liquibase-test-harness-1.0.6.jar!/liquibase/harness/data/changelogs/loadData.xml"                           | "load.csv"                                 | "jar:file:/C:/Users/example/liquibase-test-harness-1.0.6.jar!/liquibase/harness/data/changelogs/load.csv"                               | "liquibase/harness/data/changelogs/load.csv"
        "my/file.xml"                                    | "http:/local/my/file.xml"                                                                                                               | "other.csv"                                | "http:/local/my/other.csv"                                                                                                              | "my/other.csv"
        "../../oracle/package/usoi_trace_pkg.sql"        | "jar:file:core.war/WEB-INF/lib/usoi-db-scripts-24.1-plain.jar!/com/asuproject/usoi/db/manage/scripts/oracle/package/usoi_trace_pkg.sql" | "../../oracle/package/usoi_trace_pkg.sql"  | "jar:file:core.war/WEB-INF/lib/usoi-db-scripts-24.1-plain.jar!/com/asuproject/usoi/db/manage/scripts/oracle/package/usoi_trace_pkg.sql" | "com/asuproject/usoi/db/manage/scripts/oracle/package/usoi_trace_pkg.sql"
    }
}
