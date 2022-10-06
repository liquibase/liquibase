package liquibase.resource;

import liquibase.exception.CommandValidationException;

import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

/**
 * Defines the open options for {@link Resource}s in Liquibase.
 */
public class OpenOptions {
    private final boolean truncate;
    private final boolean append;

    public OpenOptions(boolean truncate, boolean append) {
        this.truncate = truncate;
        this.append = append;
        validate();
    }

    private void validate() {
        if (truncate && append) {
            throw new IllegalArgumentException("append and truncate not allowed");
        }
    }

    public boolean isTruncate() {
        return truncate;
    }

    public boolean isAppend() {
        return append;
    }

    public StandardOpenOption getStandardOpenOption() {
        if (isTruncate()) {
            return StandardOpenOption.TRUNCATE_EXISTING;
        } else if (isAppend()) {
            return StandardOpenOption.APPEND;
        } else {
            return null;
        }
    }

    public static class Builder {
        private boolean truncate = false;
        private boolean append = false;

        public Builder truncate() {
            this.truncate = true;
            return this;
        }

        public Builder append() {
            this.append = true;
            return this;
        }

        public OpenOptions build() {
            return new OpenOptions(truncate, append);
        }
    }
}
