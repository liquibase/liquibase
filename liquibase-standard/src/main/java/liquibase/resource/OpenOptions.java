package liquibase.resource;

/**
 * Defines the options for opening {@link Resource}s in Liquibase.
 */
public class OpenOptions {
    private boolean truncate;
    private boolean createIfNeeded;

    /**
     * Use default options of truncate = true, createIfNeeded = true;
     */
    public OpenOptions() {
        this.truncate = true;
        this.createIfNeeded = true;
    }

    /**
     * Should an existing file be truncated when opened. Both this and {@link #isAppend()}
     * are automatically kept in sync with each other.
     */
    public boolean isTruncate() {
        return truncate;
    }

    public OpenOptions setTruncate(boolean truncate) {
        this.truncate = truncate;
        return this;
    }

    /**
     * Should an existing file be appended to when opened. Both this and {@link #isTruncate()}
     * are automatically kept in sync with each other.
     */
    public boolean isAppend() {
        return !isTruncate();
    }

    public OpenOptions setAppend(boolean append) {
        this.truncate = !append;
        return this;
    }

    /**
     * If true, create the resource if it does not exist. If false, do not create the resource.
     */
    public boolean isCreateIfNeeded() {
        return createIfNeeded;
    }

    public OpenOptions setCreateIfNeeded(boolean createIfNeeded) {
        this.createIfNeeded = createIfNeeded;
        return this;
    }
}
