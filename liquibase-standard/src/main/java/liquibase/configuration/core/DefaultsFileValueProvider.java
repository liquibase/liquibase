package liquibase.configuration.core;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.command.CommandDefinition;
import liquibase.command.CommandFactory;
import liquibase.command.CommandScope;
import liquibase.configuration.AbstractMapConfigurationValueProvider;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.servicelocator.LiquibaseService;
import liquibase.util.StringUtil;

import java.io.*;
import java.nio.file.Files;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

@LiquibaseService(skip = true)
public class DefaultsFileValueProvider extends AbstractMapConfigurationValueProvider {

    private final Properties properties;
    private final String sourceDescription;

    protected DefaultsFileValueProvider(Properties properties) {
        this.properties = properties;
        sourceDescription = "Passed default properties";
    }

    public DefaultsFileValueProvider(InputStream stream, String sourceDescription) throws IOException {
        this.sourceDescription = sourceDescription;
        this.properties = new Properties();
        // CWE-94 guard: keep this bare java.util.Properties.load(). Do NOT add
        // ${...} interpolation, environment-variable expansion, or include-file
        // directives here. A defaults file may contain user-controlled values;
        // expansion engines have a documented history of CVE-grade injection /
        // info-disclosure issues (e.g. CVE-2022-33980 in Apache Commons Configuration).
        // Pro extensions that need safe substitution can register a
        // ConfiguredValueModifier instead of changing this loader.
        this.properties.load(stream);
        trimAllProperties();
    }

    public DefaultsFileValueProvider(File path) throws IOException {
        this.sourceDescription = "File " + path.getAbsolutePath();

        try (InputStream stream = Files.newInputStream(path.toPath())) {
            this.properties = new Properties();
            // CWE-94 guard: see the InputStream-arg ctor above. Bare Properties.load —
            // adding ${...} / env-var / include expansion requires a security review.
            this.properties.load(stream);
            trimAllProperties();
        }
    }

    @Override
    public void validate(CommandScope commandScope) throws IllegalArgumentException {
        boolean strict = GlobalConfiguration.STRICT.getCurrentValue();
        SortedSet<String> invalidKeys = new TreeSet<>();
        for (Map.Entry<Object, Object> entry : this.properties.entrySet()) {
            String key = (String) entry.getKey();
            key = StringUtil.toCamelCase(key);
            String originalKey = key;

            if (key.equalsIgnoreCase("strict") || key.startsWith("parameter.")) {
                continue;
            }

            final String genericCommandPrefix = "liquibase.command.";
            final String targetedCommandPrefix = "liquibase.command." + StringUtil.join(commandScope.getCommand().getName(), ".") + ".";
            if (!key.contains(".")) {
                if (commandScope.getCommand().getArgument(key) == null) {
                        if(!key.startsWith("liquibase")) {
                            key = "liquibase." + key;
                        }
                    if (Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getRegisteredDefinition(key) == null) {
                        invalidKeys.add(" - '" + originalKey + "'");
                    }
                }
            } else if (key.startsWith(targetedCommandPrefix)) {
                String keyAsArg = key.replace(targetedCommandPrefix, "");
                if (commandScope.getCommand().getArgument(keyAsArg) == null) {
                    invalidKeys.add(" - '" + originalKey + "'");
                }
            } else if (key.startsWith(genericCommandPrefix)) {
                String keyAsArg = key.replace(genericCommandPrefix, "");

                boolean foundMatch = false;
                for (CommandDefinition definition : Scope.getCurrentScope().getSingleton(CommandFactory.class).getCommands(true)) {
                    if (definition.getArgument(keyAsArg) != null) {
                        foundMatch = true;
                        break;
                    }
                }
                if (!foundMatch) {
                    invalidKeys.add(" - '" + originalKey + "'");
                }
            } else {
                if (Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getRegisteredDefinition(key) == null) {
                    invalidKeys.add(" - '" + originalKey + "'");
                }
            }
        }

        if (invalidKeys.size() > 0) {
            if (strict) {
                String message = "Strict check failed due to undefined key(s) for '" + StringUtil.join(commandScope.getCommand().getName(), " ")
                    + "' command in " + StringUtil.lowerCaseFirst(sourceDescription) + "':\n"
                    + StringUtil.join(invalidKeys, "\n")
                    + "\nTo define keys that could apply to any command, prefix it with 'liquibase.command.'\nTo disable strict checking, remove 'strict' from the file.";
                throw new IllegalArgumentException(message);
            } else {
                Scope.getCurrentScope().getLog(getClass()).warning("Potentially ignored key(s) in " + StringUtil.lowerCaseFirst(sourceDescription) + "\n" + StringUtil.join(invalidKeys, "\n"));
            }
        }
    }

    //
    // Remove trailing spaces on the property file values
    //
    private void trimAllProperties() {
        properties.forEach((key, value) -> {
            if (value == null) {
                return;
            }
            if (!(value instanceof String)) {
                return;
            }
            properties.put(key, StringUtil.trimToEmpty((String) value));
        });
    }

    @Override
    public int getPrecedence() {
        return 50;
    }

    @Override
    public Map<?, ?> getMap() {
        return properties;
    }

    @Override
    protected boolean keyMatches(String wantedKey, String storedKey) {
        if (super.keyMatches(wantedKey, storedKey)) {
            return true;
        }

        //Stored the argument name without a prefix
        return wantedKey.replaceFirst("^liquibase\\.", "").equalsIgnoreCase(StringUtil.toCamelCase(storedKey))
                || wantedKey.replaceFirst("^liquibase\\.command\\.", "").equalsIgnoreCase(StringUtil.toCamelCase(storedKey));
    }

    @Override
    protected String getSourceDescription() {
        return sourceDescription;
    }
}
