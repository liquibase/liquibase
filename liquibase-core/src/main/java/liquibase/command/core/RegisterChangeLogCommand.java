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
import liquibase.hub.model.HubChangeLog;
import liquibase.hub.model.Project;
import liquibase.resource.InputStreamList;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.ResourceBundle.getBundle;

public class RegisterChangeLogCommand extends AbstractSelfConfiguratingCommand<CommandResult> {
    private static ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");
    private PrintStream outputStream = System.out;

    public HubChangeLog getHubChangeLog() {
        return hubChangeLog;
    }

    private HubChangeLog hubChangeLog;
    private String changeLogFile;
    private Map<String, Object> argsMap = new HashMap<>();

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
        final HubService service = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
        if (! service.hasApiKey()) {
            String message = coreBundle.getString("no.hub.api.key");
            return new CommandResult(message, false);
        }

        //
        // Connect to Hub
        //
        service.getMe();


        final List<Project> projects = service.getProjects();
        Project project = null;
        HubConfiguration hubConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class);
        final String hubProjectName = hubConfiguration.getLiquibaseHubProject();
        if (hubProjectName != null) {
            for (Project testProject : projects) {
                if (testProject.getName().equalsIgnoreCase(hubProjectName)) {
                    project = testProject;
                }
            }
        }
        else {
            String input = null;
            while (project == null) {
                input = readProjectFromConsole(projects);
                try {
                    int projectIdx = Integer.parseInt(input);
                    if (projectIdx > 0 && projectIdx <= projects.size()) {
                        project = projects.get(projectIdx - 1);
                    } else {
                        outputStream.printf("Invalid project %d selected\n", projectIdx);
                        continue;
                    }
                    if (input.equalsIgnoreCase("N")) {
                        return new CommandResult("", true);
                    }
                }
                catch (NumberFormatException nfe) {
                    // Project not selected.  Continue.
                }
            }
        }

        //
        // CHeck for existing changeLog file
        //
        DatabaseChangeLog databaseChangeLog = (DatabaseChangeLog)argsMap.get("changeLog");
        final String changeLogId = (databaseChangeLog != null ? databaseChangeLog.getChangeLogId() : null);
        if (changeLogId != null) {
            hubChangeLog = service.getChangeLog(changeLogId, project);
            if (hubChangeLog != null) {
                return new CommandResult("Changelog '" + changeLogFile +
                   "' is already registered with changeLogId=" + changeLogId + " to project " +
                   project.getName() + " with projectId=" + project.getId().toString() +
                   ". For more information visit https://docs.liquibase.com", false);
            }
            else {
                return new CommandResult("Changelog '" + changeLogFile +
                        "' is already registered with changeLogId=" + changeLogId + ". For more information visit https://docs.liquibase.com", false);
            }
        }

        //
        // Go create the Hub Changelog
        //
        hubChangeLog = service.createChangeLog(project);

        //
        // Make changes to the changelog file
        //
        final ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        InputStreamList list = resourceAccessor.openStreams("", changeLogFile);
        List<URI> uris = list.getURIs();
        InputStream is = list.iterator().next();
        String encoding = LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding();
        String xml = StreamUtil.readStreamAsString(is, encoding);
        String patternString = "(?ms).*<databaseChangeLog[^>]*>";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            String header = xml.substring(matcher.start(), matcher.end()-1);

            String xsdPatternString = "([dbchangelog|liquibase-pro])-3.[0-9].xsd";
            Pattern xsdPattern = Pattern.compile(xsdPatternString);
            Matcher xsdMatcher = xsdPattern.matcher(header);
            String editedString = xsdMatcher.replaceAll("$1-4.0.xsd");

            String outputHeader = editedString + " changeLogId=\"" + hubChangeLog.getId().toString() + "\">";
            xml = xml.replaceFirst(patternString, outputHeader);
            is.close();
            File f = new File(uris.get(0).getPath());
            RandomAccessFile randomAccessFile = new RandomAccessFile(f, "rw");
            randomAccessFile.write(xml.getBytes(encoding));
            randomAccessFile.close();
        }

        return new CommandResult();
    }

    private String readProjectFromConsole(List<Project> projects) throws CommandLineParsingException {
        System.out.println("Registering a changelog connects Liquibase operations to a Project for monitoring and reporting. ");
        System.out.println("Register changelog <changelogfilename> to an existing Project, or create a new one.");

        System.out.println("Please make a selection:");

        System.out.println("[c] Create new Project");
        for (int i=0; i < projects.size(); i++) {
            Project project = projects.get(i);
            System.out.println(String.format("[%d] %s (Project ID:%s)", i+1, project.getName(), projects.get(0).getId()));
        }
        System.out.println("[N] to not register this changelog right now. " +
            "You can still run Liquibase commands, but no data will be saved in your Liquibase Hub account for monitoring or reports. Learn more at https://docs.liquibase.com");
        System.out.print("?> ");
        Console c = System.console();
        if (c == null) {
            throw new CommandLineParsingException("No console available");
        }
        String input = c.readLine();
        return input.trim();
    }
}
