package liquibase.util

import spock.lang.Specification

class SystemUtilTest extends Specification {

    def isWindows() {
        expect:
        SystemUtil.isWindows() == new File("c:\\").exists()
    }

    def getJavaMajorVersion() {
        expect:
        SystemUtil.getJavaMajorVersion() >= 8
    }

    def getJavaVersion() {
        expect:
        SystemUtil.getJavaVersion().contains(".")
    }
}
