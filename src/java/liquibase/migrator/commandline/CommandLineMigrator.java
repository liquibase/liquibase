package liquibase.migrator.commandline;

import liquibase.migrator.DatabaseChangeLogLock;
import liquibase.migrator.MigrationFailedException;
import liquibase.migrator.Migrator;
import liquibase.StreamUtil;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.Field;

public class CommandLineMigrator {
    protected ClassLoader classLoader;

    protected String driver;
    protected String username;
    protected String password;
    protected String url;
    protected String migrationFile;
    protected String classpath;
    protected String contexts;
    protected Boolean promptForNonLocalDatabase = null;
    protected Boolean includeSystemClasspath;

    protected String command;
    protected String commandParam;

    protected String logLevel;


    public static void main(String args[]) throws Exception {
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
            System.out.println("LiquiBase Version: "+new Migrator(null, null).getBuildVersion()+ StreamUtil.getLineSeparator());
            return;
        }

        commandLineMigrator.parseOptions(args);

        File propertiesFile = new File("liquibase.properties");
        if (propertiesFile.exists()) {
            commandLineMigrator.parsePropertiesFile(new FileInputStream(propertiesFile));
        }
        if (!commandLineMigrator.checkSetup()) {
            commandLineMigrator.printHelp(System.out);
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
            System.out.println("Migration Failed: " + message);
            Logger.getLogger(Migrator.DEFAULT_LOG_NAME).log(Level.SEVERE, message, e);
            return;
        }

        if ("migrate".equals(commandLineMigrator.command)) {
            System.out.println("Migration successful");
        } else if (commandLineMigrator.command.startsWith("rollback") && !commandLineMigrator.command.endsWith("SQL")) {
            System.out.println("Rollback successful");
        }
    }

    protected boolean checkSetup() {
        if (classpath == null
                || migrationFile == null
                || username == null
                || password == null
                || url == null
                || driver == null
                || command == null) {
            return false;
        }
        return isCommand(command);
    }

    private boolean isCommand(String arg) {
        return "migrate".equals(arg)
                || "migrateSQL".equals(arg)
                || "rollback".equals(arg)
                || "rollbackToDate".equals(arg)
                || "rollbackCount".equals(arg)
                || "rollbackSQL".equals(arg)
                || "rollbackToDateSQL".equals(arg)
                || "rollbackCountSQL".equals(arg)
                || "futureRollbackSQL".equals(arg)
                || "tag".equals(arg)
                || "listLocks".equals(arg)
                || "dropAll".equals(arg)
                || "releaseLocks".equals(arg);
    }

    protected void parsePropertiesFile(InputStream propertiesInputStream) throws IOException, CommandLineParsingException {
        Properties props = new Properties();
        props.load(propertiesInputStream);

        for (Object property : props.keySet()) {
            try {
                Field field = getClass().getDeclaredField((String) property);
                Object currentValue = field.get(this);

                if (currentValue == null) {
                    String value = (String) props.get(property);
                    if (field.getType().equals(Boolean.class)) {
                        field.set(this, Boolean.valueOf(value));
                    } else {
                        field.set(this, value);
                    }
                }
            } catch (Exception e) {
                throw new CommandLineParsingException("Unknown parameter: '" + property + "'");
            }
        }
    }

    protected void printHelp(PrintStream stream) {
        stream.println("Usage: java -jar liquibase.jar [options] [command]");
        stream.println("");
        stream.println("Standard Commands:");
        stream.println(" migrate                        Updates database to current version");
        stream.println(" rollback <tag>                 Rolls back the database to the the state is was");
        stream.println("                                when the tag was applied");
        stream.println(" rollbackToDate <date/time>     Rolls back the database to the the state is was");
        stream.println("                                at the given date/time.");
        stream.println("                                Date Format: yyyy-MM-dd HH:mm:ss");
        stream.println(" rollbackCount <value>          Rolls back the last <value> change sets");
        stream.println("                                applied to the database");
        stream.println(" migrateSQL                     Writes SQL to update database to current");
        stream.println("                                version to STDOUT");
        stream.println(" rollbackSQL <tag>              Writes SQL to roll back the database to that");
        stream.println("                                state it was in when the tag was applied");
        stream.println("                                to STDOUT");
        stream.println(" rollbackToDateSQL <date/time>  Writes SQL to roll back the database to that");
        stream.println("                                state it was in at the given date/time version");
        stream.println("                                to STDOUT");
        stream.println(" rollbackCountSQL <value>       Writes SQL to roll back the last");
        stream.println("                                <value> change sets to STDOUT");
        stream.println("                                applied to the database");
        stream.println(" futureRollbackSQL              Writes SQL to roll back the database to the ");
        stream.println("                                current state after the changes in the ");
        stream.println("                                changeslog have been applied");
        stream.println("");
        stream.println("Maintenance Commands");
        stream.println(" tag <tag string>          'Tags' the current database state for future rollback");
        stream.println(" changelogSyncSQL          Writes SQL to mark all refactorings as executed ");
        stream.println("                           in the database to STDOUT");
        stream.println(" listLocks                 Lists who currently has locks on the");
        stream.println("                           database changelog");
        stream.println(" releaseLocks              Releases all locks on the database changelog");
        stream.println(" dropAll                   Drop all database objects owned by user");
        stream.println("");
        stream.println("Required Parameters:");
        stream.println(" --classpath=<value>                        Classpath containing");
        stream.println("                                            migration files and JDBC Driver");
        stream.println(" --migrationFile=<path and filename>        Migration file");
        stream.println(" --username=<value>                         Database username");
        stream.println(" --password=<value>                         Database password");
        stream.println(" --url=<value>                              Database URL");
        stream.println(" --driver=<jdbc.driver.ClassName>           Database driver class name");
        stream.println("");
        stream.println("Optional Parameters:");
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
        stream.println(" --help                                     Prints this message");
        stream.println(" --version                                  Prints this version information");
        stream.println("");
        stream.println("Default value for parameters can be stored in a file called");
        stream.println("'liquibase.properties' that is read from the current working directory.");
        stream.println("");
        stream.println("Full documentation is available at");
        stream.println("http://www.liquibase.org/manual/latest/command_line_migrator.html");
        stream.println("");
    }

    public CommandLineMigrator() {
//        options = createOptions();
    }

    protected void parseOptions(String[] args) throws CommandLineParsingException {
        boolean seenCommand = false;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (isCommand(arg)) {
                this.command = arg;
                seenCommand = true;
            } else if (seenCommand) {
                if (commandParam == null) {
                    commandParam = arg;
                } else {
                    throw new CommandLineParsingException("Only one command option is allowed");
                }
            } else if (arg.startsWith("--")) {
                String[] splitArg = arg.split("=");
                if (splitArg.length < 2) {
                    throw new CommandLineParsingException("Could not parse '" + arg + "'");
                } else if (splitArg.length > 2) {
                    StringBuffer secondHalf = new StringBuffer();
                    for (int j=1; j<splitArg.length; j++) {
                        secondHalf.append(splitArg[j]).append("=");
                    }

                    splitArg = new String[] {
                            splitArg[0],
                            secondHalf.toString().replaceFirst("=$","")
                    };
                }

                String attributeName = splitArg[0].replaceFirst("--", "");
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
        String[] classpath;
        if (isWindows()) {
            classpath = this.classpath.split(";");
        } else {
            classpath = this.classpath.split(":");
        }

        List<URL> urls = new ArrayList<URL>();
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
        if (includeSystemClasspath) {
            classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), Thread.currentThread().getContextClassLoader());
        } else {
            classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
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
        BufferedInputStream inStream = new BufferedInputStream(jar.getInputStream(entry));
        BufferedOutputStream outStream = new BufferedOutputStream(
                new FileOutputStream(tempFile));
        int status;
        while ((status = inStream.read()) != -1) {
            outStream.write(status);
        }
        outStream.close();
        inStream.close();

        return tempFile;
    }

    protected void doMigration() throws Exception {

        if ("finest".equalsIgnoreCase(logLevel)) {
            Logger.getLogger(Migrator.DEFAULT_LOG_NAME).setLevel(Level.FINEST);
        } else if ("finer".equalsIgnoreCase(logLevel)) {
            Logger.getLogger(Migrator.DEFAULT_LOG_NAME).setLevel(Level.FINER);
        } else if ("fine".equalsIgnoreCase(logLevel)) {
            Logger.getLogger(Migrator.DEFAULT_LOG_NAME).setLevel(Level.FINE);
        } else if ("info".equalsIgnoreCase(logLevel)) {
            Logger.getLogger(Migrator.DEFAULT_LOG_NAME).setLevel(Level.INFO);
        } else if ("warning".equalsIgnoreCase(logLevel)) {
            Logger.getLogger(Migrator.DEFAULT_LOG_NAME).setLevel(Level.WARNING);
        } else if ("severe".equalsIgnoreCase(logLevel)) {
            Logger.getLogger(Migrator.DEFAULT_LOG_NAME).setLevel(Level.SEVERE);
        } else if ("off".equalsIgnoreCase(logLevel)) {
            Logger.getLogger(Migrator.DEFAULT_LOG_NAME).setLevel(Level.OFF);
        } else {
            throw new CommandLineParsingException("Unknown log level: "+logLevel);
        }

        Driver driver;
        try {
            driver = (Driver) Class.forName(this.driver, true, classLoader).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot get database driver: " + e.getMessage());
        }
        Properties info = new Properties();
        info.put("user", username);
        info.put("password", password);

        Connection connection = driver.connect(url, info);
        if (connection == null) {
            throw new MigrationFailedException("Incorrect driver for URL");
        }
        Writer outputSQLFileWriter = null;
        try {
            Migrator migrator = new Migrator(migrationFile, new CommandLineFileOpener(classLoader));
            migrator.setContexts(contexts);
            migrator.init(connection);

            if ("listLocks".equalsIgnoreCase(command)) {
                DatabaseChangeLogLock[] locks = migrator.listLocks();
                System.out.println("Database change log locks for " + migrator.getDatabase().getConnectionUsername() + "@" + migrator.getDatabase().getConnectionURL());
                if (locks.length == 0) {
                    System.out.println(" - No locks");
                }
                for (DatabaseChangeLogLock lock : locks) {
                    System.out.println(" - " + lock.getLockedBy() + " at " + DateFormat.getDateTimeInstance().format(lock.getLockGranted()));
                }
                return;
            } else if ("releaseLocks".equalsIgnoreCase(command)) {
                migrator.forceReleaseLock();
                System.out.println("Successfully released all database change log locks for " + migrator.getDatabase().getConnectionUsername() + "@" + migrator.getDatabase().getConnectionURL());
                return;
            } else if ("tag".equalsIgnoreCase(command)) {
                migrator.tag(commandParam);
                System.out.println("Successfully tagged " + migrator.getDatabase().getConnectionUsername() + "@" + migrator.getDatabase().getConnectionURL());
                return;
            } else if ("dropAll".equals(command)) {
                migrator.dropAll();
                System.out.println("All objects dropped from " + migrator.getDatabase().getConnectionUsername() + "@" + migrator.getDatabase().getConnectionURL());
                return;
            }



            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                if ("migrate".equalsIgnoreCase(command)) {
                    migrator.setMode(Migrator.EXECUTE_MODE);
                } else if ("changelogSyncSQL".equalsIgnoreCase(command)) {
                    migrator.setMode(Migrator.OUTPUT_CHANGELOG_ONLY_SQL_MODE);
                    migrator.setOutputSQLWriter(getOutputWriter());
                } else if ("migrateSQL".equalsIgnoreCase(command)) {
                    migrator.setMode(Migrator.OUTPUT_SQL_MODE);
                    migrator.setOutputSQLWriter(getOutputWriter());
                } else if ("rollback".equalsIgnoreCase(command)) {
                    migrator.setMode(Migrator.EXECUTE_ROLLBACK_MODE);
                    if (commandParam == null) {
                        throw new CommandLineParsingException("rollback requires a rollback tag");
                    }
                    migrator.setRollbackToTag(commandParam);
                } else if ("rollbackToDate".equalsIgnoreCase(command)) {
                    migrator.setMode(Migrator.EXECUTE_ROLLBACK_MODE);
                    if (commandParam == null) {
                        throw new CommandLineParsingException("rollback requires a rollback date");
                    }
                    migrator.setRollbackToDate(dateFormat.parse(commandParam));
                } else if ("rollbackCount".equalsIgnoreCase(command)) {
                    migrator.setMode(Migrator.EXECUTE_ROLLBACK_MODE);
                    migrator.setRollbackCount(Integer.parseInt(commandParam));
                } else if ("rollbackSQL".equalsIgnoreCase(command)) {
                    migrator.setMode(Migrator.OUTPUT_ROLLBACK_SQL_MODE);
                    migrator.setOutputSQLWriter(getOutputWriter());
                    if (commandParam == null) {
                        throw new CommandLineParsingException("rollbackSQL requires a rollback tag");
                    }
                    migrator.setRollbackToTag(commandParam);
                } else if ("rollbackToDateSQL".equalsIgnoreCase(command)) {
                    migrator.setMode(Migrator.OUTPUT_ROLLBACK_SQL_MODE);
                    migrator.setOutputSQLWriter(getOutputWriter());
                    if (commandParam == null) {
                        throw new CommandLineParsingException("rollbackToDateSQL requires a rollback date");
                    }
                    migrator.setRollbackToDate(dateFormat.parse(commandParam));
                } else if ("rollbackCountSQL".equalsIgnoreCase(command)) {
                    migrator.setMode(Migrator.OUTPUT_ROLLBACK_SQL_MODE);
                    migrator.setOutputSQLWriter(getOutputWriter());
                    migrator.setRollbackCount(Integer.valueOf(commandParam));
                } else if ("futureRollbackSQL".equalsIgnoreCase(command)) {
                    migrator.setMode(Migrator.OUTPUT_FUTURE_ROLLBACK_SQL_MODE);
                    migrator.setOutputSQLWriter(getOutputWriter());
                } else {
                    throw new CommandLineParsingException("Unknown command: "+command);
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
            migrator.migrate();
        } finally {
            if (connection != null) {
                connection.close();
            }
            if (outputSQLFileWriter != null) {
                outputSQLFileWriter.flush();
                outputSQLFileWriter.close();
            }
        }
    }

    private Writer getOutputWriter() {
        return new OutputStreamWriter(System.out);
    }

    public boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows ");
    }

}
