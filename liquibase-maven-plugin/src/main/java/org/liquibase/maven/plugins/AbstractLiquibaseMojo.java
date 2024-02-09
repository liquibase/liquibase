package org.liquibase.maven.plugins;

import liquibase.GlobalConfiguration;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.ThreadLocalScopeManager;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.changelog.visitor.DefaultChangeExecListener;
import liquibase.command.core.helpers.DbUrlConnectionCommandStep;
import liquibase.configuration.ConfiguredValueModifierFactory;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.configuration.core.DefaultsFileValueProvider;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.integration.IntegrationDetails;
import liquibase.integration.commandline.ChangeExecListenerUtils;
import liquibase.integration.commandline.CommandLineUtils;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.logging.LogFormat;
import liquibase.logging.LogService;
import liquibase.logging.core.JavaLogService;
import liquibase.logging.core.LogServiceFactory;
import liquibase.resource.DirectoryResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.resource.SearchPathResourceAccessor;
import liquibase.util.FileUtil;
import liquibase.util.StringUtil;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.Parameter;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.liquibase.maven.property.PropertyElement;

import javax.xml.bind.annotation.XmlSchema;
import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Handler;

import static java.util.ResourceBundle.getBundle;
import static liquibase.configuration.LiquibaseConfiguration.REGISTERED_VALUE_PROVIDERS_KEY;

/**
 * A base class for providing Liquibase {@link liquibase.Liquibase} functionality.
 *
 * @author Peter Murray
 * @author Florent Biville
 * <p>
 * Test dependency is used because when you run a goal outside the build phases you want to have the same dependencies
 * that it would have if it was run inside test phase
 * @requiresDependencyResolution test
 */
@SuppressWarnings("java:S2583")
public abstract class AbstractLiquibaseMojo extends AbstractMojo {

    static {
        // If maven is called with -T and a value larger than 1, it can get confused under heavy thread load
        Scope.setScopeManager( new ThreadLocalScopeManager(null));
    }

    /**
     * Suffix for fields that are representing a default value for a another field.
     */
    private static final String DEFAULT_FIELD_SUFFIX = "Default";

    private MavenLog logCache;

    /**
     *
     * Specifies whether to preserve the case of schemas and catalogs
     *
     * @parameter property="liquibase.preserveSchemaCase"
     *
     */
    @PropertyElement
    protected Boolean preserveSchemaCase;

    /**
     * Specifies the driver class name to use for the database connection.
     *
     * @parameter property="liquibase.driver"
     */
    @PropertyElement
    protected String driver;

    /**
     * Specifies the database URL you want to use to execute Liquibase.
     *
     * @parameter property="liquibase.url"
     */
    @PropertyElement
    protected String url;

    public void setUrl(String url) throws Exception {
        this.url = modifyValue(url);
    }

    /**
     * The Maven Wagon manager to use when obtaining server authentication details.
     *
     * @component role="org.apache.maven.artifact.manager.WagonManager"
     * @required
     * @readonly
     */
    protected WagonManager wagonManager;
    /**
     * Specifies the database username for database connection.
     *
     * @parameter property="liquibase.username"
     */
    @PropertyElement
    protected String username;

    public void setUsername(String username) throws Exception {
        this.username = modifyValue(username);
    }

    /**
     * Specifies the database password for database connection.
     *
     * @parameter property="liquibase.password"
     */
    @PropertyElement
    protected String password;

    public void setPassword(String password) throws Exception {
        this.password = modifyValue(password);
    }

    /**
     * Use an empty string as the password for the database connection. This should not be
     * used along side the {@link #password} setting.
     *
     * @parameter property="liquibase.emptyPassword" default-value="false"
     * @deprecated Use an empty or null value for the password instead.
     */
    @PropertyElement
    protected boolean emptyPassword;
    /**
     * Specifies whether to ignore the schema name.
     *
     * @parameter property="liquibase.outputDefaultSchema" default-value="false"
     */
    @PropertyElement
    protected boolean outputDefaultSchema;
    /**
     * Specifies whether to ignore the catalog/database name.
     *
     * @parameter property="liquibase.outputDefaultCatalog" default-value="false"
     */
    @PropertyElement
    protected boolean outputDefaultCatalog;

    /**
     * Specifies the default catalog name to use for the database connection.
     *
     * @parameter property="liquibase.defaultCatalogName"
     */
    @PropertyElement
    protected String defaultCatalogName;

    /**
     * Specifies the default schema name to use for the database connection.
     *
     * @parameter property="liquibase.defaultSchemaName"
     */
    @PropertyElement
    protected String defaultSchemaName;

    /**
     * Specifies the database object class.
     *
     * @parameter property="liquibase.databaseClass"
     */
    @PropertyElement
    protected String databaseClass;

    /**
     * Specifies the property provider  which must be a java.util.Properties implementation.
     *
     * @parameter property="liquibase.propertyProviderClass"
     */
    @PropertyElement
    protected String propertyProviderClass;

    /**
     * (DEPRECATED) Controls whether users are prompted before executing changeSet to a non-local database.
     *
     * @parameter property="liquibase.promptOnNonLocalDatabase" default-value="false"
     * @deprecated No longer prompts
     */
    @PropertyElement
    protected boolean promptOnNonLocalDatabase;

    /**
     * Includes a Maven project artifact in the class loader which obtains the liquibase.properties and changelog files.
     *
     * @parameter property="liquibase.includeArtifact" default-value="true"
     */
    @PropertyElement
    protected boolean includeArtifact;
    /**
     * Includes the Maven test output directory in the class loader which obtains the liquibase.properties and changelog files.
     *
     * @parameter property="liquibase.includeTestOutputDirectory" default-value="true"
     */
    @PropertyElement
    protected boolean includeTestOutputDirectory;

    /**
     * Controls the amount of output detail when you call the plugin.
     *
     * @parameter property="liquibase.verbose" default-value="false"
     */
    @PropertyElement
    protected boolean verbose;

    /**
     * Deprecated and ignored configuration property. Logging is managed via the standard maven logging system
     * either using the -e, -X or -q flags or the ${maven.home}/conf/logging/simplelogger.properties file.
     *
     * @see <a href="https://maven.apache.org/maven-logging.html">maven-logging for more information.</a>
     *
     * @parameter property="liquibase.logging"
     * @deprecated Logging managed by maven
     */
    @PropertyElement
    protected String logging;

    /**
     * Determines the minimum log level liquibase uses when logging.
     * <p>
     * Supported values are:
     *
     * <ul>
     *     <li>DEBUG</li>
     *     <li>INFO</li>
     *     <li>WARNING</li>
     *     <li>ERROR</li>
     * </ul>
     *
     * The primary use case for this option is to reduce the amount of logs from liquibase, while
     * not changing the log level of maven itself, without changing ${maven.home}/conf/logging/simplelogger.properties.
     * <p>
     * <b>NOTE:</b> The final log level is the <i>maximum</i> of this value and the maven log level.
     * Thus, it is not possible to <i>decrease</i> the effective log level with this option.
     *
     * @parameter property="liquibase.logLevel" default-value="DEBUG"
     */
    @PropertyElement
    protected String logLevel;

    /**
     * Specifies the <i>liquibase.properties</i> you want to use to configure Liquibase.
     *
     * @parameter property="liquibase.propertyFile"
     */
    @PropertyElement
    protected String propertyFile;
    /**
     * A flag which indicates you want the <i>liquibase.properties</i> file to override any settings provided in the Maven plugin configuration.
     * By default, if a property is explicitly specified it is
     * not overridden if it also appears in the properties file.
     *
     * @parameter property="liquibase.propertyFileWillOverride" default-value="false"
     */
    @PropertyElement
    protected boolean propertyFileWillOverride;

    /**
     * A flag that forces checksums to be cleared from the DATABASECHANGELOG table.
     *
     * @parameter property="liquibase.clearCheckSums" default-value="false"
     */
    @PropertyElement
    protected boolean clearCheckSums;
    /**
     * Specifies a list of system properties you want to pass to the database.
     *
     * @parameter
     */
    @PropertyElement
    protected Properties systemProperties;
    /**
     * The Maven project that plugin is running under.
     *
     * @parameter property="project"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * Specifies whether to skip running Liquibase.
     * The use of this parameter is NOT RECOMMENDED but can be used when needed.
     *
     * @parameter property="liquibase.skip" default-value="false"
     */
    @PropertyElement
    protected boolean skip;

    /**
     * Skip plugin execution if the specified file exists.
     * The use of this parameter is NOT RECOMMENDED but can be used when needed.
     *
     * @parameter property="liquibase.skipOnFileExists"
     */
    @PropertyElement
    protected String skipOnFileExists;

    /**
     * A flag which indicates you want to set the character encoding of the output file during the updateSQL phase.
     *
     * @parameter property="liquibase.outputFileEncoding"
     */
    @PropertyElement
    protected String outputFileEncoding;

    /**
     * Specifies the schema Liquibase will use to create your <i>changelog</i> tables.
     *
     * @parameter property="liquibase.changelogCatalogName"
     */
    @PropertyElement
    protected String changelogCatalogName;

    /**
     * Specifies the schema Liquibase will use to create your changelog table.
     *
     * @parameter property="liquibase.changelogSchemaName"
     */
    @PropertyElement
    protected String changelogSchemaName;
    /**
     * Specifies the table name to use for the DATABASECHANGELOG table.
     *
     * @parameter property="liquibase.databaseChangeLogTableName"
     */
    @PropertyElement
    protected String databaseChangeLogTableName;

    /**
     * Specifies the table name to use for the DATABASECHANGELOGLOCK table.
     *
     * @parameter property="liquibase.databaseChangeLogLockTableName"
     */
    @PropertyElement
    protected String databaseChangeLogLockTableName;

    /**
     * Show the liquibase banner in output.
     *
     * @parameter property="liquibase.showBanner"
     */
    @PropertyElement
    protected boolean showBanner = true;

    /**
     * Specifies the server ID in the Maven <i>settings.xml</i> to use when authenticating.
     *
     * @parameter property="liquibase.server"
     */
    @PropertyElement
    private String server;
    /**
     * The {@link Liquibase} object used modify the database.
     */
    @PropertyElement
    private Liquibase liquibase;

    /**
     * Specifies the locations where Liquibase can find your <i>changelog</i> files.
     *
     * @parameter property="liquibase.searchPath"
     */
    @PropertyElement
    protected String searchPath;


    /**
     * A property-based collection of <i>changelog</i> properties to apply.
     *
     * @parameter
     */
    private Properties expressionVars;

    /**
     * A map-based collection of <i>changelog</i> properties to apply.
     *
     * @parameter
     */
    private Map expressionVariables;

    /**
     * Specifies the location of a JDBC connection-properties file which contains properties the driver will use.
     *
     * @parameter
     */
    private File driverPropertiesFile;

    /**
     * Specifies your Liquibase Pro license key. This has been deprecated in favor of using
     * "liquibase.liquibaseLicenseKey", but this property will continue to be operational.
     *
     * @parameter property="liquibase.liquibaseProLicenseKey"
     */
    @PropertyElement
    @Deprecated
    private String liquibaseProLicenseKey;

    /**
     * Specifies your Liquibase license key.
     *
     * @parameter property="liquibase.licenseKey"
     */
    @PropertyElement
    private String liquibaseLicenseKey;

    /**
     * Specifies your psql path.
     *
     * @parameter property="liquibase.psql.path"
     */
    @PropertyElement
    protected String psqlPath;

    /**
     * Specifies whether to keep generated psql files.
     *
     * @parameter property="liquibase.psql.keep.temp"
     */
    @PropertyElement
    protected Boolean psqlKeepTemp;

    /**
     * Specifies the name of generated psql files.
     *
     * @parameter property="liquibase.psql.keep.temp.name"
     */
    @PropertyElement
    protected String psqlKeepTempName;

    /**
     * Specifies where to keep generated psql files.
     *
     * @parameter property="liquibase.psql.keep.temp.path"
     */
    @PropertyElement
    protected String psqlKeepTempPath;

    /**
     * Specifies additional psql args.
     *
     * @parameter property="liquibase.psql.args"
     */
    @PropertyElement
    protected String psqlArgs;

    /**
     * Specifies psql timeout.
     *
     * @parameter property="liquibase.psql.timeout"
     */
    @PropertyElement
    protected Integer psqlTimeout;

    /**
     * Specifies where to output psql logs.
     *
     * @parameter property="liquibase.psql.logFile"
     */
    @PropertyElement
    protected String psqlLogFile;

    /**
     * Specifies your sqlplus path.
     *
     * @parameter property="liquibase.sqlplus.path"
     */
    @PropertyElement
    protected String sqlPlusPath;

    /**
     * Specifies whether to keep generated sqlplus files.
     *
     * @parameter property="liquibase.sqlplus.keep.temp"
     */
    @PropertyElement
    protected Boolean sqlPlusKeepTemp;

    /**
     * Specifies the name of generated sqlplus files.
     *
     * @parameter property="liquibase.sqlplus.keep.temp.name"
     */
    @PropertyElement
    protected String sqlPlusKeepTempName;

    /**
     * Specifies where to keep generated sqlplus files.
     *
     * @parameter property="liquibase.sqlplus.keep.temp.path"
     */
    @PropertyElement
    protected String sqlPlusKeepTempPath;

    /**
     * Specifies whether to overwrite generated sqlplus files.
     *
     * @parameter property="liquibase.sqlplus.keep.temp.overwrite"
     */
    @PropertyElement
    protected Boolean sqlPlusKeepTempOverwrite;

    /**
     * Specifies additional sqlplus args.
     *
     * @parameter property="liquibase.sqlplus.args"
     */
    @PropertyElement
    protected String sqlPlusArgs;

    /**
     * Specifies sqlplus timeout.
     *
     * @parameter property="liquibase.sqlplus.timeout"
     */
    @PropertyElement
    protected Integer sqlPlusTimeout;

    /**
     * Specifies where to output sqlplus logs.
     *
     * @parameter property="liquibase.sqlplus.logFile"
     */
    @PropertyElement
    protected String sqlPlusLogFile;

    /**
     * Specifies your sqlcmd path.
     *
     * @parameter property="liquibase.sqlcmd.path"
     */
    @PropertyElement
    protected String sqlcmdPath;

    /**
     * Specifies whether to keep generated sqlcmd files.
     *
     * @parameter property="liquibase.sqlcmd.keep.temp"
     */
    @PropertyElement
    protected Boolean sqlcmdKeepTemp;

    /**
     * Specifies the name of generated sqlcmd files.
     *
     * @parameter property="liquibase.sqlcmd.keep.temp.name"
     */
    @PropertyElement
    protected String sqlcmdKeepTempName;

    /**
     * Specifies where to keep generated sqlcmd files.
     *
     * @parameter property="liquibase.sqlcmd.keep.temp.path"
     */
    @PropertyElement
    protected String sqlcmdKeepTempPath;

    /**
     * Specifies whether to overwrite generated sqlcmd files.
     *
     * @parameter property="liquibase.sqlcmd.keep.temp.overwrite"
     */
    @PropertyElement
    protected Boolean sqlcmdKeepTempOverwrite;

    /**
     * Specifies additional sqlcmd args.
     *
     * @parameter property="liquibase.sqlcmd.args"
     */
    @PropertyElement
    protected String sqlcmdArgs;

    /**
     * Specifies sqlcmd timeout.
     *
     * @parameter property="liquibase.sqlcmd.timeout"
     */
    @PropertyElement
    protected Integer sqlcmdTimeout;

    /**
     * Specifies where to output sqlcmd logs.
     *
     * @parameter property="liquibase.sqlcmd.logFile"
     */
    @PropertyElement
    protected String sqlcmdLogFile;

    /**
     * Specifies sqlcmd catalog name.
     *
     * @parameter property="liquibase.sqlcmd.catalogName"
     */
    @PropertyElement
    protected String sqlcmdCatalogName;

    /**
     * Specifies the fully qualified class name of the custom ChangeExecListener
     *
     * @parameter property="liquibase.changeExecListenerClass"
     */
    @PropertyElement
    protected String changeExecListenerClass;

    /**
     * Specifies the property file for controlling the custom ChangeExecListener
     *
     * @parameter property="liquibase.changeExecListenerPropertiesFile"
     */
    @PropertyElement
    protected String changeExecListenerPropertiesFile;

    /**
     * Sets the format of log output to console or log files.
     * Open Source users default to unstructured TXT logs to the console or output log files.
     * Pro users have the option to set value as JSON or JSON_PRETTY to enable json-structured log files to the console or output log files.
     *
     * @parameter property="liquibase.logFormat"
     */
    @PropertyElement
    protected String logFormat;

    protected String commandName;
    protected DefaultChangeExecListener defaultChangeExecListener;
    private static final ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");


    /**
     * Get the specified license key. This first checks liquibaseLicenseKey and if no key is found, then returns
     * liquibaseProLicenseKey.
     */
    protected String getLicenseKey() {
        if (StringUtil.isNotEmpty(liquibaseLicenseKey)) {
            return liquibaseLicenseKey;
        } else {
            return liquibaseProLicenseKey;
        }
    }

    protected Writer getOutputWriter(final File outputFile) throws IOException {
        String encoding = this.outputFileEncoding;

        if (encoding == null) {
            encoding = GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue();
        }
        getLog().debug("Writing output file with '" + encoding + "' file encoding.");

        return new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(outputFile.toPath()), encoding));
    }

    private Map<String, Object> setUpLogging() throws Exception {
        // First determine whether the specified log format requires the use of the standard Scope logger.
        boolean useScopeLogger = false;
        if (this.logFormat != null) {
            try {
                useScopeLogger = LogFormat.valueOf(this.logFormat.toUpperCase()).isUseScopeLoggerInMaven();
            } catch (Exception ignored) {

            }
        }

        Map<String, Object> scopeAttrs = new HashMap<>();
        if (!useScopeLogger) {
            // If the specified log format does not require the use of the standard Liquibase logger, just return the
            // Maven log service as is traditionally done.
            scopeAttrs.put(Scope.Attr.logService.name(), new MavenLogService(getLog()));
            scopeAttrs.put(Scope.Attr.ui.name(), new MavenUi(getLog()));
            return scopeAttrs;
        } else {
            // The log format requires the use of the standard Liquibase logger, so set it up.
            scopeAttrs.put(LiquibaseCommandLineConfiguration.LOG_FORMAT.getKey(), this.logFormat);
            scopeAttrs.put(REGISTERED_VALUE_PROVIDERS_KEY, true);
            // Get a new log service after registering the value providers, since the log service might need to load parameters using newly registered value providers.
            LogService newLogService = Scope.child(scopeAttrs, () -> Scope.getCurrentScope().getSingleton(LogServiceFactory.class).getDefaultLogService());
            // Set the formatter on all the handlers.
            java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
            for (Handler handler : rootLogger.getHandlers()) {
                JavaLogService.setFormatterOnHandler(newLogService, handler);
            }
            scopeAttrs.put(Scope.Attr.logService.name(), newLogService);
            return scopeAttrs;
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (StringUtil.trimToNull(logging) != null) {
            getLog().error("The liquibase-maven-plugin now manages logging via the standard maven logging config, not the 'logging' configuration. Use the -e, -X or -q flags or see https://maven.apache.org/maven-logging.html");
        }
        if (skip) {
            getLog().warn("Liquibase skipped due to Maven configuration");
            return;
        }

        if (skipOnFileExists != null) {
            File f = new File(skipOnFileExists);
            if (f.exists()) {
                getLog().warn("Liquibase skipped because file " + skipOnFileExists + " exists");
                return;
            }
            getLog().warn("Liquibase NOT skipped because file " + skipOnFileExists + " does NOT exists");
        }

        try {
            Scope.child(setUpLogging(), () -> {

                getLog().info(MavenUtils.LOG_SEPARATOR);

                if (server != null) {
                    AuthenticationInfo info = wagonManager.getAuthenticationInfo(server);
                    if (info != null) {
                        username = info.getUserName();
                        password = info.getPassword();
                    }
                }

                processSystemProperties();

                if (!LiquibaseCommandLineConfiguration.SHOULD_RUN.getCurrentValue()) {
                    getLog().info("Liquibase did not run because " + LiquibaseCommandLineConfiguration.SHOULD_RUN.getKey() + " was set to false");
                    return;
                }

                ClassLoader mavenClassLoader = getClassLoaderIncludingProjectClasspath();
                Map<String, Object> scopeValues = new HashMap<>();
                scopeValues.put(Scope.Attr.resourceAccessor.name(), getResourceAccessor(mavenClassLoader));
                scopeValues.put(Scope.Attr.classLoader.name(), getClassLoaderIncludingProjectClasspath());

                IntegrationDetails integrationDetails = new IntegrationDetails();
                integrationDetails.setName("maven");

                final PluginDescriptor pluginDescriptor = (PluginDescriptor) getPluginContext().get("pluginDescriptor");
                for (MojoDescriptor descriptor : pluginDescriptor.getMojos()) {
                    if (!descriptor.getImplementationClass().equals(this.getClass())) {
                        continue;
                    }

                    for (Parameter param : descriptor.getParameters()) {
                        final String name = param.getName();
                        if (name.equalsIgnoreCase("project") || name.equalsIgnoreCase("systemProperties")) {
                            continue;
                        }

                        final Field field = getField(this.getClass(), name);
                        if (field == null) {
                            getLog().debug("Cannot read current maven value for: " + name);
                        } else {
                            field.setAccessible(true);
                            final Object value = field.get(this);
                            if (value != null) {
                                try {
                                    integrationDetails.setParameter("maven__" + param.getName().replaceAll("[${}]", ""), String.valueOf(value));
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

                //
                // Add properties to this top-level scope
                //
                scopeValues.put("integrationDetails", integrationDetails);
                scopeValues.put("liquibase.licenseKey", getLicenseKey());
                String key = GlobalConfiguration.PRESERVE_SCHEMA_CASE.getKey();
                scopeValues.put(key, preserveSchemaCase);
                scopeValues.putAll(getNativeExecutorProperties());
                Scope.child(scopeValues, () -> {

                    configureFieldsAndValues();

                    if (showBanner) {
                        getLog().info(CommandLineUtils.getBanner());
                    }

                    // Displays the settings for the Mojo depending on verbosity mode.
                    displayMojoSettings();

                    // Check that all the parameters that must be specified have been by the user.
                    checkRequiredParametersAreSpecified();

                    Database database = null;
                    try {
                        if (databaseConnectionRequired()) {
                            String dbPassword = (emptyPassword || (password == null)) ? "" : password;
                            String driverPropsFile = (driverPropertiesFile == null) ? null : driverPropertiesFile.getAbsolutePath();
                            database = CommandLineUtils.createDatabaseObject(mavenClassLoader,
                                    url,
                                    username,
                                    dbPassword,
                                    driver,
                                    defaultCatalogName,
                                    defaultSchemaName,
                                    outputDefaultCatalog,
                                    outputDefaultSchema,
                                    databaseClass,
                                    driverPropsFile,
                                    propertyProviderClass,
                                    changelogCatalogName,
                                    changelogSchemaName,
                                    databaseChangeLogTableName,
                                    databaseChangeLogLockTableName);
                            DbUrlConnectionCommandStep.logMdc(url, database);
                            liquibase = createLiquibase(database);

                            configureChangeLogProperties();

                            ChangeExecListener listener = ChangeExecListenerUtils.getChangeExecListener(
                                    liquibase.getDatabase(), liquibase.getResourceAccessor(),
                                    changeExecListenerClass, changeExecListenerPropertiesFile);
                            defaultChangeExecListener = new DefaultChangeExecListener(listener);
                            liquibase.setChangeExecListener(defaultChangeExecListener);

                            getLog().debug("expressionVars = " + expressionVars);

                            if (expressionVars != null) {
                                for (Map.Entry<Object, Object> var : expressionVars.entrySet()) {
                                    this.liquibase.setChangeLogParameter(var.getKey().toString(), var.getValue());
                                }
                            }

                            getLog().debug("expressionVariables = " + expressionVariables);
                            if (expressionVariables != null) {
                                for (Map.Entry var : (Set<Map.Entry>) expressionVariables.entrySet()) {
                                    if (var.getValue() != null) {
                                        this.liquibase.setChangeLogParameter(var.getKey().toString(), var.getValue());
                                    }
                                }
                            }

                            if (clearCheckSums) {
                                getLog().info("Clearing the Liquibase checksums on the database");
                                liquibase.clearCheckSums();
                            }

                            getLog().info("Executing on Database: " + url);

                            if (isPromptOnNonLocalDatabase()) {
                                getLog().info("NOTE: The promptOnLocalDatabase functionality has been removed");
                            }
                        }
                        setupBindInfoPackage();

                        //
                        // Add another scope child with a map so that
                        // we can set the preserveSchemaCase property,
                        // which might have been specified in a defaults file
                        //
                        Map<String, Object> innerScopeValues = new HashMap<>();
                        innerScopeValues.put(key, preserveSchemaCase);
                        Scope.child(innerScopeValues, () -> performLiquibaseTask(liquibase));
                    } catch (LiquibaseException e) {
                        cleanup(database);
                        throw new MojoExecutionException("\nError setting up or running Liquibase:\n" + e.getMessage(), e);
                    }

                    cleanup(database);
                    getLog().info(MavenUtils.LOG_SEPARATOR);
                    getLog().info("");
                });
            });
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected Field getField(Class clazz, String name) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (clazz.equals(Object.class)) {
                return null;
            } else {
                return getField(clazz.getSuperclass(), name);
            }
        }
    }

    protected Liquibase getLiquibase() {
        return liquibase;
    }

    protected void setupBindInfoPackage() {
        String nsuri = "http://www.hibernate.org/xsd/orm/hbm";
        String packageInfoClassName = "org.hibernate.boot.jaxb.hbm.spi.package-info";
        try {
            final Class<?> packageInfoClass = Class.forName(packageInfoClassName);
            final XmlSchema xmlSchema = packageInfoClass.getAnnotation(XmlSchema.class);
            if (xmlSchema == null) {
                this.getLog().warn(MessageFormat
                        .format("Class [{0}] is missing the [{1}] annotation. Processing bindings will probably fail.",
                                packageInfoClassName, XmlSchema.class.getName()));
            } else {
                final String namespace = xmlSchema.namespace();
                if (nsuri.equals(namespace)) {
                    this.getLog().warn(MessageFormat
                            .format("Namespace of the [{0}] annotation does not match [{1}]. Processing bindings will probably fail.",
                                    XmlSchema.class.getName(), nsuri));
                }
            }
        } catch (ClassNotFoundException cnfex) {
            this.getLog().debug(MessageFormat
                    .format("Class [{0}] could not be found. Processing hibernate bindings will probably fail if applicable.",
                            packageInfoClassName), cnfex);
        }
    }

    protected abstract void performLiquibaseTask(Liquibase liquibase)
            throws LiquibaseException;

    /**
     * @deprecated no longer prompts
     */
    protected boolean isPromptOnNonLocalDatabase() {
        return false;
    }

    private void displayMojoSettings() {
        if (verbose) {
            getLog().info("Settings\n----------------------------");
            printSettings("    ");
            getLog().info(MavenUtils.LOG_SEPARATOR);
        }
    }

    protected Liquibase createLiquibase(Database db) throws MojoExecutionException {
        return new Liquibase("", Scope.getCurrentScope().getResourceAccessor(), db);
    }

    public void configureFieldsAndValues() throws MojoExecutionException {
        // Load the properties file if there is one, but only for values that the user has not
        // already specified.
        if (propertyFile != null) {
            getLog().info("Parsing Liquibase Properties File");
            getLog().info("  File: " + propertyFile);
            try (InputStream is = handlePropertyFileInputStream(propertyFile)) {
                if (is == null) {
                    throw new MojoExecutionException(FileUtil.getFileNotFoundMessage(propertyFile));
                }
                parsePropertiesFile(is);
                getLog().info(MavenUtils.LOG_SEPARATOR);
            } catch (IOException | MojoFailureException e) {
                throw new UnexpectedLiquibaseException(e);
            }
            try (InputStream is = handlePropertyFileInputStream(propertyFile)) {
                LiquibaseConfiguration liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);
                final DefaultsFileValueProvider fileProvider = new DefaultsFileValueProvider(is, "Property file " + propertyFile);
                liquibaseConfiguration.registerProvider(fileProvider);
            } catch (IOException | MojoFailureException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }
    }

    protected void configureChangeLogProperties() throws MojoFailureException, MojoExecutionException {
        if (propertyFile != null) {
            getLog().info("Parsing Liquibase Properties File " + propertyFile + " for changeLog parameters");
            try (InputStream propertiesInputStream = handlePropertyFileInputStream(propertyFile)) {
                Properties props = loadProperties(propertiesInputStream);
                for (Map.Entry entry : props.entrySet()) {
                    String key = (String) entry.getKey();
                    if (key.startsWith("parameter.")) {
                        getLog().debug("Setting changeLog parameter " + key);
                        liquibase.setChangeLogParameter(key.replaceFirst("^parameter.", ""), entry.getValue());
                    }
                }
            } catch (IOException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }
    }

    private static InputStream handlePropertyFileInputStream(String propertyFile) throws MojoFailureException {
        try {
            return Scope.getCurrentScope().getResourceAccessor().getExisting(propertyFile).openInputStream();
        } catch (IOException e) {
            throw new MojoFailureException("Failed to resolve the properties file.", e);
        }
    }

    protected ClassLoader getMavenArtifactClassLoader() throws MojoExecutionException {
        try {
            return MavenUtils.getArtifactClassloader(project,
                    includeArtifact,
                    includeTestOutputDirectory,
                    getClass(),
                    getLog(),
                    verbose);
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Failed to create artifact classloader", e);
        }
    }

    /**
     * Returns an isolated classloader.
     *
     * @return ClassLoader
     * @noinspection unchecked
     */
    protected ClassLoader getClassLoaderIncludingProjectClasspath() throws MojoExecutionException {
        try {
            List<String> classpathElements = project.getCompileClasspathElements();
            classpathElements.add(project.getBuild().getOutputDirectory());
            URL urls[] = new URL[classpathElements.size()];
            for (int i = 0; i < classpathElements.size(); ++i) {
                urls[i] = new File((String) classpathElements.get(i)).toURI().toURL();
            }
            return new URLClassLoader(urls, getMavenArtifactClassLoader());
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to create project classloader", e);
        }
    }

    @SuppressWarnings("java:S2095")
    protected ResourceAccessor getResourceAccessor(ClassLoader cl) throws IOException, MojoFailureException {
        ResourceAccessor mFO = new MavenResourceAccessor(cl);
        ResourceAccessor fsFO = new DirectoryResourceAccessor(project.getBasedir());
        return new SearchPathResourceAccessor(searchPath, mFO, fsFO);
    }

    /**
     * Performs some validation after the properties file has been loaded checking that all
     * properties required have been specified.
     *
     * @throws MojoFailureException If any property that is required has not been
     *                              specified.
     */
    protected void checkRequiredParametersAreSpecified() throws MojoFailureException {
        if (databaseConnectionRequired()) {
            if (url == null) {
                throw new MojoFailureException("The database URL has not been specified either as "
                        + "a parameter or in a properties file.");
            }

            if ((password != null) && emptyPassword) {
                throw new MojoFailureException("A password cannot be present and the empty "
                        + "password property both be specified.");
            }
        }
    }

    /**
     * Optionally, an implementation of this mojo can override this to indicate that a connection to the database
     * is not required.
     */
    public boolean databaseConnectionRequired() {
        return true;
    }

    /**
     * Prints the settings that have been set of defaulted for the plugin. These will only
     * be shown in verbose mode.
     *
     * @param indent The indent string to use when printing the settings.
     */
    protected void printSettings(String indent) {
        if (indent == null) {
            indent = "";
        }
        getLog().info(indent + "driver: " + driver);
        getLog().info(indent + "url: " + url);
        getLog().info(indent + "username: " + "*****");
        getLog().info(indent + "password: " + "*****");
        getLog().info(indent + "use empty password: " + emptyPassword);
        getLog().info(indent + "properties file: " + propertyFile);
        getLog().info(indent + "properties file will override? " + propertyFileWillOverride);
        getLog().info(indent + "clear checksums? " + clearCheckSums);
    }

    protected void cleanup(Database db) {
        // Clean up the connection
        if (db != null) {
            try {
                db.rollback();
                db.close();
            } catch (DatabaseException e) {
                getLog().error("Failed to close open connection to database.", e);
            }
        }
    }

    private static Properties loadProperties(InputStream propertiesInputStream) throws MojoExecutionException {
        Properties props = new Properties();
        try {
            props.load(propertiesInputStream);
            return props;
        } catch (IOException e) {
            throw new MojoExecutionException("Could not load the properties Liquibase file", e);
        }
    }

    /**
     * Parses a properties file and sets the associated fields in the plugin.
     *
     * @param propertiesInputStream The input stream which is the Liquibase properties that
     *                              needs to be parsed.
     * @throws org.apache.maven.plugin.MojoExecutionException If there is a problem parsing
     *                                                        the file.
     */
    protected void parsePropertiesFile(InputStream propertiesInputStream)
            throws MojoExecutionException {
        if (propertiesInputStream == null) {
            throw new MojoExecutionException("Properties file InputStream is null.");
        }
        Properties props = loadProperties(propertiesInputStream);

        for (Iterator<Object> it = props.keySet().iterator(); it.hasNext(); ) {
            String key = null;
            try {
                key = (String) it.next();
                Field field = MavenUtils.getDeclaredField(this.getClass(), key);

                if (propertyFileWillOverride) {
                    getLog().debug("  properties file setting value: " + field.getName());
                    setFieldValue(field, props.get(key).toString());
                } else {
                    if (!isCurrentFieldValueSpecified(field)) {
                        getLog().debug("  properties file setting value: " + field.getName());
                        setFieldValue(field, props.get(key).toString());
                    }
                }
            } catch (Exception e) {
                getLog().info("  '" + key + "' in properties file is not being used by this "
                        + "task.");
            }
        }
    }

    /**
     * This method will check to see if the user has specified a value different to that of
     * the default value. This is not an ideal solution, but should cover most situations in
     * the use of the plugin.
     *
     * @param f The Field to check if a user has specified a value for.
     * @return <code>true</code> if the user has specified a value.
     */
    private boolean isCurrentFieldValueSpecified(Field f) throws IllegalAccessException {
        Object currentValue = f.get(this);
        if (currentValue == null) {
            return false;
        }

        Object defaultValue = getDefaultValue(f);
        if (defaultValue == null) {
            return currentValue != null;
        } else {
            // There is a default value, check to see if the user has selected something other
            // than the default
            return !defaultValue.equals(f.get(this));
        }
    }

    private Object getDefaultValue(Field field) throws IllegalAccessException {
        List<Field> allFields = new ArrayList<>();
        allFields.addAll(Arrays.asList(getClass().getDeclaredFields()));
        allFields.addAll(Arrays.asList(AbstractLiquibaseMojo.class.getDeclaredFields()));

        for (Field f : allFields) {
            if (f.getName().equals(field.getName() + DEFAULT_FIELD_SUFFIX)) {
                f.setAccessible(true);
                return f.get(this);
            }
        }
        return null;
    }

    private void setFieldValue(Field field, String value) throws IllegalAccessException {
        value = Scope.getCurrentScope().getSingleton(ConfiguredValueModifierFactory.class).override(value);
        if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
            field.set(this, Boolean.valueOf(value));
        } else if (field.getType().isEnum()) {
            field.set(this, Enum.valueOf(field.getType().asSubclass(Enum.class), value));
        } else if (field.getType().equals(File.class)) {
            field.set(this, new File(value));
        } else {
            field.set(this, value);
        }
    }

    @SuppressWarnings("unchecked")
    private void processSystemProperties() {
        if (systemProperties == null) {
            systemProperties = new Properties();
        }
        // Add all system properties configured by the user
        Iterator<Object> iter = systemProperties.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            String value = systemProperties.getProperty(key);
            System.setProperty(key, value);
        }
    }

    private Map<String, Object> getNativeExecutorProperties() {
        Map<String, Object> nativeProperties = new HashMap<>();
        // Don't add properties to the map if the value is null
        nativeProperties.computeIfAbsent("liquibase.psql.path", val -> psqlPath);
        nativeProperties.computeIfAbsent("liquibase.psql.keep.temp", val -> psqlKeepTemp);
        nativeProperties.computeIfAbsent("liquibase.psql.keep.temp.name", val -> psqlKeepTempName);
        nativeProperties.computeIfAbsent("liquibase.psql.keep.temp.path", val -> psqlKeepTempPath);
        nativeProperties.computeIfAbsent("liquibase.psql.args", val -> psqlArgs);
        nativeProperties.computeIfAbsent("liquibase.psql.timeout", val -> psqlTimeout);
        nativeProperties.computeIfAbsent("liquibase.psql.logFile", val -> psqlLogFile);
        nativeProperties.computeIfAbsent("liquibase.sqlplus.path", val -> sqlPlusPath);
        nativeProperties.computeIfAbsent("liquibase.sqlplus.keep.temp", val -> sqlPlusKeepTemp);
        nativeProperties.computeIfAbsent("liquibase.sqlplus.keep.temp.name", val -> sqlPlusKeepTempName);
        nativeProperties.computeIfAbsent("liquibase.sqlplus.keep.temp.path", val -> sqlPlusKeepTempPath);
        nativeProperties.computeIfAbsent("liquibase.sqlplus.keep.temp.overwrite", val -> sqlPlusKeepTempOverwrite);
        nativeProperties.computeIfAbsent("liquibase.sqlplus.args", val -> sqlPlusArgs);
        nativeProperties.computeIfAbsent("liquibase.sqlplus.timeout", val -> sqlPlusTimeout);
        nativeProperties.computeIfAbsent("liquibase.sqlplus.logFile", val -> sqlPlusLogFile);
        nativeProperties.computeIfAbsent("liquibase.sqlcmd.path", val -> sqlcmdPath);
        nativeProperties.computeIfAbsent("liquibase.sqlcmd.keep.temp", val -> sqlcmdKeepTemp);
        nativeProperties.computeIfAbsent("liquibase.sqlcmd.keep.temp.name", val -> sqlcmdKeepTempName);
        nativeProperties.computeIfAbsent("liquibase.sqlcmd.keep.temp.path", val -> sqlcmdKeepTempPath);
        nativeProperties.computeIfAbsent("liquibase.sqlcmd.keep.temp.overwrite", val -> sqlcmdKeepTempOverwrite);
        nativeProperties.computeIfAbsent("liquibase.sqlcmd.args", val -> sqlcmdArgs);
        nativeProperties.computeIfAbsent("liquibase.sqlcmd.timeout", val -> sqlcmdTimeout);
        nativeProperties.computeIfAbsent("liquibase.sqlcmd.logFile", val -> sqlcmdLogFile);
        nativeProperties.computeIfAbsent("liquibase.sqlcmd.catalogName", val -> sqlcmdCatalogName);
        return nativeProperties;
    }

    @Override
    public synchronized Log getLog() {
        if (logCache == null) {
            logCache = new MavenLog(super.getLog(), logLevel);
        }
        return logCache;
    }

    /**
     * Returns the OutputStream based on whether there is an outputFile provided.
     * If no outputFile parameter is provided, defaults to System.out.
     * @param outputFile the string outputFile
     * @return the OutputStream to use
     * @throws LiquibaseException if we cannot create the provided outputFile
     */
    protected OutputStream getOutputStream(String outputFile) throws LiquibaseException {
        if (outputFile == null) {
            return System.out;
        }
        FileOutputStream fileOut;
        try {
            fileOut = new FileOutputStream(outputFile, false);
        } catch (IOException e) {
            Scope.getCurrentScope().getLog(getClass()).severe(String.format(
                    coreBundle.getString("could.not.create.output.file"),
                    outputFile));
            throw new LiquibaseException(e);
        }
        return fileOut;
    }

    /**
     * Calls the {@link ConfiguredValueModifierFactory} to expand the provided value.
     * @return the expanded value, or the original value if expansion is not needed
     * @throws Exception if expansion fails
     */
    private String modifyValue(String value) throws Exception {
        return Scope.child(Collections.singletonMap("liquibase.licenseKey", getLicenseKey()), () -> Scope.getCurrentScope().getSingleton(ConfiguredValueModifierFactory.class).override(value));
    }
}
