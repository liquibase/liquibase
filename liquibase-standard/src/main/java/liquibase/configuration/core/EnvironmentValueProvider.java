package liquibase.configuration.core;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandDefinition;
import liquibase.command.CommandFactory;
import liquibase.command.CommandScope;
import liquibase.configuration.AbstractMapConfigurationValueProvider;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.util.StringUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Searches for the configuration values in the system environment variables.
 * <p>
 * To handle shells that only allow underscores, it checks the following variations of a property:
 * <ul>
 * <li>foo.bar - the original name</li>
 * <li>foo_bar - with underscores for periods (if any)</li>
 * <li>FOO.BAR - original, with upper case</li>
 * <li>FOO_BAR - with underscores and upper case</li>
 * </ul>
 * Any hyphen variant of the above would work as well, or even mix dot/hyphen variants.
 */
public class EnvironmentValueProvider extends AbstractMapConfigurationValueProvider {

    private final Map<String, String> environment = System.getenv();
    private static final AtomicBoolean printedInvalidEnvironmentVariablesMessage = new AtomicBoolean(false);

    @Override
    public int getPrecedence() {
        return 150;
    }

    @Override
    protected Map<?, ?> getMap() {
        return environment;
    }

    @Override
    public void validate(CommandScope commandScope) throws IllegalArgumentException {
        //
        // Look for LIQUIBASE_* environment variables that are not defined
        //
        List<String> unknownVariables = new ArrayList<>();
        LiquibaseConfiguration liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);
        for (Map.Entry<?, ?> entry : getMap().entrySet()) {
            String originalKey = ((String) entry.getKey());
            String key = ((String) entry.getKey()).toLowerCase();
            if (!key.startsWith("liquibase_") || key.equals("liquibase_home") || key.startsWith("liquibase_launcher_")) {
                continue;
            }

            // Convert to camel case to do the lookup
            // We check for variables that:
            // 1. are not defined.  If not defined then we check the command arguments.  Skip internal commands.
            //    For command arguments, we look for environment variables of the forms:
            //        LIQUIBASE_COMMAND_<argument> and
            //        LIQUIBASE_COMMAND_<command name>_<argument>
            // 2. do not have a current value
            // 3. or only use the default value
            String editedKey = StringUtil.toCamelCase(key);
            ConfigurationDefinition<?> def = liquibaseConfiguration.getRegisteredDefinition(editedKey);
            if (def == null) {
                boolean found = false;
                SortedSet<CommandDefinition> commands = Scope.getCurrentScope().getSingleton(CommandFactory.class).getCommands(false);
                for (CommandDefinition commandDef : commands) {
                    SortedMap<String, CommandArgumentDefinition<?>> arguments = commandDef.getArguments();
                    StringBuilder fullName = new StringBuilder();
                    for (String name : commandDef.getName()) {
                        fullName.append(StringUtil.upperCaseFirst(name));
                    }
                    // Remove "liquibaseCommand<commandName>" from the key
                    String simplifiedKey = StringUtil.lowerCaseFirst(editedKey.replace("liquibaseCommand", "")
                            .replaceFirst(fullName.toString(), ""));
                    // check the normal arguments
                    found = arguments.get(simplifiedKey) != null;
                    if (!found) {
                        // then check aliases too if we didn't find anything
                        found = arguments.values()
                                .stream()
                                .anyMatch(argDef -> argDef.getAliases().contains(simplifiedKey));
                    }
                    if (found) {
                        break;
                    }
                }
                if (!found) {
                    unknownVariables.add("- " + originalKey);
                }
            } else if (def.getCurrentValue() == null || def.getCurrentConfiguredValue().wasDefaultValueUsed()) {
                unknownVariables.add("- " + originalKey);
            }
        }
        Boolean strict = GlobalConfiguration.STRICT.getCurrentValue();
        if (!unknownVariables.isEmpty()) {
            String message = System.lineSeparator() + System.lineSeparator() +
                    "Liquibase detected the following invalid LIQUIBASE_* environment variables:" + System.lineSeparator() + System.lineSeparator() +
                    StringUtil.join(unknownVariables, System.lineSeparator(), true) + System.lineSeparator() + System.lineSeparator();
            if (strict) {
                message += "Please rename them and run your command again, or set liquibase.strict=FALSE or LIQUIBASE_STRICT=FALSE." + System.lineSeparator();
            }
            message += "Find the list of valid environment variables at https://docs.liquibase.com/environment-variables" + System.lineSeparator();
            if (!printedInvalidEnvironmentVariablesMessage.getAndSet(true)) {
                Scope.getCurrentScope().getUI().sendMessage("WARNING: " + message);
                Scope.getCurrentScope().getLog(EnvironmentValueProvider.class).warning(message);
            }
            if (strict) {
                throw new IllegalArgumentException(message);
            }
        }
    }

    @Override
    protected String getSourceDescription() {
        return "Environment variable";
    }

    @Override
    protected boolean keyMatches(String wantedKey, String storedKey) {
        storedKey = StringUtil.trimToNull(storedKey);
        if (super.keyMatches(wantedKey, storedKey)) {
            return true;
        }

        wantedKey = wantedKey.replace(".", "-");
        wantedKey = StringUtil.toKabobCase(wantedKey);

        if (wantedKey.equalsIgnoreCase(storedKey)) {
            return true;
        }

        wantedKey = wantedKey.replace("-", "_");
        return wantedKey.equalsIgnoreCase(storedKey);
    }
}
