package liquibase.integration.commandline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Properties;

import liquibase.exception.CommandLineParsingException;
import liquibase.util.StringUtils;

import org.junit.Test;


/**
 * Tests for {@link Main}
 */
public class MainTest {

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
                "update",
        };

        Main cli = new Main();
        cli.parseOptions(args);

        assertEquals("DRIVER", cli.driver);
        assertEquals("USERNAME", cli.username);
        assertEquals("PASSWORD", cli.password);
        assertEquals("URL", cli.url);
        assertEquals("FILE", cli.changeLogFile);
        assertEquals("CLASSPATH;CLASSPATH2", cli.classpath);
        assertEquals("CONTEXT1,CONTEXT2", cli.contexts);
        assertEquals(Boolean.TRUE, cli.promptForNonLocalDatabase);
        assertEquals("update", cli.command);
    }

    @Test
    public void falseBooleanParameters() throws Exception {
        String[] args = new String[]{
                "--promptForNonLocalDatabase=false",
                "update",
        };

        Main cli = new Main();
        cli.parseOptions(args);

        assertEquals(Boolean.FALSE, cli.promptForNonLocalDatabase);
        assertEquals("update", cli.command);

    }

    @Test
    public void convertMigrateToUpdate() throws Exception {
        String[] args = new String[]{
                "--promptForNonLocalDatabase=false",
                "migrate",
        };

        Main cli = new Main();
        cli.parseOptions(args);

        assertEquals("update", cli.command);

    }

    @Test
    public void trueBooleanParameters() throws Exception {
        String[] args = new String[]{
                "--promptForNonLocalDatabase=true",
                "update",
        };

        Main cli = new Main();
        cli.parseOptions(args);

        assertEquals(Boolean.TRUE, cli.promptForNonLocalDatabase);
        assertEquals("update", cli.command);

    }

    @Test(expected = CommandLineParsingException.class)
    public void parameterWithoutDash() throws Exception {
        String[] args = new String[]{
                "promptForNonLocalDatabase=true",
                "update",
        };

        Main cli = new Main();
        cli.parseOptions(args);
    }

    @Test(expected = CommandLineParsingException.class)
    public void unknownParameter() throws Exception {
        String[] args = new String[]{
                "--promptForNonLocalDatabase=true",
                "--badParam=here",
                "migrate",
        };

        Main cli = new Main();
        cli.parseOptions(args);
    }

    @Test(expected = CommandLineParsingException.class)
    public void configureNonExistantClassloaderLocation() throws Exception {
        Main cli = new Main();
        cli.classpath = "badClasspathLocation";
        cli.configureClassLoader();
    }

    @Test
    public void windowsConfigureClassLoaderLocation() throws Exception {
        Main cli = new Main();

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
        Main cli = new Main();

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
        Main cli = new Main();

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
        Main cli = new Main();
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
    public void propertiesFileParsingShouldIgnoreUnknownArgumentsIfStrictFalseIsInFile() throws Exception {
        Main cli = new Main();

        Properties props = new Properties();
        props.setProperty("driver", "DRIVER");
        props.setProperty("unknown.property", "UnknownValue");
        props.setProperty("strict", "false");

        ByteArrayOutputStream propFile = new ByteArrayOutputStream();
        props.store(propFile, "");

        cli.parsePropertiesFile(new ByteArrayInputStream(propFile.toByteArray()));

        assertEquals("DRIVER", cli.driver);

    }

    @Test
    public void propertiesFileParsingShouldIgnoreUnknownArgumentsIfStrictModeIsFalse() throws Exception {
        Main cli = new Main();
        String[] args = new String[]{"--strict=false"};

        cli.parseOptions(args);
        Properties props = new Properties();
        props.setProperty("driver", "DRIVER");
        props.setProperty("unknown.property", "UnknownValue");

        ByteArrayOutputStream propFile = new ByteArrayOutputStream();
        props.store(propFile, "");

        cli.parsePropertiesFile(new ByteArrayInputStream(propFile.toByteArray()));

        assertEquals("DRIVER", cli.driver);

    }

    @Test(expected = CommandLineParsingException.class)
    public void propertiesFileParsingShouldFailOnUnknownArgumentsIfStrictMode() throws Exception {
        Main cli = new Main();

        Properties props = new Properties();
        props.setProperty("driver", "DRIVER");
        props.setProperty("unknown.property", "UnknownValue");
        props.setProperty("strict", "true");

        ByteArrayOutputStream propFile = new ByteArrayOutputStream();
        props.store(propFile, "");

        cli.parsePropertiesFile(new ByteArrayInputStream(propFile.toByteArray()));

    }

    @Test
    public void applyDefaults() {
        Main cli = new Main();

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
        Main cli = new Main();

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
        Main cli = new Main();
        assertTrue(cli.checkSetup().size() > 0);

        cli.driver = "driver";
        cli.username = "username";
        cli.password = "pwd";
        cli.url = "url";
        cli.changeLogFile = "file";
        cli.classpath = "classpath";

        assertTrue(cli.checkSetup().size() > 0);

        cli.command = "BadCommand";
        assertTrue(cli.checkSetup().size() > 0);

        cli.command = "migrate";
        assertEquals(0, cli.checkSetup().size());

        String[] noArgCommand = { "migrate", "migrateSQL", "update", "updateSQL",
                "futureRollbackSQL", "updateTestingRollback", "listLocks",
                "dropAll", "releaseLocks", "validate", "help",
                "clearCheckSums", "changelogSync", "changelogSyncSQL",
                "markNextChangeSetRan", "markNextChangeSetRanSQL"
        };

        cli.commandParams.clear();
        cli.commandParams.add("--logLevel=debug");

        // verify unexpected parameter
        for(int i=0; i<noArgCommand.length; i++) {
            cli.command = noArgCommand[i];
            assertEquals(1, cli.checkSetup().size());
        }
        
        // test update cmd with -D parameter
        cli.command = "update";
        cli.commandParams.clear();
        cli.changeLogParameters.clear();
        cli.changeLogParameters.put("engine", "myisam");
        assertEquals(0, cli.checkSetup().size());
        
        // verify normal case - comand w/o command parameters
        cli.commandParams.clear();
        for(int i=0; i<noArgCommand.length; i++) {
            cli.command = noArgCommand[i];
            assertEquals(0, cli.checkSetup().size());
        }
        
        String[] singleArgCommand = { "updateCount", "updateCountSQL",
                "rollback", "rollbackToDate", "rollbackCount",
                "rollbackSQL", "rollbackToDateSQL", "rollbackCountSQL",
                "tag", "dbDoc"
        };
        
        // verify unexpected parameter for single arg commands
        cli.commandParams.add("--logLevel=debug");
        for(int i=0; i<singleArgCommand.length; i++) {
            cli.command = singleArgCommand[i];
            assertEquals(1, cli.checkSetup().size());
        }
        
        // verify normal case - comand with string command parameter
        cli.commandParams.clear();
        cli.commandParams.add("someCommandValue");
        for(int i=0; i<singleArgCommand.length; i++) {
            cli.command = singleArgCommand[i];
            assertEquals(0, cli.checkSetup().size());
        }
            
        // status w/o parameter
        cli.command = "status";
        cli.commandParams.clear();
        assertEquals(0, cli.checkSetup().size());
        
        // status w/--verbose
        cli.commandParams.add("--verbose");
        assertEquals(0, cli.checkSetup().size());
       
        cli.commandParams.clear();
        cli.commandParams.add("--logLevel=debug");
        assertEquals(1, cli.checkSetup().size());
        
        String[] multiArgCommand = { "diff", "diffChangeLog" };
        
        //first verify diff cmds w/o args 
        cli.commandParams.clear();
        for(int i=0; i<multiArgCommand.length; i++) {
            cli.command = multiArgCommand[i];
            assertEquals(0, cli.checkSetup().size());
        }
       
        // next verify with all parms
        String[] cmdParms = { "--referenceUsername=USERNAME", "--referencePassword=PASSWORD", 
                "--referenceUrl=URL", "--referenceDriver=DRIVER"};
        // load all parms 
        for (String param : cmdParms) {
            cli.commandParams.add(param);
        }
        assertEquals(0, cli.checkSetup().size());
        
        // now add an unexpected parm
        cli.commandParams.add("--logLevel=debug");
        assertEquals(1, cli.checkSetup().size());
    }

    @Test
    public void printHelp() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Main cli = new Main();
        cli.printHelp(new PrintStream(stream));

        BufferedReader reader = new BufferedReader(new StringReader(new String(stream.toByteArray())));
        String line;
        while ((line = reader.readLine()) != null) {
            //noinspection MagicNumber
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

        Main cli = new Main();
        cli.parseOptions(args);

        assertEquals("DRIVER", cli.driver);
        assertEquals("USERNAME", cli.username);
        assertEquals("PASSWORD", cli.password);
        assertEquals("URL", cli.url);
        assertEquals("FILE", cli.changeLogFile);
        assertEquals("CLASSPATH;CLASSPATH2", cli.classpath);
        assertEquals("CONTEXT1,CONTEXT2", cli.contexts);
        assertEquals("tag", cli.command);
        assertEquals("TagHere", cli.commandParams.iterator().next());
    }

    @Test
    public void migrateWithEqualsInParams() throws Exception {
        String url = "dbc:sqlserver://127.0.0.1;DatabaseName=dev_nn;user=ffdatabase;password=p!88worD";
        String[] args = new String[]{
                "--url=" + url,
                "migrate",
        };

        Main cli = new Main();
        cli.parseOptions(args);

        assertEquals(url, cli.url);
    }
    
    @Test
    public void fixArgs() {
        Main liquibase = new Main();
        String[] fixedArgs = liquibase.fixupArgs(new String[] {"--defaultsFile","liquibase.properties", "migrate"});
        assertEquals("--defaultsFile=liquibase.properties migrate", StringUtils.join(Arrays.asList(fixedArgs), " "));

        fixedArgs = liquibase.fixupArgs(new String[] {"--defaultsFile=liquibase.properties", "migrate"});
        assertEquals("--defaultsFile=liquibase.properties migrate", StringUtils.join(Arrays.asList(fixedArgs), " "));

        fixedArgs = liquibase.fixupArgs(new String[] {"--driver=DRIVER",
                "--username=USERNAME",
                "--password=PASSWORD",
                "--url=URL",
                "--changeLogFile=FILE",
                "--classpath=CLASSPATH;CLASSPATH2",
                "--contexts=CONTEXT1,CONTEXT2",
                "--promptForNonLocalDatabase=true",
                "migrate"
        });
        assertEquals("--driver=DRIVER --username=USERNAME --password=PASSWORD --url=URL --changeLogFile=FILE --classpath=CLASSPATH;CLASSPATH2 --contexts=CONTEXT1,CONTEXT2 --promptForNonLocalDatabase=true migrate", StringUtils.join(Arrays.asList(fixedArgs), " "));
    }

    @Test
    public void testVersionArg() throws Exception {
        Main.run(new String[] {"--version"});

    }

	@Test
	public void testSplitArgWithValueEndingByEqualSing() throws CommandLineParsingException {
		final String argName = "password";
		final String argValue = "s3-cr3t=";
		Main tested = new Main();

		tested.parseOptions(new String[] { "--" + argName + "=" + argValue });

		assertEquals(argValue, tested.password);
	}
}
