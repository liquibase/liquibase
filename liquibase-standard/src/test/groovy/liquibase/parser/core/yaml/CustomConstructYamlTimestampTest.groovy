package liquibase.parser.core.yaml

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.nodes.ScalarNode
import org.yaml.snakeyaml.nodes.Tag
import spock.lang.Specification

class CustomConstructYamlTimestampTest extends Specification {
    def "validate timestamp is parsed correctly without specifying a timezone"() {
        given:
         def node = new ScalarNode(new Tag("tag:yaml.org,2002:timestamp"), "2018-03-09 08:41:31.000", null, null, DumperOptions.ScalarStyle.createStyle(new Character('\'' as char)))
        def customConstructYamlTimestamp = new CustomConstructYamlTimestamp()
        when:
        def returnedValue = customConstructYamlTimestamp.construct(node)

        then:
        returnedValue.toString() == "2018-03-09 08:41:31"
    }

    def "validate timestamp is parsed correctly specifying a timezone"() {
        given:
        TimeZone.setDefault(TimeZone.getTimeZone("PST"))
        def node = new ScalarNode(new Tag("tag:yaml.org,2002:timestamp"), "2018-03-09 08:41:31.000+08:00", null, null, DumperOptions.ScalarStyle.createStyle(new Character('\'' as char)))
        def customConstructYamlTimestamp = new CustomConstructYamlTimestamp()
        when:
        def returnedValue = customConstructYamlTimestamp.construct(node)

        then:
        returnedValue.toString() == "Thu Mar 08 16:41:31 PST 2018"
    }
}
