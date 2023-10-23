package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.LiquibaseException;
import org.liquibase.maven.property.PropertyElement;
import org.liquibase.maven.provider.FlowCommandArgumentValueProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public abstract class AbstractLiquibaseFlowMojo extends AbstractLiquibaseMojo {
    /**
     * Specifies the <i>flowFile</i> to use. If not specified, the default
     * checks will be used and no file will be created.
     *
     * @parameter property="liquibase.flowFile"
     */
    @PropertyElement
    protected String flowFile;

    /**
     * @parameter property="liquibase.outputFile"
     */
    @PropertyElement
    protected File outputFile;

    /**
     * Arbitrary map of parameters that the underlying liquibase command will use. These arguments will be passed
     * verbatim to the underlying liquibase command that is being run.
     *
     * @parameter property="flowCommandArguments"
     */
    @PropertyElement
    protected Map<String, Object> flowCommandArguments;

    @Override
    public boolean databaseConnectionRequired() {
        return false;
    }

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        CommandScope liquibaseCommand = new CommandScope(getCommandName());
        liquibaseCommand.addArgumentValue("flowFile", flowFile);
        liquibaseCommand.addArgumentValue("flowIntegration", "maven");
        if (flowCommandArguments != null) {
            FlowCommandArgumentValueProvider flowCommandArgumentValueProvider = new FlowCommandArgumentValueProvider(flowCommandArguments);
            Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).registerProvider(flowCommandArgumentValueProvider);
        }
        if (outputFile != null) {
            try {
                liquibaseCommand.setOutput(Files.newOutputStream(outputFile.toPath()));
            } catch (IOException e) {
                throw new CommandExecutionException(e);
            }
        }
        liquibaseCommand.execute();
    }

    public abstract String[] getCommandName();
}
