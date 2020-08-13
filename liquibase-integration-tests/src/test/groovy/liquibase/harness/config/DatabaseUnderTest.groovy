package liquibase.harness.config;

class DatabaseUnderTest {
    String name
    String username
    String password
    List<DatabaseVersion> versions
    List<String> changeObjects
    String dbSchema
}
