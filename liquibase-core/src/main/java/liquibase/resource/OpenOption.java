package liquibase.resource;

import java.nio.file.StandardOpenOption;

/**
 * Defines the open options for {@link Resource}s in Liquibase.
 */
public enum OpenOption {
    TRUNCATE(StandardOpenOption.TRUNCATE_EXISTING),
    APPEND(StandardOpenOption.APPEND);

    private final StandardOpenOption standardOpenOption;

    OpenOption(StandardOpenOption standardOpenOption) {
        this.standardOpenOption = standardOpenOption;
    }

    public StandardOpenOption getStandardOpenOption() {
        return standardOpenOption;
    }
}
