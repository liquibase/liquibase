package liquibase.command.core;

import liquibase.Scope;
import liquibase.changelog.ChangelogRewriter;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.*;
import liquibase.exception.ChangeLogAlreadyRegisteredException;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.CommandLineParsingException;
import liquibase.exception.LiquibaseException;
import liquibase.hub.HubConfiguration;
import liquibase.hub.HubService;
import liquibase.hub.HubServiceFactory;
import liquibase.hub.LiquibaseHubException;
import liquibase.hub.model.HubChangeLog;
import liquibase.hub.model.Project;
import liquibase.ui.UIService;
import liquibase.util.StringUtil;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

public class RegisterChangelogCommandStep extends AbstractHubChangelogCommandStep {

    public static final String[] COMMAND_NAME = {"registerChangelog"};

    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<UUID> HUB_PROJECT_ID_ARG;
    public static final CommandArgumentDefinition<String> HUB_PROJECT_NAME_ARG;

    public static final CommandResultDefinition<String> REGISTERED_CHANGELOG_ID;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class).required()
            .description("The root changelog").build();
        HUB_PROJECT_ID_ARG = builder.argument("hubProjectId", UUID.class).optional()
            .description("Used to identify the specific Project in which to record or extract data at Liquibase Hub. Available in your Liquibase Hub account at https://hub.liquibase.com.").build();
        HUB_PROJECT_NAME_ARG = builder.argument("hubProjectName", String.class).optional()
            .description("The Hub project name").build();

        REGISTERED_CHANGELOG_ID = builder.result("registeredChangeLogId", String.class).build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        //
        // Access the HubService
        // Stop if we do no have a key
        //
        final HubServiceFactory hubServiceFactory = Scope.getCurrentScope().getSingleton(HubServiceFactory.class);
        if (!hubServiceFactory.isOnline()) {
            throw new CommandExecutionException("The command registerChangeLog requires communication with Liquibase Hub, \nwhich is prevented by liquibase.hub.mode='off'. \nPlease set to 'all' or 'meta' and try again.  \nLearn more at https://hub.liquibase.com");
        }

        //
        // Check for existing changeLog file
        //
        String changeLogFile = commandScope.getArgumentValue(CHANGELOG_FILE_ARG);
        UUID hubProjectId = commandScope.getArgumentValue(HUB_PROJECT_ID_ARG);
        String hubProjectName = commandScope.getArgumentValue(HUB_PROJECT_NAME_ARG);

        doRegisterChangelog(changeLogFile, hubProjectId, hubProjectName, resultsBuilder, false);
    }

    public void doRegisterChangelog(String changelogFilepath, UUID hubProjectId, String hubProjectName, CommandResultsBuilder resultsBuilder, boolean skipPromptIfOneProject) throws LiquibaseException, CommandLineParsingException {
        try (PrintWriter output = new PrintWriter(resultsBuilder.getOutputStream())) {

            HubChangeLog hubChangeLog;
            final HubService service = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
            //
            // Check for existing changeLog file using the untouched changelog filepath
            //
            DatabaseChangeLog databaseChangeLog = parseChangeLogFile(changelogFilepath);
            if (databaseChangeLog == null) {
                throw new CommandExecutionException("Cannot parse "+changelogFilepath);
            }

            final String changeLogId = databaseChangeLog.getChangeLogId();
            if (changeLogId != null) {
                hubChangeLog = service.getHubChangeLog(UUID.fromString(changeLogId));
                if (hubChangeLog != null) {
                    throw new CommandExecutionException("Changelog '" + changelogFilepath +
                            "' is already registered with changeLogId '" + changeLogId + "' to project '" +
                            hubChangeLog.getProject().getName() + "' with project ID '" + hubChangeLog.getProject().getId().toString() + "'.\n" +
                            "For more information visit https://docs.liquibase.com.", new ChangeLogAlreadyRegisteredException(hubChangeLog));
                } else {
                    throw new CommandExecutionException("Changelog '" + changelogFilepath +
                            "' is already registered with changeLogId '" + changeLogId + "'.\n" +
                            "For more information visit https://docs.liquibase.com.", new ChangeLogAlreadyRegisteredException());
                }
            }

            //
            // Retrieve the projects
            //
            Project project;
            if (hubProjectId != null) {
                project = service.getProject(hubProjectId);
                if (project == null) {
                    throw new CommandExecutionException("Project Id '" + hubProjectId + "' does not exist or you do not have access to it");
                }
            } else if (hubProjectName != null) {
                if (hubProjectName.length() > 255) {
                    throw new CommandExecutionException("\nThe project name you gave is longer than 255 characters\n\n");
                }
                project = service.createProject(new Project().setName(hubProjectName));
                if (project == null) {
                    throw new CommandExecutionException("Unable to create project '" + hubProjectName + "'.\n" +
                            "Learn more at https://hub.liquibase.com.");
                }
                output.print("\nProject '" + project.getName() + "' created with project ID '" + project.getId() + "'.\n\n");
            } else {
                project = retrieveOrCreateProject(service, changelogFilepath, skipPromptIfOneProject);
                if (project == null) {
                    throw new CommandExecutionException("Your changelog " + changelogFilepath + " was not registered to any Liquibase Hub project. You can still run Liquibase commands, but no data will be saved in your Liquibase Hub account for monitoring or reports.  Learn more at https://hub.liquibase.com.");
                }
            }

            //
            // Go create the Hub Changelog
            //
            String changelogFilename = new File(databaseChangeLog.getFilePath()).getName();
            HubChangeLog newChangeLog = new HubChangeLog();
            newChangeLog.setProject(project);
            newChangeLog.setFileName(changelogFilename);
            newChangeLog.setName(changelogFilename);

            hubChangeLog = service.createChangeLog(newChangeLog);

            //
            // Update the changelog file
            // Add the registered changelog ID to the results so that
            // the caller can use it
            //
            ChangelogRewriter.ChangeLogRewriterResult changeLogRewriterResult = ChangelogRewriter.addChangeLogId(changelogFilepath, hubChangeLog.getId().toString(), databaseChangeLog);

            if (changeLogRewriterResult.success) {
                Scope.getCurrentScope().getLog(RegisterChangelogCommandStep.class).info(changeLogRewriterResult.message);
                output.println("* Changelog file '" + changelogFilepath + "' with changelog ID '" + hubChangeLog.getId().toString() + "' has been " +
                        "registered to Project "+project.getName() );
                resultsBuilder.addResult("statusCode", 0);
                resultsBuilder.addResult(REGISTERED_CHANGELOG_ID.getName(), hubChangeLog.getId().toString());
            }
        }
    }

    private Project retrieveOrCreateProject(HubService service, String changeLogFile, boolean skipPromptIfOneProject) throws CommandLineParsingException, LiquibaseException, LiquibaseHubException {
        final UIService ui = Scope.getCurrentScope().getUI();

        Project project = null;
        List<Project> projects = getProjectsFromHub();
        if (skipPromptIfOneProject && projects.size() == 1) {
            return projects.get(0);
        }
        boolean done = false;
        String input = null;
        while (!done) {
            input = readProjectFromConsole(projects, changeLogFile);
            try {
                if (input.equalsIgnoreCase("C")) {
                    String projectName = readProjectNameFromConsole();
                    if (StringUtil.isEmpty(projectName)) {
                        ui.sendMessage("\nNo project created\n");
                        continue;
                    } else if (projectName.length() > 255) {
                        ui.sendMessage("\nThe project name you entered is longer than 255 characters\n");
                        continue;
                    }
                    project = service.createProject(new Project().setName(projectName));
                    if (project == null) {
                        throw new CommandExecutionException("Unable to create project '" + projectName + "'.\n\n");
                    }
                    ui.sendMessage("\nProject '" + project.getName() + "' created with project ID '" + project.getId() + "'.\n");
                    projects = getProjectsFromHub();
                    done = true;
                    continue;
                } else if (input.equalsIgnoreCase("N")) {
                    throw new CommandExecutionException("Your changelog " + changeLogFile + " was not registered to any Liquibase Hub project. You can still run Liquibase commands, but no data will be saved in your Liquibase Hub account for monitoring or reports.  Learn more at https://hub.liquibase.com.");
                }
                int projectIdx = Integer.parseInt(input);
                if (projectIdx > 0 && projectIdx <= projects.size()) {
                    project = projects.get(projectIdx - 1);
                    if (project != null) {
                        done = true;
                    }
                } else {
                    ui.sendMessage("\nInvalid project '" + projectIdx + "' selected\n");
                }
            } catch (NumberFormatException nfe) {
                ui.sendMessage("\nInvalid selection '" + input + "'\n");
            }
        }
        return project;
    }

    //
    // Retrieve the projects and sort them by create date
    //
    private List<Project> getProjectsFromHub() throws LiquibaseHubException {
        final HubService service = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
        List<Project> projects = service.getProjects();
        projects.sort((o1, o2) -> {
            Date date1 = o1.getCreateDate();
            Date date2 = o2.getCreateDate();
            return date2.compareTo(date1);
        });
        return projects;
    }

    private String readProjectNameFromConsole() throws CommandLineParsingException {
        final UIService ui = Scope.getCurrentScope().getUI();

        String hubUrl = HubConfiguration.LIQUIBASE_HUB_URL.getCurrentValue();
        String input = ui.prompt("Please enter your Project name and press [enter].  This is editable in your Liquibase Hub account at " + hubUrl, null, null, String.class);
        return StringUtil.trimToEmpty(input);
    }

    private String readProjectFromConsole(List<Project> projects, String changeLogFile) throws CommandLineParsingException {
        final UIService ui = Scope.getCurrentScope().getUI();

        StringBuilder prompt = new StringBuilder("Registering a changelog connects Liquibase operations to a Project for monitoring and reporting.\n");
        prompt.append("Register changelog " + changeLogFile + " to an existing Project, or create a new one.\n");

        prompt.append("Please make a selection:\n");

        prompt.append("[c] Create new Project\n");
        String projFormat = "[%d]";
        if (projects.size() >= 10 && projects.size() < 100) {
            projFormat = "[%2d]";
        } else if (projects.size() >= 100 && projects.size() < 1000) {
            projFormat = "[%3d]";
        } else if (projects.size() >= 1000 && projects.size() < 10000) {
            projFormat = "[%4d]";
        }
        int maxLen = 40;
        for (Project project : projects) {
            if (project.getName() != null && project.getName().length() > maxLen) {
                maxLen = project.getName().length();
            }
        }
        for (int i = 0; i < projects.size(); i++) {
            Project project = projects.get(i);
            prompt.append(String.format(projFormat + " %-" + maxLen + "s (Project ID:%s) %s\n", i + 1, project.getName(), projects.get(i).getId(), projects.get(i).getCreateDate()));
        }
        prompt.append("[N] to not register this changelog right now.\n" +
                "You can still run Liquibase commands, but no data will be saved in your Liquibase Hub account for monitoring or reports.\n" +
                " Learn more at https://hub.liquibase.com.\n?");

        return StringUtil.trimToEmpty(ui.prompt(prompt.toString(), "N", null, String.class));
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Register the changelog with a Liquibase Hub project");
    }
}
