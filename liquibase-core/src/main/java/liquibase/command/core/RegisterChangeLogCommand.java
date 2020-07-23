package liquibase.command.core;

import liquibase.Scope;
import liquibase.command.AbstractSelfConfiguratingCommand;
import liquibase.command.CommandResult;
import liquibase.command.CommandValidationErrors;
import liquibase.configuration.HubConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.CommandLineParsingException;
import liquibase.hub.HubService;
import liquibase.hub.HubServiceFactory;
import liquibase.hub.model.HubChangeLog;
import liquibase.hub.model.Project;

import java.io.Console;
import java.io.PrintStream;
import java.util.List;

public class RegisterChangeLogCommand extends AbstractSelfConfiguratingCommand<CommandResult> {

    private PrintStream outputStream = System.out;

    public HubChangeLog getHubChangeLog() {
        return hubChangeLog;
    }

    private HubChangeLog hubChangeLog;

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
        final HubService service = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
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
            String input = readProjectFromConsole(projects);
            try {
                int projectIdx = Integer.parseInt(input);
                if (projectIdx > 0 && projectIdx <= projects.size()) {
                    project = projects.get(projectIdx - 1);
                }
            }
            catch (NumberFormatException nfe) {
                // Project not selected.  Continue.
            }
        }
        hubChangeLog = null;
        if (project != null) {
            hubChangeLog = service.createChangeLog(project);
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
