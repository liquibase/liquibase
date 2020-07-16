package liquibase.command.core;

import liquibase.Scope;
import liquibase.command.AbstractSelfConfiguratingCommand;
import liquibase.command.CommandResult;
import liquibase.command.CommandValidationErrors;
import liquibase.hub.HubService;
import liquibase.hub.HubServiceFactory;
import liquibase.hub.model.Project;

import java.io.PrintStream;
import java.util.List;
import java.util.UUID;

public class RegisterChangeLogCommand extends AbstractSelfConfiguratingCommand<CommandResult> {

    private PrintStream outputStream = System.out;

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
        final List<Project> projects = service.getProjects();
        for (Project project : projects) {
            outputStream.println("See project "+project.getName());
        }

        return new CommandResult();
    }


}
