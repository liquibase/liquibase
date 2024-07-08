package liquibase.sqlgenerator;

import java.util.TreeSet;

public class MockSqlGeneratorChain extends SqlGeneratorChain {
    public MockSqlGeneratorChain() {
        super(new TreeSet<SqlGenerator>());
    }
}
