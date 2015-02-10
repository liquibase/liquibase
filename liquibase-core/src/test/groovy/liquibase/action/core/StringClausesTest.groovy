package liquibase.action.core

import liquibase.actionlogic.core.CreateTableLogic
import spock.lang.Specification
import spock.lang.Unroll

class StringClausesTest extends Specification {

    @Unroll
    def "toString combines simple clauses correctly"() {
        expect:
        clauses.toString() == expected

        where:
        clauses                                                                           | expected
        new StringClauses()                                                               | ""
        new StringClauses().append("first")                                               | "first"
        new StringClauses().append("first").append("SECOND")                              | "first SECOND"
        new StringClauses().append("first").append("SECOND").append("third")              | "first SECOND third"
        new StringClauses().append("first").append("SECOND").append(null).append("third") | "first SECOND third"
        new StringClauses().append("first").append("SECOND").append("  ").append("third") | "first SECOND third"
    }

    @Unroll
    def "toString combines nested clauses correctly"() {
        expect:
        clauses.toString() == expected

        where:
        clauses                                                                                                                                    | expected
        new StringClauses().append("first").append("innerClause", new StringClauses().append("inner first").append("inner SECOND")).append("next") | "first inner first inner SECOND next"
        new StringClauses().append("first").append((String) null, new StringClauses().append("inner first").append("inner SECOND")).append("next") | "first inner first inner SECOND next"
        new StringClauses().append("first").append("innerClause", new StringClauses()).append("next")                                              | "first next"
    }

    @Unroll
    def "append trims values added"() {
        expect:
        clauses.toString() == expected

        where:
        clauses                                                                       | expected
        new StringClauses().append("first  ")                                         | "first"
        new StringClauses().append("   first")                                        | "first"
        new StringClauses().append("   first   ")                                     | "first"
        new StringClauses().append("key", "   first   ")                              | "first"
        new StringClauses().append(CreateTableLogic.Clauses.tableName, "   first   ") | "first"
        new StringClauses().append("first   ").append("       SECOND")                | "first SECOND"
        new StringClauses().append("first\n").append("SECOND")                        | "first SECOND"
    }

    @Unroll
    def "prepend works correctly"() {
        expect:
        clauses.toString() == expected

        where:
        clauses                                                                        | expected
        new StringClauses().prepend("first  ")                                         | "first"
        new StringClauses().prepend("first").prepend("SECOND").prepend("third")        | "third SECOND first"
        new StringClauses().prepend("   first")                                        | "first"
        new StringClauses().prepend("   first   ")                                     | "first"
        new StringClauses().prepend("  key", "   first   ")                            | "first"
        new StringClauses().prepend(CreateTableLogic.Clauses.tableName, "   first   ") | "first"
        new StringClauses().prepend("first   ").prepend("       SECOND")               | "SECOND first"
        new StringClauses().prepend("first\n").prepend("SECOND")                       | "SECOND first"
    }

    @Unroll
    def "can specify alternate begin, end, and separators"() {
        expect:
        clauses.toString() == expected

        where:
        clauses                                                            | expected
        new StringClauses().append("first").append("SECOND")               | "first SECOND"
        new StringClauses(", ").append("first").append("SECOND")           | "first, SECOND"
        new StringClauses("|").append("first").append("SECOND")            | "first|SECOND"
        new StringClauses("(", ", ", ")").append("first").append("SECOND") | "(first, SECOND)"
        new StringClauses("(", ", ", ")")                                  | ""
    }

    def "appending and removing by key works as expected"() {
        expect:
        def clauses = new StringClauses()

        clauses.append("first", "first")
        clauses.toString() == "first"

        clauses.append("SECOND key", "SECOND")
        clauses.toString() == "first SECOND"

        clauses.append(CreateTableLogic.Clauses.tableName, "third")
        clauses.toString() == "first SECOND third"

        clauses.get("first") == "first"
        clauses.get("SECOND key") == "SECOND"
        clauses.get(CreateTableLogic.Clauses.tableName) == "third"
        clauses.get("tableName") == "third"

        clauses.remove("SECOND key")
        clauses.toString() == "first third"

        clauses.remove("SECOND key") //re-removing already gone key
        clauses.toString() == "first third"

        clauses.remove("tableName") //can remove enum by name
        clauses.toString() == "first"

        clauses.append("new key", "new") //can remove enum by name
        clauses.toString() == "first new"

        clauses.get("NEW KEY") == "new" //can get case insensitively
        clauses.remove("NEW KEY") //can remove case insensitively
        clauses.toString() == "first"

        clauses.remove("invalid") //removing invalid doesn't throw exception
        clauses.toString() == "first"
    }

    def "replace works correctly"() {
        expect:
        def clauses = new StringClauses().append("a").append("b key", "b").append("c key", "c").append(CreateTableLogic.Clauses.tableName, "table_name")
        clauses.toString() == "a b c table_name"

        clauses.replace("b key", "new b")
        clauses.toString() == "a new b c table_name"

        clauses.replace(CreateTableLogic.Clauses.tableName, "new table")
        clauses.toString() == "a new b c new table"

        clauses.replace("tableName", "newer table")
        clauses.toString() == "a new b c newer table"

        clauses.replace("TABLENAME", "upper table")
        clauses.toString() == "a new b c upper table"

        clauses.replace("c key", "   c with spaces  ")
        clauses.toString() == "a new b c with spaces upper table"

        clauses.replace("c key", new StringClauses().append("sub 1").append("sub 2"))
        clauses.toString() == "a new b sub 1 sub 2 upper table"
    }

    def "replace exceptions work correctly"() {
        when:
        def clauses = new StringClauses().append("a").append("b key", "b").append("c key", "c").append(CreateTableLogic.Clauses.tableName, "table_name")
        clauses.replace("invalid", "a null value")

        then:
        thrown(IllegalArgumentException)
    }

    def "insertBefore works correctly"() {
        expect:
        def clauses = new StringClauses().append("a").append("b key", "b").append("c key", "c").append(CreateTableLogic.Clauses.tableName, "table_name")

        clauses.insertBefore("a", "pre-a")
        clauses.toString() == "pre-a a b c table_name"
        assert clauses.get("pre-a") != null

        clauses.insertBefore(CreateTableLogic.Clauses.tableName, "     pre-table   ")
        clauses.toString() == "pre-a a b c pre-table table_name"
        assert clauses.get("pre-table") != null

        clauses.insertBefore("c key", "pre-c key", "pre-c")
        clauses.toString() == "pre-a a b pre-c c pre-table table_name"
        assert clauses.get("pre-c key") != null
    }

    def "insertBefore exceptions work correctly"() {
        when:
        def clauses = new StringClauses().append("a").append("b key", "b").append("c key", "c").append(CreateTableLogic.Clauses.tableName, "table_name")
        clauses.insertBefore("invalid", "a null value")

        then:
        thrown(IllegalArgumentException)
    }

    def "insertAfter works correctly"() {
        expect:
        def clauses = new StringClauses().append("a").append("b key", "b").append("c key", "c").append(CreateTableLogic.Clauses.tableName, "table_name")

        clauses.insertAfter("a", "post-a")
        clauses.toString() == "a post-a b c table_name"
        assert clauses.get("post-a") != null

        clauses.insertAfter(CreateTableLogic.Clauses.tableName, "     post-table   ")
        clauses.toString() == "a post-a b c table_name post-table"
        assert clauses.get("post-table") != null

        clauses.insertAfter("c key", "post-c key", "post-c")
        clauses.toString() == "a post-a b c post-c table_name post-table"
        assert clauses.get("post-c key") != null
    }

    def "insertAfter exceptions work correctly"() {
        when:
        def clauses = new StringClauses().append("a").append("b key", "b").append("c key", "c").append(CreateTableLogic.Clauses.tableName, "table_name")
        clauses.insertAfter("invalid", "a null value")

        then:
        thrown(IllegalArgumentException)
    }

    def "get works correctly"() {
        when:
        def clauses = new StringClauses().append("a").append("b key", "b").append("c key", "c").append(CreateTableLogic.Clauses.tableName, "table_name").append("subclause", new StringClauses().append("sub-1").append("sub-2"))

        then:
        clauses.get("a") == "a"
        clauses.get("A") == "a"
        clauses.get("  A   ") == "a"

        clauses.get(CreateTableLogic.Clauses.tableName) == "table_name"
        clauses.get("tableName") == "table_name"
        clauses.get("TABLENAME") == "table_name"

        clauses.get("invalid") == null
        clauses.get("subclause") instanceof String
        clauses.get("subclause") == "sub-1 sub-2"
    }

    def "get and getSubclause traverses sub-clauses if not found in root one"() {
        when:
        def clauses = new StringClauses()
                .append("a")
                .append("b key", "b")
                .append("sub-1", new StringClauses().append("sub-1-a key", "sub-1-a").append("sub-1-b key", "sub-1-b"))
                .append("sub-2", new StringClauses().append("sub-2-a key", "sub-2-a").append("sub-2-b key", "sub-2-b").append("sub-2-child", new StringClauses().append("sub-2-child-a key", "sub-2-child-a")))

        then:
        clauses.get("a") == "a"
        clauses.get("sub-1-a key") == "sub-1-a"
        clauses.get("sub-1-b key") == "sub-1-b"
        clauses.get("sub-2-a key") == "sub-2-a"
        clauses.get("sub-2-child-a key") == "sub-2-child-a"
        clauses.getSubclause("sub-2-child-a key").toString() == "sub-2-child-a"
    }

    def "getSubclause works correctly"() {
        when:
        def clauses = new StringClauses().append("a").append("b key", "b").append("c key", "c").append(CreateTableLogic.Clauses.tableName, "table_name").append("subclause", new StringClauses().append("sub-1").append("sub-2"))

        then:
        clauses.getSubclause("a") instanceof StringClauses
        clauses.getSubclause("A").get("a") == "a"

        clauses.getSubclause(CreateTableLogic.Clauses.tableName) instanceof StringClauses
        clauses.getSubclause(CreateTableLogic.Clauses.tableName).get("table_name") == "table_name"
        clauses.getSubclause("tableName").toString() == "table_name"
        clauses.getSubclause("TABLENAME").toString() == "table_name"

        clauses.getSubclause("invalid") == null
        clauses.getSubclause("subclause") instanceof StringClauses
        clauses.getSubclause("subclause").toString() == "sub-1 sub-2"
    }

    @Unroll
    def "cannot add a duplicate key"() {
        when:
        testMethod.run()

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Cannot add clause with key 'a' because it is already defined"

        where:
        testMethod << [
                {new StringClauses().append("a").append("a")},
                {new StringClauses().append("a").append("a", new StringClauses())},
                {new StringClauses().append("a").prepend("a")},
                {new StringClauses().append("a").prepend("a", new StringClauses())},
                {new StringClauses().append("a").append("b").insertBefore("b", "a")},
                {new StringClauses().append("a").append("b").insertAfter("b", "a")},
        ]
    }
}
