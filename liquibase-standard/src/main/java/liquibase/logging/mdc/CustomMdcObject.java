package liquibase.logging.mdc;

/**
 * Marker interface that identifies an object as a custom object used in MDC. The structured log formatter automatically
 * recognizes all the classes which implement this interface and properly formats their JSON output.
 */
public interface CustomMdcObject {
}
