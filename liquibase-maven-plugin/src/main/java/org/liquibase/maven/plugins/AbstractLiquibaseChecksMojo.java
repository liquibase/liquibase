package org.liquibase.maven.plugins;

import org.liquibase.maven.property.PropertyElement;

/**
 * A base class for providing Liquibase Quality Checks functionality.
 */
public abstract class AbstractLiquibaseChecksMojo extends AbstractLiquibaseMojo{
    /**
     * Specifies the <i>checksSettingsFile</i> file for Liquibase Quality Checks to use. If not specified, the default
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
}
