package liquibase.statementlogic

import liquibase.action.Action
import liquibase.ExecutionEnvironment
import liquibase.exception.UnsupportedException
import liquibase.exception.Warnings
import liquibase.sdk.database.MockDatabase
import liquibase.exception.ValidationErrors
import liquibase.statement.Statement
import liquibase.sdk.mock.MockStatement
import liquibase.util.StringUtils
import spock.lang.Specification
import spock.lang.Unroll

public class StatementLogicChainTest extends Specification {

    def "generateActions: null collection returns null actions"() {
        when:
        StatementLogicChain chain = new StatementLogicChain(null)

        then:
        chain.generateActions(new MockStatement(), new ExecutionEnvironment(new MockDatabase())).length == 0
    }

    def "generateActions: empty collection returns empty actions"() {
        when:
        StatementLogicChain chain = new StatementLogicChain(new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator()))

        then:
        assert chain.generateActions(new MockStatement(), new ExecutionEnvironment(new MockDatabase())).length == 0
    }

    def "generateActions: one item chain"() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(1, "A1", "A2"))
        StatementLogicChain chain = new StatementLogicChain(generators)
        Action[] actions = chain.generateActions(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))

        then:
        assert actions.length == 2
        assert actions[0].describe() == "A1;"
        assert actions[1].describe() == "A2;"
    }

    @Unroll
    def "generateActions: two items in chain"() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(2, "B1", "B2").setChainLogic(chainLogic))
        generators.add(new MockStatementLogic(1, "A1", "A2").setChainLogic(chainLogic))
        StatementLogicChain chain = new StatementLogicChain(generators)
        Action[] actions = chain.generateActions(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))

        then:
        StringUtils.join(actions, ",", new StringUtils.ToStringFormatter()) == expected

        where:
        chainLogic                                     | expected
        MockStatementLogic.ChainLogic.CALL_CHAIN_FIRST | "A1;,A2;,B1;,B2;"
        MockStatementLogic.ChainLogic.CALL_CHAIN_LAST  | "B1;,B2;,A1;,A2;"
        MockStatementLogic.ChainLogic.CALL_CHAIN_NEVER | "B1;,B2;"
    }

    @Unroll
    def "generateActions: three items in chain"() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(2, "B1", "B2").setChainLogic(chainLogic))
        generators.add(new MockStatementLogic(1, "A1", "A2").setChainLogic(chainLogic))
        generators.add(new MockStatementLogic(3, "C1", "C2").setChainLogic(chainLogic))
        StatementLogicChain chain = new StatementLogicChain(generators)
        Action[] actions = chain.generateActions(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))

        then:
        StringUtils.join(actions, ",", new StringUtils.ToStringFormatter()) == expected

        where:
        chainLogic                                     | expected
        MockStatementLogic.ChainLogic.CALL_CHAIN_FIRST | "A1;,A2;,B1;,B2;,C1;,C2;"
        MockStatementLogic.ChainLogic.CALL_CHAIN_LAST  | "C1;,C2;,B1;,B2;,A1;,A2;"
        MockStatementLogic.ChainLogic.CALL_CHAIN_NEVER | "C1;,C2;"
    }

    def "validate: null chain"() {
        when:
        StatementLogicChain chain = new StatementLogicChain(null)
        ValidationErrors validationErrors = chain.validate(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))

        then:
        assert !validationErrors.hasErrors()
    }

    def "validate: one item in chain and no errors"() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(1, "A1", "A2"))

        StatementLogicChain chain = new StatementLogicChain(generators)
        ValidationErrors validationErrors = chain.validate(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))

        then:
        assert !validationErrors.hasErrors()
    }

    def "validate: one item in chain with errors"() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(1, "A1", "A2").addValidationError("E1"))

        StatementLogicChain chain = new StatementLogicChain(generators)
        ValidationErrors validationErrors = chain.validate(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))

        then:
        assert validationErrors.hasErrors()
    }

    def "validate: two items in chain with no errors"() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(2, "B1", "B2"))
        generators.add(new MockStatementLogic(1, "A1", "A2"))

        StatementLogicChain chain = new StatementLogicChain(generators)
        ValidationErrors validationErrors = chain.validate(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))

        then:
        assert !validationErrors.hasErrors()
    }

    @Unroll
    def "validate: two items in chain, first has errors"() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(2, "B1", "B2").addValidationError("E1").setChainLogic(chainLogic))
        generators.add(new MockStatementLogic(1, "A1", "A2").setChainLogic(chainLogic))

        StatementLogicChain chain = new StatementLogicChain(generators)
        ValidationErrors validationErrors = chain.validate(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))

        then:
        validationErrors.hasErrors() == expected

        where:
        chainLogic                                     | expected
        MockStatementLogic.ChainLogic.CALL_CHAIN_FIRST | true
        MockStatementLogic.ChainLogic.CALL_CHAIN_LAST  | true
        MockStatementLogic.ChainLogic.CALL_CHAIN_NEVER | true

    }

    @Unroll
    def "validate: two items in chain, second has errors"() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(2, "B1", "B2").setChainLogic(chainLogic))
        generators.add(new MockStatementLogic(1, "A1", "A2").addValidationError("E1").setChainLogic(chainLogic))

        StatementLogicChain chain = new StatementLogicChain(generators)
        ValidationErrors validationErrors = chain.validate(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))

        then:
        validationErrors.hasErrors() == expected

        where:
        chainLogic                                     | expected
        MockStatementLogic.ChainLogic.CALL_CHAIN_FIRST | true
        MockStatementLogic.ChainLogic.CALL_CHAIN_LAST  | true
        MockStatementLogic.ChainLogic.CALL_CHAIN_NEVER | false
    }

    def "warn: null chain"() {
        when:
        StatementLogicChain chain = new StatementLogicChain(null)
        Warnings warnings = chain.warn(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))

        then:
        assert !warnings.hasWarnings()
    }

    def "warn: one item in chain and no errors"() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(1, "A1", "A2"))

        StatementLogicChain chain = new StatementLogicChain(generators)
        Warnings warnings = chain.warn(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))

        then:
        assert !warnings.hasWarnings()
    }

    def "warn: one item in chain with errors"() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(1, "A1", "A2").addWarning("E1"))

        StatementLogicChain chain = new StatementLogicChain(generators)
        Warnings warnings = chain.warn(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))

        then:
        assert warnings.hasWarnings()
    }

    def "warn: two items in chain with no errors"() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(2, "B1", "B2"))
        generators.add(new MockStatementLogic(1, "A1", "A2"))

        StatementLogicChain chain = new StatementLogicChain(generators)
        Warnings warnings = chain.warn(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))

        then:
        assert !warnings.hasWarnings()
    }

    @Unroll
    def "warn: two items in chain, first has errors"() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(2, "B1", "B2").addWarning("E1").setChainLogic(chainLogic))
        generators.add(new MockStatementLogic(1, "A1", "A2").setChainLogic(chainLogic))

        StatementLogicChain chain = new StatementLogicChain(generators)
        Warnings warnings = chain.warn(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))

        then:
        warnings.hasWarnings() == expected

        where:
        chainLogic                                     | expected
        MockStatementLogic.ChainLogic.CALL_CHAIN_FIRST | true
        MockStatementLogic.ChainLogic.CALL_CHAIN_LAST  | true
        MockStatementLogic.ChainLogic.CALL_CHAIN_NEVER | true

    }

    @Unroll
    def "warn: two items in chain, second has errors"() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(2, "B1", "B2").setChainLogic(chainLogic))
        generators.add(new MockStatementLogic(1, "A1", "A2").addWarning("E1").setChainLogic(chainLogic))

        StatementLogicChain chain = new StatementLogicChain(generators)
        Warnings warnings = chain.warn(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))

        then:
        warnings.hasWarnings() == expected

        where:
        chainLogic                                     | expected
        MockStatementLogic.ChainLogic.CALL_CHAIN_FIRST | true
        MockStatementLogic.ChainLogic.CALL_CHAIN_LAST  | true
        MockStatementLogic.ChainLogic.CALL_CHAIN_NEVER | false
    }

    def "block method stops middle logic from running"() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(3, ["C1", "C2"].toArray(new String[2])) { //not sure why groovy is passing arg as "C", "1"
            @Override
            Action[] generateActions(Statement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
                chain.block(CustomMockStatementLogic.class)
                return super.generateActions(statement, env, chain)
            }
        })
        generators.add(new CustomMockStatementLogic(2, "B1", "B2"))
        generators.add(new MockStatementLogic(1, "A1", "A2"))

        StatementLogicChain chain = new StatementLogicChain(generators)

        def actions = chain.generateActions(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))

        then:
        StringUtils.join(actions, ", ", new StringUtils.ToStringFormatter()) == "A1;, A2;, C1;, C2;"
    }

    def "block method stops last logic from running"() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(3, "C1", "C2"))
        generators.add(new MockStatementLogic(2, ["B1", "B2"].toArray(new String[2])) {   //not sure why groovy is passing arg as "B", "1"
            @Override
            Action[] generateActions(Statement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
                chain.block(CustomMockStatementLogic.class)
                return super.generateActions(statement, env, chain)
            }
        })
        generators.add(new CustomMockStatementLogic(1, "A1", "A2"))

        StatementLogicChain chain = new StatementLogicChain(generators)

        def actions = chain.generateActions(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))

        then:
        StringUtils.join(actions, ", ", new StringUtils.ToStringFormatter()) == "B1;, B2;, C1;, C2;"
    }

    private static class CustomMockStatementLogic extends MockStatementLogic {
        CustomMockStatementLogic(int priority, String... returnSql) {
            super(priority, returnSql)
        }
    }
}
