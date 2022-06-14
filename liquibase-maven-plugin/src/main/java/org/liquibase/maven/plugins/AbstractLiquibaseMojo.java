package org.liquibase.maven.plugins;

import liquibase.GlobalConfiguration;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.configuration.core.DefaultsFileValueProvider;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.integration.IntegrationDetails;
import liquibase.integration.commandline.CommandLineUtils;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.util.FileUtil;
import liquibase.util.StringUtil;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.Parameter;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.liquibase.maven.property.PropertyElement;

import javax.xml.bind.annotation.XmlSchema;
import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.*;

/**
 * A base class for providing Liquibase {@link liquibase.Liquibase} functionality.
 *
 * @author Peter Murray
 * @author Florent Biville
 * <p>
 * Test dependency is used because when you run a goal outside the build phases you want to have the same dependencies
 * that it would had if it was ran inside test phase
 * @requiresDependencyResolution test
 */
@SuppressWarnings("java:S2583")
public abstract class AbstractLiquibaseMojo extends AbstractMojo {

    /**
     * Suffix for fields that are representing a default value for a another field.
     */
    private static final String DEFAULT_FIELD_SUFFIX = "Default";

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
    /**
     * Specifies the database password for database connection.
     *
     * @parameter property="liquibase.password"
     */
    @PropertyElement
    protected String password;

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
     * See https://maven.apache.org/maven-logging.html for more information.
     *
     * @parameter property="liquibase.logging"
     * @deprecated Logging managed by maven
     */
    @PropertyElement
    protected String logging;
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
     * Specifies a list of system properties you want to to pass to the database.
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

    protected String commandName;

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
        if (outputFileEncoding == null) {
            getLog().info("Char encoding not set! The created file will be system dependent!");
            return new OutputStreamWriter(new FileOutputStream(outputFile), GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue());
        }
        getLog().debug("Writing output file with [" + outputFileEncoding + "] file encoding.");
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), outputFileEncoding));
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (StringUtil.trimToNull(logging) != null) {
            getLog().error("The liquibase-maven-plugin now manages logging via the standard maven logging config, not the 'logging' configuration. Use the -e, -X or -q flags or see https://maven.apache.org/maven-logging.html");
        }

        try {
            Scope.child(Scope.Attr.logService, new MavenLogService(getLog()), () -> {

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
                if (skip) {
                    getLog().warn("Liquibase skipped due to Maven configuration");
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
                            getLog().debug("Cannot read current maven value for. Will not send the value to hub " + name);
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
                Scope.child(scopeValues, () -> {

                    configureFieldsAndValues();

                    if (showBanner) {
                        getLog().info(CommandLineUtils.getBanner());
                    }

                    // Displays the settings for the Mojo depending of verbosity mode.
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
                            liquibase = createLiquibase(database);

                            configureChangeLogProperties();

                            getLog().debug("expressionVars = " + String.valueOf(expressionVars));

                            if (expressionVars != null) {
                                for (Map.Entry<Object, Object> var : expressionVars.entrySet()) {
                                    this.liquibase.setChangeLogParameter(var.getKey().toString(), var.getValue());
                                }
                            }

                            getLog().debug("expressionVariables = " + String.valueOf(expressionVariables));
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
                        Scope.child(innerScopeValues, () -> {
                            performLiquibaseTask(liquibase);
                        });
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

    public void configureFieldsAndValues() throws MojoExecutionException, MojoFailureException {
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
            } catch (IOException e) {
                throw new UnexpectedLiquibaseException(e);
            }
            try (InputStream is = handlePropertyFileInputStream(propertyFile)) {
                if (is == null) {
                    throw new MojoExecutionException(FileUtil.getFileNotFoundMessage(propertyFile));
                }

                LiquibaseConfiguration liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);
                final DefaultsFileValueProvider fileProvider = new DefaultsFileValueProvider(is, "Property file "+propertyFile);
                liquibaseConfiguration.registerProvider(fileProvider);
            } catch (IOException e) {
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
        InputStream is;
        try {
            is = Scope.getCurrentScope().getResourceAccessor().openStream(null, propertyFile);
        } catch (IOException e) {
            throw new MojoFailureException("Failed to resolve the properties file.", e);
        }
        return is;
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
            List classpathElements = project.getCompileClasspathElements();
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

    protected ResourceAccessor getResourceAccessor(ClassLoader cl) {
        ResourceAccessor mFO = new MavenResourceAccessor(cl);
        ResourceAccessor fsFO = new FileSystemResourceAccessor(project.getBasedir());
        return new CompositeResourceAccessor(mFO, fsFO);
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

        for (Iterator it = props.keySet().iterator(); it.hasNext(); ) {
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
        if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
            field.set(this, Boolean.valueOf(value));
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
        Iterator iter = systemProperties.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            String value = systemProperties.getProperty(key);
            System.setProperty(key, value);
        }
    }

}
