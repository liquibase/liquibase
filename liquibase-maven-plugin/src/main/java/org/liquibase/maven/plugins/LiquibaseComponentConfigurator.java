package org.liquibase.maven.plugins;

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

public class LiquibaseComponentConfigurator implements ComponentConfigurator {

    @Override
    public void configureComponent(Object o, PlexusConfiguration plexusConfiguration, ClassRealm classRealm) throws ComponentConfigurationException {

    }

    @Override
    public void configureComponent(Object o, PlexusConfiguration plexusConfiguration, ExpressionEvaluator expressionEvaluator, ClassRealm classRealm) throws ComponentConfigurationException {

    }

    @Override
    public void configureComponent(Object o, PlexusConfiguration plexusConfiguration, ExpressionEvaluator expressionEvaluator, ClassRealm classRealm, ConfigurationListener configurationListener) throws ComponentConfigurationException {

    }
}
