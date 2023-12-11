package liquibase.parser.core.xml

import liquibase.changelog.ChangeLogParameters
import liquibase.sdk.supplier.resource.ResourceSupplier
import org.xml.sax.Attributes
import spock.lang.Shared
import spock.lang.Specification

class XMLChangeLogSAXHandlerTest extends Specification {

    @Shared resourceSupplier = new ResourceSupplier()
    def uri = "http://www.liquibase.org/xml/ns/dbchangelog"

    def "empty file parses to null"() {
        when:
        def handler = new XMLChangeLogSAXHandler("com/example/test.xml", resourceSupplier.simpleResourceAccessor, new ChangeLogParameters())
        then:
        handler.databaseChangeLogTree == null
    }

    def "only root node file parses to empty ChangeLogNode"() {
        when:
        def handler = new XMLChangeLogSAXHandler("com/example/test.xml", resourceSupplier.simpleResourceAccessor, new ChangeLogParameters())
        handler.startElement(uri, "databaseChangeLog", "databaseChangeLog", null);
        handler.endElement(uri, "databaseChangeLog", "databaseChangeLog");

        then:
        handler.databaseChangeLogTree.name == "databaseChangeLog"
        handler.databaseChangeLogTree.children.size() == 0
    }

    def "node with attributes are parsed"() {
        when:
        def handler = new XMLChangeLogSAXHandler("com/example/test.xml", resourceSupplier.simpleResourceAccessor, new ChangeLogParameters())
        def attributes = Mock(Attributes)
        attributes.getLength() >> 3
        attributes.getLocalName(0) >> "attr0"
        attributes.getQName(0) >> "attr0"
        attributes.getValue(0) >> "attr 0 value"

        attributes.getLocalName(1) >> "attr1"
        attributes.getQName(1) >> "attr1"
        attributes.getValue(1) >> "attr 1 value"

        attributes.getLocalName(2) >> "attr2"
        attributes.getQName(2) >> "attr2"
        attributes.getValue(2) >> "attr 2 value"

        handler.startElement(uri, "databaseChangeLog", "databaseChangeLog", attributes);
        handler.endElement(uri, "databaseChangeLog", "databaseChangeLog");

        then:
        handler.databaseChangeLogTree.name == "databaseChangeLog"
        handler.databaseChangeLogTree.children.size() == 3
        handler.databaseChangeLogTree.getChild(null, "attr0").value == "attr 0 value"
        handler.databaseChangeLogTree.getChild(null, "attr1").value == "attr 1 value"
        handler.databaseChangeLogTree.getChild(null, "attr2").value == "attr 2 value"
    }

    def "node with text is parsed"() {
        when:
        def handler = new XMLChangeLogSAXHandler("com/example/test.xml", resourceSupplier.simpleResourceAccessor, new ChangeLogParameters())
        def attributes = Mock(Attributes)
        attributes.getLength() >> 1
        attributes.getLocalName(0) >> "attr0"
        attributes.getQName(0) >> "attr0"
        attributes.getValue(0) >> "attr 0 value"

        handler.startElement(uri, "databaseChangeLog", "databaseChangeLog", attributes);
        handler.characters("Start of some".toCharArray(), 0, "Start of some".length());
        handler.characters(" more text".toCharArray(), 0, " more text".length());
        handler.endElement(uri, "databaseChangeLog", "databaseChangeLog");

        then:
        handler.databaseChangeLogTree.name == "databaseChangeLog"
        handler.databaseChangeLogTree.children.size() == 1
        handler.databaseChangeLogTree.getChild(null, "attr0").value == "attr 0 value"
        handler.databaseChangeLogTree.value == "Start of some more text"
    }

    def "complex structure with with attributes and child nodes and text is parsed"() {
        when:
        def handler = new XMLChangeLogSAXHandler("com/example/test.xml", resourceSupplier.simpleResourceAccessor, new ChangeLogParameters())
        def attributes = Mock(Attributes)
        attributes.getLength() >> 3
        attributes.getLocalName(0) >> "attr0"
        attributes.getQName(0) >> "attr0"
        attributes.getValue(0) >> "attr 0 value"

        attributes.getLocalName(1) >> "attr1"
        attributes.getQName(1) >> "attr1"
        attributes.getValue(1) >> "attr 1 value"

        attributes.getLocalName(2) >> "attr2"
        attributes.getQName(2) >> "attr2"
        attributes.getValue(2) >> "attr 2 value"

        def childAttributes = Mock(Attributes)
        childAttributes.getLength() >> 2
        childAttributes.getLocalName(0) >> "childAttr0"
        childAttributes.getQName(0) >> "childAttr0"
        childAttributes.getValue(0) >> "child attr 0 value"

        childAttributes.getLocalName(1) >> "childAttr1"
        childAttributes.getQName(1) >> "childAttr1"
        childAttributes.getValue(1) >> "child attr 1 value"

        handler.startElement(uri, "databaseChangeLog", "databaseChangeLog", attributes);
        handler.startElement(uri, "childNode1", "childNode1", null);
        handler.characters("child node 1".toCharArray(), 0, "child node 1".size());

        handler.startElement(uri, "grandChildNode1", "grandChildNode1", childAttributes);
        handler.characters("\n  grand child node 1 text with surrounding spaces\n   ".toCharArray(), 0, "\n  grand child node 1 text with surrounding spaces\n   ".size());
        handler.endElement(uri, "grandChildNode1", "grandChildNode1");

        handler.startElement(uri, "grandChildNode2", "grandChildNode2", null);
        handler.characters("grand child node 2 text".toCharArray(), 0, "grand child node 2 text".size());
        handler.endElement(uri, "grandChildNode2", "grandChildNode2");


        handler.characters(" has more text for child node 1".toCharArray(), 0, " has more text for child node 1".size());
        handler.endElement(uri, "childNode1", "childNode1");

        handler.startElement(uri, "childNode2", "childNode2", null);
        handler.endElement(uri, "childNode2", "childNode2");

        handler.endElement(uri, "databaseChangeLog", "databaseChangeLog");

        then:
        handler.databaseChangeLogTree.name == "databaseChangeLog"
        handler.databaseChangeLogTree.children.size() == 5
        handler.databaseChangeLogTree.getChild(null, "attr0").value == "attr 0 value"
        handler.databaseChangeLogTree.getChild(null, "attr1").value == "attr 1 value"
        handler.databaseChangeLogTree.getChild(null, "attr2").value == "attr 2 value"

        handler.databaseChangeLogTree.getChild(null, "childNode1").value == "child node 1 has more text for child node 1"
        handler.databaseChangeLogTree.getChild(null, "childNode1").children.size() == 2
        handler.databaseChangeLogTree.getChild(null, "childNode1").getChild(null, "grandChildNode1").value == "grand child node 1 text with surrounding spaces"
        handler.databaseChangeLogTree.getChild(null, "childNode1").getChild(null, "grandChildNode1").children.size() == 2
        handler.databaseChangeLogTree.getChild(null, "childNode1").getChild(null, "grandChildNode1").getChild(null, "childAttr0").value == "child attr 0 value"
        handler.databaseChangeLogTree.getChild(null, "childNode1").getChild(null, "grandChildNode1").getChild(null, "childAttr1").value == "child attr 1 value"

        handler.databaseChangeLogTree.getChild(null, "childNode1").getChild(null, "grandChildNode2").value == "grand child node 2 text"
        handler.databaseChangeLogTree.getChild(null, "childNode1").getChild(null, "grandChildNode2").children.size() == 0

        handler.databaseChangeLogTree.getChild(null, "childNode2").children.size() == 0
    }

}
