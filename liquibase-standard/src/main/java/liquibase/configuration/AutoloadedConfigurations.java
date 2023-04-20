package liquibase.configuration;

/**
 * Marker interface for a class containing {@link ConfigurationDefinition} which should be auto-loaded at Liquibase startup.
 * All classes that implement this interface must still be registered in the META-INF/services/liquibase.configuration.AutoloadedConfigurations file.
 */
public interface AutoloadedConfigurations {

}
