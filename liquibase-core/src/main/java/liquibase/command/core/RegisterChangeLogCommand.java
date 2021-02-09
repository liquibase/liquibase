package liquibase.command.core;

import liquibase.Scope;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.AbstractSelfConfiguratingCommand;
import liquibase.command.CommandResult;
import liquibase.command.CommandValidationErrors;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.HubConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.CommandLineParsingException;
import liquibase.exception.LiquibaseException;
import liquibase.hub.HubService;
import liquibase.hub.HubServiceFactory;
import liquibase.hub.LiquibaseHubException;
import liquibase.hub.model.HubChangeLog;
import liquibase.hub.model.Project;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.InputStreamList;
import liquibase.resource.ResourceAccessor;
import liquibase.ui.UIService;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtil;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterChangeLogCommand extends AbstractSelfConfiguratingCommand<CommandResult> {

    private PrintStream outputStream = System.out;

    public HubChangeLog getHubChangeLog() {
        return hubChangeLog;
    }

    private HubChangeLog hubChangeLog;
    private String changeLogFile;
    private Map<String, Object> argsMap = new HashMap<>();
    private UUID hubProjectId;


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
    protected CommandResult run() throws Exception {
        //
        // Access the HubService
        // Stop if we do no have a key
        //
        final HubServiceFactory hubServiceFactory = Scope.getCurrentScope().getSingleton(HubServiceFactory.class);
        if (!hubServiceFactory.isOnline()) {
            return new CommandResult("The command registerChangeLog requires access to Liquibase Hub: " + hubServiceFactory.getOfflineReason() + ".  Learn more at https://hub.liquibase.com", false);
        }

        final HubService service = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();

        //
        // CHeck for existing changeLog file
        //
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
        Project project = null;

        if (hubProjectId != null) {
            project = service.getProject(hubProjectId);

            if (project == null) {
                return new CommandResult("Project Id '" + hubProjectId + "' does not exist or you do not have access to it", false);
            }

        } else {
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
                            return new CommandResult("\nUnable to create project '" + projectName + "'.\n\n", false);
                        }
                        outputStream.print("\nProject '" + project.getName() + "' created with project ID '" + project.getId() + "'.\n\n");
                        projects = getProjectsFromHub();
                        done = true;
                        continue;
                    } else if (input.equalsIgnoreCase("N")) {
                        return new CommandResult("Your changelog " + changeLogFile + " was not registered to any Liquibase Hub project. You can still run Liquibase commands, but no data will be saved in your Liquibase Hub account for monitoring or reports.  Learn more at https://hub.liquibase.com.", false);
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
        }

        //
        // Go create the Hub Changelog
        //
        HubChangeLog newChangeLog = new HubChangeLog();
        newChangeLog.setProject(project);
        newChangeLog.setFileName(databaseChangeLog.getFilePath());
        newChangeLog.setName(databaseChangeLog.getFilePath());

        hubChangeLog = service.createChangeLog(newChangeLog);

        //
        // Make changes to the changelog file
        //
        final ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        InputStreamList list = null;
        try {
            list = resourceAccessor.openStreams("", changeLogFile);
            List<URI> uris = list.getURIs();
            InputStream is = list.iterator().next();
            String encoding = LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding();
            String changeLogString = StreamUtil.readStreamAsString(is, encoding);
            if (changeLogFile.toLowerCase().endsWith(".xml")) {
                String patternString = "(?ms).*<databaseChangeLog[^>]*>";
                Pattern pattern = Pattern.compile(patternString);
                Matcher matcher = pattern.matcher(changeLogString);
                if (matcher.find()) {
                    //
                    // Update the XSD versions
                    //
                    String header = changeLogString.substring(matcher.start(), matcher.end() - 1);
                    String xsdPatternString = "([dbchangelog|liquibase-pro])-3.[0-9]?[0-9]?.xsd";
                    Pattern xsdPattern = Pattern.compile(xsdPatternString);
                    Matcher xsdMatcher = xsdPattern.matcher(header);
                    String editedString = xsdMatcher.replaceAll("$1-" + XMLChangeLogSAXParser.getSchemaVersion() + ".xsd");

                    //
                    // Add the changeLogId attribute
                    //
                    final String outputChangeLogString = " changeLogId=\"" + hubChangeLog.getId().toString() + "\"";
                    if (changeLogString.trim().endsWith("/>")) {
                        changeLogString = changeLogString.replaceFirst("/>", outputChangeLogString + "/>");
                    }
                    else {
                        String outputHeader = editedString + outputChangeLogString + ">";
                        changeLogString = changeLogString.replaceFirst(patternString, outputHeader);
                    }
                }
            } else if (changeLogFile.toLowerCase().endsWith(".sql")) {
                //
                // Formatted SQL changelog
                //
                String newChangeLogString = changeLogString.replaceFirst("--(\\s*)liquibase formatted sql",
                        "-- liquibase formatted sql changeLogId:" + hubChangeLog.getId().toString());
                if (newChangeLogString.equals(changeLogString)) {
                    return new CommandResult("Unable to update changeLogId in changelog file '" + changeLogFile + "'", false);
                }
                changeLogString = newChangeLogString;

        } else if (changeLogFile.toLowerCase().endsWith(".json")) {
            changeLogString = changeLogString.replaceFirst("\\[", "\\[\n" +
                    "\"changeLogId\"" + ":" + "\"" + hubChangeLog.getId().toString() + "\",\n");
        } else if (changeLogFile.toLowerCase().endsWith(".yml") || changeLogFile.toLowerCase().endsWith(".yaml")) {
            changeLogString = changeLogString.replaceFirst("^databaseChangeLog:(\\s*)\n", "databaseChangeLog:$1\n" +
                    "- changeLogId: " + hubChangeLog.getId().toString() + "$1\n");
            } else {
                return new CommandResult("Changelog file '" + changeLogFile + "' is not a supported format", false);
            }

            //
            // Close the InputStream and write out the file again
            //
            File f = new File(uris.get(0).getPath());
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(f, "rw")) {
                randomAccessFile.write(changeLogString.getBytes(encoding));
            }
            //
            // Update the current DatabaseChangeLog with its id
            //
            databaseChangeLog.setChangeLogId(hubChangeLog.getId().toString());
            return new CommandResult("* Changelog file '" + changeLogFile +
                    "' registered with changelog ID '" + hubChangeLog.getId() + "' " +
                    "to project '" + project.getName() + "'\n", true);
        }
        finally {
            if (list != null) {
                list.close();
            }
        }
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

        HubConfiguration hubConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class);
        String hubUrl = hubConfiguration.getLiquibaseHubUrl();
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
