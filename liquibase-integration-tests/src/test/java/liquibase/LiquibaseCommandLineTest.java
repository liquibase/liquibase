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
                "--changeLogFile=variables/changelog.xml", "--show-banner=false",
                "--output-file=" + tempFile.toAbsolutePath(), "update-sql"});

        Assert.assertEquals(0, returnCode);
        String updateSqlOutput  = new String(Files.readAllBytes(tempFile));

        Assert.assertTrue(updateSqlOutput.contains("TABLE seq_schema.myTable"));
        Assert.assertFalse(updateSqlOutput.contains("replaceKey"));

        Files.delete(tempFile);
    }

    @Test
    public void specifyDatabaseClass() {
        final LiquibaseCommandLine cli = new LiquibaseCommandLine();
        int returnCode = cli.execute(new String[] {
                "--url=jdbc:h2:mem:liquibase",
                "--changeLogFile=changelogs/specific.dbms.xml",
                "--databaseClass=liquibase.database.core.H2Database",
                "--show-banner=false",
                "update"
        });

        Assert.assertEquals(0, returnCode);
    }

    @Test
    public void withoutSpecifyDatabaseClass() {
        final LiquibaseCommandLine cli = new LiquibaseCommandLine();
        int returnCode = cli.execute(new String[] {
                "--url=jdbc:h2:mem:liquibase",
                "--changeLogFile=changelogs/specific.dbms.xml",
                "--show-banner=false",
                "update"
        });

        Assert.assertEquals(0, returnCode);
    }
}
