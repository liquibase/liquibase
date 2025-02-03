package liquibase.command.core.helpers;

import liquibase.Beta;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandBuilder;
import liquibase.command.CommandResultsBuilder;
import liquibase.command.CommonArgumentNames;
import liquibase.configuration.ConfigurationValueObfuscator;
import liquibase.database.Database;

import java.util.Collections;
import java.util.List;

/**
 * This class contains only the arguments used by {@link liquibase.command.core.DiffCommandStep} and {@link liquibase.command.core.DiffChangelogCommandStep}.
 */
public class DiffArgumentsCommandStep extends AbstractHelperCommandStep {

    public static final String[] COMMAND_NAME = new String[]{"diffArgumentsCommandStep"};

    public static final CommandArgumentDefinition<Boolean> IGNORE_MISSING_REFERENCES;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        IGNORE_MISSING_REFERENCES = builder.argument("ignoreMissingReferences", Boolean.class)
                .description("If true, diff operations will ignore referenced objects which are not found in a snapshot.")
                .defaultValue(false)
                .build();
    }


    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        // do nothing
    }

    @Override
    public List<Class<?>> providedDependencies() {
        return Collections.singletonList(DiffArgumentsCommandStep.class);
    }
}
