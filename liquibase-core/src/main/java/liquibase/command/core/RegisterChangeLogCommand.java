package liquibase.command.core;

import liquibase.Scope;
import liquibase.changelog.ChangelogRewriter;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.AbstractSelfConfiguratingCommand;
import liquibase.command.CommandResult;
import liquibase.command.CommandValidationErrors;
import liquibase.GlobalConfiguration;
import liquibase.hub.HubConfiguration;
import liquibase.exception.CommandLineParsingException;
import liquibase.exception.LiquibaseException;
import liquibase.hub.HubService;
import liquibase.hub.HubServiceFactory;
import liquibase.hub.LiquibaseHubException;
import liquibase.hub.model.HubChangeLog;
import liquibase.hub.model.Project;
import liquibase.ui.UIService;
import liquibase.util.StringUtil;

import java.io.PrintStream;
import java.util.*;

public class RegisterChangeLogCommand extends AbstractSelfConfiguratingCommand<CommandResult> {

    private PrintStream outputStream = System.out;

    public HubChangeLog getHubChangeLog() {
        return hubChangeLog;
    }

    private HubChangeLog hubChangeLog;
    private String changeLogFile;
    private Map<String, Object> argsMap = new HashMap<>();
    private UUID hubProjectId;
    private String hubProjectName;

    public void setProjectName(String hubProjectName) {
        this.hubProjectName = hubProjectName;
    }

    public void setHubProjectId(UUID hubProjectId) {
        this.hubProjectId = hubProjectId;
    }

    @Override
    public void configure(Map<String, Object> argsMap) throws LiquibaseException {
        this.argsMap = argsMap;
    }

    public void setChangeLogFile(String changeLogFile) {
        this.changeLogFile = changeLogFile;
    }

    @Override
    public String getName() {
        return "registerChangeLog";
    }

    @Override
    public CommandValidationErrors validate() {
        return null;
    }

    public PrintStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(PrintStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public CommandResult run() throws Exception {
        //
        // Access the HubService
        // Stop if we do no have a key
        //
        final HubServiceFactory hubServiceFactory = Scope.getCurrentScope().getSingleton(HubServiceFactory.class);
        if (!hubServiceFactory.isOnline()) {
            return new CommandResult("The command registerChangeLog requires access to Liquibase Hub: " + hubServiceFactory.getOfflineReason() + ".  Learn more at https://hub.liquibase.com", false);
        }

        //
        // Check for existing changeLog file
        //
        final HubService service = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
        DatabaseChangeLog databaseChangeLog = (DatabaseChangeLog) argsMap.get("changeLog");
        final String changeLogId = (databaseChangeLog != null ? databaseChangeLog.getChangeLogId() : null);
        if (changeLogId != null) {
            hubChangeLog = service.getHubChangeLog(UUID.fromString(changeLogId));
            if (hubChangeLog != null) {
                return new CommandResult("Changelog '" + changeLogFile +
                    "' is already registered with changeLogId '" + changeLogId + "' to project '" +
                    hubChangeLog.getProject().getName() + "' with project ID '" + hubChangeLog.getProject().getId().toString() + "'.\n" +
                    "For more information visit https://docs.liquibase.com.", false);
            } else {
                return new CommandResult("Changelog '" + changeLogFile +
                    "' is already registered with changeLogId '" + changeLogId + "'.\n" +
                    "For more information visit https://docs.liquibase.com.", false);
            }
        }

        //
        // Retrieve the projects
        //
        Project project;
        if (hubProjectId != null) {
            project = service.getProject(hubProjectId);
            if (project == null) {
                return new CommandResult("Project Id '" + hubProjectId + "' does not exist or you do not have access to it", false);
            }
        } else if (hubProjectName != null) {
            if (hubProjectName.length() > 255) {
                return new CommandResult("\nThe project name you gave is longer than 255 characters\n\n");
            }
            project = service.createProject(new Project().setName(hubProjectName));
            if (project == null) {
                return new CommandResult("Unable to create project '" + hubProjectName + "'.\n" +
                    "Learn more at https://hub.liquibase.com.", false);
            }
            outputStream.print("\nProject '" + project.getName() + "' created with project ID '" + project.getId() + "'.\n\n");
        } else {
            project = retrieveOrCreateProject(service);
            if (project == null) {
                return new CommandResult("Your changelog " + changeLogFile + " was not registered to any Liquibase Hub project. You can still run Liquibase commands, but no data will be saved in your Liquibase Hub account for monitoring or reports.  Learn more at https://hub.liquibase.com.", false);
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
        String message = "* Changelog file '" + changeLogFile + "' with changelog ID '" + hubChangeLog.getId().toString() + "' has been registered";
        return new CommandResult(message, changeLogRewriterResult.success);
    }

    private Project retrieveOrCreateProject(HubService service)
        throws CommandLineParsingException, LiquibaseException, LiquibaseHubException {
        Project project = null;
        List<Project> projects = getProjectsFromHub();
        boolean done = false;
        String input = null;
        while (!done) {
            input = readProjectFromConsole(projects);
            try {
                if (input.equalsIgnoreCase("C")) {
                    String projectName = readProjectNameFromConsole();
                    if (StringUtil.isEmpty(projectName)) {
                        outputStream.print("\nNo project created\n\n");
                        continue;
                    } else if (projectName.length() > 255) {
                        outputStream.print("\nThe project name you entered is longer than 255 characters\n\n");
                        continue;
                    }
                    project = service.createProject(new Project().setName(projectName));
                    if (project == null) {
                        String message = "Unable to create project '" + projectName + "'";
                        Scope.getCurrentScope().getUI().sendMessage(message);
                        Scope.getCurrentScope().getLog(RegisterChangeLogCommand.class).warning(message);
                        return null;
                    }
                    outputStream.print("\nProject '" + project.getName() + "' created with project ID '" + project.getId() + "'.\n\n");
                    projects = getProjectsFromHub();
                    done = true;
                    continue;
                } else if (input.equalsIgnoreCase("N")) {
                    return null;
                }
                int projectIdx = Integer.parseInt(input);
                if (projectIdx > 0 && projectIdx <= projects.size()) {
                    project = projects.get(projectIdx - 1);
                    if (project != null) {
                        done = true;
                    }
                } else {
                    outputStream.printf("\nInvalid project '%d' selected\n\n", projectIdx);
                }
            } catch (NumberFormatException nfe) {
                outputStream.printf("\nInvalid selection '" + input + "'\n\n");
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

    private String readProjectFromConsole(List<Project> projects) throws CommandLineParsingException {
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
}
