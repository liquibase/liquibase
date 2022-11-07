package liquibase.util

import spock.lang.Specification

class NetUtilTest extends Specification {

    def getLocalHostAddress() {
        when:
        def address = NetUtil.getLocalHostAddress()

        then:
        address.contains(".")
        address != "127.0.0.1"
        !address.contains(":") //not an ipv6 address

        address == NetUtil.getLocalHostAddress() //correctly gets cache
    }


    def getLocalHostName() {
        expect:
        NetUtil.getLocalHostName() != null
        NetUtil.getLocalHostName() == NetUtil.getLocalHostName() //correctly gets cache

    }
}
