package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.command.CommandScope;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.LiquibaseException;
import org.liquibase.maven.property.PropertyElement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

    @Override
    public boolean databaseConnectionRequired() {
        return false;
    }

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        CommandScope liquibaseCommand = new CommandScope(getCommandName());
        liquibaseCommand.addArgumentValue("flowFile", flowFile);
        liquibaseCommand.addArgumentValue("flowIntegration", "maven");
        for (Map.Entry<String, Object> commandArgument : liquibaseCommandArguments.entrySet()) {
            liquibaseCommand.addArgumentValue(commandArgument.getKey(), commandArgument.getValue());
        }
        if (outputFile != null) {
            try {
                liquibaseCommand.setOutput(new FileOutputStream(outputFile));
            } catch (FileNotFoundException e) {
                throw new CommandExecutionException(e);
            }
        }
        liquibaseCommand.execute();
    }

    public abstract String[] getCommandName();
}
