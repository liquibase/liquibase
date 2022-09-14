package org.liquibase.maven.plugins;

import liquibase.Scope;
import liquibase.command.CommandDefinition;
import liquibase.command.CommandFactory;
import liquibase.command.CommandResults;
import liquibase.command.CommandScope;
import liquibase.integration.commandline.Main;
import liquibase.util.StringUtil;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.Parameter;
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

public class LiquibaseCommandMojo extends AbstractMojo implements MapOrientedComponent  {

    private String goal;
    private Map<String, Object> configuration;
    private ClassLoader classloader;
//    private List<Parameter> goalParameters;

    public LiquibaseCommandMojo() {
    }

    @Override
    public void addComponentRequirement(ComponentRequirement componentRequirement, Object o) throws ComponentConfigurationException {
        System.out.println("Add compp");
    }

    @Override
    public void setComponentConfiguration(Map<?, ?> map) throws ComponentConfigurationException {
        MojoExecution mojoExecution = (MojoExecution) map.get("mojoExecution");
        MavenProject project = (MavenProject) map.get("project");

        this.goal = mojoExecution.getGoal();


        try {
            List classpathElements = project.getCompileClasspathElements();
            classpathElements.add(project.getBuild().getOutputDirectory());
            URL urls[] = new URL[classpathElements.size()];
            for (int i = 0; i < classpathElements.size(); ++i) {
                urls[i] = new File((String) classpathElements.get(i)).toURI().toURL();
            }
            this.classloader = new URLClassLoader(urls, MavenUtils.getArtifactClassloader(project,
                    true,
                    true,
                    getClass(),
                    getLog(),
                    false));

            this.configuration = new HashMap<>();
            for (Map.Entry entry : map.entrySet()) {
                Object value = entry.getValue();
                if (value == null) {
                    continue;
                }
                if (value.getClass().getName().startsWith("org.apache")) {
                    continue;
                }
                this.configuration.put(String.valueOf(entry.getKey()), entry.getValue());
            }

            Properties userProperties = ((MavenSession) map.get("session")).getUserProperties();
            for (Map.Entry<Object, Object> entry : userProperties.entrySet()) {
                this.configuration.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        } catch (Exception e) {
            throw new ComponentConfigurationException(e.getMessage(), e);
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
//        System.out.println("EXECUTE COMMAND MOJO!!!"+goal);
//        System.out.println(StringUtil.join(configuration, ","));
        Main.runningFromNewCli = true;

        try {
            CommandScope commandScope = new CommandScope(goal);

            CommandFactory commandFactory = Scope.getCurrentScope().getSingleton(CommandFactory.class);
            CommandDefinition commandDef = commandFactory.getCommandDefinition(goal);
            for (String argName : commandDef.getArguments().keySet()) {
                Object value = configuration.get(argName);
                if (value != null) {
                    commandScope.addArgumentValue(argName, value);
                }
            }

            Map<String, Object> scopeValues = new HashMap<>();
            scopeValues.put(Scope.Attr.resourceAccessor.name(), new MavenResourceAccessor(classloader));
            Scope.child(scopeValues, () -> {
                commandScope.execute();
            });
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }


//    public static void setField(Object field) {
//        System.out.println("setting");
//    }
}
