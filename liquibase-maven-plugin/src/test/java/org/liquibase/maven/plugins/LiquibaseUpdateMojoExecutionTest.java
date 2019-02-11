package org.liquibase.maven.plugins;

import java.io.File;
import java.util.List;
import java.util.Optional;

import liquibase.changelog.ChangeLogParameters;

/**
 * Test class for the Liquibase Update Mojo.
 * <p>
 * Note that we need to run mvn test once to generate the Plugin artifacts
 * before we can run the UnitTest in the IDE.
 *
 * @author Martin Meyer <martin.meyer@inftec.ch>
 */
public class LiquibaseUpdateMojoExecutionTest extends AbstractLiquibaseMojoTest {
    /**
     * Test the lookup of relative path names for changeLog
     */
    public void testRelativeClobFiles() throws Exception {
        File pom = getTestFile("src/test/resources/update/relativeClobFiles/plugin_config.xml");

        if (!pom.exists()) {
            return;
        }
        LiquibaseUpdate update = (LiquibaseUpdate) lookupMojo("update", pom);
        update.execute();
    }

    public void testChangeLogParametersFromPropertiesFile() throws Exception {
        File pom = getTestFile("src/test/resources/update/parameters/plugin_config.xml");
        if (!pom.exists()) {
            return;
        }
        System.setProperty("overridden", "isoverridden");
        LiquibaseUpdate update = (LiquibaseUpdate) lookupMojo("update", pom);
        update.execute();
        List<ChangeLogParameters.ChangeLogParameter> params = update.getLiquibase().getChangeLogParameters().getChangeLogParameters();
        System.clearProperty("overridden");

        Optional<String> overridden = getChangeLogParameter(params, "overridden");
        Optional<String> notoverridden = getChangeLogParameter(params, "notoverridden");

        assertTrue("overridden should be present", overridden.isPresent());
        assertEquals("wrong value for overriden", "isoverridden", overridden.get());
        assertTrue("notoverridden should be present", notoverridden.isPresent());
        assertEquals("wrong value for notoverridden", "notoverridden", notoverridden.get());

    }

    private static Optional<String> getChangeLogParameter(List<ChangeLogParameters.ChangeLogParameter> params, String paramName){
        return params.stream()
                .filter(p -> paramName.equalsIgnoreCase(p.getKey()))
                .map(ChangeLogParameters.ChangeLogParameter::getValue)
                .map(Object::toString)
                .findFirst();
    }
}
