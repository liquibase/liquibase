package liquibase.actiongenerator

import liquibase.action.Action
import liquibase.sdk.database.MockDatabase
import liquibase.exception.ValidationErrors
import liquibase.sqlgenerator.MockSqlGenerator
import liquibase.sqlgenerator.SqlGenerator
import liquibase.sqlgenerator.SqlGeneratorChain
import liquibase.statement.core.MockSqlStatement
import spock.lang.Specification
import org.junit.Test

public class SqlGeneratorChainTest extends Specification {

    def void generateSql_nullGenerators() {
        when:
        SqlGeneratorChain chain = new SqlGeneratorChain(new ActionGeneratorChain(null))

        then:
        chain.generateSql(new MockSqlStatement(), new MockDatabase()) == null
    }

    def generateSql_noGenerators() {
        when:
        SortedSet<SqlGenerator> generators = new TreeSet<SqlGenerator>(new ActionGeneratorComparator())
        SqlGeneratorChain chain = new SqlGeneratorChain(new ActionGeneratorChain(generators))

        then:
        assert chain.generateSql(new MockSqlStatement(), new MockDatabase()).length == 0
    }

   def generateSql_oneGenerators() {
       when:
        SortedSet<SqlGenerator> generators = new TreeSet<SqlGenerator>(new ActionGeneratorComparator())
        generators.add(new MockSqlGenerator(1, "A1", "A2"))
        SqlGeneratorChain chain = new SqlGeneratorChain(new ActionGeneratorChain(generators))

        Action[] actions = chain.generateSql(new MockSqlStatement(), new MockDatabase())
       then:
        assert actions.length == 2
        assert actions[0].toSql() == "A1"
        assert actions[1].toSql() == "A2"
    }

    def void generateSql_twoGenerators() {
        when:
        SortedSet<SqlGenerator> generators = new TreeSet<SqlGenerator>(new ActionGeneratorComparator())
        generators.add(new MockSqlGenerator(2, "B1", "B2"))
        generators.add(new MockSqlGenerator(1, "A1", "A2"))
        SqlGeneratorChain chain = new SqlGeneratorChain(new ActionGeneratorChain(generators))
        Action[] actions = chain.generateSql(new MockSqlStatement(), new MockDatabase())
        
        then:
        assert actions.length == 4
        assert actions[0].toSql() == "B1"
        assert actions[1].toSql() == "B2"
        assert actions[2].toSql() == "A1"
        assert actions[3].toSql() == "A2"
    }

    def void generateSql_threeGenerators() {
        when:
        SortedSet<SqlGenerator> generators = new TreeSet<SqlGenerator>(new ActionGeneratorComparator())
        generators.add(new MockSqlGenerator(2, "B1", "B2"))
        generators.add(new MockSqlGenerator(1, "A1", "A2"))
        generators.add(new MockSqlGenerator(3, "C1", "C2"))
        SqlGeneratorChain chain = new SqlGeneratorChain(new ActionGeneratorChain(generators))
        Action[] actions = chain.generateSql(new MockSqlStatement(), new MockDatabase())
        
        then:
        assert actions.length == 6
        assert actions[0].toSql() == "C1"
        assert actions[1].toSql() == "C2"
        assert actions[2].toSql() == "B1"
        assert actions[3].toSql() == "B2"
        assert actions[4].toSql() == "A1"
        assert actions[5].toSql() == "A2"
    }

    def validate_nullGenerators() {
        when:
        SqlGeneratorChain chain = new SqlGeneratorChain(null)
        ValidationErrors validationErrors = chain.validate(new MockSqlStatement(), new MockDatabase())
        
        then:
        assert !validationErrors.hasErrors()
    }

    def validate_oneGenerators_noErrors() {
        when:
        SortedSet<SqlGenerator> generators = new TreeSet<SqlGenerator>(new ActionGeneratorComparator())
        generators.add(new MockSqlGenerator(1, "A1", "A2"))

        SqlGeneratorChain chain = new SqlGeneratorChain(new ActionGeneratorChain(generators))
        ValidationErrors validationErrors = chain.validate(new MockSqlStatement(), new MockDatabase())
        
        then:
        assert !validationErrors.hasErrors()
    }

    @Test
    public void validate_oneGenerators_hasErrors() {
        when:
        SortedSet<SqlGenerator> generators = new TreeSet<SqlGenerator>(new ActionGeneratorComparator())
        generators.add(new MockSqlGenerator(1, "A1", "A2").addValidationError("E1"))

        SqlGeneratorChain chain = new SqlGeneratorChain(new ActionGeneratorChain(generators))
        ValidationErrors validationErrors = chain.validate(new MockSqlStatement(), new MockDatabase())

        then:
        assert validationErrors.hasErrors()
    }

    def validate_twoGenerators_noErrors() {
        when:
        SortedSet<SqlGenerator> generators = new TreeSet<SqlGenerator>(new ActionGeneratorComparator())
        generators.add(new MockSqlGenerator(2, "B1", "B2"))
        generators.add(new MockSqlGenerator(1, "A1", "A2"))

        SqlGeneratorChain chain = new SqlGeneratorChain(new ActionGeneratorChain(generators))
        ValidationErrors validationErrors = chain.validate(new MockSqlStatement(), new MockDatabase())

        then:
        assert !validationErrors.hasErrors()
    }

    def validate_twoGenerators_firstHasErrors() {
        when:
        SortedSet<SqlGenerator> generators = new TreeSet<SqlGenerator>(new ActionGeneratorComparator())
        generators.add(new MockSqlGenerator(2, "B1", "B2").addValidationError("E1"))
        generators.add(new MockSqlGenerator(1, "A1", "A2"))

        SqlGeneratorChain chain = new SqlGeneratorChain(new ActionGeneratorChain(generators))
        ValidationErrors validationErrors = chain.validate(new MockSqlStatement(), new MockDatabase())

        then:
        assert validationErrors.hasErrors()
    }

    def validate_twoGenerators_secondHasErrors() {
        when:
        SortedSet<SqlGenerator> generators = new TreeSet<SqlGenerator>(new ActionGeneratorComparator())
        generators.add(new MockSqlGenerator(2, "B1", "B2"))
        generators.add(new MockSqlGenerator(1, "A1", "A2").addValidationError("E1"))

        SqlGeneratorChain chain = new SqlGeneratorChain(new ActionGeneratorChain(generators))
        ValidationErrors validationErrors = chain.validate(new MockSqlStatement(), new MockDatabase())

        then:
        assert validationErrors.hasErrors()
    }
}
