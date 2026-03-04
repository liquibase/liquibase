package liquibase.serializer;

/**
 * Marker interface indicating that implementing objects should be unwrapped during YAML/JSON serialization.
 * <p>
 * This is used to avoid nested wrapper structures in the resulting YAML and JSON outputs.
 * Implement this interface if the object should be serialized directly, rather than as a nested property.
 */
public interface UnwrappedLiquibaseSerializable extends LiquibaseSerializable {
}
