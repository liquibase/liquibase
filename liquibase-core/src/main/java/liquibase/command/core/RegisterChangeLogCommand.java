package liquibase.command.core;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.changelog.ChangelogRewriter;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.*;
import liquibase.exception.CommandArgumentValidationException;
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
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.io.PrintStream;
import java.util.*;

public class RegisterChangeLogCommand extends AbstractCommand {

    public static final CommandArgumentDefinition<HubChangeLog> HUB_CHANGELOG_ARG;
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<DatabaseChangeLog> CHANGELOG_ARG;
    public static final CommandArgumentDefinition<UUID> HUB_PROJECT_ID_ARG;
    public static final CommandArgumentDefinition<String> HUB_PROJECT_NAME_ARG;

    static {
        CommandArgumentDefinition.Builder builder = new CommandArgumentDefinition.Builder(RegisterChangeLogCommand.class);
        HUB_CHANGELOG_ARG = builder.define("hubChangeLog", HubChangeLog.class).required().build();
        CHANGELOG_FILE_ARG = builder.define("changeLogFile", String.class).required().build();
        CHANGELOG_ARG = builder.define("changeLog", DatabaseChangeLog.class).required().build();
        HUB_PROJECT_ID_ARG = builder.define("hubProjectId", UUID.class).optional().build();
        HUB_PROJECT_NAME_ARG = builder.define("hubProjectName", String.class).optional().build();
    }

    @Override
    public String[] getName() {
        return new String[]{"registerChangeLog"};
    }

    @Override
    public void run(CommandScope commandScope) throws Exception {
        final UIService ui = Scope.getCurrentScope().getUI();

        //
        // Access the HubService
        // Stop if we do no have a key
        //
        final HubServiceFactory hubServiceFactory = Scope.getCurrentScope().getSingleton(HubServiceFactory.class);
        if (!hubServiceFactory.isOnline()) {
            throw new CommandExecutionException("The command registerChangeLog requires access to Liquibase Hub: " + hubServiceFactory.getOfflineReason() + ".  Learn more at https://hub.liquibase.com");
        }

        //
        // Check for existing changeLog file
        //
        final HubService service = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
        HubChangeLog hubChangeLog;
        String changeLogFile = CHANGELOG_FILE_ARG.getValue(commandScope);
        UUID hubProjectId = HUB_PROJECT_ID_ARG.getValue(commandScope);
        String hubProjectName = HUB_PROJECT_NAME_ARG.getValue(commandScope);

        //
        // CHeck for existing changeLog file
        //
        DatabaseChangeLog databaseChangeLog = CHANGELOG_ARG.getValue(commandScope);
        final String changeLogId = (databaseChangeLog != null ? databaseChangeLog.getChangeLogId() : null);
        if (changeLogId != null) {
            hubChangeLog = service.getHubChangeLog(UUID.fromString(changeLogId));
            if (hubChangeLog != null) {
                throw new CommandExecutionException("Changelog '" + changeLogFile +
                    "' is already registered with changeLogId '" + changeLogId + "' to project '" +
                    hubChangeLog.getProject().getName() + "' with project ID '" + hubChangeLog.getProject().getId().toString() + "'.\n" +
                        "For more information visit https://docs.liquibase.com.");
            } else {
                throw new CommandExecutionException("Changelog '" + changeLogFile +
                    "' is already registered with changeLogId '" + changeLogId + "'.\n" +
                        "For more information visit https://docs.liquibase.com.");
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
            commandScope.getOutput().print("\nProject '" + project.getName() + "' created with project ID '" + project.getId() + "'.\n\n");
        } else {
            project = retrieveOrCreateProject(service, commandScope);
            if (project == null) {
                throw new CommandExecutionException("Your changelog " + changeLogFile + " was not registered to any Liquibase Hub project. You can still run Liquibase commands, but no data will be saved in your Liquibase Hub account for monitoring or reports.  Learn more at https://hub.liquibase.com.");
            }
        }

        //
        // Go create the Hub Changelog
        //
        HubChangeLog newChangeLog = new HubChangeLog();
        newChangeLog.setProject(project);
        newChangeLog.setFileName(databaseChangeLog.getFilePath());
        newChangeLog.setName(databaseChangeLog.getFilePath());

        hubChangeLog = service.createChangeLog(newChangeLog);

        ChangelogRewriter.ChangeLogRewriterResult changeLogRewriterResult =
            ChangelogRewriter.addChangeLogId(changeLogFile, hubChangeLog.getId().toString(), databaseChangeLog);
        Scope.getCurrentScope().getLog(RegisterChangeLogCommand.class).info(changeLogRewriterResult.message);
        commandScope.getOutput().println("* Changelog file '" + changeLogFile + "' with changelog ID '" + hubChangeLog.getId().toString() + "' has been registered");
    }

    private Project retrieveOrCreateProject(HubService service, CommandScope commandScope) throws CommandLineParsingException, LiquibaseException, LiquibaseHubException {
        final UIService ui = Scope.getCurrentScope().getUI();
        String changeLogFile = CHANGELOG_FILE_ARG.getValue(commandScope);

        Project project = null;
        List<Project> projects = getProjectsFromHub();
        boolean done = false;
        String input = null;
        while (!done) {
                input = readProjectFromConsole(projects, commandScope);
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
        Collections.sort(projects, new Comparator<Project>() {
            @Override
            public int compare(Project o1, Project o2) {
                Date date1 = o1.getCreateDate();
                Date date2 = o2.getCreateDate();
                return date2.compareTo(date1);
            }
        });
        return projects;
    }

    private String readProjectNameFromConsole() throws CommandLineParsingException {
        final UIService ui = Scope.getCurrentScope().getUI();

        String hubUrl = HubConfiguration.LIQUIBASE_HUB_URL.getCurrentValue();
        String input = ui.prompt("Please enter your Project name and press [enter].  This is editable in your Liquibase Hub account at " + hubUrl, null, null, String.class);
        return StringUtil.trimToEmpty(input);
    }

    private String readProjectFromConsole(List<Project> projects, CommandScope commandScope) throws CommandLineParsingException {
        final UIService ui = Scope.getCurrentScope().getUI();

        StringBuilder prompt = new StringBuilder("Registering a changelog connects Liquibase operations to a Project for monitoring and reporting.\n");
        prompt.append("Register changelog " + CHANGELOG_FILE_ARG.getValue(commandScope) + " to an existing Project, or create a new one.\n");

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
}
