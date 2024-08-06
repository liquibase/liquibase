package liquibase.resource;

import lombok.Getter;

/**
 * Defines the options for opening {@link Resource}s in Liquibase.
 */
@Getter
public class OpenOptions {
    /**
     * -- GETTER --
     *  Should an existing file be truncated when opened. Both this and
     *  are automatically kept in sync with each other.
     */
    private boolean truncate;
    /**
     * -- GETTER --
     *  If true, create the resource if it does not exist. If false, do not create the resource.
     */
    private boolean createIfNeeded;

    /**
     * Use default options of truncate = true, createIfNeeded = true;
     */
    public OpenOptions() {
        this.truncate = true;
        this.createIfNeeded = true;
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

    public OpenOptions setCreateIfNeeded(boolean createIfNeeded) {
        this.createIfNeeded = createIfNeeded;
        return this;
    }
}
