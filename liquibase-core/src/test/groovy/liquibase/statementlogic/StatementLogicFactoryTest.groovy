package liquibase.statementlogic

import liquibase.ExecutionEnvironment
import liquibase.change.Change
import liquibase.database.core.H2Database
import liquibase.database.core.InformixDatabase
import liquibase.exception.UnsupportedException
import liquibase.sdk.database.MockDatabase
import liquibase.sqlgenerator.core.*
import liquibase.statement.Statement
import liquibase.statement.core.AddAutoIncrementStatement
import liquibase.statement.core.CreateTableStatement
import liquibase.sdk.mock.MockStatement
import org.hamcrest.Matchers
import spock.lang.Specification
import spock.lang.Unroll

import static spock.util.matcher.HamcrestSupport.that

public class StatementLogicFactoryTest extends Specification {
    private ExecutionEnvironment env = new ExecutionEnvironment(new MockDatabase())

    def cleanup() {
        StatementLogicFactory.reset();
    }

    def getInstance() {
        when:
        StatementLogicFactory.getInstance() != null

        then:
        StatementLogicFactory.getInstance() == StatementLogicFactory.getInstance()
    }

    def register() {
        when:
        StatementLogicFactory.getInstance().clearRegistry();
        then:
        StatementLogicFactory.getInstance().getRegistry().size() == 0

        when:
        StatementLogicFactory.getInstance().register(new MockStatementLogic(1, "A1"));
        then:
        StatementLogicFactory.getInstance().getRegistry().size() == 1
    }

    def "unregister passing instance"() {
        when:
        StatementLogicFactory factory = StatementLogicFactory.getInstance();
        factory.clearRegistry();

        then:
        factory.getRegistry().size() == 0

        when:
        AddAutoIncrementGeneratorHsqlH2 logic = new AddAutoIncrementGeneratorHsqlH2();

        factory.register(new AddAutoIncrementGenerator());
        factory.register(logic);
        factory.register(new AddAutoIncrementGeneratorDB2());

        then:
        factory.getRegistry().size() == 3

        when:
        factory.unregister(logic);
        then:
        factory.getRegistry().size() == 2
    }

    def "unregister by class"() {
        when:
        StatementLogicFactory factory = StatementLogicFactory.getInstance();

        factory.clearRegistry();

        then:
        factory.getRegistry().size() == 0

        when:
        factory.register(new AddAutoIncrementGenerator());
        factory.register(new AddAutoIncrementGeneratorHsqlH2());
        factory.register(new AddAutoIncrementGeneratorDB2());
        then:
        factory.getRegistry().size() == 3

        when:
        factory.unregister(AddAutoIncrementGeneratorHsqlH2.class);
        then:
        factory.getRegistry().size() == 2
    }

    def "unregister when class does not exist"() {
        when:
        StatementLogicFactory factory = StatementLogicFactory.getInstance();
        factory.clearRegistry();

        then:
        factory.getRegistry().size() == 0

        when:
        factory.register(new AddAutoIncrementGenerator());
        factory.register(new AddAutoIncrementGeneratorHsqlH2());
        factory.register(new AddAutoIncrementGeneratorDB2());

        then:
        factory.getRegistry().size() == 3

        when:
        factory.unregister(AddColumnGenerator.class);
        then:
        factory.getRegistry().size() == 3
    }

    def "unregister passing null"() {
        given:
        StatementLogicFactory factory = StatementLogicFactory.getInstance();
        factory.clearRegistry();
        factory.register(new AddAutoIncrementGenerator());
        factory.register(new AddAutoIncrementGeneratorHsqlH2());

        when:
        factory.unregister((Class) null)
        then:
        factory.getRegistry().size() == 2

        when:
        factory.unregister((StatementLogic) null)
        then:
        factory.getRegistry().size() == 2
    }

    def "register passing null"() {
        given:
        StatementLogicFactory factory = StatementLogicFactory.getInstance();
        factory.clearRegistry();
        factory.register(new AddAutoIncrementGenerator());
        factory.register(new AddAutoIncrementGeneratorHsqlH2());

        when:
        factory.register((StatementLogic) null)
        then:
        factory.getRegistry().size() == 2
    }

    def void reset() {
        when:
        StatementLogicFactory instance1 = StatementLogicFactory.getInstance();
        StatementLogicFactory.reset();
        then:
        instance1 != StatementLogicFactory.getInstance()
    }

    def builtInLogicIsFound() {
        expect:
        StatementLogicFactory.getInstance().getRegistry().size() > 10
    }

    def "getStatementLogic: statement with single logic class"() {
        when:
        SortedSet<StatementLogic> set = StatementLogicFactory.getInstance().getStatementLogic(new AddAutoIncrementStatement(null, null, "person", "name", "varchar(255)", null, null), new ExecutionEnvironment(new H2Database()));

        then:
        set != null
        set.size() == 1
        set.iterator().next().getClass() == AddAutoIncrementGeneratorHsqlH2
    }

    def "getStatementLogic: statement with multiple logic classes"() {
        when:
        List<StatementLogic> list = new ArrayList<StatementLogic>(StatementLogicFactory.getInstance().getStatementLogic(new CreateTableStatement(null, null, "person"), new ExecutionEnvironment(new InformixDatabase())));

        then:
        list != null
        list.size() == 2
        list[0].getClass() == CreateTableGeneratorInformix
        list[1].getClass() == CreateTableGenerator
    }

    def "getStatementLogic: statement without logic"() {
        when:
        SortedSet<StatementLogic> set = StatementLogicFactory.getInstance().getStatementLogic(new MockStatement(), env);

        then:
        set != null
        set.size() == 0
    }

    @Unroll
    def "generateActions: passed a Change"() {
        given:
        def change = Mock(Change)
        change.generateStatements(env) >> generatedStatements

        StatementLogicFactory.instance.register(new MockStatementLogic(1, "sql from A", "more sql from A").setSupportsId("A"))
        StatementLogicFactory.instance.register(new MockStatementLogic(1, "only sql from B").setSupportsId("B"))

        when:
        def actions = StatementLogicFactory.instance.generateActions(change, env)

        then:
        actions.collect { it.describe() } == expected

        where:
        generatedStatements                                               | expected
        null                                                              | []
        ([new MockStatement("A")] as Statement[])                         | ["sql from A;", "more sql from A;"]
        ([new MockStatement("B")] as Statement[])                         | ["only sql from B;"]
        ([new MockStatement("A"), new MockStatement("B")] as Statement[]) | ["sql from A;", "more sql from A;", "only sql from B;"]
    }

    def "generateActions: passed an unsupported change"() {
        given:
        def change = Mock(Change)
        change.generateStatements(env) >> ([new MockStatement("A")] as Statement[])

        when:
        StatementLogicFactory.instance.generateActions(change, env)

        then:
        thrown(UnsupportedException)
    }

    @Unroll
    def "generateActions: passed a statement array"() {
        given:
        StatementLogicFactory.instance.register(new MockStatementLogic(1, "sql from A", "more sql from A").setSupportsId("A"))
        StatementLogicFactory.instance.register(new MockStatementLogic(1, "only sql from B").setSupportsId("B"))

        when:
        def actions = StatementLogicFactory.instance.generateActions(statements as Statement[], env)

        then:
        actions.collect { it.describe() } == expected

        where:
        statements                                                        | expected
        null                                                              | []
        ([new MockStatement("A")] as Statement[])                         | ["sql from A;", "more sql from A;"]
        ([new MockStatement("B")] as Statement[])                         | ["only sql from B;"]
        ([new MockStatement("A"), new MockStatement("B")] as Statement[]) | ["sql from A;", "more sql from A;", "only sql from B;"]
    }

    @Unroll
    def "generateActions: passed an unsupported statement in an array"() {
        given:
        StatementLogicFactory.instance.register(new MockStatementLogic(1, "sql from A").setSupportsId("A"))
        when:
        StatementLogicFactory.instance.generateActions(statements, env)

        then:
        thrown(UnsupportedException)

        where:
        statements << [
                ([new MockStatement("B")] as Statement[]),
                ([new MockStatement("A"), new MockStatement("B")] as Statement[]),
                ([new MockStatement("B"), new MockStatement("A")] as Statement[]),
        ]
    }

    @Unroll
    def "generateActions: passed a statement"() {
        given:
        StatementLogicFactory.instance.register(new MockStatementLogic(1, "sql from A", "more sql from A").setSupportsId("A"))
        StatementLogicFactory.instance.register(new MockStatementLogic(1, "only sql from B").setSupportsId("B").addWarning("A test warning"))

        when:
        def actions = StatementLogicFactory.instance.generateActions(statement as Statement, env)

        then:
        actions.collect { it.describe() } == expected

        where:
        statement              | expected
        null                   | []
        new MockStatement("A") | ["sql from A;", "more sql from A;"]
        new MockStatement("B") | ["only sql from B;"]
    }

    @Unroll
    def "generateActions: unsupported statements"() {
        given:
        StatementLogicFactory.instance.register(new MockStatementLogic(1, "sql from A").setSupportsId("A").addValidationError("failed validation"))

        when:
        StatementLogicFactory.instance.generateActions(statement, env)

        then:
        thrown(UnsupportedException)

        where:
        statement << [
                new MockStatement("A"),
                new MockStatement("B"),
        ]
    }

    @Unroll
    def "supports"() {
        when:
        StatementLogicFactory.instance.register(new MockStatementLogic(1, "sql from A").setSupportsId("A"))

        then:
        StatementLogicFactory.instance.supports(statement, env) == expected

        where:
        statement | expected
        new MockStatement("A") | true
        new MockStatement("B") | false
        null | true
    }

    def "validate"() {
        given:
        StatementLogicFactory.instance.register(new MockStatementLogic(1, "sql from A").setSupportsId("A").addWarning("a warning message"))
        StatementLogicFactory.instance.register(new MockStatementLogic(1, "sql from B").setSupportsId("B").addValidationError("a B validation error").addValidationError("another B validation error"))
        StatementLogicFactory.instance.register(new MockStatementLogic(1, "sql from C").setSupportsId("C").addValidationError("a C validation error"))
        StatementLogicFactory.instance.register(new MockStatementLogic(2, "more sql from C").setSupportsId("C").addValidationError("another C validation error"))

        when:
        def aErrors = StatementLogicFactory.instance.validate(new MockStatement("A"), env)
        def bErrors = StatementLogicFactory.instance.validate(new MockStatement("B"), env)
        def cErrors = StatementLogicFactory.instance.validate(new MockStatement("C"), env)

        then:
        assert !aErrors.hasErrors()

        that bErrors.getErrorMessages(), Matchers.containsInAnyOrder(["a B validation error", "another B validation error"] as Object[])
        that cErrors.getErrorMessages(), Matchers.containsInAnyOrder(["a C validation error", "another C validation error"] as Object[])
    }

    def "generateActionsIsVolatile"() {
        when:
        StatementLogicFactory.instance.register(new MockStatementLogic(1, "sql from A").setSupportsId("A"))
        StatementLogicFactory.instance.register(new MockStatementLogic(1, "sql from B").setSupportsId("B").setGenerateActionsIsVolatile(true))

        StatementLogicFactory.instance.register(new MockStatementLogic(1, "sql from C").setSupportsId("C"))
        StatementLogicFactory.instance.register(new MockStatementLogic(2, "more sql from C").setSupportsId("C").setGenerateActionsIsVolatile(true))

        StatementLogicFactory.instance.register(new MockStatementLogic(1, "sql from D").setSupportsId("D").setGenerateActionsIsVolatile(true))
        StatementLogicFactory.instance.register(new MockStatementLogic(2, "more sql from D").setSupportsId("D"))

        StatementLogicFactory.instance.register(new MockStatementLogic(1, "more sql from E").setSupportsId("E"))
        StatementLogicFactory.instance.register(new MockStatementLogic(2, "sql from E").setSupportsId("E").setGenerateActionsIsVolatile(true))
        StatementLogicFactory.instance.register(new MockStatementLogic(3, "more sql from E").setSupportsId("E"))

        then:
        assert !StatementLogicFactory.instance.generateActionsIsVolatile(new MockStatement("A"), env)
        assert StatementLogicFactory.instance.generateActionsIsVolatile(new MockStatement("B"), env)
        assert StatementLogicFactory.instance.generateActionsIsVolatile(new MockStatement("C"), env)
        assert StatementLogicFactory.instance.generateActionsIsVolatile(new MockStatement("D"), env)
        assert StatementLogicFactory.instance.generateActionsIsVolatile(new MockStatement("E"), env)
    }
}
