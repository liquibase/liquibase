package liquibase.command.core.init;

import liquibase.Scope;
import liquibase.command.*;
import liquibase.command.copy.ProjectCopier;
import liquibase.command.copy.ProjectCopierFactory;
import liquibase.command.core.helpers.AbstractHelperCommandStep;
import liquibase.configuration.ConfiguredValue;
import liquibase.exception.UnexpectedLiquibaseException;

public class InitCopyCommandStep extends AbstractHelperCommandStep {

    public static final String[] COMMAND_NAME = {"init", "copy"};
    private static final String[] ALIAS = {"init", "project-clone"};

    public static final CommandArgumentDefinition<String> INIT_COPY_SOURCE_DIR_ARG;
    public static final CommandArgumentDefinition<String> INIT_COPY_TARGET_DIR_ARG;
    public static final CommandArgumentDefinition<Boolean> INIT_COPY_RECURSIVE_ARG;
    public static final CommandArgumentDefinition<ProjectCopier> INIT_COPY_PROJECT_COPIER_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        INIT_COPY_SOURCE_DIR_ARG = builder.argument("source", String.class)
                .description("Source directory where the project files will be copied from")
                .defaultValue(".")
                .build();
        INIT_COPY_TARGET_DIR_ARG = builder.argument("target", String.class)
                .description("Path to the directory where the project files will be created")
                .build();
        INIT_COPY_RECURSIVE_ARG = builder.argument("recursive", Boolean.class)
                .description("Recursively copy files from the source directory")
                .defaultValue(false)
                .build();
        INIT_COPY_PROJECT_COPIER_ARG = builder.argument("projectCopier", ProjectCopier.class)
                .description("ProjectCopier object that can be passed in")
                .hidden()
                .build();
    }

    @Override
    public final void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        ConfiguredValue<String> sourceDirConfig = commandScope.getConfiguredValue(INIT_COPY_SOURCE_DIR_ARG);
        ConfiguredValue<String> targetDirConfig = commandScope.getConfiguredValue(INIT_COPY_TARGET_DIR_ARG);
        ConfiguredValue<Boolean> recursiveConfig = commandScope.getConfiguredValue(INIT_COPY_RECURSIVE_ARG);
        String sourceDir = sourceDirConfig.getValue();
        String targetDir = targetDirConfig.getValue();
        Boolean recursive = recursiveConfig.getValue();
        ProjectCopier copier = determineProjectCopier(commandScope, targetDir);
        copier.copy(sourceDir, targetDir, recursive);

        resultsBuilder.addResult("statusCode", 0);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        super.adjustCommandDefinition(commandDefinition);
        commandDefinition.setShortDescription(
            "Copy project files from the source directory to the target directory.");
        commandDefinition.setGroupShortDescription(new String[]{"init"}, "Init commands");
        commandDefinition.addAlias(ALIAS);
    }

    //
    // Use the internal ProjectCopier if available otherwise retrieve a new one
    //
    public static ProjectCopier determineProjectCopier(CommandScope commandScope, String targetDir) {
        ProjectCopier copier = commandScope.getConfiguredValue(INIT_COPY_PROJECT_COPIER_ARG).getValue();
        if (copier != null) {
            return copier;
        }
        ProjectCopierFactory factory = Scope.getCurrentScope().getSingleton(ProjectCopierFactory.class);
        copier = factory.getProjectCopier(targetDir);
        if (copier == null) {
            throw new UnexpectedLiquibaseException("Unable to locate a ProjectCopier instance on the classpath for '" + targetDir + "'");
        }
        return copier;
    }

    @Override
    public boolean isInternal() {
        return false;
    }
}
