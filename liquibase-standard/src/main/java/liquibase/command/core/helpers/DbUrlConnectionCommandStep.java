package liquibase.command.core.helpers;

import liquibase.Beta;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.command.CleanUpCommandStep;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandResultsBuilder;
import liquibase.command.CommandScope;
import liquibase.configuration.ConfiguredValue;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.license.LicenseTrack;
import liquibase.license.LicenseTrackingArgs;
import liquibase.logging.mdc.MdcKey;
import liquibase.util.ExceptionUtil;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Internal command step to be used on CommandStep pipeline to manage the database connection.
 */
public class DbUrlConnectionCommandStep extends AbstractDatabaseConnectionCommandStep implements CleanUpCommandStep {

    protected static final String[] COMMAND_NAME = {"dbUrlConnectionCommandStep"};

    /**
     * @deprecated This field is retained for backwards compatibility. Use the fields in {@link DbUrlConnectionArgumentsCommandStep} instead.
     */
    @Deprecated
    public static final CommandArgumentDefinition<Database> DATABASE_ARG = DbUrlConnectionArgumentsCommandStep.DATABASE_ARG;
    /**
     * @deprecated This field is retained for backwards compatibility. Use the fields in {@link DbUrlConnectionArgumentsCommandStep} instead.
     */
    @Deprecated
    public static final CommandArgumentDefinition<String> URL_ARG = DbUrlConnectionArgumentsCommandStep.URL_ARG;
    /**
     * @deprecated This field is retained for backwards compatibility. Use the fields in {@link DbUrlConnectionArgumentsCommandStep} instead.
     */
    @Deprecated
    public static final CommandArgumentDefinition<String> DEFAULT_SCHEMA_NAME_ARG = DbUrlConnectionArgumentsCommandStep.DEFAULT_SCHEMA_NAME_ARG;
    /**
     * @deprecated This field is retained for backwards compatibility. Use the fields in {@link DbUrlConnectionArgumentsCommandStep} instead.
     */
    @Deprecated
    public static final CommandArgumentDefinition<String> DEFAULT_CATALOG_NAME_ARG = DbUrlConnectionArgumentsCommandStep.DEFAULT_CATALOG_NAME_ARG;
    /**
     * @deprecated This field is retained for backwards compatibility. Use the fields in {@link DbUrlConnectionArgumentsCommandStep} instead.
     */
    @Deprecated
    public static final CommandArgumentDefinition<String> USERNAME_ARG = DbUrlConnectionArgumentsCommandStep.USERNAME_ARG;
    /**
     * @deprecated This field is retained for backwards compatibility. Use the fields in {@link DbUrlConnectionArgumentsCommandStep} instead.
     */
    @Deprecated
    public static final CommandArgumentDefinition<String> PASSWORD_ARG = DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG;
    /**
     * @deprecated This field is retained for backwards compatibility. Use the fields in {@link DbUrlConnectionArgumentsCommandStep} instead.
     */
    @Deprecated
    public static final CommandArgumentDefinition<String> DRIVER_ARG = DbUrlConnectionArgumentsCommandStep.DRIVER_ARG;
    /**
     * @deprecated This field is retained for backwards compatibility. Use the fields in {@link DbUrlConnectionArgumentsCommandStep} instead.
     */
    @Deprecated
    public static final CommandArgumentDefinition<String> DRIVER_PROPERTIES_FILE_ARG = DbUrlConnectionArgumentsCommandStep.DRIVER_PROPERTIES_FILE_ARG;

    /**
     * @deprecated This field is retained for backwards compatibility. Use the fields in {@link DbUrlConnectionArgumentsCommandStep} instead.
     */
    @Beta
    @Deprecated
    public static final CommandArgumentDefinition<Boolean> SKIP_DATABASE_STEP_ARG = DbUrlConnectionArgumentsCommandStep.SKIP_DATABASE_STEP_ARG;

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        if (commandScope.getArgumentValue(DbUrlConnectionArgumentsCommandStep.SKIP_DATABASE_STEP_ARG)) {
            return;
        }
        commandScope.provideDependency(Database.class, this.obtainDatabase(commandScope));
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Collections.singletonList(DbUrlConnectionArgumentsCommandStep.class);
    }

    @Override
    public List<Class<?>> providedDependencies() {
        return Collections.singletonList(Database.class);
    }

    /**
     * Try to retrieve and set the database object from the command scope, otherwise creates a new one .
     *
     * @param commandScope current command scope
     * @throws DatabaseException Thrown when there is a connection error
     */
    public Database obtainDatabase(CommandScope commandScope) throws DatabaseException {
        AtomicReference<Database> database = new AtomicReference<>(commandScope.getArgumentValue(DATABASE_ARG));
        String url = commandScope.getArgumentValue(URL_ARG);
        if (database.get() == null) {
            Map<String, Object> scopedValues = new HashMap<>();
            scopedValues.put(Database.IGNORE_MISSING_REFERENCES_KEY, commandScope.getArgumentValue(DiffArgumentsCommandStep.IGNORE_MISSING_REFERENCES));
            try {
                Scope.child(scopedValues, () -> {
                    database.set(createDatabaseObject(
                            url,
                            commandScope.getArgumentValue(USERNAME_ARG),
                            commandScope.getArgumentValue(PASSWORD_ARG),
                            commandScope.getArgumentValue(DbUrlConnectionArgumentsCommandStep.DEFAULT_SCHEMA_NAME_ARG),
                            commandScope.getArgumentValue(DbUrlConnectionArgumentsCommandStep.DEFAULT_CATALOG_NAME_ARG),
                            getDriver(commandScope),
                            commandScope.getArgumentValue(DbUrlConnectionArgumentsCommandStep.DRIVER_PROPERTIES_FILE_ARG),
                            StringUtils.trimToNull(GlobalConfiguration.LIQUIBASE_CATALOG_NAME.getCurrentValue()),
                            StringUtils.trimToNull(GlobalConfiguration.LIQUIBASE_SCHEMA_NAME.getCurrentValue())
                    ));
                });
            } catch (Exception e) {
                throw new DatabaseException(e);
            }
        }
        String urlForLogging = url == null ? database.get().getConnection().getURL() : url;
        logMdc(urlForLogging, database.get());
        logLicenseUsage(urlForLogging, database.get());
        return database.get();
    }

    private static String getDriver(CommandScope commandScope) {
        ConfiguredValue<String> globalConfig = LiquibaseCommandLineConfiguration.DRIVER.getCurrentConfiguredValue();
        ConfiguredValue<String> commandConfig = commandScope.getConfiguredValue(DbUrlConnectionArgumentsCommandStep.DRIVER_ARG);
        if (globalConfig.found() && commandConfig.found()) {
            Scope.getCurrentScope().getLog(DbUrlConnectionCommandStep.class).fine("Ignoring the global " + LiquibaseCommandLineConfiguration.DRIVER.getKey() + " value in favor of the command value.");
        }
        if (commandConfig.found()) {
            return commandConfig.getValue();
        }
        if (globalConfig.found()) {
            return globalConfig.getValue();
        }
        return null;
    }

    public static void logMdc(String url, Database database) {
        Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_TARGET_URL, JdbcConnection.sanitizeUrl(url));
        Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_CATALOG_NAME, database.getLiquibaseCatalogName());
        Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_SCHEMA_NAME, database.getLiquibaseSchemaName());
        ExceptionUtil.doSilently(() -> {
            Scope.getCurrentScope().getAnalyticsEvent().setDatabasePlatform(database.getDatabaseProductName());
        });
        ExceptionUtil.doSilently(() -> {
            Scope.getCurrentScope().getAnalyticsEvent().setDatabaseVersion(database.getDatabaseProductVersion());
        });
    }

    private void logLicenseUsage(String url, Database database) {
        if (Boolean.TRUE.equals(LicenseTrackingArgs.ENABLED.getCurrentValue())) {
            try {
                DatabaseConnection connection = database.getConnection();
                Connection underlyingConnection = connection.getUnderlyingConnection();
                Scope.getCurrentScope().getLicenseTrackList().getLicenseTracks().add(new LicenseTrack(removeQueryParameters(JdbcConnection.sanitizeUrl(url)), underlyingConnection.getSchema(), underlyingConnection.getCatalog()));
            } catch (SQLException | URISyntaxException e) {
                Scope.getCurrentScope().getLog(getClass()).severe("Failed to handle license tracking event", e);
            }
        }
    }

    private static String removeQueryParameters(String jdbcUrl) throws URISyntaxException {
        String jdbcPrefix = "jdbc:";
        boolean startsWithJdbc = jdbcUrl.startsWith(jdbcPrefix);
        URI uri = new URI(jdbcUrl.replaceFirst(jdbcPrefix, StringUtils.EMPTY));
        String cleanedUri = uri.toString();
        try {
            // This the more robust (IMO) way of stripping parameters, but doesn't work for all path formats.
            cleanedUri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, uri.getFragment()).toString();
        } catch (Exception e) {
            // Remove the query parameters manually.
            cleanedUri = cleanedUri.split("\\?")[0];
        }
        // Neither of the methods for cleansing the parameters handle SQL Server type parameters, so these must be handled manually.
        cleanedUri = cleanedUri.split("\\;")[0];

        if (startsWithJdbc) {
            cleanedUri = jdbcPrefix + cleanedUri;
        }
        // Remove trailing slash
        if (cleanedUri.endsWith("/")) {
            cleanedUri = cleanedUri.substring(0, cleanedUri.length() - 1);
        }
        return cleanedUri;
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

}
