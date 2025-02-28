package liquibase.integration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores information about the integration running Liquibase.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationDetails {
    /**
     * A name which defines what integration is executing Liquibase. An example might be "cli" or "maven". This is not
     * representative of the environnment that Liquibase is executing inside of, so "docker" would not be a valid name.
     */
    private String name;

    private final Map<String, String> parameters = new HashMap<>();

    public void setParameter(String key, String value) {
        this.parameters.put(key, value);
    }
}
