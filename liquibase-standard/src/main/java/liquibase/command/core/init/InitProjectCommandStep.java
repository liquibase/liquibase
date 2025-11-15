package liquibase.command.core.init;


import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.command.*;
import liquibase.command.copy.ProjectCopier;
import liquibase.command.copy.ProjectCopierFactory;
import liquibase.configuration.ConfigurationValueObfuscator;
import liquibase.configuration.ConfigurationValueProvider;
import liquibase.configuration.ConfiguredValue;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.configuration.core.DefaultsFileValueProvider;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.license.LicenseService;
import liquibase.license.LicenseServiceFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static liquibase.integration.commandline.LiquibaseCommandLineConfiguration.DEFAULTS_FILE;

public class InitProjectCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"init", "project"};
    private static final String[] ALIAS = {"init", "project-new"};

    public static final CommandArgumentDefinition<String> INIT_PROJECT_DIR_ARG;
    public static final CommandArgumentDefinition<String> INIT_FORMAT_ARG;
    public static final CommandArgumentDefinition<String> INIT_CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> INIT_DEFAULTS_FILE_ARG;
    public static final CommandArgumentDefinition<Boolean> INIT_PROJECT_GUIDE_ARG;
    public static final CommandArgumentDefinition<Boolean> INIT_PROJECT_RECURSIVE_ARG;
    public static final CommandArgumentDefinition<Boolean> INIT_PROJECT_KEEP_TEMP_FILES_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> USERNAME_ARG;
    public static final CommandArgumentDefinition<String> PASSWORD_ARG;
    public static final CommandArgumentDefinition<Boolean> COPY_EXAMPLE_FLOW_FILES;
    public static final CommandArgumentDefinition<Boolean> COPY_EXAMPLE_CHECKS_PACKAGE_FILE;

    public enum FileTypeEnum {
        sql, xml, json, yml, yaml
    }

    public static final String SHOULD_BACKUP_DEFAULTS_FILE_ARG = "shouldBackupDefaultsFile";

    /**
     * The key that will be used in the command results map to indicate whether
     * the user selected values which indicate that they are using H2 as their database of choice.
     */
    public static final String USED_H2_KEY = "usedH2";

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        INIT_PROJECT_DIR_ARG = builder.argument("projectDir", String.class)
                .description("Relative or fully qualified path to the directory where the project files will be created")
                .defaultValue((String) InitProjectPromptingEnum.PROJECT_DIR.defaultValue)
                .build();
        INIT_FORMAT_ARG = builder.argument("format", String.class)
                .description("Format of the project changelog sql|xml|json|yaml|yml")
                .defaultValue(InitProjectPromptingEnum.FILETYPE.defaultValue.toString())
                .build();
        INIT_DEFAULTS_FILE_ARG = builder.argument("projectDefaultsFile", String.class)
                .description(DEFAULTS_FILE.getDescription())
                .defaultValue(DEFAULTS_FILE.getDefaultValue())
                .build();
        INIT_PROJECT_GUIDE_ARG = builder.argument("projectGuide", Boolean.class)
                .description("Allow interactive prompts for init project")
                .defaultValue(true)
                .build();
        INIT_PROJECT_RECURSIVE_ARG = builder.argument("recursive", Boolean.class)
                .description("For remote project locations, recursive copy all project files to the remote location")
                .defaultValue(false)
                .hidden()
                .build();
        INIT_PROJECT_KEEP_TEMP_FILES_ARG = builder.argument("keepTempFiles", Boolean.class)
                .description("For remote project locations, do not delete temporary project files")
                .defaultValue(false)
                .build();
        INIT_CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class)
                .description("Relative or fully qualified path to the changelog file")
                .defaultValue((String) InitProjectPromptingEnum.SAMPLE_CHANGELOG_NAME.defaultValue)
                .build();
        URL_ARG = builder.argument(CommonArgumentNames.URL, String.class)
                .description("The JDBC database connection URL")
                .defaultValue((String) InitProjectPromptingEnum.JDBC_URL.defaultValue)
                .build();
        USERNAME_ARG = builder.argument(CommonArgumentNames.USERNAME, String.class)
                .description("Username to use to connect to the database")
                .defaultValue((String) InitProjectPromptingEnum.USERNAME.defaultValue)
                .build();
        PASSWORD_ARG = builder.argument(CommonArgumentNames.PASSWORD, String.class)
                .description("Password to use to connect to the database")
                .defaultValue((String) InitProjectPromptingEnum.PASSWORD.defaultValue)
                .setValueObfuscator(ConfigurationValueObfuscator.STANDARD)
                .build();
        COPY_EXAMPLE_FLOW_FILES = builder.argument("copyExampleFlowFiles", Boolean.class)
                .defaultValue(true)
                .hidden()
                .build();
        COPY_EXAMPLE_CHECKS_PACKAGE_FILE = builder.argument("copyExampleChecksPackageFile", Boolean.class)
                .defaultValue(true)
                .hidden()
                .build();
    }

    private void doPrompt(ConfiguredValue<String> configuredValue, CommandArgumentDefinition<String> commandArgumentDefinition, CommandScope commandScope, InitProjectPromptingEnum enumToPrompt, InteractivePromptingValueProvider interactivePromptingValueProvider) throws Exception {
        doPrompt(configuredValue, commandArgumentDefinition, commandScope, enumToPrompt, interactivePromptingValueProvider, null);
    }

    private void doPrompt(ConfiguredValue<String> configuredValue, CommandArgumentDefinition<String> commandArgumentDefinition, CommandScope commandScope, InitProjectPromptingEnum enumToPrompt, InteractivePromptingValueProvider interactivePromptingValueProvider, Object currentValue) throws Exception {
        if (configuredValue.wasDefaultValueUsed()) {
            Object prompt = enumToPrompt.interactiveCommandLineValueGetter.prompt(enumToPrompt, currentValue);
            // only add the value if it is not equal to the default value
            if (!configuredValue.getValue().equals(prompt)) {
                interactivePromptingValueProvider.addValue(commandScope.getCompleteConfigPrefix() + "." + commandArgumentDefinition.getName(), prompt);
            }
        }
    }

    @Override
    public final void run(CommandResultsBuilder resultsBuilder) throws Exception {
        //
        // Use a Scope.child() call to let us override the headless
        // argument value in the case where projectGuide=on
        //
        CommandScope commandScope = resultsBuilder.getCommandScope();
        ConfiguredValue<Boolean> projectGuideConfig = commandScope.getConfiguredValue(INIT_PROJECT_GUIDE_ARG);
        Map<String, Object> scopeValues = new HashMap<>();
        if (!projectGuideConfig.wasDefaultValueUsed()) {
            Boolean projectGuide = projectGuideConfig.getValue();
            if (Boolean.TRUE.equals(projectGuide)) {
                scopeValues.put(GlobalConfiguration.HEADLESS.getKey(), false);
            }
        }
        try {
            LiquibaseConfiguration lbConf = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);
            Optional<ConfigurationValueProvider> providerOpt = lbConf.getProviders().stream().filter(vp -> vp instanceof DefaultsFileValueProvider).findFirst();
            providerOpt.ifPresent(lbConf::unregisterProvider);

            Scope.child(scopeValues, () -> {
                internalRun(resultsBuilder);
            });

            providerOpt.ifPresent(lbConf::registerProvider);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void internalRun(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        ConfiguredValue<String> defaultsFileConfig = commandScope.getConfiguredValue(INIT_DEFAULTS_FILE_ARG);
        ConfiguredValue<String> formatConfig = commandScope.getConfiguredValue(INIT_FORMAT_ARG);
        ConfiguredValue<String> changelogFileConfig = commandScope.getConfiguredValue(INIT_CHANGELOG_FILE_ARG);
        ConfiguredValue<String> projectDirConfig = commandScope.getConfiguredValue(INIT_PROJECT_DIR_ARG);
        ConfiguredValue<String> urlConfig = commandScope.getConfiguredValue(URL_ARG);
        ConfiguredValue<String> usernameConfig = commandScope.getConfiguredValue(USERNAME_ARG);
        ConfiguredValue<String> passwordConfig = commandScope.getConfiguredValue(PASSWORD_ARG);
        Boolean projectGuide = commandScope.getConfiguredValue(INIT_PROJECT_GUIDE_ARG).getValue();
        boolean skippedChangelogFileCreation = false;
        boolean skippedDefaultsFileCreation = false;

        //
        // Determine the ProjectCopier instance and if it is null
        // then quit
        //
        String inputProjectDir = projectDirConfig.getValue();
        String projectDir = projectDirConfig.getValue();
        ProjectCopier projectCopier = Scope.getCurrentScope().getSingleton(ProjectCopierFactory.class).getProjectCopier(projectDir);
        checkForS3Jar(projectCopier);
        if (shouldPrompt(defaultsFileConfig, formatConfig, changelogFileConfig, projectDirConfig, urlConfig, usernameConfig, passwordConfig, projectGuide)) {
            String response = Scope.getCurrentScope().getUI().prompt("Setup new liquibase.properties, flowfile, and sample changelog? Enter (Y)es with defaults, yes with (C)ustomization, or (N)o.", "Y", (input, type) -> {
                List<String> permissibleEntries = Arrays.asList("y", "yes", "n", "no", "c", "custom", "customize");
                if (input == null || permissibleEntries.stream().noneMatch(pe -> pe.equalsIgnoreCase(input))) {
                    throw new IllegalArgumentException();
                } else {
                    return input;
                }
            }, String.class);
            if (response.toLowerCase().startsWith("c")) {
                // Register the value provider so that we actually have a place to store the entries
                InteractivePromptingValueProvider interactivePromptingValueProvider = new InteractivePromptingValueProvider();
                Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).registerProvider(interactivePromptingValueProvider);

                doPrompt(projectDirConfig, INIT_PROJECT_DIR_ARG, commandScope, InitProjectPromptingEnum.PROJECT_DIR, interactivePromptingValueProvider);
                projectDirConfig = commandScope.getConfiguredValue(INIT_PROJECT_DIR_ARG);
                projectDir = projectDirConfig.getValue();
                projectCopier = Scope.getCurrentScope().getSingleton(ProjectCopierFactory.class).getProjectCopier(projectDir);

                String latestChangelogFilename = findLatestChangelogFilename(projectDirConfig);
                doPrompt(changelogFileConfig, INIT_CHANGELOG_FILE_ARG, commandScope, InitProjectPromptingEnum.SAMPLE_CHANGELOG_NAME, interactivePromptingValueProvider, latestChangelogFilename);
                changelogFileConfig = commandScope.getConfiguredValue(INIT_CHANGELOG_FILE_ARG);
                skippedChangelogFileCreation = isSkipped(changelogFileConfig);
                if (skippedChangelogFileCreation) {
                    String message = "No changelog file will be created. Specify a valid changelog file on the CLI, via Environment variable, or in your defaults file. ";
                    Scope.getCurrentScope().getUI().sendMessage(addGettingStarted(message));
                } else {
                    if (InitProjectUtil.determineFormatType(changelogFileConfig.getValue(), null) == null) {
                        doPrompt(formatConfig, INIT_FORMAT_ARG, commandScope, InitProjectPromptingEnum.FILETYPE, interactivePromptingValueProvider);
                    }
                }
                doPrompt(defaultsFileConfig, INIT_DEFAULTS_FILE_ARG, commandScope, InitProjectPromptingEnum.DEFAULTS_FILENAME, interactivePromptingValueProvider);
                defaultsFileConfig = commandScope.getConfiguredValue(INIT_DEFAULTS_FILE_ARG);
                skippedDefaultsFileCreation = isSkipped(defaultsFileConfig);
                if (skippedDefaultsFileCreation) {
                    String message = "No defaults file will be created. Specify a valid defaults file on the CLI, via Environment variable, or pass all required properties on the CLI, via Environment variables. ";
                    Scope.getCurrentScope().getUI().sendMessage(addGettingStarted(message));
                } else {
                    doPrompt(urlConfig, URL_ARG, commandScope, InitProjectPromptingEnum.JDBC_URL, interactivePromptingValueProvider);
                    doPrompt(usernameConfig, USERNAME_ARG, commandScope, InitProjectPromptingEnum.USERNAME, interactivePromptingValueProvider);
                    doPrompt(passwordConfig, PASSWORD_ARG, commandScope, InitProjectPromptingEnum.PASSWORD, interactivePromptingValueProvider);
                }

                // Reload the values here after possibly doing prompting.
                formatConfig = commandScope.getConfiguredValue(INIT_FORMAT_ARG);
                urlConfig = commandScope.getConfiguredValue(URL_ARG);
                usernameConfig = commandScope.getConfiguredValue(USERNAME_ARG);
                passwordConfig = commandScope.getConfiguredValue(PASSWORD_ARG);

                if (!skippedChangelogFileCreation || !skippedDefaultsFileCreation) {
                    String message = String.format("Setting up new Liquibase project in '%s'...", fullPathToProjectDir(projectCopier, projectDirConfig));
                    Scope.getCurrentScope().getLog(getClass()).info(message);
                    Scope.getCurrentScope().getUI().sendMessage(message);
                }
            } else if (response.toLowerCase().startsWith("y")) {
                String message = String.format("Setting up new Liquibase project in '%s'...", fullPathToProjectDir(projectCopier, projectDirConfig));
                Scope.getCurrentScope().getLog(getClass()).info(message);
                Scope.getCurrentScope().getUI().sendMessage(message);
            } else if (response.toLowerCase().startsWith("n")) {
                String message = "No files created. Set 'liquibase.command.init.project.projectGuide=off' in your defaults file or set LIQUIBASE_COMMAND_INIT_PROJECT_PROJECT_GUIDE=off as an environment variable to not be asked again. Getting Started and project setup available anytime, run \"liquibase init project --help\" for information.";
                Scope.getCurrentScope().getLog(getClass()).info(message);
                Scope.getCurrentScope().getUI().sendMessage(message);
                return;
            }
        }

        //
        // Make sure the projectDir argument is a directory
        //
        File projectDirFile = projectCopier.createWorkingStorage(projectDir, commandScope.getConfiguredValue(INIT_PROJECT_KEEP_TEMP_FILES_ARG).getValue());
        InitProjectUtil.validateProjectDirectory(projectDirFile);

        //
        // Create the project directory if necessary
        //
        InitProjectUtil.createProjectDirectory(projectDirFile);

        if (!skippedChangelogFileCreation || !skippedDefaultsFileCreation) {

            InitProjectUtil.FileCreationResultEnum changelogFileCreationResult = InitProjectUtil.FileCreationResultEnum.skipped_changelog_step;
            String format = null;

            if (!skippedChangelogFileCreation) {
                //
                // Check the changelog file path to make sure it is a simple name
                //
                String changelogFilePath = changelogFileConfig.getValue();
                InitProjectUtil.validateChangelogFilePath(changelogFilePath);

                //
                // Determine the format of the changelog file
                //
                if (formatConfig.wasDefaultValueUsed()) {
                    format = InitProjectUtil.determineFormatType(changelogFilePath, formatConfig.getValue());
                    if (format == null) {
                        String message = "Unable to determine format for the changelog file '" + changelogFilePath + "'";
                        throw new CommandExecutionException(message);
                    }
                }
                if (format == null) {
                    format = commandScope.getConfiguredValue(INIT_FORMAT_ARG).getValue();
                }
                if (!changelogFilePath.contains("." + format)) {
                    changelogFilePath += "." + format;
                }

                //
                // Copy the appropriate changelog file if necessary
                //
                changelogFileCreationResult = InitProjectUtil.copyExampleChangelog(format, projectDirFile, changelogFilePath, changelogFileConfig);

                //
                // Do not use Paths.get on the projectDir path to avoid illegal path exceptions for remote protocols
                //
                if (projectDir.endsWith("/")) {
                    resultsBuilder.addResult(INIT_CHANGELOG_FILE_ARG.getName(), projectDir + changelogFilePath);
                } else {
                    resultsBuilder.addResult(INIT_CHANGELOG_FILE_ARG.getName(), projectDir + "/" + changelogFilePath);
                }
            }

            boolean usedH2 = createDefaultsFile(resultsBuilder, defaultsFileConfig, changelogFileConfig, urlConfig, usernameConfig, passwordConfig, skippedDefaultsFileCreation, projectDir, projectCopier, projectDirFile, changelogFileCreationResult, format);

            final LicenseService licenseService = Scope.getCurrentScope().getSingleton(LicenseServiceFactory.class).getLicenseService();
            if (licenseService != null) {
                if (commandScope.getArgumentValue(COPY_EXAMPLE_FLOW_FILES)) {
                    //Add example flow files to directory
                    InitProjectUtil.copyExampleFlowFiles(format, projectDirFile);
                }
                if (commandScope.getArgumentValue(COPY_EXAMPLE_CHECKS_PACKAGE_FILE)) {
                    InitProjectUtil.copyChecksPackageFile(format, projectDirFile);
                }
            }
            Scope.getCurrentScope().getUI().sendMessage("");

            resultsBuilder.addResult(USED_H2_KEY, usedH2);

            outputEndInstructions(projectDirConfig.wasDefaultValueUsed(), defaultsFileConfig, projectDir, usedH2);
        } else {
            String message = addGettingStarted(" ");
            Scope.getCurrentScope().getLog(InitProjectCommandStep.class).info(message);
            Scope.getCurrentScope().getUI().sendMessage(message);
        }

        runInitCopyCommand(inputProjectDir, projectDir, projectCopier, projectDirFile);
        resultsBuilder.addResult("statusCode", 0);
    }

    private boolean shouldPrompt(ConfiguredValue<String> defaultsFileConfig, ConfiguredValue<String> formatConfig, ConfiguredValue<String> changelogFileConfig, ConfiguredValue<String> projectDirConfig, ConfiguredValue<String> urlConfig, ConfiguredValue<String> usernameConfig, ConfiguredValue<String> passwordConfig, Boolean projectGuide) {
        return Boolean.TRUE.equals(projectGuide) && (projectDirConfig.wasDefaultValueUsed()
                || changelogFileConfig.wasDefaultValueUsed()
                || formatConfig.wasDefaultValueUsed()
                || defaultsFileConfig.wasDefaultValueUsed()
                || urlConfig.wasDefaultValueUsed()
                || usernameConfig.wasDefaultValueUsed()
                || passwordConfig.wasDefaultValueUsed());
    }

    private void checkForS3Jar(ProjectCopier projectCopier) {
        if (projectCopier == null) {
            throw new UnexpectedLiquibaseException("The AWS S3 extension JAR is not available on the classpath");
        }
    }

    private void runInitCopyCommand(String inputProjectDir, String projectDir, ProjectCopier projectCopier, File projectDirFile) throws CommandExecutionException {
        if (projectCopier.isRemote()) {
            CommandScope copyCommand = new CommandScope("init", "copy");
            copyCommand.addArgumentValue(InitCopyCommandStep.INIT_COPY_SOURCE_DIR_ARG, projectDirFile.getAbsolutePath());
            if (!projectDir.equals(inputProjectDir)) {
                copyCommand.addArgumentValue(InitCopyCommandStep.INIT_COPY_TARGET_DIR_ARG, projectDir);
            } else {
                copyCommand.addArgumentValue(InitCopyCommandStep.INIT_COPY_TARGET_DIR_ARG, inputProjectDir);
            }
            copyCommand.addArgumentValue(InitCopyCommandStep.INIT_COPY_PROJECT_COPIER_ARG, projectCopier);
            copyCommand.execute();
        }
    }

    private boolean createDefaultsFile(CommandResultsBuilder resultsBuilder,
                                       ConfiguredValue<String> defaultsFileConfig,
                                       ConfiguredValue<String> changelogFileConfig,
                                       ConfiguredValue<String> urlConfig,
                                       ConfiguredValue<String> usernameConfig,
                                       ConfiguredValue<String> passwordConfig,
                                       boolean skippedDefaultsFileCreation,
                                       String projectDir,
                                       ProjectCopier projectCopier,
                                       File projectDirFile,
                                       InitProjectUtil.FileCreationResultEnum changelogFileCreationResult,
                                       String format) throws CommandExecutionException, IOException {
        if (!skippedDefaultsFileCreation) {
            //
            // Copy the appropriate properties file if necessary
            //
            boolean newDefaultsFile = false;
            File defaultsFile = new File(projectDirFile, defaultsFileConfig.getValue());
            if (!defaultsFile.exists()) {
                if (changelogFileCreationResult == InitProjectUtil.FileCreationResultEnum.skipped_changelog_step || changelogFileCreationResult == InitProjectUtil.FileCreationResultEnum.already_existed) {
                    Scope.getCurrentScope().getUI().sendMessage(System.lineSeparator());
                }
                if (projectCopier.isRemote()) {
                    defaultsFile = InitProjectUtil.copyExampleProperties(format, projectDirFile.getAbsolutePath(), defaultsFileConfig.getValue());
                } else {
                    defaultsFile = InitProjectUtil.copyExampleProperties(format, projectDir, defaultsFileConfig.getValue());
                }
                newDefaultsFile = true;
            }
            Boolean shouldBackupDefaultsFile = Scope.getCurrentScope().get(SHOULD_BACKUP_DEFAULTS_FILE_ARG, Boolean.class);
            InitProjectUtil.updateDefaultsFile(defaultsFile, newDefaultsFile, format, changelogFileConfig, urlConfig, usernameConfig, passwordConfig, changelogFileCreationResult, shouldBackupDefaultsFile, resultsBuilder);
            resultsBuilder.addResult(INIT_DEFAULTS_FILE_ARG.getName(), defaultsFile);
            return InitProjectUtil.wasH2Used(urlConfig, usernameConfig, passwordConfig);
        }
        return false;
    }

    public static void outputEndInstructions(boolean wasDefaultProjectDirValueUsed, ConfiguredValue<String> defaultsFileConfig, String projectDir, boolean usedH2) {
        String message = "To use the new project files";
        if (!wasDefaultProjectDirValueUsed) {
            message += String.format(", please cd into '%s',", projectDir);
        }
        message += " make sure your database is active and accessible";
        String updateCommand = "liquibase update" + (defaultsFileConfig.wasDefaultValueUsed() || defaultsFileConfig.getValue().equals(INIT_DEFAULTS_FILE_ARG.getDefaultValue()) ? "" : " --defaults-file=" + defaultsFileConfig.getValue());
        if (usedH2) {
            message += " by opening a new terminal window to run \"liquibase init start-h2\", and then return to this terminal window to run \"" + updateCommand + "\" command.";
        } else {
            message += " and run \"" + updateCommand + "\".";
        }
        message = addGettingStarted(message);
        Scope.getCurrentScope().getLog(InitProjectCommandStep.class).info(message);
        Scope.getCurrentScope().getUI().sendMessage(message);
    }

    private static String fullPathToProjectDir(ProjectCopier projectCopier, ConfiguredValue<String> projectDirConfig) {
        if (projectCopier.isRemote()) {
            return projectDirConfig.getValue();
        }
        return new File(projectDirConfig.getValue()).getAbsolutePath();
    }

    private static String addGettingStarted(String message) {
        message += "\nFor more details, visit the Getting Started Guide at https://docs.liquibase.com/start/home.html";
        return message;
    }

    /**
     * Determine if the user skipped a particular prompt by entering the "s" value.
     *
     * @return true if skipped
     */
    private boolean isSkipped(ConfiguredValue<String> promptedValue) {
        return promptedValue.getValue().equalsIgnoreCase("s");
    }

    private String findLatestChangelogFilename(ConfiguredValue<String> projectDirConfig) {
        try {
            File[] changeLogsInProjectDir = InitProjectUtil.findChangeLogsInProjectDir(new File(projectDirConfig.getValue()));
            if (changeLogsInProjectDir != null && changeLogsInProjectDir.length > 0) {
                // reverse sort so that the newest file is first in the array
                Arrays.sort(changeLogsInProjectDir, (c1, c2) -> Long.compare(c2.lastModified(), c1.lastModified()));
                return changeLogsInProjectDir[0].getName();
            }
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).fine("Failed to find an existing changelog file in the specified project directory, using default instead", e);
        }
        return null;
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        super.adjustCommandDefinition(commandDefinition);
        commandDefinition.setShortDescription(
                "Creates the directory and files needed to run Liquibase commands. Run without any flags on the CLI, or set via Environment variable, etc. will launch an interactive guide to walk users through setting up the necessary project's default and changelog files. This guide can be turned off by setting the 'liquibase.command.init.project.projectGuide=off'");
        commandDefinition.setGroupShortDescription(new String[]{"init"}, "Init commands");
        commandDefinition.addAlias(ALIAS);
    }
}
