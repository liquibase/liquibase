package org.liquibase.maven.plugins;

import liquibase.Scope;
import liquibase.command.CommandDefinition;
import liquibase.command.CommandFactory;
import liquibase.command.CommandResults;
import liquibase.command.CommandScope;
import liquibase.configuration.AbstractMapConfigurationValueProvider;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.CommandExecutionException;
import liquibase.integration.commandline.Main;
import liquibase.util.StringUtil;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.MapOrientedComponent;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.repository.ComponentRequirement;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
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
            CommandScope commandScope = new CommandScope(goal);

            mojoConfigurationValueProvider = new MojoConfigurationValueProvider(configuration);
            liquibaseConfiguration.registerProvider(mojoConfigurationValueProvider);
            /*
            CommandFactory commandFactory = Scope.getCurrentScope().getSingleton(CommandFactory.class);
            CommandDefinition commandDef = commandFactory.getCommandDefinition(goal);
            for (String argName : commandDef.getArguments().keySet()) {
                Object value = configuration.get(argName);
                if (value != null) {
                    commandScope.addArgumentValue(argName, value);
                }
            }
            */

            Map<String, Object> scopeValues = new HashMap<>();
            scopeValues.put(Scope.Attr.resourceAccessor.name(), new MavenResourceAccessor(project));
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
