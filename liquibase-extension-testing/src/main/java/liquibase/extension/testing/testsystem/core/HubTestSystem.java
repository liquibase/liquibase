package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.TestSystem;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

public class HubTestSystem extends TestSystem {

    private final SortedSet<String> configurationKeys = new TreeSet<>(Arrays.asList("apiKey", "url", "username", "orgId"));

    public HubTestSystem() {
        super("hub");
    }

    public HubTestSystem(Definition definition) {
        super(definition);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    public String getApiKey() {
        return getConfiguredValue("apiKey", String.class);
    }

    public String getUrl() {
        return getConfiguredValue("url", String.class);
    }

    public String getUsername() {
        return getConfiguredValue("username", String.class);
    }

    public UUID getOrgId() {
        return UUID.fromString(getConfiguredValue("orgId", String.class));
    }

    @Override
    public SortedSet<String> getConfigurationKeys() {
        final SortedSet<String> returnKeys = super.getConfigurationKeys();
        returnKeys.addAll(this.configurationKeys);
        return returnKeys;
    }
}
