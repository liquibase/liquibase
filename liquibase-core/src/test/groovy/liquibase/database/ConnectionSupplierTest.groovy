package liquibase.database

import liquibase.database.core.UnsupportedDatabaseSupplier
import org.hamcrest.Matchers
import spock.lang.Specification

import java.lang.reflect.Array

import static org.hamcrest.Matchers.containsInAnyOrder
import static spock.util.matcher.HamcrestSupport.that


class ConnectionSupplierTest extends Specification {

    def "getContainers"() {
        expect:
        def supplier = new UnsupportedDatabaseSupplier()

        that supplier.getContainers(true)*.toString(), containsInAnyOrder([
                "lbcat",
                "lbcat2",
                "#DEFAULT",
                "lbschema.lbcat",
                "lbschema.lbcat2",
                "lbschema2.lbcat",
                "lbschema2.lbcat2",
                "#DEFAULT.lbcat2",
                "#DEFAULT.lbcat",
        ].toArray())

        that supplier.getContainers(false)*.toString(), containsInAnyOrder([
                "lbschema.lbcat",
                "lbschema.lbcat2",
                "lbschema2.lbcat",
                "lbschema2.lbcat2",
        ].toArray())
    }
}
