package liquibase.integration.commandline.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static liquibase.util.LiquibaseLauncherSettings.LiquibaseLauncherSetting.LIQUIBASE_CLASSPATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for ParameterUtil, specifically for reading parameters from properties files.
 */
public class ParameterUtilTest {

    @Test
    public void testGetParameter_classpathFromPropertiesFile_shortForm(@TempDir Path tempDir) throws IOException {
        // Create a temporary properties file with "classpath" (short form)
        File propertiesFile = tempDir.resolve("liquibase.properties").toFile();
        try (FileWriter writer = new FileWriter(propertiesFile)) {
            writer.write("classpath=../drivers/mssql-jdbc-12.6.1.jre11.jar;./liquibase/db-migration.jar\n");
        }

        String[] args = {"--defaults-file=" + propertiesFile.getAbsolutePath()};

        String result = ParameterUtil.getParameter(LIQUIBASE_CLASSPATH, "classpath", args, true);

        assertEquals("../drivers/mssql-jdbc-12.6.1.jre11.jar;./liquibase/db-migration.jar", result);
    }

    @Test
    public void testGetParameter_classpathFromPropertiesFile_longForm(@TempDir Path tempDir) throws IOException {
        // Create a temporary properties file with "liquibase.classpath" (long form)
        File propertiesFile = tempDir.resolve("liquibase.properties").toFile();
        try (FileWriter writer = new FileWriter(propertiesFile)) {
            writer.write("liquibase.classpath=../drivers/mssql-jdbc-12.6.1.jre11.jar;./liquibase/db-migration.jar\n");
        }

        String[] args = {"--defaults-file=" + propertiesFile.getAbsolutePath()};

        String result = ParameterUtil.getParameter(LIQUIBASE_CLASSPATH, "classpath", args, true);

        assertEquals("../drivers/mssql-jdbc-12.6.1.jre11.jar;./liquibase/db-migration.jar", result);
    }

    @Test
    public void testGetParameter_classpathFromPropertiesFile_notPresent(@TempDir Path tempDir) throws IOException {
        // Create a temporary properties file without classpath
        File propertiesFile = tempDir.resolve("liquibase.properties").toFile();
        try (FileWriter writer = new FileWriter(propertiesFile)) {
            writer.write("driver=com.microsoft.sqlserver.jdbc.SQLServerDriver\n");
        }

        String[] args = {"--defaults-file=" + propertiesFile.getAbsolutePath()};

        String result = ParameterUtil.getParameter(LIQUIBASE_CLASSPATH, "classpath", args, true);

        assertNull(result);
    }

    @Test
    public void testGetParameter_classpathFromCommandLine() throws IOException {
        String[] args = {"--classpath=../drivers/test.jar"};

        String result = ParameterUtil.getParameter(LIQUIBASE_CLASSPATH, "classpath", args, false);

        assertEquals("../drivers/test.jar", result);
    }

    @Test
    public void testGetParameter_classpathFromCommandLine_withSpace() throws IOException {
        String[] args = {"--classpath", "../drivers/test.jar"};

        String result = ParameterUtil.getParameter(LIQUIBASE_CLASSPATH, "classpath", args, false);

        assertEquals("../drivers/test.jar", result);
    }
}
