package liquibase.command.core;

import liquibase.command.AbstractCommand;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandScope;
import liquibase.integration.commandline.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpdateCommand extends AbstractCommand {
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> LABELS_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;

    static {
        CommandArgumentDefinition.Builder builder = new CommandArgumentDefinition.Builder(UpdateCommand.class);
        CHANGELOG_FILE_ARG = builder.define("changeLogFile", String.class).required().build();
        LABELS_ARG = builder.define("labels", String.class).build();
        CONTEXTS_ARG = builder.define("contexts", String.class).build();
    }

    @Override
    public String[] getName() {
        return new String[] {"update"};
    }

    @Override
    public void run(CommandScope commandScope) throws Exception {
        List<String> argsList = new ArrayList<>();
        Map<String, CommandArgumentDefinition> arguments = commandScope.getArguments();
        arguments.entrySet().forEach( arg -> {
            argsList.add("-- " + arg.getKey());
            argsList.add(arg.getValue().getValue(commandScope).toString());
        });
        String[] args = new String[argsList.size()];
        argsList.toArray(args);
        Main.run(args);
    }
}
