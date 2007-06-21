package liquibase.migrator.commandline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import liquibase.migrator.exception.CommandLineParsingException;

import org.junit.Test;

/**
 * Tests for {@link CommandLineMigrator}
 */
public class CommandLineMigratorTest {

    @Test
    public void migrateWithAllParameters() throws Exception {
        String[] args = new String[]{
                "--driver=DRIVER",
                "--username=USERNAME",
                "--password=PASSWORD",
                "--url=URL",
                "--changeLogFile=FILE",
                "--classpath=CLASSPATH;CLASSPATH2",
                "--contexts=CONTEXT1,CONTEXT2",
                "--promptForNonLocalDatabase=true",
                "migrate",
        };

        CommandLineMigrator cli = new CommandLineMigrator();
        cli.parseOptions(args);

        assertEquals("DRIVER", cli.driver);
        assertEquals("USERNAME", cli.username);
        assertEquals("PASSWORD", cli.password);
        assertEquals("URL", cli.url);
        assertEquals("FILE", cli.changeLogFile);
        assertEquals("CLASSPATH;CLASSPATH2", cli.classpath);
        assertEquals("CONTEXT1,CONTEXT2", cli.contexts);
        assertEquals(Boolean.TRUE, cli.promptForNonLocalDatabase);
        assertEquals("migrate", cli.command);
    }

    @Test
    public void falseBooleanParameters() throws Exception {
        String[] args = new String[]{
                "--promptForNonLocalDatabase=false",
                "migrate",
        };

        CommandLineMigrator cli = new CommandLineMigrator();
        cli.parseOptions(args);

        assertEquals(Boolean.FALSE, cli.promptForNonLocalDatabase);
        assertEquals("migrate", cli.command);

    }

    @Test
    public void trueBooleanParameters() throws Exception {
        String[] args = new String[]{
                "--promptForNonLocalDatabase=true",
                "migrate",
        };

        CommandLineMigrator cli = new CommandLineMigrator();
        cli.parseOptions(args);

        assertEquals(Boolean.TRUE, cli.promptForNonLocalDatabase);
        assertEquals("migrate", cli.command);

    }

    @Test(expected = CommandLineParsingException.class)
    public void parameterWithoutDash() throws Exception {
        String[] args = new String[]{
                "promptForNonLocalDatabase=true",
                "migrate",
        };

        CommandLineMigrator cli = new CommandLineMigrator();
        cli.parseOptions(args);
    }

    @Test(expected = CommandLineParsingException.class)
    public void parameterWithoutEquals() throws Exception {
        String[] args = new String[]{
                "--promptForNonLocalDatabase", "true",
                "migrate",
        };

        CommandLineMigrator cli = new CommandLineMigrator();
        cli.parseOptions(args);
    }

    @Test(expected = CommandLineParsingException.class)
    public void unknownParameter() throws Exception {
        String[] args = new String[]{
                "--promptForNonLocalDatabase=true",
                "--badParam=here",
                "migrate",
        };

        CommandLineMigrator cli = new CommandLineMigrator();
        cli.parseOptions(args);
    }

    @Test(expected = CommandLineParsingException.class)
    public void configureNonExistantClassloaderLocation() throws Exception {
        CommandLineMigrator cli = new CommandLineMigrator();
        cli.classpath = "badClasspathLocation";
        cli.configureClassLoader();
    }

    @Test
    public void windowsConfigureClassLoaderLocation() throws Exception {
        CommandLineMigrator cli = new CommandLineMigrator();

        if (cli.isWindows())
        {
          System.setProperty("os.name", "Windows XP");
          cli.classpath = "c:\\;c:\\windows\\";
          cli.applyDefaults();
          cli.configureClassLoader();

          URL[] classloaderURLs = ((URLClassLoader) cli.classLoader).getURLs();
          assertEquals(2, classloaderURLs.length);
          assertEquals("file:/c:/", classloaderURLs[0].toExternalForm());
          assertEquals("file:/c:/windows/", classloaderURLs[1].toExternalForm());
        }
    }

    @Test
    public void unixConfigureClassLoaderLocation() throws Exception {
        CommandLineMigrator cli = new CommandLineMigrator();

        if (!cli.isWindows())
        {
          System.setProperty("os.name", "Linux");
          cli.classpath = "/tmp:/";
          cli.applyDefaults();

          cli.configureClassLoader();

          URL[] classloaderURLs = ((URLClassLoader) cli.classLoader).getURLs();
          assertEquals(2, classloaderURLs.length);
          assertEquals("file:/tmp/", classloaderURLs[0].toExternalForm());
          assertEquals("file:/", classloaderURLs[1].toExternalForm());
        }
    }

    @Test
    public void propertiesFileWithNoOtherArgs() throws Exception {
        CommandLineMigrator cli = new CommandLineMigrator();

        Properties props = new Properties();
        props.setProperty("driver", "DRIVER");
        props.setProperty("username", "USERNAME");
        props.setProperty("password", "PASSWD");
        props.setProperty("url", "URL");
        props.setProperty("changeLogFile", "FILE");
        props.setProperty("classpath", "CLASSPAHT");
        props.setProperty("contexts", "CONTEXTS");
        props.setProperty("promptForNonLocalDatabase", "TRUE");

        ByteArrayOutputStream propFile = new ByteArrayOutputStream();
        props.store(propFile, "");

        cli.parsePropertiesFile(new ByteArrayInputStream(propFile.toByteArray()));

        assertEquals("DRIVER", cli.driver);
        assertEquals("USERNAME", cli.username);
        assertEquals("PASSWD", cli.password);
        assertEquals("URL", cli.url);
        assertEquals("FILE", cli.changeLogFile);
        assertEquals("CLASSPAHT", cli.classpath);
        assertEquals("CONTEXTS", cli.contexts);
        assertEquals(Boolean.TRUE, cli.promptForNonLocalDatabase);

    }

    @Test
    public void propertiesFileWithOtherArgs() throws Exception {
        CommandLineMigrator cli = new CommandLineMigrator();
        cli.username = "PASSED USERNAME";
        cli.password = "PASSED PASSWD";


        Properties props = new Properties();
        props.setProperty("driver", "DRIVER");
        props.setProperty("username", "USERNAME");
        props.setProperty("password", "PASSWD");
        props.setProperty("url", "URL");
        props.setProperty("changeLogFile", "FILE");
        props.setProperty("classpath", "CLASSPAHT");
        props.setProperty("contexts", "CONTEXTS");
        props.setProperty("promptForNonLocalDatabase", "TRUE");

        ByteArrayOutputStream propFile = new ByteArrayOutputStream();
        props.store(propFile, "");

        cli.parsePropertiesFile(new ByteArrayInputStream(propFile.toByteArray()));

        assertEquals("DRIVER", cli.driver);
        assertEquals("PASSED USERNAME", cli.username);
        assertEquals("PASSED PASSWD", cli.password);
        assertEquals("URL", cli.url);
        assertEquals("FILE", cli.changeLogFile);
        assertEquals("CLASSPAHT", cli.classpath);
        assertEquals("CONTEXTS", cli.contexts);
        assertEquals(Boolean.TRUE, cli.promptForNonLocalDatabase);

    }

    @Test
    public void applyDefaults() {
        CommandLineMigrator cli = new CommandLineMigrator();

        cli.promptForNonLocalDatabase = Boolean.TRUE;
        cli.applyDefaults();
        assertEquals(Boolean.TRUE, cli.promptForNonLocalDatabase);

        cli.promptForNonLocalDatabase = Boolean.FALSE;
        cli.applyDefaults();
        assertEquals(Boolean.FALSE, cli.promptForNonLocalDatabase);

        cli.promptForNonLocalDatabase = null;
        cli.applyDefaults();
        assertEquals(Boolean.FALSE, cli.promptForNonLocalDatabase);

    }

    @Test(expected = CommandLineParsingException.class)
    public void propertiesFileWithBadArgs() throws Exception {
        CommandLineMigrator cli = new CommandLineMigrator();

        Properties props = new Properties();
        props.setProperty("driver", "DRIVER");
        props.setProperty("username", "USERNAME");
        props.setProperty("badArg", "ARG");

        ByteArrayOutputStream propFile = new ByteArrayOutputStream();
        props.store(propFile, "");

        cli.parsePropertiesFile(new ByteArrayInputStream(propFile.toByteArray()));
    }

    @Test
    public void checkSetup() {
        CommandLineMigrator cli = new CommandLineMigrator();
        assertFalse(cli.checkSetup());

        cli.driver = "driver";
        cli.username = "username";
        cli.password = "pwd";
        cli.url = "url";
        cli.changeLogFile = "file";
        cli.classpath = "classpath";

        assertFalse(cli.checkSetup());

        cli.command = "BadCommand";
        assertFalse(cli.checkSetup());

        cli.command = "migrate";
        assertTrue(cli.checkSetup());
    }

    @Test
    public void printHelp() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CommandLineMigrator cli = new CommandLineMigrator();
        cli.printHelp(new PrintStream(stream));

        BufferedReader reader = new BufferedReader(new StringReader(new String(stream.toByteArray())));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.length() > 80) {
                fail("'" + line + "' is longer than 80 chars");
            }
        }
    }

    @Test
    public void tag() throws Exception {
        String[] args = new String[]{
                "--driver=DRIVER",
                "--username=USERNAME",
                "--password=PASSWORD",
                "--url=URL",
                "--changeLogFile=FILE",
                "--classpath=CLASSPATH;CLASSPATH2",
                "--contexts=CONTEXT1,CONTEXT2",
                "tag", "TagHere"
        };

        CommandLineMigrator cli = new CommandLineMigrator();
        cli.parseOptions(args);

        assertEquals("DRIVER", cli.driver);
        assertEquals("USERNAME", cli.username);
        assertEquals("PASSWORD", cli.password);
        assertEquals("URL", cli.url);
        assertEquals("FILE", cli.changeLogFile);
        assertEquals("CLASSPATH;CLASSPATH2", cli.classpath);
        assertEquals("CONTEXT1,CONTEXT2", cli.contexts);
        assertEquals("tag", cli.command);
        assertEquals("TagHere", cli.commandParam);
    }

    @Test
    public void migrateWithEqualsInParams() throws Exception {
        String url = "dbc:sqlserver://127.0.0.1;DatabaseName=dev_nn;user=ffdatabase;password=p!88worD";
        String[] args = new String[]{
                "--url=" + url,
                "migrate",
        };

        CommandLineMigrator cli = new CommandLineMigrator();
        cli.parseOptions(args);

        assertEquals(url, cli.url);
    }
}
