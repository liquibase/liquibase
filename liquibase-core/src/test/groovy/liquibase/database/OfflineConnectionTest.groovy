package liquibase.database

import liquibase.test.JUnitResourceAccessor
import spock.lang.Specification

class OfflineConnectionTest extends Specification {

    def "constructor parses query parameters correctly"() {
        when:
        def connection = new OfflineConnection(url, new JUnitResourceAccessor());

        then:
        connection.params == expectedParams


        where:
        url                      | expectedParams
        "offline:oracle?a=b"     | ["a": "b"]
        "offline:oracle?a=b&c=d" | ["a": "b", "c": "d"]
        "offline:oracle?space=b%20c&d=e" | ["d": "e", "space": "b c"]
        "offline:oracle?eq=b%20%3D%20c&d=e" | ["d": "e", "eq": "b = c"]
        "offline:oracle?fancy%20%3D%20key=b%20%3D%20c&d=e" | ["d": "e", "fancy = key": "b = c"]

    }
}
