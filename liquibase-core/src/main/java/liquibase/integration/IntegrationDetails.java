package liquibase.integration;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores information about the integration running Liquibase.
 *
 * NOTE: This class is under development and will likely change over time.
 */
public class IntegrationDetails {
    private String name;

    private Map<String, String> parameters = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public void setParameter(String key, String value) {
        this.parameters.put(key, value);
    }
}
