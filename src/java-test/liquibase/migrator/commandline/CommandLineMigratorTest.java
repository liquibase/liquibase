package liquibase.migrator.commandline;

import junit.framework.TestCase;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

public class CommandLineMigratorTest extends TestCase {

    public void testMigrateWithAllParameters() throws Exception {
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

    public void testFalseBooleanParameters() throws Exception {
        String[] args = new String[]{
                "--promptForNonLocalDatabase=false",
                "migrate",
        };

        CommandLineMigrator cli = new CommandLineMigrator();
        cli.parseOptions(args);

        assertEquals(Boolean.FALSE, cli.promptForNonLocalDatabase);
        assertEquals("migrate", cli.command);

    }

    public void testTrueBooleanParameters() throws Exception {
        String[] args = new String[]{
                "--promptForNonLocalDatabase=true",
                "migrate",
        };

        CommandLineMigrator cli = new CommandLineMigrator();
        cli.parseOptions(args);

        assertEquals(Boolean.TRUE, cli.promptForNonLocalDatabase);
        assertEquals("migrate", cli.command);

    }

    public void testParameterWithoutDash() throws Exception {
        String[] args = new String[]{
                "promptForNonLocalDatabase=true",
                "migrate",
        };

        CommandLineMigrator cli = new CommandLineMigrator();
        try {
            cli.parseOptions(args);
            fail("Should have thrown an exception");
        } catch (CommandLineParsingException e) {
            assertEquals("Parameters must start with a '--'", e.getMessage());
        }

    }

    public void testParameterWithoutEquals() throws Exception {
        String[] args = new String[]{
                "--promptForNonLocalDatabase", "true",
                "migrate",
        };

        CommandLineMigrator cli = new CommandLineMigrator();
        try {
            cli.parseOptions(args);
            fail("Should have thrown an exception");
        } catch (CommandLineParsingException e) {
            assertEquals("Could not parse '--promptForNonLocalDatabase'", e.getMessage());
        }
    }

    public void testUnknownParameter() throws Exception {
        String[] args = new String[]{
                "--promptForNonLocalDatabase=true",
                "--badParam=here",
                "migrate",
        };

        CommandLineMigrator cli = new CommandLineMigrator();
        try {
            cli.parseOptions(args);
            fail("Should have thrown an exception");
        } catch (CommandLineParsingException e) {
            assertEquals("Unknown parameter: 'badParam'", e.getMessage());
        }
    }

    public void testConfigureNonExistantClassloaderLocation() throws Exception {
        CommandLineMigrator cli = new CommandLineMigrator();
        cli.classpath = "badClasspathLocation";

        try {
            cli.configureClassLoader();
            fail("Should have thrown an exception");
        } catch (CommandLineParsingException e) {
            assertTrue("Did not find message in " + e.getMessage(), e.getMessage().contains("badClasspathLocation does not exist"));
        }
    }

    public void testWindowsConfigureClassLoaderLocation() throws Exception {
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

    public void testUNIXConfigureClassLoaderLocation() throws Exception {
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


    public void testPropertiesFileWithNoOtherArgs() throws Exception {
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

    public void testPropertiesFileWithOtherArgs() throws Exception {
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

    public void testApplyDefaults() {
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

    public void testPropertiesFileWithBadArgs() throws Exception {
        CommandLineMigrator cli = new CommandLineMigrator();

        Properties props = new Properties();
        props.setProperty("driver", "DRIVER");
        props.setProperty("username", "USERNAME");
        props.setProperty("badArg", "ARG");

        ByteArrayOutputStream propFile = new ByteArrayOutputStream();
        props.store(propFile, "");

        try {
            cli.parsePropertiesFile(new ByteArrayInputStream(propFile.toByteArray()));
            fail("Should have thrown an exception");
        } catch (CommandLineParsingException e) {
            assertEquals("Unknown parameter: 'badArg'", e.getMessage());
        }
    }

    public void testCheckSetup() {
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

    public void testPrintHelp() throws Exception {
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

    public void testTag() throws Exception {
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

    public void testMigrateWithEqualsInParams() throws Exception {
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
