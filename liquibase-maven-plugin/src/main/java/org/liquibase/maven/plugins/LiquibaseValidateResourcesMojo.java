package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;
import liquibase.sdk.database.MockDatabase;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Executes process to validate changelog resources like sql files.
 *
 * @author Artem Ptushkin
 * @description Liquibase #validateResources Maven plugin
 * @goal validateResources
 */
public class LiquibaseValidateResourcesMojo extends AbstractLiquibaseChangeLogMojo {

    /**
     * Parameter to run with Mocked Database.
     * Needed for resource validation without database connection.
     *
     * @parameter expression="${liquibase.localRun}"
     */
    protected boolean localRun = true;

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
       liquibase.validateResources();
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info(MavenUtils.LOG_SEPARATOR);

        LiquibaseConfiguration liquibaseConfiguration = LiquibaseConfiguration.getInstance();

        if (!liquibaseConfiguration.getConfiguration(GlobalConfiguration.class).getShouldRun()) {
            getLog().info("Liquibase did not run because " + liquibaseConfiguration.describeValueLookupLogic(GlobalConfiguration.class, GlobalConfiguration.SHOULD_RUN) + " was set to false");
            return;
        }
        if (skip) {
            getLog().warn("Liquibase skipped due to maven configuration");
            return;
        }

        getLog().warn(withOutConnectionMessage());

        ClassLoader artifactClassLoader = getMavenArtifactClassLoader();
        ResourceAccessor fileOpener = getFileOpener(artifactClassLoader);
        Liquibase liquibase = createLiquibase(fileOpener, createMockDatabase());

        try {
            performLiquibaseTask(liquibase);
        } catch (LiquibaseException e) {
            throw new MojoExecutionException(failedValidationMessage(e), e);
        }
        getLog().info(MavenUtils.LOG_SEPARATOR);
    }

    private String withOutConnectionMessage() {
        return "Database connection will be ignored. ResourceValidation does not need database connection";
    }

    private String failedValidationMessage(LiquibaseException e) {
        return "Failed to validate changelog resources: " + e.getMessage();
    }

    private Database createMockDatabase() {
        return new MockDatabase();
    }
}
