package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.configuration.GlobalConfiguration;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.integration.commandline.CommandLineUtils;
import liquibase.logging.LogFactory;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;
import liquibase.util.ui.UIFactory;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.wagon.authentication.AuthenticationInfo;

import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * A base class for providing Liquibase {@link liquibase.Liquibase} functionality.
 *
 * @author Peter Murray
 * @author Florent Biville
 *
 * Test dependency is used because when you run a goal outside the build phases you want to have the same dependencies
 * that it would had if it was ran inside test phase
 * @requiresDependencyResolution test
 */
public abstract class AbstractLiquibaseMojo extends AbstractMojo {

    /**
     * Suffix for fields that are representing a default value for a another field.
     */
    private static final String DEFAULT_FIELD_SUFFIX = "Default";

    /**
     * The fully qualified name of the driver class to use to connect to the database.
     *
     * @parameter expression="${liquibase.driver}"
     */
    protected String driver;

    /**
     * The Database URL to connect to for executing Liquibase.
     *
     * @parameter expression="${liquibase.url}"
     */
    protected String url;

    /**

     The Maven Wagon manager to use when obtaining server authentication details.
     @component role="org.apache.maven.artifact.manager.WagonManager"
     @required
     @readonly
     */
    protected WagonManager wagonManager;
    /**
     * The server id in settings.xml to use when authenticating with.
     *
     * @parameter expression="${liquibase.server}"
     */
    private String server;

    /**
     * The database username to use to connect to the specified database.
     *
     * @parameter expression="${liquibase.username}"
     */
    protected String username;

    /**
     * The database password to use to connect to the specified database.
     *
     * @parameter expression="${liquibase.password}"
     */
    protected String password;

    /**
     * Use an empty string as the password for the database connection. This should not be
     * used along side the {@link #password} setting.
     *
     * @parameter expression="${liquibase.emptyPassword}" default-value="false"
     * @deprecated Use an empty or null value for the password instead.
     */
    protected boolean emptyPassword;

	/**
	 * Whether to ignore the schema name.
	 *
	 * @parameter expression="${liquibase.outputDefaultSchema}"
	 */
	protected boolean outputDefaultSchema;

    /**
     * Whether to ignore the schema name.
     *
     * @parameter expression="${liquibase.outputDefaultCatalog}"
     */
    protected boolean outputDefaultCatalog;

    /**
     * The default catalog name to use the for database connection.
     *
     * @parameter expression="${liquibase.defaultCatalogName}"
     */
    protected String defaultCatalogName;

    /**
     * The default schema name to use the for database connection.
     *
     * @parameter expression="${liquibase.defaultSchemaName}"
     */
    protected String defaultSchemaName;

    /**
     * The class to use as the database object.
     *
     * @parameter expression="${liquibase.databaseClass}"
     */
    protected String databaseClass;
    
    /**
     * The class to use as the property provider (must be a java.util.Properties implementation).
     * 
     * @parameter expression="${liquibase.propertyProviderClass}"
     */
    protected String propertyProviderClass;

    /**
     * Controls the prompting of users as to whether or not they really want to run the
     * changes on a database that is not local to the machine that the user is current
     * executing the plugin on.
     *
     * @parameter expression="${liquibase.promptOnNonLocalDatabase}" default-value="true"
     */
    protected boolean promptOnNonLocalDatabase;

    /**
     * Allows for the maven project artifact to be included in the class loader for
     * obtaining the Liquibase property and DatabaseChangeLog files.
     *
     * @parameter expression="${liquibase.includeArtifact}" default-value="true"
     */
    protected boolean includeArtifact;

    /**
     * Allows for the maven test output directory to be included in the class loader for
     * obtaining the Liquibase property and DatabaseChangeLog files.
     *
     * @parameter expression="${liquibase.includeTestOutputDirectory}" default-value="true"
     */
    protected boolean includeTestOutputDirectory;

    /**
     * Controls the verbosity of the output from invoking the plugin.
     *
     * @parameter expression="${liquibase.verbose}" default-value="false"
     * @description Controls the verbosity of the plugin when executing
     */
    protected boolean verbose;

    /**
     * Controls the level of logging from Liquibase when executing. The value can be
     * "debug", "info", "warning", "severe", or "off". The value is
     * case insensitive.
     *
     * @parameter expression="${liquibase.logging}" default-value="INFO"
     * @description Controls the verbosity of the plugin when executing
     */
    protected String logging;

    /**
     * The Liquibase properties file used to configure the Liquibase {@link
     * liquibase.Liquibase}.
     *
     * @parameter expression="${liquibase.propertyFile}"
     */
    protected String propertyFile;

    /**
     * Flag allowing for the Liquibase properties file to override any settings provided in
     * the Maven plugin configuration. By default if a property is explicity specified it is
     * not overridden if it also appears in the properties file.
     *
     * @parameter expression="${liquibase.propertyFileWillOverride}" default-value="false"
     */
    protected boolean propertyFileWillOverride;

    /**
     * Flag for forcing the checksums to be cleared from teh DatabaseChangeLog table.
     *
     * @parameter expression="${liquibase.clearCheckSums}" default-value="false"
     */
    protected boolean clearCheckSums;

    /**
     * List of system properties to pass to the database.
     *
     * @parameter
     */
    protected Properties systemProperties;

    /**
     * The Maven project that plugin is running under.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The {@link Liquibase} object used modify the database.
     */
    private Liquibase liquibase;

    /**
     * Array to put a expression variable to maven plugin.
     *
     * @parameter
     */
    private Properties expressionVars;

    /**
     * Set this to 'false' to skip running liquibase. Its use is NOT RECOMMENDED, but quite
     * convenient on occasion.
     *
     * @parameter expression="${liquibase.should.run}"
     */
    protected boolean liquibaseShouldRun;

    /**
     * Array to put a expression variable to maven plugin.
     *
     * @parameter
     */
    private Map expressionVariables;

    /**
     * Flag to set the character encoding of the output file produced by Liquibase during the updateSQL phase.
     *
     * @parameter expression="${liquibase.outputFileEncoding}"
     */
    protected String outputFileEncoding;
    /**
     * Schema against which Liquibase changelog tables will be created.
     *
     * @parameter expression="${liquibase.changelogCatalogName}"
     */
    protected String changelogCatalogName;
    /**
     * Schema against which Liquibase changelog tables will be created.
     *
     * @parameter expression="${liquibase.changelogSchemaName}"
     */
    protected String changelogSchemaName;


    protected Writer getOutputWriter(final File outputFile) throws IOException {
        if (outputFileEncoding==null) {
            getLog().info("Char encoding not set! The created file will be system dependent!");
            return new FileWriter(outputFile);
        }
        getLog().debug("Writing output file with [" + outputFileEncoding + "] file encoding.");
        return new BufferedWriter(new OutputStreamWriter( new FileOutputStream(outputFile), outputFileEncoding));
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info(MavenUtils.LOG_SEPARATOR);

        if (server != null) {
            AuthenticationInfo info = wagonManager.getAuthenticationInfo(server);
            if (info != null) {
                username = info.getUserName();
                password = info.getPassword();
            }
        }

        processSystemProperties();

        LiquibaseConfiguration liquibaseConfiguration = LiquibaseConfiguration.getInstance();

        if (!liquibaseConfiguration.getConfiguration(GlobalConfiguration.class).getShouldRun()) {
            getLog().info("Liquibase did not run because " + liquibaseConfiguration.describeValueLookupLogic(GlobalConfiguration.class, GlobalConfiguration.SHOULD_RUN) + " was set to false");
            return;
        }

        if (!liquibaseShouldRun) {
            getLog().warn("Liquibase skipped due to maven configuration");
            return;
        }

        ClassLoader artifactClassLoader = getMavenArtifactClassLoader();
        configureFieldsAndValues(getFileOpener(artifactClassLoader));

        LogFactory.getInstance().setDefaultLoggingLevel(logging);

        // Displays the settings for the Mojo depending of verbosity mode.
        displayMojoSettings();

        // Check that all the parameters that must be specified have been by the user.
        checkRequiredParametersAreSpecified();

        Database database = null;
        try {
            String dbPassword = emptyPassword || password == null ? "" : password;
            database = CommandLineUtils.createDatabaseObject(artifactClassLoader,
                    url,
                    username,
                    dbPassword,
                    driver,
                    defaultCatalogName,
                    defaultSchemaName,
                    outputDefaultCatalog,
                    outputDefaultSchema,
                    databaseClass,
                    null,
                    propertyProviderClass,
                    changelogCatalogName,
                    changelogSchemaName);
            liquibase = createLiquibase(getFileOpener(artifactClassLoader), database);

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
                getLog().info("Clearing the Liquibase Checksums on the database");
                liquibase.clearCheckSums();
            }

            getLog().info("Executing on Database: " + url);

            if (isPromptOnNonLocalDatabase()) {
                if (!liquibase.isSafeToRunUpdate()) {
                    if (UIFactory.getInstance().getFacade().promptForNonLocalDatabase(liquibase.getDatabase())) {
                        throw new LiquibaseException("User decided not to run against non-local database");
                    }
                }
            }

            performLiquibaseTask(liquibase);
        }
        catch (LiquibaseException e) {
            cleanup(database);
            throw new MojoExecutionException("Error setting up or running Liquibase: " + e.getMessage(), e);
        }

        cleanup(database);
        getLog().info(MavenUtils.LOG_SEPARATOR);
        getLog().info("");
    }

    protected Liquibase getLiquibase() {
        return liquibase;
    }

    protected abstract void performLiquibaseTask(Liquibase liquibase)
            throws LiquibaseException;

    protected boolean isPromptOnNonLocalDatabase() {
        return promptOnNonLocalDatabase;
    }

    private void displayMojoSettings() {
        if (verbose) {
            getLog().info("Settings\n----------------------------");
            printSettings("    ");
            getLog().info(MavenUtils.LOG_SEPARATOR);
        }
    }

    protected Liquibase createLiquibase(ResourceAccessor fo, Database db) throws MojoExecutionException {
        try {
            return new Liquibase("", fo, db);
        } catch (LiquibaseException ex) {
            throw new MojoExecutionException("Error creating liquibase: "+ex.getMessage(),ex);
        }
    }

    public void configureFieldsAndValues(ResourceAccessor fo)
            throws MojoExecutionException, MojoFailureException {
        // Load the properties file if there is one, but only for values that the user has not
        // already specified.
        if (propertyFile != null) {
            getLog().info("Parsing Liquibase Properties File");
            getLog().info("  File: " + propertyFile);
            InputStream is;
            try {
                is = StreamUtil.singleInputStream(propertyFile, fo);
            } catch (IOException e) {
                throw new UnexpectedLiquibaseException(e);
            }
            if (is == null) {
                throw new MojoFailureException("Failed to resolve the properties file.");
            }
            parsePropertiesFile(is);
            getLog().info(MavenUtils.LOG_SEPARATOR);
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
        }
        catch (MalformedURLException e) {
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
                urls[i] = new File((String) classpathElements.get(i)).toURL();
            }
            return new URLClassLoader(urls, getMavenArtifactClassLoader());
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to create project classloader", e);
        }
    }

    protected ResourceAccessor getFileOpener(ClassLoader cl) {
        ResourceAccessor mFO = new MavenResourceAccessor(cl);
        ResourceAccessor fsFO = new FileSystemResourceAccessor(project.getBasedir().getAbsolutePath());
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
        if (url == null) {
            throw new MojoFailureException("The database URL has not been specified either as "
                    + "a parameter or in a properties file.");
        }

        if (password != null && emptyPassword) {
            throw new MojoFailureException("A password cannot be present and the empty "
                    + "password property both be specified.");
        }
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
        getLog().info(indent + "username: " + username);
        getLog().info(indent + "password: " + "*****");
        getLog().info(indent + "use empty password: " + emptyPassword);
        getLog().info(indent + "properties file: " + propertyFile);
        getLog().info(indent + "properties file will override? " + propertyFileWillOverride);
        getLog().info(indent + "prompt on non-local database? " + promptOnNonLocalDatabase);
        getLog().info(indent + "clear checksums? " + clearCheckSums);
    }

    protected void cleanup(Database db) {
        // Clean up the connection
        if (db != null) {
            try {
                db.rollback();
                db.close();
            }
            catch (DatabaseException e) {
                getLog().error("Failed to close open connection to database.", e);
            }
        }
    }

    /**
     * Parses a properties file and sets the assocaited fields in the plugin.
     *
     * @param propertiesInputStream The input stream which is the Liquibase properties that
     *                              needs to be parsed.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *          If there is a problem parsing
     *          the file.
     */
    protected void parsePropertiesFile(InputStream propertiesInputStream)
            throws MojoExecutionException {
        if (propertiesInputStream == null) {
            throw new MojoExecutionException("Properties file InputStream is null.");
        }
        Properties props = new Properties();
        try {
            props.load(propertiesInputStream);
        }
        catch (IOException e) {
            throw new MojoExecutionException("Could not load the properties Liquibase file", e);
        }

        for (Iterator it = props.keySet().iterator(); it.hasNext();) {
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
            }
            catch (Exception e) {
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
        List<Field> allFields = new ArrayList<Field>();
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
        } else {
            field.set(this, value);
        }
    }

    @SuppressWarnings("unchecked")
    private void processSystemProperties() {
        if (systemProperties == null)
        {
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
