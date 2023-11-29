package liquibase.command.core.helpers;

import liquibase.Beta;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandBuilder;
import liquibase.command.CommandResultsBuilder;
import liquibase.command.CommonArgumentNames;
import liquibase.configuration.ConfigurationValueObfuscator;
import liquibase.database.Database;

import java.util.Collections;
import java.util.List;

/**
 * This class contains only the arguments used by {@link DbUrlConnectionCommandStep}.
 */
public class DbUrlConnectionArgumentsCommandStep extends AbstractHelperCommandStep {

    public static final String[] COMMAND_NAME = new String[]{"dbUrlConnectionArguments"};

    public static final CommandArgumentDefinition<Database> DATABASE_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> DEFAULT_SCHEMA_NAME_ARG;
    public static final CommandArgumentDefinition<String> DEFAULT_CATALOG_NAME_ARG;
    public static final CommandArgumentDefinition<String> USERNAME_ARG;
    public static final CommandArgumentDefinition<String> PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> DRIVER_ARG;
    public static final CommandArgumentDefinition<String> DRIVER_PROPERTIES_FILE_ARG;


    /**
     * This Argument skips this step. It may not be a definitive solution, as improvements to pipeline may
     * change the way that we remove/skip steps.
     */
    @Beta
    @Deprecated
    public static final CommandArgumentDefinition<Boolean> SKIP_DATABASE_STEP_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        DEFAULT_SCHEMA_NAME_ARG = builder.argument("defaultSchemaName", String.class)
                .description("The default schema name to use for the database connection").build();
        DEFAULT_CATALOG_NAME_ARG = builder.argument("defaultCatalogName", String.class)
                .description("The default catalog name to use for the database connection").build();
        DRIVER_ARG = builder.argument("driver", String.class)
                .description("The JDBC driver class").build();
        DRIVER_PROPERTIES_FILE_ARG = builder.argument("driverPropertiesFile", String.class)
                .description("The JDBC driver properties file").build();
        USERNAME_ARG = builder.argument(CommonArgumentNames.USERNAME, String.class)
                .description("Username to use to connect to the database").build();
        PASSWORD_ARG = builder.argument(CommonArgumentNames.PASSWORD, String.class)
                .description("Password to use to connect to the database")
                .setValueObfuscator(ConfigurationValueObfuscator.STANDARD)
                .build();
        DATABASE_ARG = builder.argument("database", Database.class).hidden().build();
        URL_ARG = builder.argument(CommonArgumentNames.URL, String.class).required().supersededBy(DATABASE_ARG)
                .description("The JDBC database connection URL").build();
        DATABASE_ARG.setSupersededBy(URL_ARG);

        SKIP_DATABASE_STEP_ARG = builder.argument("skipDatabaseStep", Boolean.class).hidden().defaultValue(false).build();
    }


    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        // do nothing
    }

    @Override
    public List<Class<?>> providedDependencies() {
        return Collections.singletonList(DbUrlConnectionArgumentsCommandStep.class);
    }
}
