package liquibase.parser.core

import liquibase.util.ISODateFormat
import spock.lang.Specification
import spock.lang.Unroll

class ParsedNodeTest extends Specification {

    def "read and write children with null namespaces"() {
        when:
        def changeLogNode = new ParsedNode(null, "testNode")

        then:
        changeLogNode.getValue() == null
        changeLogNode.getChild(null, "testAttr") == null
        changeLogNode.getChildValue(null, "testAttr") == null

        when:
        changeLogNode.setValue("child value")
        then:
        changeLogNode.value == "child value"

        when:
        changeLogNode.addChild(null, "testAttr", "test attr")
        changeLogNode.addChild(null, "otherTestAttr", "other test attr")
        then:
        changeLogNode.getChild(null, "testAttr").value == "test attr"
        changeLogNode.getChild(null, "otherTestAttr").value == "other test attr"
        changeLogNode.getChild(null, "invalidAttr") == null
    }

    def "read and write children with set namespaces"() {
        when:
        def changeLogNode = new ParsedNode("ns1", "testNode")

        then:
        changeLogNode.getChild("ns1", "testAttr") == null
        changeLogNode.getChild("ns2", "testAttr") == null

        when:
        changeLogNode.addChild("ns1", "testAttr", "test attr 1")
        changeLogNode.addChild("ns2", "testAttr", "test attr 2")
        changeLogNode.addChild("ns1", "otherTestAttr", "other test attr")
        then:
        changeLogNode.getChild(null, "testAttr") == null
        changeLogNode.getChild("ns1", "testAttr").value == "test attr 1"
        changeLogNode.getChild("ns2", "testAttr").value == "test attr 2"
        changeLogNode.getChild("ns3", "testAttr") == null
        changeLogNode.getChild("ns1", "otherTestAttr").value == "other test attr"
        changeLogNode.getChild(null, "invalidAttr") == null
    }

    @Unroll("#featureName to #expectedOut")
    def "toString output"() {
        expect:
        changeLogNode.toString() == expectedOut

        where:
        changeLogNode                                                                                        | expectedOut
        new ParsedNode(null, "a")                                                                            | "a"
        new ParsedNode(null, "a").addChild(null, "x", "1").addChild(null, "x", "2").addChild(null, "x", "3") | "a[x=1,x=2,x=3]"
        new ParsedNode(null, "a").setValue("y")                                                              | "a=y"
        new ParsedNode(null, "a").addChild(null, "1", "2")                                                   | "a[1=2]"
        new ParsedNode(null, "a").addChild(null, "1", "2").addChild(null, "bb", "22")                        | "a[1=2,bb=22]"
        new ParsedNode(null, "a").addChild(null, "1", "2").setValue("y")                                     | "a[1=2]=y"
        new ParsedNode(null, "a").setValue(["1", "2", "3"])                                                  | "a=(1,2,3)"
        new ParsedNode(null, "a").addChild(null, "1", ["1a", "1b"]).addChild(null, "2", ["2a", "2b"])        | "a[1=(1a,1b),2=(2a,2b)]"
    }

    @Unroll
    def "setValue with different inputs"() {
        when:
        def changeLog = new ParsedNode(null, "root")
        changeLog.setValue(obj)

        then:
        changeLog.value == expected

        where:
        obj        | expected
        null       | null
        1          | 1
        "a"        | "a"
        [1, 2]     | [1, 2]
        [a: "val"] | null
    }

    @Unroll
    def "equals method takes attributes and all into account"() {
        expect:
        assert changeLog1.equals(changeLog2) == expected

        where:
        changeLog1                            | changeLog2                            | expected
        new ParsedNode(null, "a")             | new ParsedNode(null, "a")             | true
        new ParsedNode(null, "a")             | new ParsedNode(null, "b")             | false
        new ParsedNode(null, "a").setValue(1) | new ParsedNode(null, "a").setValue(1) | true
        new ParsedNode(null, "a").setValue(1) | new ParsedNode(null, "a").setValue(2) | false
    }

    @Unroll("#featureName expecting #expected")
    def "setValue with various values"() {
        when:
        def changeLog = new ParsedNode(null, "root")
        changeLog.setValue(children)

        then:
        changeLog.toString() == expected

        where:
        children                                                                                                                            | expected
        "simple value"                                                                                                                      | "root=simple value"
        [x: "1", y: "2"]                                                                                                                    | "root[x=1,y=2]"
        [x: "1", y: "2", child1: [childX: "1"], child2: [childX: "2", childY: "Z"], z: "3"]                                                 | "root[child1[childX=1],child2[childX=2,childY=Z],x=1,y=2,z=3]"
        [x: "1", y: "2", child1: [childX: "1"], child2: [childX: "2", childY: [grandChild1: "AA", grandChild2: "BB"], childZ: "3"], z: "4"] | "root[child1[childX=1],child2[childX=2,childY[grandChild1=AA,grandChild2=BB],childZ=3],x=1,y=2,z=4]"
        ["1", "2", "3"]                                                                                                                     | "root=(1,2,3)"
        [x: "1", list: ["a", "b", "c"]]                                                                                                     | "root[list=(a,b,c),x=1]"
        [x: "1", list: ["a", [childNode: "cn1"], [childNode2: [childNode2a: "cn2", childNode2b: "cn2b"]], "b"]]                             | "root[list[childNode2[childNode2a=cn2,childNode2b=cn2b],childNode=cn1]=(a,b),x=1]"
    }

    def "getChildren passing search string"() {
        when:
        def changeLog = new ParsedNode(null, "root").setValue([att1: "1", att2: "2-1", node1: [node1a: "1a", node1b: "1b"]]).addChild(null, "att2", "2-2")

        then:
        changeLog.getChildren(null, "att1").size() == 1
        changeLog.getChildren(null, "att1")[0].name == "att1"
        changeLog.getChildren(null, "att1")[0].value == "1"

        changeLog.getChildren(null, "att2").size() == 2
        changeLog.getChildren(null, "att2")[0].name == "att2"
        changeLog.getChildren(null, "att2")[0].value == "2-1"
        changeLog.getChildren(null, "att2")[1].name == "att2"
        changeLog.getChildren(null, "att2")[1].value == "2-2"

        changeLog.getChildren(null, "node1").size() == 1
        changeLog.getChildren(null, "fake").size() == 0
    }

    def "getChildValue with default"() {
        when:
        def changeLog = new ParsedNode(null, "root").setValue([letter: "a", number: 1, bool: false])

        then:
        changeLog.getChildValue(null, "letter", "b") == "a"
        changeLog.getChildValue(null, "otherLetter", "b") == "b"
        changeLog.getChildValue(null, "number", 5) == 1
        changeLog.getChildValue(null, "otherNumber", 5) == 5
        assert !changeLog.getChildValue(null, "bool", true)
        assert changeLog.getChildValue(null, "otherBool", true)
    }

    @Unroll("#featureName: '#attr' => #type.name")
    def "getChildValue will convert values"() {
        when:
        def node = new ParsedNode(null, "root")
                .addChild(null, "simpleString", "simple value")
                .addChild(null, "intString", "12")
                .addChild(null, "decimalString", "15.213")
                .addChild(null, "intValue", 14)
                .addChild(null, "longValue", 78184L)
                .addChild(null, "floatValue", 13.42F)
                .addChild(null, "trueString", "true")
                .addChild(null, "falseString", "false")
                .addChild(null, "trueValue", true)
                .addChild(null, "falseValue", false)
                .addChild(null, "dateTimeString", "2017-02-20 12:31:55")
                .addChild(null, "dateTimeValue", new ISODateFormat().parse("2013-11-17 19:44:21"));

        then:
        assert node.getChildValue(null, attr, type) == expectedValue

        where:
        attr             | type             | expectedValue
        "simpleString"   | String.class     | "simple value"
        "intString"      | String.class     | "12"
        "intString"      | Integer.class    | 12
        "intString"      | Float.class      | 12F
        "intString"      | Double.class     | 12D
        "intString"      | Long.class       | 12L
        "intString"      | BigInteger.class | BigInteger.valueOf(12)
        "decimalString"  | String.class     | "15.213"
        "decimalString"  | Double.class     | 15.213D
        "decimalString"  | Float.class      | 15.213F
        "intValue"       | String.class     | "14"
        "intValue"       | Integer.class    | 14
        "intValue"       | Long.class       | 14L
        "intValue"       | Double.class     | 14D
        "intValue"       | BigInteger.class | BigInteger.valueOf(14)
        "longValue"      | String.class     | "78184"
        "longValue"      | Long.class       | 78184L
        "longValue"      | Integer.class    | 78184
        "longValue"      | Double.class     | 78184D
        "longValue"      | Float.class      | 78184F
        "longValue"      | BigInteger.class | BigInteger.valueOf(78184)
        "floatValue"     | String.class     | "13.42"
        "floatValue"     | Float.class      | 13.42F
        "floatValue"     | Double.class     | 13.42D
        "trueString"     | String.class     | "true"
        "trueString"     | Boolean.class    | true
        "falseString"    | String.class     | "false"
        "falseString"    | Boolean.class    | false
        "dateTimeString" | String.class     | "2017-02-20 12:31:55"
        "dateTimeString" | Date.class       | new Date(new ISODateFormat().parse("2017-02-20 12:31:55").getTime())
        "dateTimeValue"  | String.class     | "2013-11-17 19:44:21.0"
        "dateTimeValue"  | Date.class       | new Date(new ISODateFormat().parse("2013-11-17 19:44:21").getTime())

    }

    def "setValue passing a ParsedNode will add it as a child instead"() {
        when:
        def node = new ParsedNode(null, "root")
        node.setValue(new ParsedNode(null, "valueNode").setValue("the node value"))

        then:
        node.value == null
        node.getChild(null, "valueNode").value == "the node value"
    }

    def "setValue passing in a collection of ParsedNodes will add them as children instead"() {
        when:
        def node = new ParsedNode(null, "root")
        node.setValue([
                new ParsedNode(null, "valueNode1").setValue("value 1a"),
                new ParsedNode(null, "valueNode2").setValue("value 2"),
                new ParsedNode(null, "valueNode1").setValue("value 1b"),
        ])

        then:
        node.value == null
        node.getChildren(null, "valueNode1")[0].value == "value 1a"
        node.getChildren(null, "valueNode1")[1].value == "value 1b"
        node.getChild(null, "valueNode2").value == "value 2"
    }

    def "setValue passing in a collection of maps will add them as child nodes instead"() {
        when:
        def node = new ParsedNode(null, "root")
        node.setValue([
                [valueNode1: "value 1a"],
                [valueNode2: "value 2"],
                [valueNode1: "value 1b"],
        ])

        then:
        node.value == null
        node.getChildren(null, "valueNode1")[0].value == "value 1a"
        node.getChildren(null, "valueNode1")[1].value == "value 1b"
        node.getChild(null, "valueNode2").value == "value 2"
    }

    def "setValue passing in a collection of objects including one simple object will add them as child nodes and a simple value"() {
        when:
        def node = new ParsedNode(null, "root")
        node.setValue([
                [valueNode1: "value 1a"],
                [valueNode2: "value 2"],
                "simple value",
                new ParsedNode(null, "valueNode1").setValue("value 1b"),
        ])

        then:
        node.value == "simple value"
        node.getChildren(null, "valueNode1")[0].value == "value 1a"
        node.getChildren(null, "valueNode1")[1].value == "value 1b"
        node.getChild(null, "valueNode2").value == "value 2"
    }

    def "setValue passing in a collection of objects including multiple simple objects will add them as child nodes and a list of simple values"() {
        when:
        def node = new ParsedNode(null, "root")
        node.setValue([
                [valueNode1: "value 1a"],
                "simple value 1",
                [valueNode2: "value 2"],
                "simple value 2",
                new ParsedNode(null, "valueNode1").setValue("value 1b"),
        ])

        then:
        node.value instanceof List
        node.value[0] == "simple value 1"
        node.value[1] == "simple value 2"
        node.getChildren(null, "valueNode1")[0].value == "value 1a"
        node.getChildren(null, "valueNode1")[1].value == "value 1b"
        node.getChild(null, "valueNode2").value == "value 2"
    }

    @Unroll
    def "addChild with a map"() {
        when:
        def node = new ParsedNode(null, "root").addChildren(map)

        then:
        node.toString() == expected

        where:
        map                                                                                                                   | expected
        null                                                                                                                  | "root"
        new HashMap()                                                                                                         | "root"
        [test1: "a value"]                                                                                                    | "root[test1=a value]"
        [test1: [child1a: "child value a", child1b: "child value b"]]                                                         | "root[test1[child1a=child value a,child1b=child value b]]"
        [test1: [child1a: "child value a", child1b: ["child value b1", "child value b2"]]]                                    | "root[test1[child1a=child value a,child1b=(child value b1,child value b2)]]"
        [test1: [child1a: "child value a", child1b: [grandChild1: [gc1: "x", gc2: "y"], grandChild2: "grand child value b"]]] | "root[test1[child1a=child value a,child1b[grandChild1[gc1=x,gc2=y],grandChild2=grand child value b]]]"

    }

    def "getChild when multiple match should throw exception"() {
        when:
        def node = new ParsedNode(null, "root")
                .addChild(null, "child", "value 1")
                .addChild(null, "child", "value 2")
        node.getChild(null, "child")

        then:
        thrown(ParsedNodeException)

    }
}
