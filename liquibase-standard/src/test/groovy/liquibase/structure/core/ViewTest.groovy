package liquibase.structure.core

import spock.lang.Specification

class ViewTest extends Specification {

    def "toString() logic"() {
        expect:
        view.toString() == expected

        where:
        view | expected
        new View(null, null, "view_name") | "view_name"
        new View(null, "schema", "view_name") | "view_name"
        new View("cat", "schema", "view_name") | "view_name"
        new View(null, null, "view_name").addColumn(new Column(View.class, null, null, "view_name", "col1")) | "view_name"
    }
}
