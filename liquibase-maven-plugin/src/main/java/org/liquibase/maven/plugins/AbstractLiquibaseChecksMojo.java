package org.liquibase.maven.plugins;

import org.liquibase.maven.property.PropertyElement;

/**
 * A base class for providing Liquibase Policy Checks functionality.
 */
public abstract class AbstractLiquibaseChecksMojo extends AbstractLiquibaseMojo{
    /**
     * Specifies the <i>checksSettingsFile</i> file for Liquibase Policy Checks to use. If not specified, the default
     * checks will be used and no file will be created.
     *
     * @parameter property="liquibase.checksSettingsFile"
     */
    @PropertyElement
    protected String checksSettingsFile;

    @Override
    public boolean databaseConnectionRequired() {
        return false;
    }
    protected boolean doesMarkerClassExist() {
        try {
            Class.forName("com.datical.liquibase.ext.command.helpers.ChecksCompatibilityCommandStep");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

}
