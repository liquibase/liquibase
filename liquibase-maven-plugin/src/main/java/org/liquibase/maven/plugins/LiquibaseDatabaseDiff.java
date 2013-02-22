// Version:   $Id: $
// Copyright: Copyright(c) 2008 Trace Financial Limited
package org.liquibase.maven.plugins;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.exception.LiquibaseException;
import liquibase.integration.commandline.CommandLineUtils;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;

/**
 * Generates a diff between the specified database and the reference database.
 *
 * @author Peter Murray
 * @goal diff
 */
public class LiquibaseDatabaseDiff extends AbstractLiquibaseChangeLogMojo {

    /**
     * The fully qualified name of the driver class to use to connect to the reference database.
     * If this is not specified, then the {@link #driver} will be used instead.
     *
     * @parameter expression="${liquibase.referenceDriver}"
     */
    protected String referenceDriver;

    /**
     * The reference database URL to connect to for executing Liquibase. If performing a diff
     * against a Hibernate config xml file, then use <b>"hibernate:PATH_TO_CONFIG_XML"</b>
     * as the URL. The path to the hibernate configuration file can be relative to the test
     * classpath for the Maven project.
     *
     * @parameter expression="${liquibase.referenceUrl}"
     */
    protected String referenceUrl;

    /**
     * The reference database username to use to connect to the specified database.
     *
     * @parameter expression="${liquibase.referenceUsername}"
     */
    protected String referenceUsername;

    /**
     * The reference database password to use to connect to the specified database. If this is
     * null then an empty password will be used.
     *
     * @parameter expression="${liquibase.referencePassword}"
     */
    protected String referencePassword;

    /**
     * The reference database catalog.
     *
     * @parameter expression="${liquibase.referenceDefaultCatalogName}"
     */
    protected String referenceDefaultCatalogName;

    /**
     * The reference database schema.
     *
     * @parameter expression="${liquibase.referenceDefaultSchemaName}"
     */
    protected String referenceDefaultSchemaName;

    /**
     * The server id in settings.xml to use when authenticating with.
     *
     * @parameter expression="${liquibase.referenceServer}"
     */
    private String referenceServer;


    /**
     * The diff change log file to output the differences to. If this is null then the
     * differences will be output to the screen.
     *
     * @parameter expression="${liquibase.diffChangeLogFile}"
     */
    protected String diffChangeLogFile;

    /**
     * Include the catalog in the diff output? If this is null then the catalog will not be included
     *
     * @parameter expression="${liquibase.diffIncludeCatalog}"
     */
    protected boolean diffIncludeCatalog;

    /**
     * Include the schema in the diff output? If this is null then the schema will not be included
     *
     * @parameter expression="${liquibase.diffIncludeSchema}"
     */
    protected boolean diffIncludeSchema;

    /**
     * Include the tablespace in the diff output? If this is null then the tablespace will not be included
     *
     * @parameter expression="${liquibase.diffIncludeTablespace}"
     */
    protected boolean diffIncludeTablespace;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
    	if(referenceServer!=null) {
    		AuthenticationInfo referenceInfo = wagonManager.getAuthenticationInfo(referenceServer);
    		if (referenceInfo != null) {
    			referenceUsername = referenceInfo.getUserName();
    			referencePassword = referenceInfo.getPassword();
    		}
    	}

        super.execute();
    }

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        ClassLoader cl = null;
        try {
            cl = getClassLoaderIncludingProjectClasspath();
            Thread.currentThread().setContextClassLoader(cl);
        }
        catch (MojoExecutionException e) {
            throw new LiquibaseException("Could not create the class loader, " + e, e);
        }

        Database db = liquibase.getDatabase();
        Database referenceDatabase = CommandLineUtils.createDatabaseObject(cl, referenceUrl, referenceUsername, referencePassword, referenceDriver, referenceDefaultCatalogName, referenceDefaultSchemaName, null, null);

        getLog().info("Performing Diff on database " + db.toString());
        if (diffChangeLogFile != null) {
            try {
                CommandLineUtils.doDiffToChangeLog(diffChangeLogFile, referenceDatabase, db, new DiffOutputControl(diffIncludeCatalog, diffIncludeSchema, diffIncludeTablespace));
                getLog().info("Differences written to Change Log File, " + diffChangeLogFile);
            }
            catch (IOException e) {
                throw new LiquibaseException(e);
            }
            catch (ParserConfigurationException e) {
                throw new LiquibaseException(e);
            }
        } else {
            CommandLineUtils.doDiff(referenceDatabase, db);
        }
    }

    @Override
    protected void printSettings(String indent) {
        super.printSettings(indent);
        getLog().info(indent + "referenceDriver: " + referenceDriver);
        getLog().info(indent + "referenceUrl: " + referenceUrl);
        getLog().info(indent + "referenceUsername: " + referenceUsername);
        getLog().info(indent + "referencePassword: " + referencePassword);
        getLog().info(indent + "referenceDefaultSchema: " + referenceDefaultSchemaName);
        getLog().info(indent + "diffChangeLogFile: " + diffChangeLogFile);
    }

    @Override
    protected void checkRequiredParametersAreSpecified() throws MojoFailureException {
        super.checkRequiredParametersAreSpecified();

        if (referenceUrl == null) {
            throw new MojoFailureException("A reference database or hibernate configuration file "
                    + "must be provided to perform a diff.");
        }

        if (referencePassword == null) {
            referencePassword = "";
        }
    }

    @Override
    protected boolean isPromptOnNonLocalDatabase() {
        return false;
  }
}
