package liquibase.util


import spock.lang.Specification

import javax.xml.parsers.DocumentBuilderFactory

class XMLUtilTest extends Specification {

    def "getTextContent"() {

        when:
        def document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        def element = document.createElement("testElement")
        element.setAttribute("attr1", "Attribute 1")
        element.setAttribute("attr2", "Attribute 2")

        def child = document.createElement("childElement")
        child.setAttribute("child1", "Child 1")
        child.setAttribute("child2", "Child 2")
        child.setTextContent("Child text")
        element.appendChild(child)

        then:
        XMLUtil.getTextContent(element) == ""
        XMLUtil.getTextContent(child) == "Child text"

        when:
        element.setTextContent("Added text")
        then:
        XMLUtil.getTextContent(element) == "Added text"
    }
}
