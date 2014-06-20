package liquibase.actiongenerator

import liquibase.database.core.H2Database
import liquibase.sqlgenerator.SqlGenerator
import liquibase.sqlgenerator.core.AddAutoIncrementGenerator
import liquibase.sqlgenerator.core.AddAutoIncrementGeneratorDB2
import liquibase.sqlgenerator.core.AddAutoIncrementGeneratorHsqlH2
import liquibase.sqlgenerator.core.AddColumnGenerator
import liquibase.statement.core.AddAutoIncrementStatement
import spock.lang.Specification

public class ActionGeneratorFactoryTest extends Specification {

    def setup() {
        ActionGeneratorFactory.reset();
    }

    def getInstance() {
        when:
        ActionGeneratorFactory.getInstance() != null

        then:
        ActionGeneratorFactory.getInstance() == ActionGeneratorFactory.getInstance()
    }

    def register() {
        when:
        ActionGeneratorFactory.getInstance().getGenerators().clear();
        then:
        ActionGeneratorFactory.getInstance().getGenerators().size() == 0

        when:
        ActionGeneratorFactory.getInstance().register(new MockActionGenerator(1, "A1"));
        then:
        ActionGeneratorFactory.getInstance().getGenerators().size() == 1
    }

    def unregister_instance() {
        when:
        ActionGeneratorFactory factory = ActionGeneratorFactory.getInstance();
        factory.getGenerators().clear();

        then:
        factory.getGenerators().size() == 0

        when:
        AddAutoIncrementGeneratorHsqlH2 sqlGenerator = new AddAutoIncrementGeneratorHsqlH2();

        factory.register(new AddAutoIncrementGenerator());
        factory.register(sqlGenerator);
        factory.register(new AddAutoIncrementGeneratorDB2());

        then:
        factory.getGenerators().size() == 3

        when:
        factory.unregister(sqlGenerator);
        then:
        factory.getGenerators().size() == 2
    }

    def unregister_class() {
        when:
        ActionGeneratorFactory factory = ActionGeneratorFactory.getInstance();

        factory.getGenerators().clear();

        then:
        factory.getGenerators().size() == 0

        when:
        AddAutoIncrementGeneratorHsqlH2 sqlGenerator = new AddAutoIncrementGeneratorHsqlH2();

        factory.register(new AddAutoIncrementGenerator());
        factory.register(sqlGenerator);
        factory.register(new AddAutoIncrementGeneratorDB2());
        then:
        factory.getGenerators().size() == 3

        when:
        factory.unregister(AddAutoIncrementGeneratorHsqlH2.class);
        then:
        factory.getGenerators().size() == 2
    }

     def unregister_class_doesNotExist() {
         when:
        ActionGeneratorFactory factory = ActionGeneratorFactory.getInstance();

        factory.getGenerators().clear();

         then:
        factory.getGenerators().size() == 0

         when:
        factory.register(new AddAutoIncrementGenerator());
        factory.register(new AddAutoIncrementGeneratorHsqlH2());
        factory.register(new AddAutoIncrementGeneratorDB2());

         then:
        factory.getGenerators().size() == 3

         when:
        factory.unregister(AddColumnGenerator.class);
         then:
        factory.getGenerators().size() == 3
    }

   def void reset() {
       when:
        ActionGeneratorFactory instance1 = ActionGeneratorFactory.getInstance();
        ActionGeneratorFactory.reset();
       then:
        instance1 != ActionGeneratorFactory.getInstance()
    }

    def builtInGeneratorsAreFound() {
        expect:
        ActionGeneratorFactory.getInstance().getGenerators().size() > 10
    }

    def getGenerators() {
        when:
        SortedSet<SqlGenerator> allGenerators = ActionGeneratorFactory.getInstance().getGenerators(new AddAutoIncrementStatement(null, null, "person", "name", "varchar(255)", null, null), new H2Database());

        then:
        allGenerators != null
        allGenerators.size() == 1        
    }
}
