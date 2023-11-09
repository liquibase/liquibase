package liquibase.command;

/**
 * A common place to store commonly used command argument names.
 */
public enum CommonArgumentNames {
    USERNAME("username"),
    PASSWORD("password"),
    URL("url"),
    REFERENCE_URL("referenceUrl"),
    CHANGELOG_FILE("changelogFile");

    private final String argumentName;

    CommonArgumentNames(String argumentName) {
        this.argumentName = argumentName;
    }

    public String getArgumentName() {
        return argumentName;
    }
}
