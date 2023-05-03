package liquibase;

import liquibase.integration.commandline.LiquibaseCommandLine;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LiquibaseCommandLineTest {

    @Test
    public void variablesDeclaredOnPropertiesFileAreReplacedOnChangelogFile() throws IOException {
        Path tempFile = Files.createTempFile("variableReplacementUpdateSql", ".txt");
        final LiquibaseCommandLine cli = new LiquibaseCommandLine();
        int returnCode = cli.execute(new String[] {"--url=jdbc:h2:mem:liquibase",
                "--defaults-file=variables/liquibase.variables.replacement.properties",
                "--changeLogFile=variables/changelog.xml",
                "--output-file=" + tempFile.toAbsolutePath(), "update-sql"});

        Assert.assertEquals(0, returnCode);
        String updateSqlOutput  = new String(Files.readAllBytes(tempFile));

        Assert.assertTrue(updateSqlOutput.contains("TABLE seq_schema.myTable"));
        Assert.assertFalse(updateSqlOutput.contains("replaceKey"));

        Files.delete(tempFile);
    }
}
