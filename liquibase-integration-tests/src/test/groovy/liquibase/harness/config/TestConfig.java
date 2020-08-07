package liquibase.harness.config;

import java.util.List;

public class TestConfig {
    private List<DatabaseUnderTest> databasesUnderTest;

    public List<DatabaseUnderTest> getDatabasesUnderTest() {
        return databasesUnderTest;
    }

    public void setDatabasesUnderTest(List<DatabaseUnderTest> databasesUnderTest) {
        this.databasesUnderTest = databasesUnderTest;
    }
}
