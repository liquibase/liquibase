package liquibase.commandline;

import liquibase.CompositeFileOpener;
import liquibase.FileSystemFileOpener;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.diff.Diff;
import liquibase.diff.DiffResult;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.CommandLineParsingException;
import liquibase.exception.JDBCException;
import liquibase.exception.ValidationFailedException;
import liquibase.lock.LockHandler;
import liquibase.log.LogFactory;
import liquibase.migrator.Migrator;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for executing LiquiBase via the command line.
 */
public class CommandLineMigrator {
    protected ClassLoader classLoader;

    protected String driver;
    protected String username;
    protected String password;
    protected String url;
    protected String databaseClass;
    protected String changeLogFile;
    protected String classpath;
    protected String contexts;
    protected Boolean promptForNonLocalDatabase = null;
    protected Boolean includeSystemClasspath;
    protected String defaultsFile = "liquibase.properties";

    protected String currentDateTimeFunction;

    protected String command;
    protected Set<String> commandParams = new HashSet<String>();

    protected String logLevel;


    public static void main(String args[]) throws CommandLineParsingException, IOException {
        String shouldRunProperty = System.getProperty(Migrator.SHOULD_RUN_SYSTEM_PROPERTY);
        if (shouldRunProperty != null && !Boolean.valueOf(shouldRunProperty)) {
            System.out.println("Migrator did not run because '" + Migrator.SHOULD_RUN_SYSTEM_PROPERTY + "' system property was set to false");
            return;
        }

        CommandLineMigrator commandLineMigrator = new CommandLineMigrator();
        if (args.length == 1 && "--help".equals(args[0])) {
            commandLineMigrator.printHelp(System.out);
            return;
        } else if (args.length == 1 && "--version".equals(args[0])) {
            System.out.println("LiquiBase Version: " + LiquibaseUtil.getBuildVersion() + StreamUtil.getLineSeparator());
            return;
        }

        commandLineMigrator.parseOptions(args);

        File propertiesFile = new File(commandLineMigrator.defaultsFile);

        if (propertiesFile.exists()) {
            commandLineMigrator.parsePropertiesFile(new FileInputStream(propertiesFile));
        }
        List<String> setupMessages = commandLineMigrator.checkSetup();
        if (setupMessages.size() > 0) {
            commandLineMigrator.printHelp(setupMessages, System.out);
            return;
        }

        try {
            commandLineMigrator.applyDefaults();
            commandLineMigrator.configureClassLoader();
            commandLineMigrator.doMigration();
        } catch (Throwable e) {
            String message = e.getMessage();
            if (e.getCause() != null) {
                message = e.getCause().getMessage();
            }
            if (message == null) {
                message = "Unknown Reason";
            }

            if (e.getCause() instanceof ValidationFailedException) {
                ((ValidationFailedException) e.getCause()).printDescriptiveError(System.out);
            } else {
                System.out.println("Migration Failed: " + message + generateLogLevelWarningMessage());
                LogFactory.getLogger().log(Level.SEVERE, message, e);
            }
            return;
        }

        if ("update".equals(commandLineMigrator.command)) {
            System.out.println("Migration successful");
        } else if (commandLineMigrator.command.startsWith("rollback") && !commandLineMigrator.command.endsWith("SQL")) {
            System.out.println("Rollback successful");
        }
    }

    private static String generateLogLevelWarningMessage() {
        Logger logger = LogFactory.getLogger();
        if (logger == null || logger.getLevel() == null || (logger.getLevel().equals(Level.OFF))) {
            return "";
        } else {
            return ".  For more information, use the --logLevel flag)";
        }
    }

    /**
     * On windows machines, it splits args on '=' signs.  Put it back like it was.
     */
    protected String[] fixupArgs(String[] args) {
        List<String> fixedArgs = new ArrayList<String>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--") && !arg.contains("=")) {
                String nextArg = null;
                if (i+1 < args.length) {
                    nextArg = args[i + 1];
                }
                if (nextArg != null && !nextArg.startsWith("--") && !isCommand(nextArg)) {
                    arg = arg + "=" + nextArg;
                    i++;
                }
            }
            fixedArgs.add(arg);
        }

        return fixedArgs.toArray(new String[fixedArgs.size()]);
    }

    protected List<String> checkSetup() {
        List<String> messages = new ArrayList<String>();
        if (command == null) {
            messages.add("Command not passed");
        } else if (!isCommand(command)) {
            messages.add("Unknown command: " + command);
        } else {
            if (username == null) {
                messages.add("--username is required");
            }
            if (url == null) {
                messages.add("--url is required");
            }

            if (isChangeLogRequired(command) && changeLogFile == null) {
                messages.add("--changeLog is required");
            }
        }
        return messages;
    }

    private boolean isChangeLogRequired(String command) {
        return command.toLowerCase().startsWith("update")
                || command.toLowerCase().startsWith("rollback")
                || "validate".equals(command);
    }

    private boolean isCommand(String arg) {
        return "migrate".equals(arg)
                || "migrateSQL".equalsIgnoreCase(arg)
                || "update".equalsIgnoreCase(arg)
                || "updateSQL".equalsIgnoreCase(arg)
                || "updateCount".equalsIgnoreCase(arg)
                || "updateCountSQL".equalsIgnoreCase(arg)
                || "rollback".equalsIgnoreCase(arg)
                || "rollbackToDate".equalsIgnoreCase(arg)
                || "rollbackCount".equalsIgnoreCase(arg)
                || "rollbackSQL".equalsIgnoreCase(arg)
                || "rollbackToDateSQL".equalsIgnoreCase(arg)
                || "rollbackCountSQL".equalsIgnoreCase(arg)
                || "futureRollbackSQL".equalsIgnoreCase(arg)
                || "tag".equalsIgnoreCase(arg)
                || "listLocks".equalsIgnoreCase(arg)
                || "dropAll".equalsIgnoreCase(arg)
                || "releaseLocks".equalsIgnoreCase(arg)
                || "status".equalsIgnoreCase(arg)
                || "validate".equalsIgnoreCase(arg)
                || "help".equalsIgnoreCase(arg)
                || "diff".equalsIgnoreCase(arg)
                || "diffChangeLog".equalsIgnoreCase(arg)
                || "generateChangeLog".equalsIgnoreCase(arg)
                || "clearCheckSums".equalsIgnoreCase(arg)
                || "dbDoc".equalsIgnoreCase(arg)
                || "changelogSync".equalsIgnoreCase(arg)
                || "changelogSyncSQL".equalsIgnoreCase(arg);
    }

    protected void parsePropertiesFile(InputStream propertiesInputStream) throws IOException, CommandLineParsingException {
        Properties props = new Properties();
        props.load(propertiesInputStream);

        for (Map.Entry entry : props.entrySet()) {
            try {
                Field field = getClass().getDeclaredField((String) entry.getKey());
                Object currentValue = field.get(this);

                if (currentValue == null) {
                    String value = entry.getValue().toString().trim();
                    if (field.getType().equals(Boolean.class)) {
                        field.set(this, Boolean.valueOf(value));
                    } else {
                        field.set(this, value);
                    }
                }
            } catch (Exception e) {
                throw new CommandLineParsingException("Unknown parameter: '" + entry.getKey() + "'");
            }
        }
    }

    protected void printHelp(List<String> errorMessages, PrintStream stream) {
        stream.println("Errors:");
        for (String message : errorMessages) {
            stream.println("  " + message);
        }
        stream.println();
        printHelp(stream);
    }

    protected void printHelp(PrintStream stream) {
        stream.println("Usage: java -jar liquibase.jar [options] [command]");
        stream.println("");
        stream.println("Standard Commands:");
        stream.println(" update                         Updates database to current version");
        stream.println(" updateSQL                      Writes SQL to update database to current");
        stream.println("                                version to STDOUT");
        stream.println(" updateCount <num>              Applies next NUM changes to the  database");
        stream.println(" updateSQL <num>                Writes SQL to apply next NUM changes the database");
        stream.println(" rollback <tag>                 Rolls back the database to the the state is was");
        stream.println("                                when the tag was applied");
        stream.println(" rollbackSQL <tag>              Writes SQL to roll back the database to that");
        stream.println("                                state it was in when the tag was applied");
        stream.println("                                to STDOUT");
        stream.println(" rollbackToDate <date/time>     Rolls back the database to the the state is was");
        stream.println("                                at the given date/time.");
        stream.println("                                Date Format: yyyy-MM-dd HH:mm:ss");
        stream.println(" rollbackToDateSQL <date/time>  Writes SQL to roll back the database to that");
        stream.println("                                state it was in at the given date/time version");
        stream.println("                                to STDOUT");
        stream.println(" rollbackCount <value>          Rolls back the last <value> change sets");
        stream.println("                                applied to the database");
        stream.println(" rollbackCountSQL <value>       Writes SQL to roll back the last");
        stream.println("                                <value> change sets to STDOUT");
        stream.println("                                applied to the database");
        stream.println(" futureRollbackSQL              Writes SQL to roll back the database to the ");
        stream.println("                                current state after the changes in the ");
        stream.println("                                changeslog have been applied");
        stream.println(" generateChangeLog              Writes Change Log XML to copy the current state");
        stream.println("                                of the database to standard out");
        stream.println("");
        stream.println("Diff Commands");
        stream.println(" diff [diff parameters]          Writes description of differences");
        stream.println("                                 to standard out");
        stream.println(" diffChangeLog [diff parameters] Writes Change Log XML to update");
        stream.println("                                 the base database");
        stream.println("                                 to the target database to standard out");
        stream.println("");
        stream.println("Documentation Commands");
        stream.println(" dbDoc <outputDirectory>         Generates Javadoc-like documentation");
        stream.println("                                 based on current database and change log");
        stream.println("");
        stream.println("Maintenance Commands");
        stream.println(" tag <tag string>          'Tags' the current database state for future rollback");
        stream.println(" status [--verbose]        Outputs count (list if --verbose) of unrun changesets");
        stream.println(" validate                  Checks changelog for errors");
        stream.println(" clearCheckSums            Removes all saved checksums from database log.");
        stream.println("                           Useful for 'MD5Sum Check Failed' errors");
        stream.println(" changelogSync             Mark all changes as executed in the database");
        stream.println(" changelogSyncSQL          Writes SQL to mark all changes as executed ");
        stream.println("                           in the database to STDOUT");
        stream.println(" listLocks                 Lists who currently has locks on the");
        stream.println("                           database changelog");
        stream.println(" releaseLocks              Releases all locks on the database changelog");
        stream.println(" dropAll                   Drop all database objects owned by user");
        stream.println("");
        stream.println("Required Parameters:");
        stream.println(" --changeLogFile=<path and filename>        Migration file");
        stream.println(" --username=<value>                         Database username");
        stream.println(" --password=<value>                         Database password");
        stream.println(" --url=<value>                              Database URL");
        stream.println("");
        stream.println("Optional Parameters:");
        stream.println(" --classpath=<value>                        Classpath containing");
        stream.println("                                            migration files and JDBC Driver");
        stream.println(" --driver=<jdbc.driver.ClassName>           Database driver class name");
        stream.println(" --databaseClass=<database.ClassName>       custom liquibase.database.Database");
        stream.println("                                            implementation to use");
        stream.println(" --contexts=<value>                         ChangeSet contexts to execute");
        stream.println(" --defaultsFile=</path/to/file.properties>  File with default option values");
        stream.println("                                            (default: ./liquibase.properties)");
        stream.println(" --includeSystemClasspath=<true|false>      Include the system classpath");
        stream.println("                                            in the LiquiBase classpath");
        stream.println("                                            (default: true)");
        stream.println(" --promptForNonLocalDatabase=<true|false>   Prompt if non-localhost");
        stream.println("                                            databases (default: false)");
        stream.println(" --logLevel=<level>                         Execution log level");
        stream.println("                                            (finest, finer, fine, info,");
        stream.println("                                            warning, severe)");
        stream.println(" --currentDateTimeFunction=<value>          Overrides current date time function");
        stream.println("                                            used in SQL.");
        stream.println("                                            Useful for unsupported databases");
        stream.println(" --help                                     Prints this message");
        stream.println(" --version                                  Prints this version information");
        stream.println("");
        stream.println("Required Diff Parameters:");
        stream.println(" --baseUsername=<value>                     Base Database username");
        stream.println(" --basePassword=<value>                     Base Database password");
        stream.println(" --baseUrl=<value>                          Base Database URL");
        stream.println("");
        stream.println("Optional Diff Parameters:");
        stream.println(" --baseDriver=<jdbc.driver.ClassName>       Base Database driver class name");
        stream.println("");
        stream.println("Default value for parameters can be stored in a file called");
        stream.println("'liquibase.properties' that is read from the current working directory.");
        stream.println("");
        stream.println("Full documentation is available at");
        stream.println("http://www.liquibase.org/manual/command_line");
        stream.println("");
    }

    public CommandLineMigrator() {
//        options = createOptions();
    }

    protected void parseOptions(String[] args) throws CommandLineParsingException {
        args = fixupArgs(args);

        boolean seenCommand = false;
        for (String arg : args) {
            if (isCommand(arg)) {
                this.command = arg;
                if (this.command.equalsIgnoreCase("migrate")) {
                    this.command = "update";
                } else if (this.command.equalsIgnoreCase("migrateSQL")) {
                        this.command = "updateSQL";
                }
                seenCommand = true;
            } else if (seenCommand) {
                commandParams.add(arg);
            } else if (arg.startsWith("--")) {
                String[] splitArg = splitArg(arg);

                String attributeName = splitArg[0];
                String value = splitArg[1];

                try {
                    Field field = getClass().getDeclaredField(attributeName);
                    if (field.getType().equals(Boolean.class)) {
                        field.set(this, Boolean.valueOf(value));
                    } else {
                        field.set(this, value);
                    }
                } catch (Exception e) {
                    throw new CommandLineParsingException("Unknown parameter: '" + attributeName + "'");
                }
            } else {
                throw new CommandLineParsingException("Parameters must start with a '--'");
            }
        }

    }

    private String[] splitArg(String arg) throws CommandLineParsingException {
        String[] splitArg = arg.split("=");
        if (splitArg.length < 2) {
            throw new CommandLineParsingException("Could not parse '" + arg + "'");
        } else if (splitArg.length > 2) {
            StringBuffer secondHalf = new StringBuffer();
            for (int j = 1; j < splitArg.length; j++) {
                secondHalf.append(splitArg[j]).append("=");
            }

            splitArg = new String[]{
                    splitArg[0],
                    secondHalf.toString().replaceFirst("=$", "")
            };
        }

        splitArg[0] = splitArg[0].replaceFirst("--", "");
        return splitArg;
    }

    protected void applyDefaults() {
        if (this.promptForNonLocalDatabase == null) {
            this.promptForNonLocalDatabase = Boolean.FALSE;
        }
        if (this.logLevel == null) {
            this.logLevel = "off";
        }
        if (this.includeSystemClasspath == null) {
            this.includeSystemClasspath = Boolean.TRUE;
        }

    }

    protected void configureClassLoader() throws CommandLineParsingException {
        final List<URL> urls = new ArrayList<URL>();
        if (this.classpath != null) {
            String[] classpath;
            if (isWindows()) {
                classpath = this.classpath.split(";");
            } else {
                classpath = this.classpath.split(":");
            }

            for (String classpathEntry : classpath) {
                File classPathFile = new File(classpathEntry);
                if (!classPathFile.exists()) {
                    throw new CommandLineParsingException(classPathFile.getAbsolutePath() + " does not exist");
                }
                try {
                    if (classpathEntry.endsWith(".war")) {
                        addWarFileClasspathEntries(classPathFile, urls);
                    } else if (classpathEntry.endsWith(".ear")) {
                        JarFile earZip = new JarFile(classPathFile);

                        Enumeration<? extends JarEntry> entries = earZip.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            if (entry.getName().toLowerCase().endsWith(".jar")) {
                                File jar = extract(earZip, entry);
                                urls.add(new URL("jar:" + jar.toURL() + "!/"));
                                jar.deleteOnExit();
                            } else if (entry.getName().toLowerCase().endsWith("war")) {
                                File warFile = extract(earZip, entry);
                                addWarFileClasspathEntries(warFile, urls);
                            }
                        }

                    } else {
                        urls.add(new File(classpathEntry).toURL());
                    }
                } catch (Exception e) {
                    throw new CommandLineParsingException(e);
                }
            }
        }
        if (includeSystemClasspath) {
            classLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
                public URLClassLoader run() {
                    return new URLClassLoader(urls.toArray(new URL[urls.size()]), Thread.currentThread().getContextClassLoader());
                }
            });

        } else {
            classLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
                public URLClassLoader run() {
                    return new URLClassLoader(urls.toArray(new URL[urls.size()]));
                }
            });
        }
    }

    private void addWarFileClasspathEntries(File classPathFile, List<URL> urls) throws IOException {
        URL url = new URL("jar:" + classPathFile.toURL() + "!/WEB-INF/classes/");
        urls.add(url);
        JarFile warZip = new JarFile(classPathFile);
        Enumeration<? extends JarEntry> entries = warZip.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().startsWith("WEB-INF/lib")
                    && entry.getName().toLowerCase().endsWith(".jar")) {
                File jar = extract(warZip, entry);
                urls.add(new URL("jar:" + jar.toURL() + "!/"));
                jar.deleteOnExit();
            }
        }
    }


    private File extract(JarFile jar, JarEntry entry) throws IOException {
        // expand to temp dir and add to list
        File tempFile = File.createTempFile("migrator.tmp", null);
        // read from jar and write to the tempJar file
        BufferedInputStream inStream = null;

        BufferedOutputStream outStream = null;
        try {
            inStream = new BufferedInputStream(jar.getInputStream(entry));
            outStream = new BufferedOutputStream(
                    new FileOutputStream(tempFile));
            int status;
            while ((status = inStream.read()) != -1) {
                outStream.write(status);
            }
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException ioe) {
                    ;
                }
            }
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException ioe) {
                    ;
                }
            }
        }

        return tempFile;
    }

    protected void doMigration() throws Exception {
        if ("help".equalsIgnoreCase(command)) {
            printHelp(System.out);
            return;
        }

        if ("finest".equalsIgnoreCase(logLevel)) {
            LogFactory.getLogger().setLevel(Level.FINEST);
        } else if ("finer".equalsIgnoreCase(logLevel)) {
            LogFactory.getLogger().setLevel(Level.FINER);
        } else if ("fine".equalsIgnoreCase(logLevel)) {
            LogFactory.getLogger().setLevel(Level.FINE);
        } else if ("info".equalsIgnoreCase(logLevel)) {
            LogFactory.getLogger().setLevel(Level.INFO);
        } else if ("warning".equalsIgnoreCase(logLevel)) {
            LogFactory.getLogger().setLevel(Level.WARNING);
        } else if ("severe".equalsIgnoreCase(logLevel)) {
            LogFactory.getLogger().setLevel(Level.SEVERE);
        } else if ("off".equalsIgnoreCase(logLevel)) {
            LogFactory.getLogger().setLevel(Level.OFF);
        } else {
            throw new CommandLineParsingException("Unknown log level: " + logLevel);
        }

        FileSystemFileOpener fsOpener = new FileSystemFileOpener();
        CommandLineFileOpener clOpener = new CommandLineFileOpener(classLoader);
        Driver driver;
        DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
        if (this.databaseClass != null) {
            databaseFactory.addDatabaseImplementation((Database) Class.forName(this.databaseClass, true, classLoader).newInstance());
        }

        try {
            if (this.driver == null) {
                this.driver = databaseFactory.findDefaultDriver(url);
            }

            if (this.driver == null) {
                throw new RuntimeException("Driver class was not specified and could not be determined from the url");
            }

            driver = (Driver) Class.forName(this.driver, true, classLoader).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot find database driver: " + e.getMessage());
        }
        Properties info = new Properties();
        info.put("user", username);
        if (password != null) {
            info.put("password", password);
        }

        Connection connection = driver.connect(url, info);
        if (connection == null) {
            throw new JDBCException("Connection could not be created to " + url + " with driver " + driver.getClass().getName() + ".  Possibly the wrong driver for the given database URL");
        }


        if ("diff".equalsIgnoreCase(command)) {
            doDiff(connection, createConnectionFromCommandParams(commandParams));
            return;
        } else if ("diffChangeLog".equalsIgnoreCase(command)) {
            doDiffToChangeLog(connection, createConnectionFromCommandParams(commandParams));
            return;
        } else if ("generateChangeLog".equalsIgnoreCase(command)) {
            doGenerateChangeLog(connection);
            return;
        }

        try {
            Database database = databaseFactory.findCorrectDatabaseImplementation(connection);
            Migrator migrator = new Migrator(changeLogFile, new CompositeFileOpener(fsOpener, clOpener), database);

            if ("listLocks".equalsIgnoreCase(command)) {
                migrator.reportLocks(System.out);
                return;
            } else if ("releaseLocks".equalsIgnoreCase(command)) {
                LockHandler.getInstance(database).forceReleaseLock();
                System.out.println("Successfully released all database change log locks for " + migrator.getDatabase().getConnectionUsername() + "@" + migrator.getDatabase().getConnectionURL());
                return;
            } else if ("tag".equalsIgnoreCase(command)) {
                migrator.tag(commandParams.iterator().next());
                System.out.println("Successfully tagged " + migrator.getDatabase().getConnectionUsername() + "@" + migrator.getDatabase().getConnectionURL());
                return;
            } else if ("dropAll".equals(command)) {
                migrator.dropAll();
                System.out.println("All objects dropped from " + migrator.getDatabase().getConnectionUsername() + "@" + migrator.getDatabase().getConnectionURL());
                return;
            } else if ("status".equalsIgnoreCase(command)) {
                boolean runVerbose = false;

                if (commandParams.contains("--verbose")) {
                    runVerbose = true;
                }
                migrator.reportStatus(runVerbose, contexts, getOutputWriter());
                return;
            } else if ("validate".equalsIgnoreCase(command)) {
                try {
                    migrator.validate();
                } catch (ValidationFailedException e) {
                    e.printDescriptiveError(System.out);
                    return;
                }
                System.out.println("No validation errors found");
                return;
            } else if ("clearCheckSums".equalsIgnoreCase(command)) {
                migrator.clearCheckSums();
                return;
            } else if ("dbdoc".equalsIgnoreCase(command)) {
                if (commandParams.size() == 0) {
                    throw new CommandLineParsingException("dbdoc requires an output directory");
                }
                if (changeLogFile == null) {
                    throw new CommandLineParsingException("dbdoc requires a changeLog parameter");
                }
                migrator.generateDocumentation(commandParams.iterator().next());
                return;
            }

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                if ("update".equalsIgnoreCase(command)) {
                    migrator.update(contexts);
                } else if ("changelogSync".equalsIgnoreCase(command)) {
                    migrator.changeLogSync(contexts);
                } else if ("changelogSyncSQL".equalsIgnoreCase(command)) {
                    migrator.changeLogSync(contexts, getOutputWriter());
                } else if ("updateCount".equalsIgnoreCase(command)) {
                    migrator.update(Integer.parseInt(commandParams.iterator().next()), contexts);
                } else if ("updateCountSQL".equalsIgnoreCase(command)) {
                    migrator.update(Integer.parseInt(commandParams.iterator().next()), contexts, getOutputWriter());
                } else if ("updateSQL".equalsIgnoreCase(command)) {
                    migrator.update(contexts, getOutputWriter());
                } else if ("rollback".equalsIgnoreCase(command)) {
                    if (commandParams == null) {
                        throw new CommandLineParsingException("rollback requires a rollback tag");
                    }
                    migrator.rollback(commandParams.iterator().next(), contexts);
                } else if ("rollbackToDate".equalsIgnoreCase(command)) {
                    if (commandParams == null) {
                        throw new CommandLineParsingException("rollback requires a rollback date");
                    }
                    migrator.rollback(dateFormat.parse(commandParams.iterator().next()), contexts);
                } else if ("rollbackCount".equalsIgnoreCase(command)) {
                    migrator.rollback(Integer.parseInt(commandParams.iterator().next()), contexts);

                } else if ("rollbackSQL".equalsIgnoreCase(command)) {
                    if (commandParams == null) {
                        throw new CommandLineParsingException("rollbackSQL requires a rollback tag");
                    }
                    migrator.rollback(commandParams.iterator().next(), contexts, getOutputWriter());
                } else if ("rollbackToDateSQL".equalsIgnoreCase(command)) {
                    if (commandParams == null) {
                        throw new CommandLineParsingException("rollbackToDateSQL requires a rollback date");
                    }
                    migrator.rollback(dateFormat.parse(commandParams.iterator().next()), contexts, getOutputWriter());
                } else if ("rollbackCountSQL".equalsIgnoreCase(command)) {
                    if (commandParams == null) {
                        throw new CommandLineParsingException("rollbackCountSQL requires a rollback tag");
                    }

                    migrator.rollback(Integer.parseInt(commandParams.iterator().next()), contexts, getOutputWriter());
                } else if ("futureRollbackSQL".equalsIgnoreCase(command)) {
                    migrator.futureRollbackSQL(contexts, getOutputWriter());
                } else {
                    throw new CommandLineParsingException("Unknown command: " + command);
                }
            } catch (ParseException e) {
                throw new CommandLineParsingException("Unexpected date/time format.  Use 'yyyy-MM-dd HH:mm:ss'");
            }

//            String promptForNonLocal = cmd.getOptionValue("promptForNonLocalDatabase");
//            if (promptForNonLocal != null && Boolean.valueOf(promptForNonLocal)) {
//                if (!migrator.isSaveToRunMigration()) {
//
//                    if (JOptionPane.showConfirmDialog(null, "You are running a database refactoring against a non-local database.\n" +
//                            "Database URL is: " + migrator.getDatabase().getConnectionURL() + "\n" +
//                            "Username is: " + migrator.getDatabase().getConnectionUsername() + "\n\n" +
//                            "Area you sure you want to do this?",
//                            "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
//                        System.out.println("Chose not to run against non-production database");
//                        System.exit(-1);
//                    }
//                }
//            }
        } finally {
            connection.close();
        }
    }

    private Connection createConnectionFromCommandParams(Set<String> commandParams) throws CommandLineParsingException, SQLException, JDBCException {
        String driver = null;
        String url = null;
        String username = null;
        String password = null;

        for (String param : commandParams) {
            String[] splitArg = splitArg(param);

            String attributeName = splitArg[0];
            String value = splitArg[1];
            if ("baseDriver".equalsIgnoreCase(attributeName)) {
                driver = value;
            } else if ("baseUrl".equalsIgnoreCase(attributeName)) {
                url = value;
            } else if ("baseUsername".equals(attributeName)) {
                username = value;
            } else if ("basePassword".equals(attributeName)) {
                password = value;
            }
        }

        if (driver == null) {
            driver = DatabaseFactory.getInstance().findDefaultDriver(url);
        }

        Driver driverObject;
        try {
            driverObject = (Driver) Class.forName(driver, true, classLoader).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot find database driver: " + e.getMessage());
        }

        Properties info = new Properties();
        info.put("user", username);
        info.put("password", password);

        Connection connection;
        try {
            connection = driverObject.connect(url, info);
        } catch (SQLException e) {
            throw new JDBCException("Connection could not be created to " + url + ": " + e.getMessage(), e);
        }
        if (connection == null) {
            throw new JDBCException("Connection could not be created to " + url + " with driver " + driver.getClass().getName() + ".  Possibly the wrong driver for the given database URL");
        }
        return connection;
    }

    private Writer getOutputWriter() {
        return new OutputStreamWriter(System.out);
    }

    public boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows ");
    }

    private void doDiff(Connection baseDatabase, Connection targetDatabase) throws JDBCException {
        Diff diff = new Diff(baseDatabase, targetDatabase);
        diff.addStatusListener(new OutDiffStatusListener());
        DiffResult diffResult = diff.compare();

        System.out.println("");
        System.out.println("Diff Results:");
        diffResult.printResult(System.out);
    }

    private void doDiffToChangeLog(Connection baseDatabase, Connection targetDatabase) throws JDBCException, IOException, ParserConfigurationException {
        Diff diff = new Diff(baseDatabase, targetDatabase);
        diff.addStatusListener(new OutDiffStatusListener());
        DiffResult diffResult = diff.compare();

        diffResult.printChangeLog(System.out, DatabaseFactory.getInstance().findCorrectDatabaseImplementation(targetDatabase));
    }

    private void doGenerateChangeLog(Connection originalDatabase) throws JDBCException, IOException, ParserConfigurationException {
        Diff diff = new Diff(originalDatabase);
        diff.addStatusListener(new OutDiffStatusListener());
        DiffResult diffResult = diff.compare();

        PrintStream outputStream = System.out;

        if (StringUtils.trimToNull(changeLogFile) != null) {
            File changeFile = new File(changeLogFile);
            outputStream = new PrintStream(changeFile);
        }
        diffResult.printChangeLog(outputStream, DatabaseFactory.getInstance().findCorrectDatabaseImplementation(originalDatabase));
    }


    private static class OutDiffStatusListener implements DiffStatusListener {
        public void statusUpdate(String message) {
            System.err.println(message);
        }
    }
}
