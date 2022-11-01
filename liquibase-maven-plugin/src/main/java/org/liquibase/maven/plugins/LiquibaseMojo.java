package org.liquibase.maven.plugins;

import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.configuration.AbstractMapConfigurationValueProvider;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.integration.commandline.Main;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.MapOrientedComponent;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.repository.ComponentRequirement;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class LiquibaseMojo extends AbstractMojo implements MapOrientedComponent  {

    private Map<String, Object> configuration;
    private MavenProject project;
    private MojoExecution mojoExecution;

    public LiquibaseMojo() {
    }

    @Override
    public void addComponentRequirement(ComponentRequirement componentRequirement, Object o) throws ComponentConfigurationException {
        //TODO: How does this get used?
    }

    @Override
    public void setComponentConfiguration(Map<?, ?> config) throws ComponentConfigurationException {
        this.mojoExecution = (MojoExecution) config.get("mojoExecution");
        this.project = (MavenProject) config.get("project");

        try {
            this.configuration = new HashMap<>();
            for (Map.Entry<?, ?> entry : config.entrySet()) {
                Object value = entry.getValue();
                if (value == null) {
                    continue;
                }
                if (value.getClass().getName().startsWith("org.apache")) {
                    continue;
                }
                this.configuration.put(String.valueOf(entry.getKey()), entry.getValue());
            }

            Properties userProperties = ((MavenSession) config.get("session")).getUserProperties();
            for (Map.Entry<Object, Object> entry : userProperties.entrySet()) {
                this.configuration.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        } catch (Exception e) {
            throw new ComponentConfigurationException(e.getMessage(), e);
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Main.runningFromNewCli = true;

        String goal = mojoExecution.getGoal();
        LiquibaseConfiguration liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);
        MojoConfigurationValueProvider mojoConfigurationValueProvider = null;
        try {
            mojoConfigurationValueProvider = new MojoConfigurationValueProvider(configuration);
            liquibaseConfiguration.registerProvider(mojoConfigurationValueProvider);

            Map<String, Object> scopeValues = new HashMap<>();
            scopeValues.put(Scope.Attr.resourceAccessor.name(), new MavenResourceAccessor(project));
            CommandScope commandScope = new CommandScope(goal);
            Scope.child(scopeValues, () -> {
                commandScope.execute();
            });
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            if (mojoConfigurationValueProvider != null) {
                liquibaseConfiguration.unregisterProvider(mojoConfigurationValueProvider);
                mojoConfigurationValueProvider = null;
            }
        }
    }

    private static class MojoConfigurationValueProvider extends AbstractMapConfigurationValueProvider {
        private final Map<String, Object> configurationMap;

        MojoConfigurationValueProvider(Map<String, Object> configurationMap) {
            this.configurationMap = configurationMap;
        }

        @Override
        protected Map<?, ?> getMap() {
            return configurationMap;
        }

        /**
         *
         * This override of keyMatches allows the global arguments
         * to be specified as either "liquibase.arg" or "arg"
         *
         * @param   wantedKey the configuration key requested
         * @param   storedKey the key stored in the map
         * @return  boolean
         *
         */
        @Override
        protected boolean keyMatches(String wantedKey, String storedKey) {
            if (super.keyMatches(wantedKey, storedKey)) {
                return true;
            }
            if (wantedKey.startsWith("liquibase.command.")) {
                return super.keyMatches(wantedKey.replaceFirst("^liquibase\\.command\\.", ""), storedKey);
            }

            return super.keyMatches(wantedKey.replaceFirst("^liquibase\\.", ""), storedKey);
        }

        @Override
        protected String getSourceDescription() {
            return "Maven value provider";
        }

        @Override
        public int getPrecedence() {
            return 1;
        }
    }

//    public static void setField(Object field) {
//        System.out.println("setting");
//    }
}
