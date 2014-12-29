package liquibase.integration.commandline;

import liquibase.CatalogAndSchema;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.change.CheckSum;
import liquibase.command.ExecuteSqlCommand;
import liquibase.command.SnapshotCommand;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.configuration.GlobalConfiguration;
import liquibase.database.Database;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.StandardObjectChangeFilter;
import liquibase.exception.*;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.LogFactory;
import liquibase.logging.LogLevel;
import liquibase.logging.Logger;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.servicelocator.ServiceLocator;
import liquibase.util.ISODateFormat;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.ParseException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Class for executing Liquibase via the command line.
 */
public class Main {
    protected ClassLoader classLoader;

    protected String driver;
    protected String username;
    protected String password;
    protected String url;
    protected String databaseClass;
    protected String defaultSchemaName;
    protected String outputDefaultSchema;
    protected String outputDefaultCatalog;
	protected String liquibaseCatalogName;
	protected String liquibaseSchemaName;
    protected String defaultCatalogName;
    protected String changeLogFile;
    protected String classpath;
    protected String contexts;
    protected String labels;
    protected String driverPropertiesFile;
    protected String propertyProviderClass = null;
    protected Boolean promptForNonLocalDatabase = null;
    protected Boolean includeSystemClasspath;
    protected Boolean strict = Boolean.TRUE;
    protected String defaultsFile = "liquibase.properties";

    protected String diffTypes;
    protected String changeSetAuthor;
    protected String changeSetContext;
    protected String dataOutputDirectory;

    protected String referenceDriver;
    protected String referenceUrl;
    protected String referenceUsername;
    protected String referencePassword;

    protected String currentDateTimeFunction;

    protected String command;
    protected Set<String> commandParams = new LinkedHashSet<String>();

    protected String logLevel;
    protected String logFile;

    protected Map<String, Object> changeLogParameters = new HashMap<String, Object>();

    protected String outputFile;

	public static void main(String args[]) throws CommandLineParsingException, IOException {
		try {
			run(args);
		} catch (LiquibaseException e) {
			System.exit(-1);
		}
		System.exit(0);
	}

    public static void run(String args[]) throws CommandLineParsingException, IOException, LiquibaseException {
        try {
            GlobalConfiguration globalConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class);

            if (!globalConfiguration.getShouldRun()) {
                System.err.println("Liquibase did not run because '" + LiquibaseConfiguration.getInstance().describeValueLookupLogic(globalConfiguration.getProperty(GlobalConfiguration.SHOULD_RUN)) + " was set to false");
                return;
            }

//            if (!System.getProperties().contains("file.encoding")) {
//                System.setProperty("file.encoding", "UTF-8");
//            }

            Main main = new Main();
            if (args.length == 1 && "--help".equals(args[0])) {
                main.printHelp(System.err);
                return;
            } else if (args.length == 1 && "--version".equals(args[0])) {
                System.err.println("Liquibase Version: " + LiquibaseUtil.getBuildVersion() + StreamUtil.getLineSeparator());
                return;
            }

            try {
                main.parseOptions(args);
            } catch (CommandLineParsingException e) {
	            // Print the help before throwing the exception
                main.printHelp(Arrays.asList(e.getMessage()), System.err);
                throw e;
            }

            File propertiesFile = new File(main.defaultsFile);
            String localDefaultsPathName = main.defaultsFile.replaceFirst("(\\.[^\\.]+)$", ".local$1");
            File localPropertiesFile = new File(localDefaultsPathName);

            if (localPropertiesFile.exists()) {
                FileInputStream stream = new FileInputStream(localPropertiesFile);
                try {
                    main.parsePropertiesFile(stream);
                } finally {
                    stream.close();
                }
            } else {
                InputStream resourceAsStream = main.getClass().getClassLoader().getResourceAsStream(localDefaultsPathName);
                if (resourceAsStream != null) {
                    try {
                        main.parsePropertiesFile(resourceAsStream);
                    } finally {
                        resourceAsStream.close();
                    }
                }
            }
            if (propertiesFile.exists()) {
                FileInputStream stream = new FileInputStream(propertiesFile);
                main.parsePropertiesFile(stream);
            } else {
                InputStream resourceAsStream = main.getClass().getClassLoader().getResourceAsStream(main.defaultsFile);
                if (resourceAsStream != null) {
                    try {
                        main.parsePropertiesFile(resourceAsStream);
                    } finally {
                        resourceAsStream.close();
                    }
                }

            }

            List<String> setupMessages = main.checkSetup();
            if (setupMessages.size() > 0) {
                main.printHelp(setupMessages, System.err);
                return;
            }

            main.applyDefaults();
            main.configureClassLoader();
            main.doMigration();

            if ("update".equals(main.command)) {
                System.err.println("Liquibase Update Successful");
            } else if (main.command.startsWith("rollback") && !main.command.endsWith("SQL")) {
                System.err.println("Liquibase Rollback Successful");
            } else if (!main.command.endsWith("SQL")) {
                System.err.println("Liquibase '"+main.command+"' Successful");
            }
        } catch (Throwable e) {
            String message = e.getMessage();
	        if ( e.getCause() != null ) {
		        message = e.getCause().getMessage();
	        }
	        if ( message == null ) {
		        message = "Unknown Reason";
	        }
	        // At a minimum, log the message.  We don't need to print the stack
	        // trace because the logger already did that upstream.
            try {
	            if ( e.getCause() instanceof ValidationFailedException ) {
		            ((ValidationFailedException)e.getCause()).printDescriptiveError(System.out);
	            } else {
	                System.err.println("Unexpected error running Liquibase: " + message + "\n");
                    LogFactory.getInstance().getLog().severe(message, e);
		            System.err.println(generateLogLevelWarningMessage());
	            }
            } catch (Exception e1) {
                e.printStackTrace();
            }
            throw new LiquibaseException("Unexpected error running Liquibase: " + message, e);
        }
    }

    private static String generateLogLevelWarningMessage() {
        Logger logger = LogFactory.getInstance().getLog();
        if (logger != null && logger.getLogLevel() != null && (logger.getLogLevel().equals(LogLevel.OFF))) {
            return "";
        } else {
            return "\n\nFor more information, use the --logLevel flag";
        }
    }

    /**
     * On windows machines, it splits args on '=' signs.  Put it back like it was.
     */
    protected String[] fixupArgs(String[] args) {
        List<String> fixedArgs = new ArrayList<String>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ((arg.startsWith("--") || arg.startsWith("-D")) && !arg.contains("=")) {
                String nextArg = null;
                if (i + 1 < args.length) {
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
            if (url == null) {
                messages.add("--url is required");
            }

            if (isChangeLogRequired(command) && changeLogFile == null) {
                messages.add("--changeLogFile is required");
            }

            if (isNoArgCommand(command) && !commandParams.isEmpty()) {
                messages.add("unexpected command parameters: "+commandParams);
            } else {
                validateCommandParameters(messages);
            }
        }
        return messages;
    }

    private void checkForUnexpectedCommandParameter(List<String> messages) {
        if ("updateCount".equalsIgnoreCase(command)
            || "updateCountSQL".equalsIgnoreCase(command)
            || "rollback".equalsIgnoreCase(command)
            || "rollbackToDate".equalsIgnoreCase(command)
            || "rollbackCount".equalsIgnoreCase(command)
            || "rollbackSQL".equalsIgnoreCase(command)
            || "rollbackToDateSQL".equalsIgnoreCase(command)
            || "rollbackCountSQL".equalsIgnoreCase(command)
            || "calculateCheckSum".equalsIgnoreCase(command)
            || "dbDoc".equalsIgnoreCase(command)
            || "tag".equalsIgnoreCase(command)) {

            if (commandParams.size() > 0 && commandParams.iterator().next().startsWith("-")) {
                messages.add("unexpected command parameters: "+commandParams);
            }
        } else if ("status".equalsIgnoreCase(command)
                || "unexpectedChangeSets".equalsIgnoreCase(command)) {
            if (commandParams.size() > 0 && !commandParams.iterator().next().equalsIgnoreCase("--verbose")) {
                messages.add("unexpected command parameters: "+commandParams);
            }
        } else if ("diff".equalsIgnoreCase(command)
            || "diffChangeLog".equalsIgnoreCase(command)) {
            if (commandParams.size() > 0) {
                for (String cmdParm : commandParams) {
                    if (!cmdParm.startsWith("--referenceUsername")
                      && !cmdParm.startsWith("--referencePassword")
                      && !cmdParm.startsWith("--referenceDriver")
                      && !cmdParm.startsWith("--referenceDefaultCatalogName")
                      && !cmdParm.startsWith("--referenceDefaultSchemaName")
                      && !cmdParm.startsWith("--includeSchema")
                      && !cmdParm.startsWith("--includeCatalog")
                      && !cmdParm.startsWith("--includeTablespace")
                      && !cmdParm.startsWith("--schemas")
                      && !cmdParm.startsWith("--referenceUrl")
                      && !cmdParm.startsWith("--excludeObjects")
                      && !cmdParm.startsWith("--includeObjects")
                      && !cmdParm.startsWith("--diffTypes")) {
                      messages.add("unexpected command parameter: "+cmdParm);
                    }
                }
            }
        }

    }

    private void validateCommandParameters(final List<String> messages) {
        checkForUnexpectedCommandParameter(messages);
        checkForMissingCommandParameters(messages);
        checkForMalformedCommandParameters(messages);
    }

    private void checkForMissingCommandParameters(final List<String> messages) {
        if (commandParams.isEmpty() || commandParams.iterator().next().startsWith("-")) {
            if ("calculateCheckSum".equalsIgnoreCase(command)) {
                messages.add("missing changeSet identifier");
            }
        }
    }

    private void checkForMalformedCommandParameters(final List<String> messages) {
      if (!commandParams.isEmpty()) {
        if ("calculateCheckSum".equalsIgnoreCase(command)) {
          for (final String param : commandParams) {
            assert param != null;
            if (param != null && !param.startsWith("-")) {
              final String[] parts = param.split("::");
              if (parts == null || parts.length < 3) {
                messages.add("changeSet identifier must be of the form filepath::id::author");
                break;
              }
            }
          }
        }
      }
    }

    private boolean isChangeLogRequired(String command) {
        return command.toLowerCase().startsWith("update")
                || command.toLowerCase().startsWith("rollback")
                || "calculateCheckSum".equalsIgnoreCase(command)
                || "status".equalsIgnoreCase(command)
                || "validate".equalsIgnoreCase(command)
                || "changeLogSync".equalsIgnoreCase(command)
                || "changeLogSyncSql".equalsIgnoreCase(command)
                || "generateChangeLog".equalsIgnoreCase(command);
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
                || "futureRollbackCountSQL".equalsIgnoreCase(arg)
                || "updateTestingRollback".equalsIgnoreCase(arg)
                || "tag".equalsIgnoreCase(arg)
                || "listLocks".equalsIgnoreCase(arg)
                || "dropAll".equalsIgnoreCase(arg)
                || "releaseLocks".equalsIgnoreCase(arg)
                || "status".equalsIgnoreCase(arg)
                || "unexpectedChangeSets".equalsIgnoreCase(arg)
                || "validate".equalsIgnoreCase(arg)
                || "help".equalsIgnoreCase(arg)
                || "diff".equalsIgnoreCase(arg)
                || "diffChangeLog".equalsIgnoreCase(arg)
                || "generateChangeLog".equalsIgnoreCase(arg)
                || "snapshot".equalsIgnoreCase(arg)
                || "snapshotReference".equalsIgnoreCase(arg)
                || "executeSql".equalsIgnoreCase(arg)
                || "calculateCheckSum".equalsIgnoreCase(arg)
                || "clearCheckSums".equalsIgnoreCase(arg)
                || "dbDoc".equalsIgnoreCase(arg)
                || "changelogSync".equalsIgnoreCase(arg)
                || "changelogSyncSQL".equalsIgnoreCase(arg)
                || "markNextChangeSetRan".equalsIgnoreCase(arg)
                || "markNextChangeSetRanSQL".equalsIgnoreCase(arg);
    }

    private boolean isNoArgCommand(String arg) {
            return "migrate".equals(arg)
                    || "migrateSQL".equalsIgnoreCase(arg)
                    || "update".equalsIgnoreCase(arg)
                    || "updateSQL".equalsIgnoreCase(arg)
                    || "futureRollbackSQL".equalsIgnoreCase(arg)
                    || "updateTestingRollback".equalsIgnoreCase(arg)
                    || "listLocks".equalsIgnoreCase(arg)
                    || "dropAll".equalsIgnoreCase(arg)
                    || "releaseLocks".equalsIgnoreCase(arg)
                    || "validate".equalsIgnoreCase(arg)
                    || "help".equalsIgnoreCase(arg)
                    || "clearCheckSums".equalsIgnoreCase(arg)
                    || "changelogSync".equalsIgnoreCase(arg)
                    || "changelogSyncSQL".equalsIgnoreCase(arg)
                    || "markNextChangeSetRan".equalsIgnoreCase(arg)
                    || "markNextChangeSetRanSQL".equalsIgnoreCase(arg);
    }



    protected void parsePropertiesFile(InputStream propertiesInputStream) throws IOException, CommandLineParsingException {
        Properties props = new Properties();
        props.load(propertiesInputStream);
        if(props.containsKey("strict")){
            strict = Boolean.valueOf(props.getProperty("strict"));
        }

        for (Map.Entry entry : props.entrySet()) {
            try {
                if (entry.getKey().equals("promptOnNonLocalDatabase")) {
                    continue;
                }
                if (((String) entry.getKey()).startsWith("parameter.")) {
                    changeLogParameters.put(((String) entry.getKey()).replaceFirst("^parameter.", ""), entry.getValue());
                } else {
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
                }
            } catch (NoSuchFieldException nsfe){
                if(strict){
                    throw new CommandLineParsingException("Unknown parameter: '" + entry.getKey() + "'");
                } else {
                    LogFactory.getInstance().getLog().info("Ignored parameter: " + entry.getKey());
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

    protected void printWarning(List<String> warningMessages, PrintStream stream) {
        stream.println("Warnings:");
        for (String message : warningMessages) {
            stream.println("  " + message);
        }
        stream.println();
    }


    protected void printHelp(PrintStream stream) {
        stream.println("Usage: java -jar liquibase.jar [options] [command]");
        stream.println("");
        stream.println("Standard Commands:");
        stream.println(" update                         Updates database to current version");
        stream.println(" updateSQL                      Writes SQL to update database to current");
        stream.println("                                version to STDOUT");
        stream.println(" updateCount <num>              Applies next NUM changes to the database");
        stream.println(" updateCountSQL <num>           Writes SQL to apply next NUM changes");
        stream.println("                                to the database");
        stream.println(" rollback <tag>                 Rolls back the database to the the state is was");
        stream.println("                                when the tag was applied");
        stream.println(" rollbackSQL <tag>              Writes SQL to roll back the database to that");
        stream.println("                                state it was in when the tag was applied");
        stream.println("                                to STDOUT");
        stream.println(" rollbackToDate <date/time>     Rolls back the database to the the state is was");
        stream.println("                                at the given date/time.");
        stream.println("                                Date Format: yyyy-MM-dd'T'HH:mm:ss");
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
        stream.println(" futureRollbackSQL <value>      Writes SQL to roll back the database to the ");
        stream.println("                                current state after <value> changes in the ");
        stream.println("                                changeslog have been applied");
        stream.println(" updateTestingRollback          Updates database, then rolls back changes before");
        stream.println("                                updating again. Useful for testing");
        stream.println("                                rollback support");
        stream.println(" generateChangeLog              Writes Change Log XML to copy the current state");
        stream.println("                                of the database to standard out");
        stream.println(" snapshot                       Writes the current state");
        stream.println("                                of the database to standard out");
        stream.println(" snapshotReference              Writes the current state");
        stream.println("                                of the referenceUrl database to standard out");
        stream.println("");
        stream.println("Diff Commands");
        stream.println(" diff [diff parameters]          Writes description of differences");
        stream.println("                                 to standard out");
        stream.println(" diffChangeLog [diff parameters] Writes Change Log XML to update");
        stream.println("                                 the database");
        stream.println("                                 to the reference database to standard out");
        stream.println("");
        stream.println("Documentation Commands");
        stream.println(" dbDoc <outputDirectory>         Generates Javadoc-like documentation");
        stream.println("                                 based on current database and change log");
        stream.println("");
        stream.println("Maintenance Commands");
        stream.println(" tag <tag string>          'Tags' the current database state for future rollback");
        stream.println(" status [--verbose]        Outputs count (list if --verbose) of unrun changesets");
        stream.println(" unexpectedChangeSets [--verbose]");
        stream.println("                           Outputs count (list if --verbose) of changesets run");
        stream.println("                           in the database that do not exist in the changelog.");
        stream.println(" validate                  Checks changelog for errors");
        stream.println(" calculateCheckSum <id>    Calculates and prints a checksum for the changeset");
        stream.println("                           with the given id in the format filepath::id::author.");
        stream.println(" clearCheckSums            Removes all saved checksums from database log.");
        stream.println("                           Useful for 'MD5Sum Check Failed' errors");
        stream.println(" changelogSync             Mark all changes as executed in the database");
        stream.println(" changelogSyncSQL          Writes SQL to mark all changes as executed ");
        stream.println("                           in the database to STDOUT");
        stream.println(" markNextChangeSetRan      Mark the next change changes as executed ");
        stream.println("                           in the database");
        stream.println(" markNextChangeSetRanSQL   Writes SQL to mark the next change ");
        stream.println("                           as executed in the database to STDOUT");
        stream.println(" listLocks                 Lists who currently has locks on the");
        stream.println("                           database changelog");
        stream.println(" releaseLocks              Releases all locks on the database changelog");
        stream.println(" dropAll                   Drop all database objects owned by user");
        stream.println("");
        stream.println("Required Parameters:");
        stream.println(" --changeLogFile=<path and filename>        Migration file");
        stream.println(" --username=<value>                         Database username");
        stream.println(" --password=<value>                         Database password. If values");
        stream.println("                                            is PROMPT, Liquibase will");
        stream.println("                                            prompt for a password");
        stream.println(" --url=<value>                              Database URL");
        stream.println("");
        stream.println("Optional Parameters:");
        stream.println(" --classpath=<value>                        Classpath containing");
        stream.println("                                            migration files and JDBC Driver");
        stream.println(" --driver=<jdbc.driver.ClassName>           Database driver class name");
        stream.println(" --databaseClass=<database.ClassName>       custom liquibase.database.Database");
        stream.println("                                            implementation to use");
        stream.println(" --propertyProviderClass=<properties.ClassName>  custom Properties");
        stream.println("                                            implementation to use");
        stream.println(" --defaultSchemaName=<name>                 Default database schema to use");
        stream.println(" --contexts=<value>                         ChangeSet contexts to execute");
        stream.println(" --labels=<expression>                      Expression defining labeled");
        stream.println("                                            ChangeSet to execute");
        stream.println(" --defaultsFile=</path/to/file.properties>  File with default option values");
        stream.println("                                            (default: ./liquibase.properties)");
        stream.println(" --driverPropertiesFile=</path/to/file.properties>  File with custom properties");
        stream.println("                                            to be set on the JDBC connection");
        stream.println("                                            to be created");
        stream.println(" --liquibaseCatalogName=<name>              The name of the catalog with the");
        stream.println("                                            liquibase tables");
        stream.println(" --liquibaseSchemaName=<name>               The name of the schema with the");
        stream.println("                                            liquibase tables");
        stream.println(" --includeSystemClasspath=<true|false>      Include the system classpath");
        stream.println("                                            in the Liquibase classpath");
        stream.println("                                            (default: true)");
        stream.println(" --promptForNonLocalDatabase=<true|false>   Prompt if non-localhost");
        stream.println("                                            databases (default: false)");
        stream.println(" --logLevel=<level>                         Execution log level");
        stream.println("                                            (debug, info, warning, severe, off");
        stream.println(" --logFile=<file>                           Log file");
        stream.println(" --currentDateTimeFunction=<value>          Overrides current date time function");
        stream.println("                                            used in SQL.");
        stream.println("                                            Useful for unsupported databases");
        stream.println(" --outputDefaultSchema=<true|false>         If true, SQL object references");
        stream.println("                                            include the schema name, even if");
        stream.println("                                            it is the default schema. ");
        stream.println("                                            Defaults to true");
        stream.println(" --outputDefaultCatalog=<true|false>        If true, SQL object references");
        stream.println("                                            include the catalog name, even if");
        stream.println("                                            it is the default catalog.");
        stream.println("                                            Defaults to true");
        stream.println(" --outputFile=<file>                        File to write output to for commands");
        stream.println("                                            that write output, e.g. updateSQL.");
        stream.println("                                            If not specified, writes to sysout.");
        stream.println(" --help                                     Prints this message");
        stream.println(" --version                                  Prints this version information");
        stream.println("");
        stream.println("Required Diff Parameters:");
        stream.println(" --referenceUsername=<value>                Reference Database username");
        stream.println(" --referencePassword=<value>                Reference Database password. If");
        stream.println("                                            value is PROMPT, Liquibase will");
        stream.println("                                            prompt for a password");
        stream.println(" --referenceUrl=<value>                     Reference Database URL");
        stream.println("");
        stream.println("Optional Diff Parameters:");
        stream.println(" --defaultCatalogName=<name>                Default database catalog to use");
        stream.println(" --defaultSchemaName=<name>                 Default database schema to use");
        stream.println(" --referenceDefaultCatalogName=<name>       Reference database catalog to use");
        stream.println(" --referenceDefaultSchemaName=<name>        Reference database schema to use");
        stream.println(" --schemas=<name1,name2>                    Database schemas to include");
        stream.println("                                            objects from in comparison");
        stream.println(" --includeCatalog=<true|false>              If true, the catalog will be");
        stream.println("                                            included in generated changeSets");
        stream.println("                                            Defaults to false");
        stream.println(" --includeSchema=<true|false>               If true, the schema will be");
        stream.println("                                            included in generated changeSets");
        stream.println("                                            Defaults to false");
        stream.println(" --referenceDriver=<jdbc.driver.ClassName>  Reference database driver class name");
        stream.println(" --dataOutputDirectory=DIR                  Output data as CSV in the given ");
        stream.println("                                            directory");
        stream.println(" --diffTypes                                List of diff types to include in");
        stream.println("                                            Change Log expressed as a comma");
        stream.println("                                            separated list from: tables, views,");
        stream.println("                                            columns, indexes, foreignkeys,");
        stream.println("                                            primarykeys, uniqueconstraints");
        stream.println("                                            data.");
        stream.println("                                            If this is null then the default");
        stream.println("                                            types will be: tables, views,");
        stream.println("                                            columns, indexes, foreignkeys,");
        stream.println("                                             primarykeys, uniqueconstraints.");
        stream.println("");
        stream.println("Change Log Properties:");
        stream.println(" -D<property.name>=<property.value>         Pass a name/value pair for");
        stream.println("                                            substitution in the change log(s)");
        stream.println("");
        stream.println("Default value for parameters can be stored in a file called");
        stream.println("'liquibase.properties' that is read from the current working directory.");
        stream.println("");
        stream.println("Full documentation is available at");
        stream.println("http://www.liquibase.org/documentation/command_line.html");
        stream.println("");
    }

    public Main() {
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
                if (arg.startsWith("-D")) {
                    String[] splitArg = splitArg(arg);

                    String attributeName = splitArg[0].replaceFirst("^-D", "");
                    String value = splitArg[1];

                    changeLogParameters.put(attributeName, value);
                } else {
                    commandParams.add(arg);
                }
            } else if (arg.startsWith("--")) {
                String[] splitArg = splitArg(arg);

                String attributeName = splitArg[0];
                String value = splitArg[1];

                if (StringUtils.trimToEmpty(value).equalsIgnoreCase("PROMPT")) {
                    Console c = System.console();
                    if (c == null) {
                        throw new CommandLineParsingException("Console unavailable");
                    }
                    //Prompt for value
                    if (attributeName.toLowerCase().contains("password")) {
                        value = new String(c.readPassword(attributeName+": "));
                    } else {
                        value = new String(c.readLine(attributeName+": "));
                    }
                }

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
//            } else if(arg.equals("-p")) {
//            	//Prompt for password
//            	password = new String(System.console().readPassword("DB Password:"));
//            } else if(arg.equals("-rp")) {
//            	//Prompt for reference password
//            	referencePassword = new String(System.console().readPassword("Reference DB Password:"));
            } else {
                throw new CommandLineParsingException("Unexpected value " + arg + ": parameters must start with a '--'");
            }
        }

    }

    private String[] splitArg(String arg) throws CommandLineParsingException {
        String[] splitArg = arg.split("=", 2);
        if (splitArg.length < 2) {
            throw new CommandLineParsingException("Could not parse '" + arg + "'");
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

        if (this.outputDefaultCatalog == null) {
            this.outputDefaultCatalog = "true";
        }
        if (this.outputDefaultSchema == null) {
            this.outputDefaultSchema = "true";
        }
        if (this.defaultsFile == null) {
            this.defaultsFile = "liquibase.properties";
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
                @Override
                public URLClassLoader run() {
                    return new URLClassLoader(urls.toArray(new URL[urls.size()]), Thread.currentThread().getContextClassLoader());
                }
            });

        } else {
            classLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
                @Override
                public URLClassLoader run() {
                    return new URLClassLoader(urls.toArray(new URL[urls.size()]));
                }
            });
        }

        ServiceLocator.getInstance().setResourceAccessor(new ClassLoaderResourceAccessor(classLoader));
        Thread.currentThread().setContextClassLoader(classLoader);
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
        File tempFile = File.createTempFile("liquibase.tmp", null);
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
            printHelp(System.err);
            return;
        }

        try {
            if (null != logFile) {
	            LogFactory.getInstance().getLog().setLogLevel(logLevel, logFile);
            } else {
	            LogFactory.getInstance().getLog().setLogLevel(logLevel);
            }
        } catch (IllegalArgumentException e) {
            throw new CommandLineParsingException(e.getMessage(), e);
        }

        FileSystemResourceAccessor fsOpener = new FileSystemResourceAccessor();
        CommandLineResourceAccessor clOpener = new CommandLineResourceAccessor(classLoader);
        Database database = CommandLineUtils.createDatabaseObject(classLoader, this.url,
            this.username, this.password, this.driver, this.defaultCatalogName,this.defaultSchemaName,  Boolean.parseBoolean(outputDefaultCatalog), Boolean.parseBoolean(outputDefaultSchema), this.databaseClass, this.driverPropertiesFile, this.propertyProviderClass, this.liquibaseCatalogName, this.liquibaseSchemaName);
        try {


            CompositeResourceAccessor fileOpener = new CompositeResourceAccessor(fsOpener, clOpener);

            boolean includeCatalog = Boolean.parseBoolean(getCommandParam("includeCatalog", "false"));
            boolean includeSchema = Boolean.parseBoolean(getCommandParam("includeSchema", "false"));
            boolean includeTablespace = Boolean.parseBoolean(getCommandParam("includeTablespace", "false"));
            String excludeObjects = StringUtils.trimToNull(getCommandParam("excludeObjects", null));
            String includeObjects = StringUtils.trimToNull(getCommandParam("includeObjects", null));
            DiffOutputControl diffOutputControl = new DiffOutputControl(includeCatalog, includeSchema, includeTablespace);

            if (excludeObjects != null && includeObjects != null) {
                throw new UnexpectedLiquibaseException("Cannot specify both excludeObjects and includeObjects");
            }
            if (excludeObjects != null) {
                diffOutputControl.setObjectChangeFilter(new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.EXCLUDE, excludeObjects));
            }
            if (includeObjects != null) {
                diffOutputControl.setObjectChangeFilter(new StandardObjectChangeFilter(StandardObjectChangeFilter.FilterType.INCLUDE, includeObjects));
            }

            String referenceSchemaNames = getCommandParam("schemas", null);
            CompareControl.SchemaComparison[] finalSchemaComparisons;
            CatalogAndSchema[] finalSchemas;
            if (referenceSchemaNames == null) {
                finalSchemaComparisons = new CompareControl.SchemaComparison[] {new CompareControl.SchemaComparison(new CatalogAndSchema(defaultCatalogName, defaultSchemaName), new CatalogAndSchema(defaultCatalogName, defaultSchemaName))};
                finalSchemas = new CatalogAndSchema[] {new CatalogAndSchema(defaultCatalogName, defaultSchemaName)};
            } else {
                List<CompareControl.SchemaComparison> schemaComparisons = new ArrayList<CompareControl.SchemaComparison>();
                List<CatalogAndSchema> schemas = new ArrayList<CatalogAndSchema>();
                for (String schema : referenceSchemaNames.split(",")) {
                    CatalogAndSchema correctedSchema = new CatalogAndSchema(null, schema).customize(database);
                    schemaComparisons.add(new CompareControl.SchemaComparison(correctedSchema, correctedSchema));
                    schemas.add(correctedSchema);
                    diffOutputControl.addIncludedSchema(correctedSchema);
                }
                finalSchemaComparisons  = schemaComparisons.toArray(new CompareControl.SchemaComparison[schemaComparisons.size()]);
                finalSchemas  = schemas.toArray(new CatalogAndSchema[schemas.size()]);
            }

            for (CompareControl.SchemaComparison schema : finalSchemaComparisons) {
                diffOutputControl.addIncludedSchema(schema.getReferenceSchema());
                diffOutputControl.addIncludedSchema(schema.getComparisonSchema());
            }

            if ("diff".equalsIgnoreCase(command)) {
                CommandLineUtils.doDiff(createReferenceDatabaseFromCommandParams(commandParams), database, StringUtils.trimToNull(diffTypes), finalSchemaComparisons);
                return;
            } else if ("diffChangeLog".equalsIgnoreCase(command)) {
                CommandLineUtils.doDiffToChangeLog(changeLogFile, createReferenceDatabaseFromCommandParams(commandParams), database, diffOutputControl,  StringUtils.trimToNull(diffTypes), finalSchemaComparisons);
                return;
            } else if ("generateChangeLog".equalsIgnoreCase(command)) {
                String changeLogFile = this.changeLogFile;
                if (changeLogFile == null) {
                    changeLogFile = ""; //will output to stdout
                }
                // By default the generateChangeLog command is destructive, and
                // Liquibase's attempt to append doesn't work properly. Just
                // fail the build if the file already exists.
                File file = new File(changeLogFile);
                if ( file.exists() ) {
                    throw new LiquibaseException("ChangeLogFile " + changeLogFile + " already exists!");
                }

	            CommandLineUtils.doGenerateChangeLog(changeLogFile, database, finalSchemas, StringUtils.trimToNull(diffTypes), StringUtils.trimToNull(changeSetAuthor), StringUtils.trimToNull(changeSetContext), StringUtils.trimToNull(dataOutputDirectory), diffOutputControl);
                return;
            } else if ("snapshot".equalsIgnoreCase(command)) {
                SnapshotCommand command = new SnapshotCommand();
                command.setDatabase(database);
                command.setSchemas(getCommandParam("schemas", database.getDefaultSchema().getSchemaName()));
                System.out.println(command.execute());
                return;
            } else if ("executeSql".equalsIgnoreCase(command)) {
                ExecuteSqlCommand command = new ExecuteSqlCommand();
                command.setDatabase(database);
                command.setSql(getCommandParam("sql", null));
                command.setSqlFile(getCommandParam("sqlFile", null));
                System.out.println(command.execute());
                return;
            } else if ("snapshotReference".equalsIgnoreCase(command)) {
                SnapshotCommand command = new SnapshotCommand();
                Database referenceDatabase = createReferenceDatabaseFromCommandParams(commandParams);
                command.setDatabase(referenceDatabase);
                command.setSchemas(getCommandParam("schemas", referenceDatabase.getDefaultSchema().getSchemaName()));
                System.out.println(command.execute());
                return;
            }


            Liquibase liquibase = new Liquibase(changeLogFile, fileOpener, database);
            liquibase.setCurrentDateTimeFunction(currentDateTimeFunction);
            for (Map.Entry<String, Object> entry : changeLogParameters.entrySet()) {
                liquibase.setChangeLogParameter(entry.getKey(), entry.getValue());
            }

            if ("listLocks".equalsIgnoreCase(command)) {
                liquibase.reportLocks(System.err);
                return;
            } else if ("releaseLocks".equalsIgnoreCase(command)) {
                LockService lockService = LockServiceFactory.getInstance().getLockService(database);
                lockService.forceReleaseLock();
                System.err.println("Successfully released all database change log locks for " + liquibase.getDatabase().getConnection().getConnectionUserName() + "@" + liquibase.getDatabase().getConnection().getURL());
                return;
            } else if ("tag".equalsIgnoreCase(command)) {
                liquibase.tag(commandParams.iterator().next());
                System.err.println("Successfully tagged " + liquibase.getDatabase().getConnection().getConnectionUserName() + "@" + liquibase.getDatabase().getConnection().getURL());
                return;
            } else if ("dropAll".equals(command)) {
                liquibase.dropAll();
                System.err.println("All objects dropped from " + liquibase.getDatabase().getConnection().getConnectionUserName() + "@" + liquibase.getDatabase().getConnection().getURL());
                return;
            } else if ("status".equalsIgnoreCase(command)) {
                boolean runVerbose = false;

                if (commandParams.contains("--verbose")) {
                    runVerbose = true;
                }
                liquibase.reportStatus(runVerbose, new Contexts(contexts), new LabelExpression(labels), getOutputWriter());
                return;
            } else if ("unexpectedChangeSets".equalsIgnoreCase(command)) {
                boolean runVerbose = false;

                if (commandParams.contains("--verbose")) {
                    runVerbose = true;
                }
                liquibase.reportUnexpectedChangeSets(runVerbose, contexts, getOutputWriter());
                return;
            } else if ("validate".equalsIgnoreCase(command)) {
                try {
                    liquibase.validate();
                } catch (ValidationFailedException e) {
                    e.printDescriptiveError(System.err);
                    return;
                }
                System.err.println("No validation errors found");
                return;
            } else if ("clearCheckSums".equalsIgnoreCase(command)) {
                liquibase.clearCheckSums();
                return;
            } else if ("calculateCheckSum".equalsIgnoreCase(command)) {
                CheckSum checkSum = null;
                checkSum = liquibase.calculateCheckSum(commandParams.iterator().next());
                System.out.println(checkSum);
                return;
            } else if ("dbdoc".equalsIgnoreCase(command)) {
                if (commandParams.size() == 0) {
                    throw new CommandLineParsingException("dbdoc requires an output directory");
                }
                if (changeLogFile == null) {
                    throw new CommandLineParsingException("dbdoc requires a changeLog parameter");
                }
                liquibase.generateDocumentation(commandParams.iterator().next(), contexts);
                return;
            }

            try {
                if ("update".equalsIgnoreCase(command)) {
                    liquibase.update(new Contexts(contexts), new LabelExpression(labels));
                } else if ("changelogSync".equalsIgnoreCase(command)) {
                    liquibase.changeLogSync(new Contexts(contexts), new LabelExpression(labels));
                } else if ("changelogSyncSQL".equalsIgnoreCase(command)) {
                    liquibase.changeLogSync(new Contexts(contexts), new LabelExpression(labels), getOutputWriter());
                } else if ("markNextChangeSetRan".equalsIgnoreCase(command)) {
                    liquibase.markNextChangeSetRan(new Contexts(contexts), new LabelExpression(labels));
                } else if ("markNextChangeSetRanSQL".equalsIgnoreCase(command)) {
                    liquibase.markNextChangeSetRan(new Contexts(contexts), new LabelExpression(labels), getOutputWriter());
                } else if ("updateCount".equalsIgnoreCase(command)) {
                    liquibase.update(Integer.parseInt(commandParams.iterator().next()), new Contexts(contexts), new LabelExpression(labels));
                } else if ("updateCountSQL".equalsIgnoreCase(command)) {
                    liquibase.update(Integer.parseInt(commandParams.iterator().next()), new Contexts(contexts), new LabelExpression(labels), getOutputWriter());
                } else if ("updateSQL".equalsIgnoreCase(command)) {
                    liquibase.update(new Contexts(contexts), new LabelExpression(labels), getOutputWriter());
                } else if ("rollback".equalsIgnoreCase(command)) {
                    if (commandParams == null || commandParams.size() == 0) {
                        throw new CommandLineParsingException("rollback requires a rollback tag");
                    }
                    liquibase.rollback(commandParams.iterator().next(), new Contexts(contexts), new LabelExpression(labels));
                } else if ("rollbackToDate".equalsIgnoreCase(command)) {
                    if (commandParams == null || commandParams.size() == 0) {
                        throw new CommandLineParsingException("rollback requires a rollback date");
                    }
                    liquibase.rollback(new ISODateFormat().parse(commandParams.iterator().next()), new Contexts(contexts), new LabelExpression(labels));
                } else if ("rollbackCount".equalsIgnoreCase(command)) {
                    liquibase.rollback(Integer.parseInt(commandParams.iterator().next()), new Contexts(contexts), new LabelExpression(labels));

                } else if ("rollbackSQL".equalsIgnoreCase(command)) {
                    if (commandParams == null || commandParams.size() == 0) {
                        throw new CommandLineParsingException("rollbackSQL requires a rollback tag");
                    }
                    liquibase.rollback(commandParams.iterator().next(), new Contexts(contexts), new LabelExpression(labels), getOutputWriter());
                } else if ("rollbackToDateSQL".equalsIgnoreCase(command)) {
                    if (commandParams == null || commandParams.size() == 0) {
                        throw new CommandLineParsingException("rollbackToDateSQL requires a rollback date");
                    }
                    liquibase.rollback(new ISODateFormat().parse(commandParams.iterator().next()), new Contexts(contexts), new LabelExpression(labels), getOutputWriter());
                } else if ("rollbackCountSQL".equalsIgnoreCase(command)) {
                    if (commandParams == null || commandParams.size() == 0) {
                        throw new CommandLineParsingException("rollbackCountSQL requires a rollback tag");
                    }

                    liquibase.rollback(Integer.parseInt(commandParams.iterator().next()), new Contexts(contexts), new LabelExpression(labels), getOutputWriter());
                } else if ("futureRollbackSQL".equalsIgnoreCase(command)) {
                    liquibase.futureRollbackSQL(null, new Contexts(contexts), new LabelExpression(labels), getOutputWriter());
                } else if ("futureRollbackCountSQL".equalsIgnoreCase(command)) {
                    if (commandParams == null || commandParams.size() == 0) {
                        throw new CommandLineParsingException("futureRollbackCountSQL requires a rollback count");
                    }

                    liquibase.futureRollbackSQL(Integer.parseInt(commandParams.iterator().next()), new Contexts(contexts), new LabelExpression(labels), getOutputWriter());
                } else if ("updateTestingRollback".equalsIgnoreCase(command)) {
                    liquibase.updateTestingRollback(new Contexts(contexts), new LabelExpression(labels));
                } else {
                    throw new CommandLineParsingException("Unknown command: " + command);
                }
            } catch (ParseException e) {
                throw new CommandLineParsingException("Unexpected date/time format.  Use 'yyyy-MM-dd'T'HH:mm:ss'");
            }
        } finally {
            try {
                database.rollback();
                database.close();
            } catch (DatabaseException e) {
	            LogFactory.getInstance().getLog().warning("problem closing connection", e);
            }
        }
    }

    private String getCommandParam(String paramName, String defaultValue) throws CommandLineParsingException {
        for (String param : commandParams) {
            if (!param.contains("=")) {
                return null;
            }
            String[] splitArg = splitArg(param);

            String attributeName = splitArg[0];
            String value = splitArg[1];
            if (attributeName.equalsIgnoreCase(paramName)) {
                return value;
            }
        }

        return defaultValue;
    }

    private Database createReferenceDatabaseFromCommandParams(Set<String> commandParams) throws CommandLineParsingException, DatabaseException {
        String driver = referenceDriver;
        String url = referenceUrl;
        String username = referenceUsername;
        String password = referencePassword;
        String defaultSchemaName = this.defaultSchemaName;
        String defaultCatalogName = this.defaultCatalogName;

        for (String param : commandParams) {
            String[] splitArg = splitArg(param);

            String attributeName = splitArg[0];
            String value = splitArg[1];
            if ("referenceDriver".equalsIgnoreCase(attributeName)) {
                driver = value;
            } else if ("referenceUrl".equalsIgnoreCase(attributeName)) {
                url = value;
            } else if ("referenceUsername".equalsIgnoreCase(attributeName)) {
                username = value;
            } else if ("referencePassword".equalsIgnoreCase(attributeName)) {
                password = value;
            } else if ("referenceDefaultCatalogName".equalsIgnoreCase(attributeName)) {
                defaultCatalogName = value;
            } else if ("referenceDefaultSchemaName".equalsIgnoreCase(attributeName)) {
                defaultSchemaName = value;
            } else if ("dataOutputDirectory".equalsIgnoreCase(attributeName)) {
                dataOutputDirectory = value;
            }
        }

//        if (driver == null) {
//            driver = DatabaseFactory.getWriteExecutor().findDefaultDriver(url);
//        }

        if (url == null) {
            throw new CommandLineParsingException("referenceUrl parameter missing");
        }

        return CommandLineUtils.createDatabaseObject(classLoader, url, username, password, driver, defaultCatalogName, defaultSchemaName, Boolean.parseBoolean(outputDefaultCatalog), Boolean.parseBoolean(outputDefaultSchema), null, null, this.propertyProviderClass, this.liquibaseCatalogName, this.liquibaseSchemaName);
//        Driver driverObject;
//        try {
//            driverObject = (Driver) Class.forName(driver, true, classLoader).newInstance();
//        } catch (Exception e) {
//            throw new RuntimeException("Cannot find database driver: " + e.getMessage());
//        }
//
//        Properties info = new Properties();
//        info.put("user", username);
//        info.put("password", password);
//
//        Connection connection;
//        try {
//            connection = driverObject.connect(url, info);
//        } catch (SQLException e) {
//            throw new DatabaseException("Connection could not be created to " + url + ": " + e.getMessage(), e);
//        }
//        if (connection == null) {
//            throw new DatabaseException("Connection could not be created to " + url + " with driver " + driver.getClass().getName() + ".  Possibly the wrong driver for the given database URL");
//        }
//
//        Database database = DatabaseFactory.getWriteExecutor().findCorrectDatabaseImplementation(connection);
//        database.setDefaultSchemaName(defaultSchemaName);
//
//        return database;
    }

    private Writer getOutputWriter() throws UnsupportedEncodingException, IOException {
        String charsetName = LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding();

        if (outputFile != null) {
            try {
                FileOutputStream fileOut = new FileOutputStream(outputFile, false);
                return new OutputStreamWriter(fileOut, charsetName);
            } catch (IOException e) {
                System.err.printf("Could not create output file %s\n", outputFile);
                throw e;
            }
        } else {
            return new OutputStreamWriter(System.out, charsetName);
        }
    }

    public boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows ");
    }
}
