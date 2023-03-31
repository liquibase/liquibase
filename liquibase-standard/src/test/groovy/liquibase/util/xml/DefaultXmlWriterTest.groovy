package liquibase.util.xml

import spock.lang.Specification

import javax.xml.parsers.DocumentBuilderFactory

class DefaultXmlWriterTest extends Specification {

    def write() {
        when:
        def doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream("<test><doc text='true'>value</doc></test>".getBytes()))

        def output = new ByteArrayOutputStream()
        new DefaultXmlWriter().write(doc, output)

        then:
        output.toString() == "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>${System.lineSeparator()}" +
                "<test>${System.lineSeparator()}" +
                "    <doc text=\"true\">value</doc>${System.lineSeparator()}" +
                "</test>${System.lineSeparator()}"
    }
}
