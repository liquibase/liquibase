package liquibase.integration.ant.type;

import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.types.PropertySet;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class ConnectionProperties {

    /**
     * Substring tokens (lowercase) that mark a {@code <connectionProperty>} as
     * credential-bearing for the {@link #clearCredentialValues()} sweep. Matched
     * case-insensitively against the lowercased property name via
     * {@link Locale#ROOT} so the Turkish-locale dotless-i quirk does not cause
     * an {@code I} in {@code APIKEY} to map to {@code ı} and miss the
     * {@code apikey} token. Mirrors the analogous denylists used elsewhere in
     * the May-2026 OSS credential-handling audit (CommandScope, IntegrationDetails).
     */
    private static final String[] CREDENTIAL_NAME_TOKENS = {
            "password", "passwd", "secret", "token", "apikey", "accesskey", "credentials"
    };

    private final List<PropertySet> propertySets;
    private final List<Property> properties;

    public ConnectionProperties() {
        propertySets = new LinkedList<>();
        properties = new LinkedList<>();
    }

    public Properties buildProperties() {
        Properties retProps = new Properties();
        for(PropertySet propertySet : propertySets) {
            retProps.putAll(propertySet.getProperties());
        }

        for(Property property : properties) {
            retProps.setProperty(property.getName(), property.getValue());
        }
        return retProps;
    }

    public void add(PropertySet propertySet) {
        propertySets.add(propertySet);
    }

    public void addConnectionProperty(Property property) {
        properties.add(property);
    }

    /**
     * Overwrites the value of any {@code <connectionProperty>} whose name marks it as
     * credential-bearing (per {@link #CREDENTIAL_NAME_TOKENS}) with {@code "*****"} so
     * the raw credential String no longer lives in this object after the Ant build
     * finishes. Called from {@link DatabaseType#clearCredentials()}'s buildFinished
     * listener (CWE-316: Cleartext Storage of Sensitive Information in Memory).
     * <p>
     * Note: this method intentionally does <em>not</em> touch {@link #propertySets}.
     * Those entries reference Project-level property sets owned by the surrounding
     * Ant build, not the {@code DatabaseType} itself; clearing them would mutate
     * unrelated build state.
     */
    void clearCredentialValues() {
        for (Property property : properties) {
            String name = property.getName();
            if (name == null) {
                continue;
            }
            String lowerName = name.toLowerCase(Locale.ROOT);
            for (String token : CREDENTIAL_NAME_TOKENS) {
                if (lowerName.contains(token)) {
                    property.setValue("*****");
                    break;
                }
            }
        }
    }
}
