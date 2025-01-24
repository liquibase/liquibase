package liquibase.serializer.core.yaml

import spock.lang.Specification

class YamlSerializerTest extends Specification {

    def "timestamps are double quoted properly"() {
        when:
        def formatted = YamlSerializer.removeClassTypeMarksFromSerializedJson("""
[
  {
    "description": "sql",
    "end": !!timestamp "2025-01-15T20:59:25.755Z",
    "outcome": "success",
    "start": !!timestamp "2025-01-15T20:59:25.738Z",
    "tag": !!null "null"
  }
]
""")

        then:
        formatted == """
[
  {
    "description": "sql",
    "end": "2025-01-15T20:59:25.755Z",
    "outcome": "success",
    "start": "2025-01-15T20:59:25.738Z",
    "tag": "null"
  }
]
"""
    }
}
