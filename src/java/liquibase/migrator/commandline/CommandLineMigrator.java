package liquibase.migrator.commandline;

import liquibase.migrator.MigrationFailedException;
import liquibase.migrator.Migrator;
import liquibase.migrator.DatabaseChangeLogLock;
import liquibase.migrator.commandline.cli.*;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.DateFormat;

public class CommandLineMigrator {
    private Options options;
    private CommandLine cmd;
    private ClassLoader classLoader;

    public static void main(String args[]) throws Exception {
        String shouldRunProperty = System.getProperty(Migrator.SHOULD_RUN_SYSTEM_PROPERTY);
        if (shouldRunProperty != null && !Boolean.valueOf(shouldRunProperty)) {
            System.out.println("Migrator did not run because '" + Migrator.SHOULD_RUN_SYSTEM_PROPERTY + "' system property was set to false");
            return;
        }


        CommandLineMigrator commandLineMigrator = new CommandLineMigrator();
        commandLineMigrator.parseOptions(args);
        commandLineMigrator.doMigration();

        System.exit(0);

    }

    public CommandLineMigrator() {
        options = createOptions();
    }

    private void parseOptions(String[] args) throws MigrationFailedException {
        CommandLineParser parser = new GnuParser();
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            new HelpFormatter().printHelp("java -jar liquibase.jar", options);
            System.exit(-1);
        }
        String[] classpath;
        if (isWindows()) {
            classpath = cmd.getOptionValue("classpath").split(";");
        } else {
            classpath = cmd.getOptionValue("classpath").split(":");
        }

        List<URL> urls = new ArrayList<URL>();
        for (String classpathEntry : classpath) {
            File classPathFile = new File(classpathEntry);
            if (!classPathFile.exists()) {
                throw new MigrationFailedException(classPathFile.getAbsolutePath() + " does not exist");
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
                throw new MigrationFailedException(e);
            }
        }
        classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), Thread.currentThread().getContextClassLoader());
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
        int status = -1;
        while ((status = inStream.read()) != -1) {
            outStream.write(status);
        }
        outStream.close();
        inStream.close();

        return tempFile;
    }

    private Options createOptions() {
        Options options = new Options();
        options.addOption(OptionBuilder.withArgName("value").hasArg().withDescription("Database Driver").isRequired().create("driver"));
        options.addOption(OptionBuilder.withArgName("value").hasArg().withDescription("Username").isRequired().create("username"));
        options.addOption(OptionBuilder.withArgName("value").hasArg().withDescription("Database Password").isRequired().create("password"));
        options.addOption(OptionBuilder.withArgName("value").hasArg().withDescription("Database URL").isRequired().create("url"));
        options.addOption(OptionBuilder.withArgName("value").hasArg().withDescription("Migration File").isRequired(false).create("migrationFile"));
        options.addOption(OptionBuilder.withArgName("value").hasArg().withDescription("Classpath").isRequired(false).create("classpath"));
        options.addOption(OptionBuilder.withDescription("Execute Mode").isRequired(false).create("execute"));
        options.addOption(OptionBuilder.withArgName("filename").hasArg().withDescription("Output SQL Mode").isRequired(false).create("outputSQL"));
        options.addOption(OptionBuilder.withArgName("filename").hasArg().withDescription("Output Changelog SQL Only Mode").isRequired(false).create("outputChangelogSQL"));
        options.addOption(OptionBuilder.withDescription("Drop All Database Objects First").isRequired(false).create("dropAllFirst"));
        options.addOption(OptionBuilder.withDescription("Display Change Log Lock").isRequired(false).create("listLocks"));
        options.addOption(OptionBuilder.withDescription("Release Change Log Locks").isRequired(false).create("releaseLocks"));
        options.addOption(OptionBuilder.withArgName("value").hasArg().withDescription("Context of Deployment").isRequired(false).create("contexts"));
        options.addOption(OptionBuilder.withArgName("true|false").hasArg().withDescription("Prompt For Non-localhost databases").isRequired(false).create("promptForNonLocalhostDatabase"));

         return options;
    }


    private void doMigration() throws Exception {

        Driver driver;
        try {
            driver = (Driver) Class.forName(cmd.getOptionValue("driver"), true, classLoader).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot get database driver: " + e.getMessage());
        }
        Properties info = new Properties();
        info.put("user", cmd.getOptionValue("username"));
        info.put("password", cmd.getOptionValue("password"));

        Connection connection = driver.connect(cmd.getOptionValue("url"), info);
        if (connection == null) {
            throw new MigrationFailedException("Incorrect driver for URL");
        }
        Writer outputSQLFileWriter = null;
        File outputSqlFile;
        try {
            Migrator migrator = new Migrator(cmd.getOptionValue("migrationFile"), new CommandLineFileOpener(classLoader));
            migrator.setContexts(cmd.getOptionValue("contexts"));
            migrator.init(connection);

            if (cmd.hasOption("listLocks")) {
                DatabaseChangeLogLock[] locks = migrator.listLocks();
                System.out.println("Database change log locks for " + migrator.getDatabase().getConnectionUsername() + "@" + migrator.getDatabase().getConnectionURL());
                if (locks.length == 0) {
                    System.out.println(" - No locks");
                }
                for (DatabaseChangeLogLock lock : locks) {
                    System.out.println(" - " + lock.getLockedBy() + " at " + DateFormat.getDateTimeInstance().format(lock.getLockGranted()));
                }
                return;
            }

            if (cmd.hasOption("releaseLocks")) {
                migrator.forceReleaseLock();
                System.out.println("Successfully released all database change log locks for " + migrator.getDatabase().getConnectionUsername() + "@" + migrator.getDatabase().getConnectionURL());
                return;
            }

            migrator.setMode(Migrator.EXECUTE_MODE);
            if (cmd.hasOption("outputSQL") || cmd.hasOption("outputChangelogSQL")) {
                if (cmd.hasOption("outputChangelogSQL")) {
                    migrator.setMode(Migrator.OUTPUT_CHANGELOG_ONLY_SQL_MODE);
                    outputSqlFile = new File(cmd.getOptionValue("outputChangelogSQL"));
                } else {
                    migrator.setMode(Migrator.OUTPUT_SQL_MODE);
                    outputSqlFile = new File(cmd.getOptionValue("outputSQL"));
                }
                if (outputSqlFile.exists()) {
                    throw new MigrationFailedException(outputSqlFile.getAbsolutePath() + " already exists");
                }
                outputSQLFileWriter = new BufferedWriter(new FileWriter(outputSqlFile));
                migrator.setOutputSQLWriter(outputSQLFileWriter);
            }
            if (cmd.hasOption("dropAllFirst")) {
                migrator.setShouldDropDatabaseObjectsFirst(true);
            }


            String promptForNonLocal = cmd.getOptionValue("promptForNonLocalhostDatabase");
            if (promptForNonLocal != null && Boolean.valueOf(promptForNonLocal)) {
                if (!migrator.isSaveToRunMigration()) {
//                if (migrator == null) {
//                    System.out.println("Migrator is null");
//                } else  if (migrator.getDatabase() == null) {
//                    System.out.println("Database Is Null");
//                } else {
//                    System.out.println("Migrator and Database are not-null");
//                }

                    if (JOptionPane.showConfirmDialog(null, "You are running a database refactoring against a non-local database.\n" +
                            "Database URL is: " + migrator.getDatabase().getConnectionURL() + "\n" +
                            "Username is: " + migrator.getDatabase().getConnectionUsername() + "\n\n" +
                            "Area you sure you want to do this?",
                            "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                        System.out.println("Chose not to run against non-production database");
                        System.exit(-1);
                    }
                }
            }
            migrator.migrate();
        } catch (Throwable e) {
            String message = e.getMessage();
            if (e.getCause() != null) {
                message = e.getCause().getMessage();
            }
            System.out.println("Migration Failed: " + message);
            Logger.getLogger(Migrator.DEFAULT_LOG_NAME).log(Level.SEVERE, message, e);
            System.exit(-1);
        } finally {
            if (connection != null) {
                connection.close();
            }
            if (outputSQLFileWriter != null) {
                outputSQLFileWriter.close();
            }
        }
    }

    public boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows ");
    }

}
