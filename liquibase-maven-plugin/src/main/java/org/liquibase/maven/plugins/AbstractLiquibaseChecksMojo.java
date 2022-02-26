package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.checks.config.ChecksFileAccessor;
import liquibase.checks.config.FileAccessor;
import liquibase.checks.config.InMemoryChecksFileAccessor;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.LiquibaseException;
import liquibase.util.StringUtil;
import org.liquibase.maven.property.PropertyElement;

import java.util.Collections;

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

    /**
     * Specifies the <i>format</i> file for Liquibase Quality Checks to use. If not specified, the default
     * format will be used
     *
     * @parameter property="liquibase.format"
     */
    @PropertyElement
    protected String format;

    @Override
    protected final void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        try {
            if (format == null) {
                format = "txt";
            }
            FileAccessor fileAccessor;
            // If no checksSettingsFile is specified, then run with the default quality checks file in memory.
            if (StringUtil.isEmpty(checksSettingsFile)) {
                fileAccessor = new InMemoryChecksFileAccessor();
            } else {
                fileAccessor = new ChecksFileAccessor();
            }

            Scope.child(Collections.singletonMap("fileAccessor", fileAccessor), this::performChecksTask);
        } catch (Exception e) {
            throw new LiquibaseException(e);
        }
    }


    @Override
    public boolean shouldLoadLiquibaseProperties() {
        return false;
    }

    @Override
    public boolean databaseConnectionRequired() {
        return false;
    }

    protected abstract void performChecksTask() throws CommandExecutionException;
}
