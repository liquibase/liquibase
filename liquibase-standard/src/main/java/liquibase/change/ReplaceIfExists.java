package liquibase.change;

/**
 * Marker interface for all {@link Change}s providing a {@code setReplaceIfExists()} method.
 */
public interface ReplaceIfExists {
    void setReplaceIfExists(Boolean replaceIfExists);
}
