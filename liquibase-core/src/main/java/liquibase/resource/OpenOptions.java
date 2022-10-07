package liquibase.resource;

import java.nio.file.StandardOpenOption;

/**
 * Defines the open options for {@link Resource}s in Liquibase.
 */
public class OpenOptions {
    private boolean truncate;
    private boolean createIfNeeded;

    public OpenOptions(boolean truncate, boolean createIfNeeded) {
        this.truncate = truncate;
        this.createIfNeeded = createIfNeeded;
    }

    public boolean isTruncate() {
        return truncate;
    }

    public void setTruncate(boolean truncate) {
        this.truncate = truncate;
    }

    public boolean isAppend() {
        return !isTruncate();
    }

    public void setAppend(boolean append) {
        this.truncate = !append;
    }

    public boolean isCreateIfNeeded() {
        return createIfNeeded;
    }

    public void setCreateIfNeeded(boolean createIfNeeded) {
        this.createIfNeeded = createIfNeeded;
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
        private boolean createIfNeeded = true;

        public Builder truncate() {
            this.truncate = true;
            return this;
        }

        public Builder append() {
            this.truncate = false;
            return this;
        }

        public Builder createIfNeeded(boolean createIfNeeded) {
            this.createIfNeeded = createIfNeeded;
            return this;
        }

        public OpenOptions build() {
            return new OpenOptions(truncate, createIfNeeded);
        }
    }
}
