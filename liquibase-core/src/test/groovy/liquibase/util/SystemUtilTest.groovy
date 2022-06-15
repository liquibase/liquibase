package liquibase.util

import spock.lang.Specification

class SystemUtilTest extends Specification {

    def isWindows() {
        expect:
        SystemUtil.isWindows() == new File("c:\\").exists()
    }
}
