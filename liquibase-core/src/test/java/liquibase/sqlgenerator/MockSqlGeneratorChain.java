package liquibase.sqlgenerator;

import liquibase.actiongenerator.ActionGenerator;
import liquibase.actiongenerator.ActionGeneratorChain;

import java.util.TreeSet;

public class MockSqlGeneratorChain extends SqlGeneratorChain {
    public MockSqlGeneratorChain() {
        super(new ActionGeneratorChain(new TreeSet<ActionGenerator>()));
    }
}
