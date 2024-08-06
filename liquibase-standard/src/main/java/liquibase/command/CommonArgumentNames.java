package liquibase.command;

import lombok.Getter;

/**
 * A common place to store commonly used command argument names.
 */
@Getter
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

}
