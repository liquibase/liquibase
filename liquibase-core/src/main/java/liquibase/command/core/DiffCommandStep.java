package liquibase.command.core;

import liquibase.command.*;
import liquibase.integration.commandline.Main;

public class DiffCommandStep extends AbstractCliWrapperCommandStep {
    public static final CommandArgumentDefinition<String> REFERENCE_USERNAME_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_URL_ARG;
    public static final CommandArgumentDefinition<String> USERNAME_ARG;
    public static final CommandArgumentDefinition<String> PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> FORMAT_ARG;
    public static final CommandArgumentDefinition<String> OUTPUT_FILE_ARG;
    public static final CommandArgumentDefinition<String> LIQUIBASE_PRO_LICENSE_KEY_ARG;

    static {
        CommandStepBuilder builder = new CommandStepBuilder(DiffCommandStep.class);
        REFERENCE_URL_ARG = builder.argument("referenceUrl", String.class).required()
            .description("The JDBC reference database connection URL").build();
        REFERENCE_USERNAME_ARG = builder.argument("username", String.class)
            .description("The reference database username").build();
        REFERENCE_PASSWORD_ARG = builder.argument("password", String.class)
            .description("The reference database password").build();
        URL_ARG = builder.argument("url", String.class).required()
            .description("The JDBC target database connection URL").build();
        USERNAME_ARG = builder.argument("username", String.class)
            .description("The target database username").build();
        PASSWORD_ARG = builder.argument("password", String.class)
            .description("The target database password").build();
        FORMAT_ARG = builder.argument("format", String.class)
            .description("Option to create JSON output").build();
        OUTPUT_FILE_ARG = builder.argument("outputFile", String.class)
            .description("File for writing the diff report").build();
        LIQUIBASE_PRO_LICENSE_KEY_ARG = builder.argument("liquibaseProLicenseKey", String.class)
            .description("Your Liquibase Pro license key").build();
    }

    @Override
    public String[] getName() {
        return new String[] {"diff"};
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        String[] args = createParametersFromArgs(createArgs(commandScope), "--format");
        int statusCode = Main.run(args);
        if (statusCode == 0) {
            resultsBuilder.addResult("statusMessage", "Successfully executed formattedDiff");
        }
        else {
            resultsBuilder.addResult("statusMessage", "Unsuccessfully executed formattedDiff");
        }
        resultsBuilder.addResult("statusCode", statusCode);
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Compare two databases");
        commandDefinition.setLongDescription("Compare two databases");
    }
}
