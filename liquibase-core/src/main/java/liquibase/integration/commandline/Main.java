package liquibase.integration.commandline;


import liquibase.CatalogAndSchema;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.change.CheckSum;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.command.CommandFactory;
import liquibase.command.core.DropAllCommand;
import liquibase.command.core.ExecuteSqlCommand;
import liquibase.command.core.SnapshotCommand;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
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
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.ServiceLocator;
import liquibase.util.ISODateFormat;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
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
    private static final String ERRORMSG_UNEXPECTED_COMMANDLINE_PARAMETERS = "unexpected command parameters: ";

    private static final String OPTION_VERBOSE = "--verbose";

    private static final String COMMAND_CALCULATE_CHECKSUM = "calculateCheckSum";
    private static final String COMMAND_CHANGELOG_SYNC = "changelogSync";
    private static final String COMMAND_CHANGELOG_SYNC_SQL = "changelogSyncSQL";
    private static final String COMMAND_CLEAR_CHECKSUMS = "clearCheckSums";
    private static final String COMMAND_DB_DOC = "dbDoc";
    private static final String COMMAND_DIFF = "diff";
    private static final String COMMAND_DIFF_CHANGELOG = "diffChangeLog";
    private static final String COMMAND_DROP_ALL = "dropAll";
    private static final String COMMAND_EXECUTE_SQL = "executeSql";
    private static final String COMMAND_FUTURE_ROLLBACK_COUNT_SQL = "futureRollbackCountSQL";
    private static final String COMMAND_FUTURE_ROLLBACK_FROM_TAG_SQL = "futureRollbackFromTagSQL";
    private static final String COMMAND_FUTURE_ROLLBACK_SQL = "futureRollbackSQL";
    private static final String COMMAND_FUTURE_ROLLBACK_TO_TAG_SQL = "futureRollbackToTagSQL";
    private static final String COMMAND_GENERATE_CHANGELOG = "generateChangeLog";
    private static final String COMMAND_HELP = "help";
    private static final String COMMAND_LIST_LOCKS = "listLocks";
    private static final String COMMAND_MARK_NEXT_CHANGESET_RAN = "markNextChangeSetRan";
    private static final String COMMAND_MARK_NEXT_CHANGESET_RAN_SQL = "markNextChangeSetRanSQL";
    private static final String COMMAND_MIGRATE = "migrate";
    private static final String COMMAND_MIGRATE_SQL = "migrateSQL";
    private static final String COMMAND_RELEASE_LOCKS = "releaseLocks";
    private static final String COMMAND_ROLLBACK = "rollback";
    private static final String COMMAND_ROLLBACK_COUNT = "rollbackCount";
    private static final String COMMAND_ROLLBACK_COUNT_SQL = "rollbackCountSQL";
    private static final String COMMAND_ROLLBACK_SCRIPT = "rollbackScript";
    private static final String COMMAND_ROLLBACK_SQL = "rollbackSQL";
    private static final String COMMAND_ROLLBACK_TO_DATE = "rollbackToDate";
    private static final String COMMAND_ROLLBACK_TO_DATE_SQL = "rollbackToDateSQL";
    private static final String COMMAND_SNAPSHOT = "snapshot";
    private static final String COMMAND_SNAPSHOT_REFERENCE = "snapshotReference";
    private static final String COMMAND_STATUS = "status";
    private static final String COMMAND_TAG = "tag";
    private static final String COMMAND_TAG_EXISTS = "tagExists";
    private static final String COMMAND_UNEXPECTED_CHANGESETS = "unexpectedChangeSets";
    private static final String COMMAND_UPDATE = "update";
    private static final String COMMAND_UPDATE_COUNT = "updateCount";
    private static final String COMMAND_UPDATE_COUNT_SQL = "updateCountSQL";
    private static final String COMMAND_UPDATE_SQL = "updateSQL";
    private static final String COMMAND_UPDATE_TESTING_ROLLBACK = "updateTestingRollback";
    private static final String COMMAND_UPDATE_TO_TAG = "updateToTag";
    private static final String COMMAND_UPDATE_TO_TAG_SQL = "updateToTagSQL";
    private static final String COMMAND_VALIDATE = "validate";

    private static final String OPTION_CHANGELOG_FILE = "changeLogFile";
    private static final String OPTION_DATA_OUTPUT_DIRECTORY = "dataOutputDirectory";
    private static final String OPTION_DIFF_TYPES = "diffTypes";
    private static final String OPTION_EXCLUDE_OBJECTS = "excludeObjects";
    private static final String OPTION_INCLUDE_CATALOG = "includeCatalog";
    private static final String OPTION_INCLUDE_OBJECTS = "includeObjects";
    private static final String OPTION_INCLUDE_SCHEMA = "includeSchema";
    private static final String OPTION_INCLUDE_TABLESPACE = "includeTablespace";
    private static final String OPTION_OUTPUT_SCHEMAS_AS = "outputSchemasAs";
    private static final String OPTION_REFERENCE_DEFAULT_CATALOG_NAME = "referenceDefaultCatalogName";
    private static final String OPTION_REFERENCE_DEFAULT_SCHEMA_NAME = "referenceDefaultSchemaName";
    private static final String OPTION_REFERENCE_DRIVER = "referenceDriver";
    @SuppressWarnings("squid:S2068") // SONAR confuses this constant name with a hard-coded password :)
    private static final String OPTION_REFERENCE_PASSWORD = "referencePassword";
    private static final String OPTION_REFERENCE_SCHEMAS = "referenceSchemas";
    private static final String OPTION_REFERENCE_URL = "referenceUrl";
    private static final String OPTION_REFERENCE_USERNAME = "referenceUsername";
    private static final String OPTION_SCHEMAS = "schemas";
    private static final String OPTION_URL = "url";

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
    protected String databaseChangeLogTableName;
    protected String databaseChangeLogLockTableName;
    protected String defaultCatalogName;
    protected String changeLogFile;
    protected String overwriteOutputFile;
    protected String classpath;
    protected String contexts;
    protected String labels;
    protected String driverPropertiesFile;
    protected String propertyProviderClass = null;
    protected String changeExecListenerClass;
    protected String changeExecListenerPropertiesFile;
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
    protected String referenceDefaultCatalogName;
    protected String referenceDefaultSchemaName;

    protected String currentDateTimeFunction;

    protected String command;
    protected Set<String> commandParams = new LinkedHashSet<>();

    protected String logLevel;
    protected String logFile;

    protected Map<String, Object> changeLogParameters = new HashMap<>();

    protected String outputFile;

    public static void main(String[] args) throws CommandLineParsingException, IOException {
        try {
            run(args);
        } catch (LiquibaseException ignored) {
            System.exit(-1);
        }
        System.exit(0);
    }

    public static void run(String[] args) throws CommandLineParsingException, IOException, LiquibaseException {
        try {
            GlobalConfiguration globalConfiguration = LiquibaseConfiguration.getInstance().getConfiguration
                    (GlobalConfiguration.class);

            if (!globalConfiguration.getShouldRun()) {
                System.err.println("DB-Manul did not run because '" + LiquibaseConfiguration.getInstance()
                        .describeValueLookupLogic(globalConfiguration.getProperty(GlobalConfiguration.SHOULD_RUN)) +
                        " was set to false");
                return;
            }

            Main main = new Main();
            System.out.println(CommandLineUtils.getBanner());

            if (args.length == 1 && "--help".equals(args[0])) {
                main.printHelp(System.out);
                return;
            } else if (args.length == 1 && "--version".equals(args[0])) {
                System.out.println("DB-Manul Version: " + LiquibaseUtil.getBuildVersion() + StreamUtil.getLineSeparator
                        ());
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
                InputStream resourceAsStream = main.getClass().getClassLoader().getResourceAsStream
                        (localDefaultsPathName);
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
                try {
                    main.parsePropertiesFile(stream);
                } finally {
                    stream.close();
                }
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
            if (!setupMessages.isEmpty()) {
                main.printHelp(setupMessages, System.err);
                return;
            }

            main.applyDefaults();
            main.configureClassLoader();
            main.doMigration();

            if (COMMAND_UPDATE.equals(main.command)) {
                System.err.println("DB-Manul Update Successful");
            } else if (main.command.startsWith(COMMAND_ROLLBACK) && !main.command.endsWith("SQL")) {
                System.err.println("DB-Manul Rollback Successful");
            } else if (!main.command.endsWith("SQL")) {
                System.err.println("DB-Manul '" + main.command + "' Successful");
            }
        } catch (Exception e) {
            String message = e.getMessage();
            if (e.getCause() != null) {
                message = e.getCause().getMessage();
            }
            if (message == null) {
                message = "Unknown Reason";
            }
            // At a minimum, log the message.  We don't need to print the stack
            // trace because the logger already did that upstream.
            try {
                if (e.getCause() instanceof ValidationFailedException) {
                    ((ValidationFailedException) e.getCause()).printDescriptiveError(System.out);
                } else {
                    System.err.println("Unexpected error running DB-Manul: " + message + "\n");
                    LogFactory.getInstance().getLog().severe(message, e);
                    System.err.println(generateLogLevelWarningMessage());
                }
            } catch (Exception e1) {
                e.printStackTrace();
            }
            throw new LiquibaseException("Unexpected error running DB-Manul: " + message, e);
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

    private static String[] splitArg(String arg) throws CommandLineParsingException {
        String[] splitArg = arg.split("=", 2);
        if (splitArg.length < 2) {
            throw new CommandLineParsingException("Could not parse '" + arg + "'");
        }

        splitArg[0] = splitArg[0].replaceFirst("--", "");
        return splitArg;
    }

    /**
     * On windows machines, it splits args on '=' signs.  Put it back like it was.
     */
    protected String[] fixupArgs(String[] args) {
        List<String> fixedArgs = new ArrayList<>();

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

            arg = arg.replace("\\,", ","); //sometimes commas come through escaped still
            fixedArgs.add(arg);
        }

        return fixedArgs.toArray(new String[fixedArgs.size()]);
    }

    protected List<String> checkSetup() {
        List<String> messages = new ArrayList<>();
        if (command == null) {
            messages.add("Command not passed");
        } else if (!isCommand(command)) {
            messages.add("Unknown command: " + command);
        } else {
            if (url == null) {
                messages.add("--" + OPTION_URL + " is required");
            }

            if (isChangeLogRequired(command) && changeLogFile == null) {
                messages.add("--" + OPTION_CHANGELOG_FILE + " is required");
            }

            if (isNoArgCommand(command) && !commandParams.isEmpty()) {
                messages.add(ERRORMSG_UNEXPECTED_COMMANDLINE_PARAMETERS + commandParams);
            } else {
                validateCommandParameters(messages);
            }
        }
        return messages;
    }

    private void checkForUnexpectedCommandParameter(List<String> messages) {
        if (COMMAND_UPDATE_COUNT.equalsIgnoreCase(command)
                || COMMAND_UPDATE_COUNT_SQL.equalsIgnoreCase(command)
                || COMMAND_UPDATE_TO_TAG.equalsIgnoreCase(command)
                || COMMAND_UPDATE_TO_TAG_SQL.equalsIgnoreCase(command)
                || COMMAND_CALCULATE_CHECKSUM.equalsIgnoreCase(command)
                || COMMAND_DB_DOC.equalsIgnoreCase(command)
                || COMMAND_TAG.equalsIgnoreCase(command)
                || COMMAND_TAG_EXISTS.equalsIgnoreCase(command)) {

            if (commandParams.size() > 0 && commandParams.iterator().next().startsWith("-")) {
                messages.add(ERRORMSG_UNEXPECTED_COMMANDLINE_PARAMETERS + commandParams);
            }
        } else if (COMMAND_STATUS.equalsIgnoreCase(command)
                || COMMAND_UNEXPECTED_CHANGESETS.equalsIgnoreCase(command)) {
            if (commandParams.size() > 0 && !commandParams.iterator().next().equalsIgnoreCase(OPTION_VERBOSE)) {
                messages.add(ERRORMSG_UNEXPECTED_COMMANDLINE_PARAMETERS + commandParams);
            }
        } else if (COMMAND_DIFF.equalsIgnoreCase(command)
                || COMMAND_DIFF_CHANGELOG.equalsIgnoreCase(command)) {
            if (commandParams.size() > 0) {
                for (String cmdParm : commandParams) {
                    if (!cmdParm.startsWith("--" + OPTION_REFERENCE_USERNAME)
                            && !cmdParm.startsWith("--" + OPTION_REFERENCE_PASSWORD)
                            && !cmdParm.startsWith("--" + OPTION_REFERENCE_DRIVER)
                            && !cmdParm.startsWith("--" + OPTION_REFERENCE_DEFAULT_CATALOG_NAME)
                            && !cmdParm.startsWith("--" + OPTION_REFERENCE_DEFAULT_SCHEMA_NAME)
                            && !cmdParm.startsWith("--" + OPTION_INCLUDE_SCHEMA)
                            && !cmdParm.startsWith("--" + OPTION_INCLUDE_CATALOG)
                            && !cmdParm.startsWith("--" + OPTION_INCLUDE_TABLESPACE)
                            && !cmdParm.startsWith("--" + OPTION_SCHEMAS)
                            && !cmdParm.startsWith("--" + OPTION_OUTPUT_SCHEMAS_AS)
                            && !cmdParm.startsWith("--" + OPTION_REFERENCE_SCHEMAS)
                            && !cmdParm.startsWith("--" + OPTION_REFERENCE_URL)
                            && !cmdParm.startsWith("--" + OPTION_EXCLUDE_OBJECTS)
                            && !cmdParm.startsWith("--" + OPTION_INCLUDE_OBJECTS)
                            && !cmdParm.startsWith("--" + OPTION_DIFF_TYPES)) {
                        messages.add("unexpected command parameter: " + cmdParm);
                    }
                }
            } else if ((COMMAND_SNAPSHOT.equalsIgnoreCase(command)
                    || COMMAND_GENERATE_CHANGELOG.equalsIgnoreCase(command)
            )
                    && (!commandParams.isEmpty())) {
                for (String cmdParm : commandParams) {
                    if (!cmdParm.startsWith("--" + OPTION_INCLUDE_SCHEMA)
                            && !cmdParm.startsWith("--" + OPTION_INCLUDE_CATALOG)
                            && !cmdParm.startsWith("--" + OPTION_INCLUDE_TABLESPACE)
                            && !cmdParm.startsWith("--" + OPTION_SCHEMAS)) {
                        messages.add("unexpected command parameter: " + cmdParm);
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
        if ((commandParams.isEmpty() || commandParams.iterator().next().startsWith("-"))
                && (COMMAND_CALCULATE_CHECKSUM.equalsIgnoreCase(command))) {
            messages.add("missing changeSet identifier");
        }
    }

    private void checkForMalformedCommandParameters(final List<String> messages) {
        if (commandParams.isEmpty())
            return;

        if (COMMAND_CALCULATE_CHECKSUM.equalsIgnoreCase(command)) {
            for (final String param : commandParams) {
                if (param != null && !param.startsWith("-")) {
                    final String[] parts = param.split("::");
                    if (parts == null || parts.length < 3) {
                        messages.add("changeSet identifier must be of the form filepath::id::author");
                        break;
                    }
                }
            }
        } else if (COMMAND_DIFF_CHANGELOG.equalsIgnoreCase(command)
                && diffTypes != null
                && diffTypes.toLowerCase().contains("data")) {
            messages.add("Including " + OPTION_DIFF_TYPES + "=data in the diffChangeLog command has no effect. " +
                    "This option should only be used with the " + COMMAND_GENERATE_CHANGELOG + " command.");
        }
    }

    private boolean isChangeLogRequired(String command) {
        return command.toLowerCase().startsWith(COMMAND_UPDATE)
                || command.toLowerCase().startsWith(COMMAND_ROLLBACK)
                || COMMAND_CALCULATE_CHECKSUM.equalsIgnoreCase(command)
                || COMMAND_STATUS.equalsIgnoreCase(command)
                || COMMAND_VALIDATE.equalsIgnoreCase(command)
                || COMMAND_CHANGELOG_SYNC.equalsIgnoreCase(command)
                || COMMAND_CHANGELOG_SYNC_SQL.equalsIgnoreCase(command)
                || COMMAND_GENERATE_CHANGELOG.equalsIgnoreCase(command);
    }

    private boolean isCommand(String arg) {
        return COMMAND_MIGRATE.equals(arg)
                || COMMAND_MIGRATE_SQL.equalsIgnoreCase(arg)
                || COMMAND_UPDATE.equalsIgnoreCase(arg)
                || COMMAND_UPDATE_SQL.equalsIgnoreCase(arg)
                || COMMAND_UPDATE_COUNT.equalsIgnoreCase(arg)
                || COMMAND_UPDATE_COUNT_SQL.equalsIgnoreCase(arg)
                || COMMAND_UPDATE_TO_TAG.equalsIgnoreCase(arg)
                || COMMAND_UPDATE_TO_TAG_SQL.equalsIgnoreCase(arg)
                || COMMAND_ROLLBACK.equalsIgnoreCase(arg)
                || COMMAND_ROLLBACK_TO_DATE.equalsIgnoreCase(arg)
                || COMMAND_ROLLBACK_COUNT.equalsIgnoreCase(arg)
                || COMMAND_ROLLBACK_SQL.equalsIgnoreCase(arg)
                || COMMAND_ROLLBACK_TO_DATE_SQL.equalsIgnoreCase(arg)
                || COMMAND_ROLLBACK_COUNT_SQL.equalsIgnoreCase(arg)
                || COMMAND_FUTURE_ROLLBACK_SQL.equalsIgnoreCase(arg)
                || COMMAND_FUTURE_ROLLBACK_COUNT_SQL.equalsIgnoreCase(arg)
                || COMMAND_FUTURE_ROLLBACK_TO_TAG_SQL.equalsIgnoreCase(arg)
                || COMMAND_UPDATE_TESTING_ROLLBACK.equalsIgnoreCase(arg)
                || COMMAND_TAG.equalsIgnoreCase(arg)
                || COMMAND_TAG_EXISTS.equalsIgnoreCase(arg)
                || COMMAND_LIST_LOCKS.equalsIgnoreCase(arg)
                || COMMAND_DROP_ALL.equalsIgnoreCase(arg)
                || COMMAND_RELEASE_LOCKS.equalsIgnoreCase(arg)
                || COMMAND_STATUS.equalsIgnoreCase(arg)
                || COMMAND_UNEXPECTED_CHANGESETS.equalsIgnoreCase(arg)
                || COMMAND_VALIDATE.equalsIgnoreCase(arg)
                || COMMAND_HELP.equalsIgnoreCase(arg)
                || COMMAND_DIFF.equalsIgnoreCase(arg)
                || COMMAND_DIFF_CHANGELOG.equalsIgnoreCase(arg)
                || COMMAND_GENERATE_CHANGELOG.equalsIgnoreCase(arg)
                || COMMAND_SNAPSHOT.equalsIgnoreCase(arg)
                || COMMAND_SNAPSHOT_REFERENCE.equalsIgnoreCase(arg)
                || COMMAND_EXECUTE_SQL.equalsIgnoreCase(arg)
                || COMMAND_CALCULATE_CHECKSUM.equalsIgnoreCase(arg)
                || COMMAND_CLEAR_CHECKSUMS.equalsIgnoreCase(arg)
                || COMMAND_DB_DOC.equalsIgnoreCase(arg)
                || COMMAND_CHANGELOG_SYNC.equalsIgnoreCase(arg)
                || COMMAND_CHANGELOG_SYNC_SQL.equalsIgnoreCase(arg)
                || COMMAND_MARK_NEXT_CHANGESET_RAN.equalsIgnoreCase(arg)
                || COMMAND_MARK_NEXT_CHANGESET_RAN_SQL.equalsIgnoreCase(arg);
    }

    private boolean isNoArgCommand(String arg) {
        return COMMAND_MIGRATE.equals(arg)
                || COMMAND_MIGRATE_SQL.equalsIgnoreCase(arg)
                || COMMAND_UPDATE.equalsIgnoreCase(arg)
                || COMMAND_UPDATE_SQL.equalsIgnoreCase(arg)
                || COMMAND_FUTURE_ROLLBACK_SQL.equalsIgnoreCase(arg)
                || COMMAND_UPDATE_TESTING_ROLLBACK.equalsIgnoreCase(arg)
                || COMMAND_LIST_LOCKS.equalsIgnoreCase(arg)
                || COMMAND_DROP_ALL.equalsIgnoreCase(arg)
                || COMMAND_RELEASE_LOCKS.equalsIgnoreCase(arg)
                || COMMAND_VALIDATE.equalsIgnoreCase(arg)
                || COMMAND_HELP.equalsIgnoreCase(arg)
                || COMMAND_CLEAR_CHECKSUMS.equalsIgnoreCase(arg)
                || COMMAND_CHANGELOG_SYNC.equalsIgnoreCase(arg)
                || COMMAND_CHANGELOG_SYNC_SQL.equalsIgnoreCase(arg)
                || COMMAND_MARK_NEXT_CHANGESET_RAN.equalsIgnoreCase(arg)
                || COMMAND_MARK_NEXT_CHANGESET_RAN_SQL.equalsIgnoreCase(arg);
    }

    protected void parsePropertiesFile(InputStream propertiesInputStream) throws IOException,
            CommandLineParsingException {
        Properties props = new Properties();
        props.load(propertiesInputStream);
        if (props.containsKey("strict")) {
            strict = Boolean.valueOf(props.getProperty("strict"));
        }

        for (Map.Entry entry : props.entrySet()) {
            try {
                if (entry.getKey().equals("promptOnNonLocalDatabase")) {
                    continue;
                }
                if (((String) entry.getKey()).startsWith("parameter.")) {
                    changeLogParameters.put(((String) entry.getKey()).replaceFirst("^parameter.", ""), entry.getValue
                            ());
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
            } catch (NoSuchFieldException ignored) {
                if (strict) {
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
    }

    protected void printWarning(List<String> warningMessages, PrintStream stream) {
        stream.println("Warnings:");
        for (String message : warningMessages) {
            stream.println("  " + message);
        }
        stream.println();
    }

    @SuppressWarnings("squid:S2068") // SONAR interprets the command line help as hardcoded passwords.
    protected void printHelp(PrintStream stream) {
        stream.println("Usage: java -jar dbmanul.jar [options] [command]");
        stream.println("");
        stream.println("Standard Commands:");
        stream.println(" update                         Updates database to current version");
        stream.println(" updateSQL                      Writes SQL to update database to current");
        stream.println("                                version to STDOUT");
        stream.println(" updateCount <num>              Applies next NUM changes to the database");
        stream.println(" updateCountSQL <num>           Writes SQL to apply next NUM changes");
        stream.println("                                to the database");
        stream.println(" updateToTag <tag>              Updates the database to the changeSet with the");
        stream.println("                                specified tag");
        stream.println(" updateToTagSQL <tag>           Writes (to standard out) the SQL to update to");
        stream.println("                                the changeSet with the specified tag");
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
        stream.println(" futureRollbackFromTagSQL <tag> Writes (to standard out) the SQL to roll back");
        stream.println("                                the database to its current state after the");
        stream.println("                                changes up to the specified tag have been");
        stream.println("                                applied");
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
        stream.println(" tagExists <tag string>    Checks whether the given tag is already existing");
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
        stream.println("                                            is PROMPT, DB-Manul will");
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
        stream.println(" --delimiter=<string>                       Used with executeSql command to set");
        stream.println("                                            the string used to break up files");
        stream.println("                                            that consist of multiple statements.");
        stream.println(" --driverPropertiesFile=</path/to/file.properties>  File with custom properties");
        stream.println("                                            to be set on the JDBC connection");
        stream.println("                                            to be created");
        stream.println(" --changeExecListenerClass=<ChangeExecListener.ClassName>     Custom Change Exec");
        stream.println("                                            listener implementation to use");
        stream.println(" --changeExecListenerPropertiesFile=</path/to/file.properties> Properties for");
        stream.println("                                            Custom Change Exec listener");
        stream.println(" --liquibaseCatalogName=<name>              The name of the catalog with the");
        stream.println("                                            liquibase tables");
        stream.println(" --liquibaseSchemaName=<name>               The name of the schema with the");
        stream.println("                                            liquibase tables");
        stream.println(" --databaseChangeLogTableName=<name>        The name of the DB-Manul ChangeLog");
        stream.println("                                            table (default: DATABASECHANGELOG)");
        stream.println(" --databaseChangeLogLockTableName=<name>    The name of the DB-Manul ChangeLog");
        stream.println("                                            Lock table");
        stream.println("                                            (default: DATABASECHANGELOGLOCK)");
        stream.println(" --liquibaseSchemaName=<name>               The name of the schema with the");
        stream.println("                                            liquibase tables");
        stream.println(" --includeSystemClasspath=<true|false>      Include the system classpath");
        stream.println("                                            in the DB-Manul classpath");
        stream.println("                                            (default: true)");
        stream.println(" --overwriteOutputFile=true                 Force overwriting generated ");
        stream.println("                                            changelog/SQL files");
        stream.println(" --promptForNonLocalDatabase=<true|false>   Prompt if non-localhost");
        stream.println("                                            databases (default: false)");
        stream.println(" --logLevel=<level>                         Execution log level");
        stream.println("                                            (debug, sql, info, warning, severe");
        stream.println("                                             or off");
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
        stream.println("                                            value is PROMPT, DB-Manul will");
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
        stream.println(" --referenceSchemas=<name1,name2>           Reference database schemas to");
        stream.println("                                            include objects from in comparison");
        stream.println("                                            only needed if different than");
        stream.println("                                            --schemas");
        stream.println(" --outputSchemaAs=<name1,name2>             On diffChangeLog/generateChangeLog,");
        stream.println("                                            use these names as schemaName");
        stream.println("                                            instead of the real names.");
        stream.println(" --includeCatalog=<true|false>              If true, the catalog will be");
        stream.println("                                            included in generated changeSets");
        stream.println("                                            Defaults to true");
        stream.println(" --includeSchema=<true|false>               If true, the schema will be");
        stream.println("                                            included in generated changeSets");
        stream.println("                                            Defaults to true");
        stream.println(" --includeTablespace=<true|false>           If true, the tablespace of tables");
        stream.println("                                            and indexes will be included.");
        stream.println("                                            Defaults to true");
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
        stream.println("http://www.dbmanul.org");
        stream.println("");
    }

    protected void parseOptions(String[] paramArgs) throws CommandLineParsingException {
        String[] args = fixupArgs(paramArgs);

        boolean seenCommand = false;
        for (String arg : args) {
            if (isCommand(arg)) {
                this.command = arg;
                if (this.command.equalsIgnoreCase(COMMAND_MIGRATE)) {
                    this.command = COMMAND_UPDATE;
                } else if (this.command.equalsIgnoreCase(COMMAND_MIGRATE_SQL)) {
                    this.command = COMMAND_UPDATE_SQL;
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
                        value = new String(c.readPassword(attributeName + ": "));
                    } else {
                        value = c.readLine(attributeName + ": ");
                    }
                }

                try {
                    Field field = getClass().getDeclaredField(attributeName);
                    if (field.getType().equals(Boolean.class)) {
                        field.set(this, Boolean.valueOf(value));
                    } else {
                        field.set(this, value);
                    }
                } catch (Exception ignored) {
                    throw new CommandLineParsingException("Unknown parameter: '" + attributeName + "'");
                }
            } else {
                throw new CommandLineParsingException("Unexpected value " + arg + ": parameters must start with a " +
                        "'--'");
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
        final List<URL> urls = new ArrayList<>();
        if (this.classpath != null) {
            String[] classpathSoFar;
            if (isWindows()) {
                classpathSoFar = this.classpath.split(";");
            } else {
                classpathSoFar = this.classpath.split(":");
            }

            Logger logger = LogFactory.getInstance().getLog();
            for (String classpathEntry : classpathSoFar) {
                File classPathFile = new File(classpathEntry);
                if (!classPathFile.exists()) {
                    throw new CommandLineParsingException(classPathFile.getAbsolutePath() + " does not exist");
                }

                if (classpathEntry.endsWith(".war")) {
                    try {
                        addWarFileClasspathEntries(classPathFile, urls);
                    } catch (Exception e) {
                        throw new CommandLineParsingException(e);
                    }
                } else if (classpathEntry.endsWith(".ear")) {
                    try (JarFile earZip = new JarFile(classPathFile)) {
                        Enumeration<? extends JarEntry> entries = earZip.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            if (entry.getName().toLowerCase().endsWith(".jar")) {
                                File jar = extract(earZip, entry);
                                URL newUrl = new URL("jar:" + jar.toURI().toURL() + "!/");
                                urls.add(newUrl);
                                logger.debug("Adding '" + newUrl + "' to classpath");
                                jar.deleteOnExit();
                            } else if (entry.getName().toLowerCase().endsWith("war")) {
                                File warFile = extract(earZip, entry);
                                addWarFileClasspathEntries(warFile, urls);
                            }
                        }
                    } catch (Exception e) {
                        throw new CommandLineParsingException(e);
                    }

                } else {
                    URL newUrl = null;
                    try {
                        newUrl = new File(classpathEntry).toURI().toURL();
                    } catch (MalformedURLException e) {
                        throw new CommandLineParsingException(e);
                    }
                    logger.debug("Adding '" + newUrl + "' to classpath");
                    urls.add(newUrl);
                }
            }
        }
        if (includeSystemClasspath) {
            classLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
                @Override
                public URLClassLoader run() {
                    return new URLClassLoader(urls.toArray(new URL[urls.size()]), Thread.currentThread()
                            .getContextClassLoader());
                }
            });

        } else {
            classLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
                @Override
                public URLClassLoader run() {
                    return new URLClassLoader(urls.toArray(new URL[urls.size()]), null);
                }
            });
        }

        ServiceLocator.getInstance().setResourceAccessor(new ClassLoaderResourceAccessor(classLoader));
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    private void addWarFileClasspathEntries(File classPathFile, List<URL> urls) throws IOException {
        Logger logger = LogFactory.getInstance().getLog();
        URL jarUrl = new URL("jar:" + classPathFile.toURI().toURL() + "!/WEB-INF/classes/");
        logger.info("adding '" + jarUrl + "' to classpath");
        urls.add(jarUrl);

        try (
                JarFile warZip = new JarFile(classPathFile)
        ) {
            Enumeration<? extends JarEntry> entries = warZip.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith("WEB-INF/lib")
                        && entry.getName().toLowerCase().endsWith(".jar")) {
                    File jar = extract(warZip, entry);
                    URL newUrl = new URL("jar:" + jar.toURI().toURL() + "!/");
                    logger.info("adding '" + newUrl + "' to classpath");
                    urls.add(newUrl);
                    jar.deleteOnExit();
                }
            }
        }
    }

    private File extract(JarFile jar, JarEntry entry) throws IOException {
        // expand to temp dir and add to list
        File tempFile = File.createTempFile("liquibase.tmp", null);
        // read from jar and write to the tempJar file
        try (
                BufferedInputStream inStream = new BufferedInputStream(jar.getInputStream(entry));
                BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(tempFile))
        ) {
            int status;
            while ((status = inStream.read()) != -1) {
                outStream.write(status);
            }
        }

        return tempFile;
    }

    protected void doMigration() throws Exception {
        if (COMMAND_HELP.equalsIgnoreCase(command)) {
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
        CompositeResourceAccessor fileOpener = new CompositeResourceAccessor(fsOpener, clOpener);

        Database database = CommandLineUtils.createDatabaseObject(fileOpener, this.url,
                this.username, this.password, this.driver, this.defaultCatalogName, this.defaultSchemaName,
                Boolean.parseBoolean(outputDefaultCatalog), Boolean.parseBoolean(outputDefaultSchema),
                this.databaseClass, this.driverPropertiesFile, this.propertyProviderClass,
                this.liquibaseCatalogName, this.liquibaseSchemaName, this.databaseChangeLogTableName,
                this.databaseChangeLogLockTableName);
        try {

            CompareControl.ComputedSchemas computedSchemas = CompareControl.computeSchemas(
                    getCommandParam(OPTION_SCHEMAS, null),
                    getCommandParam(OPTION_REFERENCE_SCHEMAS, null),
                    getCommandParam(OPTION_OUTPUT_SCHEMAS_AS, null),
                    defaultCatalogName, defaultSchemaName,
                    referenceDefaultCatalogName, referenceDefaultSchemaName,
                    database);
            CompareControl.SchemaComparison[] finalSchemaComparisons = computedSchemas.finalSchemaComparisons;
            CatalogAndSchema[] finalTargetSchemas = computedSchemas.finalTargetSchemas;

            boolean includeCatalog = Boolean.parseBoolean(getCommandParam(OPTION_INCLUDE_CATALOG, "false"));
            boolean includeSchema = Boolean.parseBoolean(getCommandParam(OPTION_INCLUDE_SCHEMA, "false"));
            boolean includeTablespace = Boolean.parseBoolean(getCommandParam(OPTION_INCLUDE_TABLESPACE, "true"));
            String excludeObjects = StringUtils.trimToNull(getCommandParam(OPTION_EXCLUDE_OBJECTS, null));
            String includeObjects = StringUtils.trimToNull(getCommandParam(OPTION_INCLUDE_OBJECTS, null));
            DiffOutputControl diffOutputControl = new DiffOutputControl(
                    includeCatalog, includeSchema, includeTablespace, finalSchemaComparisons);

            if (excludeObjects != null && includeObjects != null) {
                throw new UnexpectedLiquibaseException("Cannot specify both " + OPTION_EXCLUDE_OBJECTS + " and " +
                        OPTION_INCLUDE_OBJECTS);
            }
            if (excludeObjects != null) {
                diffOutputControl.setObjectChangeFilter(
                        new StandardObjectChangeFilter(
                                StandardObjectChangeFilter.FilterType.EXCLUDE, excludeObjects
                        )
                );
            }
            if (includeObjects != null) {
                diffOutputControl.setObjectChangeFilter(
                        new StandardObjectChangeFilter(
                                StandardObjectChangeFilter.FilterType.INCLUDE, includeObjects
                        )
                );
            }

            for (CompareControl.SchemaComparison schema : finalSchemaComparisons) {
                diffOutputControl.addIncludedSchema(schema.getReferenceSchema());
                diffOutputControl.addIncludedSchema(schema.getComparisonSchema());
            }

            if (COMMAND_DIFF.equalsIgnoreCase(command)) {
                CommandLineUtils.doDiff(
                        createReferenceDatabaseFromCommandParams(commandParams, fileOpener),
                        database, StringUtils.trimToNull(diffTypes), finalSchemaComparisons
                );
                return;
            } else if (COMMAND_DIFF_CHANGELOG.equalsIgnoreCase(command)) {
                CommandLineUtils.doDiffToChangeLog(changeLogFile,
                        createReferenceDatabaseFromCommandParams(commandParams, fileOpener), database,
                        diffOutputControl, StringUtils.trimToNull(diffTypes), finalSchemaComparisons
                );
                return;
            } else if (COMMAND_GENERATE_CHANGELOG.equalsIgnoreCase(command)) {
                String currentChangeLogFile = this.changeLogFile;
                if (currentChangeLogFile == null) {
                    currentChangeLogFile = ""; //will output to stdout
                }
                // By default the generateChangeLog command is destructive, and
                // Liquibase's attempt to append doesn't work properly. Just
                // fail the build if the file already exists.
                File file = new File(currentChangeLogFile);
                if (file.exists() && (!Boolean.parseBoolean(overwriteOutputFile))) {
                    throw new LiquibaseException("ChangeLogFile " + currentChangeLogFile + " already exists!");
                } else {
                    try {
                        if (!file.delete()) {
                            // Nothing needs to be done
                        }
                    } catch (Exception e) {
                        throw new LiquibaseException("Attempt to delete the file " + currentChangeLogFile +
                                "  failed. Cannot continue.", e);
                    }
                }

                CommandLineUtils.doGenerateChangeLog(currentChangeLogFile, database, finalTargetSchemas,
                        StringUtils.trimToNull(diffTypes), StringUtils.trimToNull(changeSetAuthor),
                        StringUtils.trimToNull(changeSetContext), StringUtils.trimToNull(dataOutputDirectory),
                        diffOutputControl);
                return;
            } else if (COMMAND_SNAPSHOT.equalsIgnoreCase(command)) {
                SnapshotCommand snapshotCommand = (SnapshotCommand) CommandFactory.getInstance()
                        .getCommand(COMMAND_SNAPSHOT);
                snapshotCommand.setDatabase(database);
                snapshotCommand.setSchemas(
                        getCommandParam(
                                OPTION_SCHEMAS, database.getDefaultSchema().getSchemaName()
                        )
                );
                snapshotCommand.setSerializerFormat(getCommandParam("snapshotFormat", null));
                Writer outputWriter = getOutputWriter();
                outputWriter.write(snapshotCommand.execute().print());
                outputWriter.flush();
                outputWriter.close();
                return;
            } else if (COMMAND_EXECUTE_SQL.equalsIgnoreCase(command)) {
                ExecuteSqlCommand executeSqlCommand = (ExecuteSqlCommand) CommandFactory.getInstance().getCommand(
                        COMMAND_EXECUTE_SQL);
                executeSqlCommand.setDatabase(database);
                executeSqlCommand.setSql(getCommandParam("sql", null));
                executeSqlCommand.setSqlFile(getCommandParam("sqlFile", null));
                executeSqlCommand.setDelimiter(getCommandParam("delimiter", ";"));
                Writer outputWriter = getOutputWriter();
                outputWriter.write(executeSqlCommand.execute().print());
                outputWriter.flush();
                outputWriter.close();
                return;
            } else if (COMMAND_SNAPSHOT_REFERENCE.equalsIgnoreCase(command)) {
                SnapshotCommand snapshotCommand = (SnapshotCommand) CommandFactory.getInstance()
                        .getCommand(COMMAND_SNAPSHOT);
                Database referenceDatabase = createReferenceDatabaseFromCommandParams(commandParams, fileOpener);
                snapshotCommand.setDatabase(referenceDatabase);
                snapshotCommand.setSchemas(
                        getCommandParam(
                                OPTION_SCHEMAS, referenceDatabase.getDefaultSchema().getSchemaName()
                        )
                );
                Writer outputWriter = getOutputWriter();
                outputWriter.write(snapshotCommand.execute().print());
                outputWriter.flush();
                outputWriter.close();

                return;
            }

            Liquibase liquibase = new Liquibase(changeLogFile, fileOpener, database);
            ChangeExecListener listener = ChangeExecListenerUtils.getChangeExecListener(
                    liquibase.getDatabase(), liquibase.getResourceAccessor(),
                    changeExecListenerClass, changeExecListenerPropertiesFile);
            liquibase.setChangeExecListener(listener);

            liquibase.setCurrentDateTimeFunction(currentDateTimeFunction);
            for (Map.Entry<String, Object> entry : changeLogParameters.entrySet()) {
                liquibase.setChangeLogParameter(entry.getKey(), entry.getValue());
            }

            if (COMMAND_LIST_LOCKS.equalsIgnoreCase(command)) {
                liquibase.reportLocks(System.err);
                return;
            } else if (COMMAND_RELEASE_LOCKS.equalsIgnoreCase(command)) {
                LockService lockService = LockServiceFactory.getInstance().getLockService(database);
                lockService.forceReleaseLock();
                System.err.println(
                        "Successfully released all database change log locks for " +
                                liquibase.getDatabase().getConnection().getConnectionUserName() +
                                "@" + liquibase.getDatabase().getConnection().getURL()
                );
                return;
            } else if (COMMAND_TAG.equalsIgnoreCase(command)) {
                liquibase.tag(getCommandArgument());
                LogFactory.getInstance().getLog().info("Successfully tagged " + liquibase.getDatabase().getConnection
                        ().getConnectionUserName() + "@" + liquibase.getDatabase().getConnection().getURL());
                return;
            } else if (COMMAND_TAG_EXISTS.equalsIgnoreCase(command)) {
                String tag = commandParams.iterator().next();
                boolean exists = liquibase.tagExists(tag);
                if (exists) {
                    LogFactory.getInstance().getLog().info("The tag " + tag + " already exists in " + liquibase
                            .getDatabase().getConnection().getConnectionUserName() + "@" + liquibase.getDatabase()
                            .getConnection().getURL());
                } else {
                    LogFactory.getInstance().getLog().info("The tag " + tag + " does not exist in " + liquibase
                            .getDatabase().getConnection().getConnectionUserName() + "@" + liquibase.getDatabase()
                            .getConnection().getURL());
                }
                return;
            } else if (COMMAND_DROP_ALL.equals(command)) {
                DropAllCommand dropAllCommand = (DropAllCommand) CommandFactory.getInstance().getCommand
                        (COMMAND_DROP_ALL);
                dropAllCommand.setDatabase(liquibase.getDatabase());
                dropAllCommand.setSchemas(getCommandParam(OPTION_SCHEMAS, database.getDefaultSchema().getSchemaName()));

                System.err.println(dropAllCommand.execute().print());
                return;
            } else if (COMMAND_STATUS.equalsIgnoreCase(command)) {
                boolean runVerbose = false;

                if (commandParams.contains(OPTION_VERBOSE)) {
                    runVerbose = true;
                }
                liquibase.reportStatus(runVerbose, new Contexts(contexts), new LabelExpression(labels),
                        getOutputWriter());
                return;
            } else if (COMMAND_UNEXPECTED_CHANGESETS.equalsIgnoreCase(command)) {
                boolean runVerbose = false;

                if (commandParams.contains(OPTION_VERBOSE)) {
                    runVerbose = true;
                }
                liquibase.reportUnexpectedChangeSets(runVerbose, contexts, getOutputWriter());
                return;
            } else if (COMMAND_VALIDATE.equalsIgnoreCase(command)) {
                try {
                    liquibase.validate();
                } catch (ValidationFailedException e) {
                    e.printDescriptiveError(System.err);
                    return;
                }
                System.err.println("No validation errors found");
                return;
            } else if (COMMAND_CLEAR_CHECKSUMS.equalsIgnoreCase(command)) {
                liquibase.clearCheckSums();
                return;
            } else if (COMMAND_CALCULATE_CHECKSUM.equalsIgnoreCase(command)) {
                CheckSum checkSum = null;
                checkSum = liquibase.calculateCheckSum(commandParams.iterator().next());
                System.out.println(checkSum);
                return;
            } else if (COMMAND_DB_DOC.equalsIgnoreCase(command)) {
                if (commandParams.isEmpty()) {
                    throw new CommandLineParsingException("dbdoc requires an output directory");
                }
                if (changeLogFile == null) {
                    throw new CommandLineParsingException("dbdoc requires a changeLog parameter");
                }
                liquibase.generateDocumentation(commandParams.iterator().next(), contexts);
                return;
            }

            try {
                if (COMMAND_UPDATE.equalsIgnoreCase(command)) {
                    liquibase.update(new Contexts(contexts), new LabelExpression(labels));
                } else if (COMMAND_CHANGELOG_SYNC.equalsIgnoreCase(command)) {
                    liquibase.changeLogSync(new Contexts(contexts), new LabelExpression(labels));
                } else if (COMMAND_CHANGELOG_SYNC_SQL.equalsIgnoreCase(command)) {
                    liquibase.changeLogSync(new Contexts(contexts), new LabelExpression(labels), getOutputWriter());
                } else if (COMMAND_MARK_NEXT_CHANGESET_RAN.equalsIgnoreCase(command)) {
                    liquibase.markNextChangeSetRan(new Contexts(contexts), new LabelExpression(labels));
                } else if (COMMAND_MARK_NEXT_CHANGESET_RAN_SQL.equalsIgnoreCase(command)) {
                    liquibase.markNextChangeSetRan(new Contexts(contexts), new LabelExpression(labels),
                            getOutputWriter());
                } else if (COMMAND_UPDATE_COUNT.equalsIgnoreCase(command)) {
                    liquibase.update(Integer.parseInt(commandParams.iterator().next()), new Contexts(contexts), new
                            LabelExpression(labels));
                } else if (COMMAND_UPDATE_COUNT_SQL.equalsIgnoreCase(command)) {
                    liquibase.update(Integer.parseInt(commandParams.iterator().next()), new Contexts(contexts), new
                            LabelExpression(labels), getOutputWriter());
                } else if (COMMAND_UPDATE_TO_TAG.equalsIgnoreCase(command)) {
                    if (commandParams == null || commandParams.isEmpty()) {
                        throw new CommandLineParsingException("updateToTag requires a tag");
                    }

                    liquibase.update(commandParams.iterator().next(), new Contexts(contexts), new LabelExpression
                            (labels));
                } else if (COMMAND_UPDATE_TO_TAG_SQL.equalsIgnoreCase(command)) {
                    if (commandParams == null || commandParams.isEmpty()) {
                        throw new CommandLineParsingException("updateToTagSQL requires a tag");
                    }

                    liquibase.update(commandParams.iterator().next(), new Contexts(contexts), new LabelExpression
                            (labels), getOutputWriter());
                } else if (COMMAND_UPDATE_SQL.equalsIgnoreCase(command)) {
                    liquibase.update(new Contexts(contexts), new LabelExpression(labels), getOutputWriter());
                } else if (COMMAND_ROLLBACK.equalsIgnoreCase(command)) {
                    if (getCommandArgument() == null) {
                        throw new CommandLineParsingException("rollback requires a rollback tag");
                    }
                    liquibase.rollback(getCommandArgument(), getCommandParam(COMMAND_ROLLBACK_SCRIPT, null), new
                            Contexts(contexts), new LabelExpression(labels));
                } else if (COMMAND_ROLLBACK_TO_DATE.equalsIgnoreCase(command)) {
                    if (getCommandArgument() == null) {
                        throw new CommandLineParsingException("rollback requires a rollback date");
                    }
                    liquibase.rollback(new ISODateFormat().parse(getCommandArgument()), getCommandParam
                            (COMMAND_ROLLBACK_SCRIPT, null), new Contexts(contexts), new LabelExpression(labels));
                } else if (COMMAND_ROLLBACK_COUNT.equalsIgnoreCase(command)) {
                    liquibase.rollback(Integer.parseInt(getCommandArgument()), getCommandParam
                            (COMMAND_ROLLBACK_SCRIPT, null), new Contexts(contexts), new LabelExpression(labels));

                } else if (COMMAND_ROLLBACK_SQL.equalsIgnoreCase(command)) {
                    if (getCommandArgument() == null) {
                        throw new CommandLineParsingException("rollbackSQL requires a rollback tag");
                    }
                    liquibase.rollback(getCommandArgument(), getCommandParam(COMMAND_ROLLBACK_SCRIPT, null), new
                            Contexts(contexts), new LabelExpression(labels), getOutputWriter());
                } else if (COMMAND_ROLLBACK_TO_DATE_SQL.equalsIgnoreCase(command)) {
                    if (getCommandArgument() == null) {
                        throw new CommandLineParsingException("rollbackToDateSQL requires a rollback date");
                    }
                    liquibase.rollback(new ISODateFormat().parse(getCommandArgument()), getCommandParam
                                    (COMMAND_ROLLBACK_SCRIPT, null), new Contexts(contexts), new LabelExpression
                                    (labels),
                            getOutputWriter());
                } else if (COMMAND_ROLLBACK_COUNT_SQL.equalsIgnoreCase(command)) {
                    if (getCommandArgument() == null) {
                        throw new CommandLineParsingException("rollbackCountSQL requires a rollback count");
                    }

                    liquibase.rollback(Integer.parseInt(getCommandArgument()), getCommandParam
                                    (COMMAND_ROLLBACK_SCRIPT, null), new Contexts(contexts), new LabelExpression(labels),
                            getOutputWriter());
                } else if (COMMAND_FUTURE_ROLLBACK_SQL.equalsIgnoreCase(command)) {
                    liquibase.futureRollbackSQL(new Contexts(contexts), new LabelExpression(labels), getOutputWriter());
                } else if (COMMAND_FUTURE_ROLLBACK_COUNT_SQL.equalsIgnoreCase(command)) {
                    if (getCommandArgument() == null) {
                        throw new CommandLineParsingException("futureRollbackCountSQL requires a rollback count");
                    }

                    liquibase.futureRollbackSQL(Integer.parseInt(getCommandArgument()), new Contexts(contexts), new
                            LabelExpression(labels), getOutputWriter());
                } else if (COMMAND_FUTURE_ROLLBACK_FROM_TAG_SQL.equalsIgnoreCase(command)) {
                    if (getCommandArgument() == null) {
                        throw new CommandLineParsingException("futureRollbackFromTagSQL requires a tag");
                    }

                    liquibase.futureRollbackSQL(getCommandArgument(), new Contexts(contexts), new LabelExpression
                            (labels), getOutputWriter());
                } else if (COMMAND_UPDATE_TESTING_ROLLBACK.equalsIgnoreCase(command)) {
                    liquibase.updateTestingRollback(new Contexts(contexts), new LabelExpression(labels));
                } else {
                    throw new CommandLineParsingException("Unknown command: " + command);
                }
            } catch (ParseException ignored) {
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

    private String getCommandArgument() {
        for (String param : commandParams) {
            if (!param.contains("=")) {
                return param;
            }
        }

        return null;
    }

    private String getCommandParam(String paramName, String defaultValue) throws CommandLineParsingException {
        for (String param : commandParams) {
            if (!param.contains("=")) {
                continue;
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

    private Database createReferenceDatabaseFromCommandParams(
            Set<String> commandParams, ResourceAccessor resourceAccessor)
            throws CommandLineParsingException, DatabaseException {
        String driver = referenceDriver;
        String url = referenceUrl;
        String username = referenceUsername;
        String password = referencePassword;
        String defSchemaName = this.referenceDefaultSchemaName;
        String defCatalogName = this.referenceDefaultCatalogName;

        for (String param : commandParams) {
            String[] splitArg = splitArg(param);

            String attributeName = splitArg[0];
            String value = splitArg[1];
            if (OPTION_REFERENCE_DRIVER.equalsIgnoreCase(attributeName)) {
                driver = value;
            } else if (OPTION_REFERENCE_URL.equalsIgnoreCase(attributeName)) {
                url = value;
            } else if (OPTION_REFERENCE_USERNAME.equalsIgnoreCase(attributeName)) {
                username = value;
            } else if (OPTION_REFERENCE_PASSWORD.equalsIgnoreCase(attributeName)) {
                password = value;
            } else if (OPTION_REFERENCE_DEFAULT_CATALOG_NAME.equalsIgnoreCase(attributeName)) {
                defCatalogName = value;
            } else if (OPTION_REFERENCE_DEFAULT_SCHEMA_NAME.equalsIgnoreCase(attributeName)) {
                defSchemaName = value;
            } else if (OPTION_DATA_OUTPUT_DIRECTORY.equalsIgnoreCase(attributeName)) {
                dataOutputDirectory = value;
            }
        }

        if (url == null) {
            throw new CommandLineParsingException("referenceUrl parameter missing");
        }

        return CommandLineUtils.createDatabaseObject(resourceAccessor, url, username, password, driver,
                defCatalogName, defSchemaName, Boolean.parseBoolean(outputDefaultCatalog), Boolean.parseBoolean
                        (outputDefaultSchema), null, null, this.propertyProviderClass, this.liquibaseCatalogName,
                this.liquibaseSchemaName, this.databaseChangeLogTableName, this.databaseChangeLogLockTableName);
    }

    private Writer getOutputWriter() throws IOException {
        String charsetName = LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class)
                .getOutputEncoding();

        if (outputFile != null) {
            try (
                    FileOutputStream fileOut = new FileOutputStream(outputFile, false)
            ) {
                return new OutputStreamWriter(fileOut, charsetName);
            } catch (IOException e) {
                LogFactory.getInstance().getLog().severe(String.format("Could not create output file %s%n",
                        outputFile));
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
