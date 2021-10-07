package liquibase.util

import spock.lang.Specification

class NetUtilTest extends Specification {

    def getLocalHostAddress() {
        expect:
        NetUtil.getLocalHostAddress().contains(".")
    }


    def getLocalHostName() {
        expect:
        NetUtil.getLocalHostName() != null
    }
}
