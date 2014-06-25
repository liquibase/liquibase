package liquibase.statementlogic

import liquibase.action.Action
import  liquibase.ExecutionEnvironment
import liquibase.sdk.database.MockDatabase
import liquibase.exception.ValidationErrors
import liquibase.statement.core.MockStatement
import spock.lang.Specification
import org.junit.Test

public class StatementLogicChainTest extends Specification {

    def void generateActions_nullGenerators() {
        when:
        StatementLogicChain chain = new StatementLogicChain(null)

        then:
        chain.generateActions(new MockStatement(), new ExecutionEnvironment(new MockDatabase())) == null
    }

    def generateActions_noGenerators() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        StatementLogicChain chain =new StatementLogicChain(generators)

        then:
        assert chain.generateActions(new MockStatement(), new ExecutionEnvironment(new MockDatabase())).length == 0
    }

   def generateActions_oneGenerators() {
       when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(1, "A1", "A2"))
        StatementLogicChain chain =new StatementLogicChain(generators)

        Action[] actions = chain.generateActions(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))
       then:
        assert actions.length == 2
        assert actions[0].describe() == "A1;"
        assert actions[1].describe() == "A2;"
    }

    def void generateActions_twoGenerators() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(2, "B1", "B2"))
        generators.add(new MockStatementLogic(1, "A1", "A2"))
        StatementLogicChain chain =new StatementLogicChain(generators)
        Action[] actions = chain.generateActions(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))
        
        then:
        assert actions.length == 4
        assert actions[0].describe() == "B1;"
        assert actions[1].describe() == "B2;"
        assert actions[2].describe() == "A1;"
        assert actions[3].describe() == "A2;"
    }

    def void generateActions_threeGenerators() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(2, "B1", "B2"))
        generators.add(new MockStatementLogic(1, "A1", "A2"))
        generators.add(new MockStatementLogic(3, "C1", "C2"))
        StatementLogicChain chain =new StatementLogicChain(generators)
        Action[] actions = chain.generateActions(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))
        
        then:
        assert actions.length == 6
        assert actions[0].describe() == "C1;"
        assert actions[1].describe() == "C2;"
        assert actions[2].describe() == "B1;"
        assert actions[3].describe() == "B2;"
        assert actions[4].describe() == "A1;"
        assert actions[5].describe() == "A2;"
    }

    def validate_nullGenerators() {
        when:
        StatementLogicChain chain = new StatementLogicChain(null)
        ValidationErrors validationErrors = chain.validate(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))
        
        then:
        assert !validationErrors.hasErrors()
    }

    def validate_oneGenerators_noErrors() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(1, "A1", "A2"))

        StatementLogicChain chain =new StatementLogicChain(generators)
        ValidationErrors validationErrors = chain.validate(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))
        
        then:
        assert !validationErrors.hasErrors()
    }

    @Test
    public void validate_oneGenerators_hasErrors() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(1, "A1", "A2").addValidationError("E1"))

        StatementLogicChain chain =new StatementLogicChain(generators)
        ValidationErrors validationErrors = chain.validate(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))

        then:
        assert validationErrors.hasErrors()
    }

    def validate_twoGenerators_noErrors() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(2, "B1", "B2"))
        generators.add(new MockStatementLogic(1, "A1", "A2"))

        StatementLogicChain chain =new StatementLogicChain(generators)
        ValidationErrors validationErrors = chain.validate(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))

        then:
        assert !validationErrors.hasErrors()
    }

    def validate_twoGenerators_firstHasErrors() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(2, "B1", "B2").addValidationError("E1"))
        generators.add(new MockStatementLogic(1, "A1", "A2"))

        StatementLogicChain chain =new StatementLogicChain(generators)
        ValidationErrors validationErrors = chain.validate(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))

        then:
        assert validationErrors.hasErrors()
    }

    def validate_twoGenerators_secondHasErrors() {
        when:
        SortedSet<StatementLogic> generators = new TreeSet<StatementLogic>(new StatementLogicFactory.StatementLogicComparator())
        generators.add(new MockStatementLogic(2, "B1", "B2"))
        generators.add(new MockStatementLogic(1, "A1", "A2").addValidationError("E1"))

        StatementLogicChain chain =new StatementLogicChain(generators)
        ValidationErrors validationErrors = chain.validate(new MockStatement(), new ExecutionEnvironment(new MockDatabase()))

        then:
        assert validationErrors.hasErrors()
    }
}
