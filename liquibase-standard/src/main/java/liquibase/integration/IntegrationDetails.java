package liquibase.integration;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores information about the integration running Liquibase.
 * <p>
 * NOTE: This class is under development and will likely change over time.
 */
@Setter
@Getter
public class IntegrationDetails {
    private String name;

    private Map<String, String> parameters = new HashMap<>();

    public void setParameter(String key, String value) {
        this.parameters.put(key, value);
    }
}
