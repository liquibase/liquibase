package liquibase.configuration.core;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.command.CommandDefinition;
import liquibase.command.CommandFactory;
import liquibase.command.CommandScope;
import liquibase.configuration.AbstractMapConfigurationValueProvider;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.resource.InputStreamList;
import liquibase.servicelocator.LiquibaseService;
import liquibase.util.StringUtil;

import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

@LiquibaseService(skip = true)
public class DefaultsFileValueProvider extends AbstractMapConfigurationValueProvider {

    private static final int DEFAULT_PRECEDENCE = 50;
    private final Properties properties;
    private final String sourceDescription;
    private final int precedence;

    protected DefaultsFileValueProvider(Properties properties, String sourceDescription, int precedenceOffset) {
        this.properties = properties;
        this.sourceDescription = sourceDescription;
        this.precedence = DEFAULT_PRECEDENCE - precedenceOffset;
    }

    protected DefaultsFileValueProvider(Properties properties, int precedenceOffset) throws IOException {
        this(properties, "Passed default properties", precedenceOffset);
    }

    protected DefaultsFileValueProvider(Properties properties) throws IOException {
        this(properties, 0);
    }

    public DefaultsFileValueProvider(Map<String,?> properties, int precedenceOffset) throws IOException {
        this(propertiesFromMap(properties), precedenceOffset);
    }

    public DefaultsFileValueProvider(Map<String,?> properties) throws IOException {
        this(properties, 0);
    }

    public DefaultsFileValueProvider(InputStreamList streamList, String sourceDescription, int precedenceOffset) throws IOException {
        this(propertiesFromStreams(streamList), sourceDescription, precedenceOffset);
    }

    public DefaultsFileValueProvider(InputStream stream, String sourceDescription, int precedenceOffset) throws IOException {
        this(propertiesFromStream(stream), sourceDescription, precedenceOffset);
    }

    public DefaultsFileValueProvider(File path, int precedenceOffset) throws IOException {
        this(propertiesFromFile(path), "File " + path.getAbsolutePath(), precedenceOffset);
    }

    public DefaultsFileValueProvider(InputStream stream, String sourceDescription) throws IOException {
        this(stream, sourceDescription, 0);
    }

    public DefaultsFileValueProvider(File path) throws IOException {
        this(path, 0);
    }

    private static Properties propertiesFromStream(InputStream stream) throws IOException {

        Properties properties = new Properties();
        // CWE-94 guard: keep this bare java.util.Properties.load(). Do NOT add
        // ${...} interpolation, environment-variable expansion, or include-file
        // directives here. A defaults file may contain user-controlled values;
        // expansion engines have a documented history of CVE-grade injection /
        // info-disclosure issues (e.g. CVE-2022-33980 in Apache Commons Configuration).
        // Pro extensions that need safe substitution can register a
        // ConfiguredValueModifier instead of changing this loader.
        properties.load(stream);
        trimAllProperties(properties);
        return properties;
    }

    private static Properties propertiesFromStreams(InputStreamList streamList) throws IOException {

        Properties properties = new Properties();
        for(InputStream stream : streamList.getInputStreams()) {
            properties.putAll(propertiesFromStream(stream));
        }
        return properties;
    }

    private static Properties propertiesFromFile(File file) throws IOException {

        try (InputStream stream = new FileInputStream(file)) {
            return propertiesFromStream(stream);
        }
    }

    private static Properties propertiesFromMap(Map<String,?> map) {

        Properties properties = new Properties();
        properties.putAll(map);
        trimAllProperties(properties);
        return properties;
    }

    @Override
    public void validate(CommandScope commandScope) throws IllegalArgumentException {
        boolean strict = GlobalConfiguration.STRICT.getCurrentValue();
        SortedSet<String> invalidKeys = new TreeSet<>();
        for (Map.Entry<Object, Object> entry : this.properties.entrySet()) {
            String key = StringUtil.toCamelCase(entry.getKey().toString());
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

        if (!invalidKeys.isEmpty()) {
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
    private static void trimAllProperties(Properties properties) {
        properties.forEach((key, value) -> {
            if (value instanceof String) {
                properties.put(key, StringUtil.trimToEmpty((String) value));
            }
        });
    }

    @Override
    public int getPrecedence() {
        return this.precedence;
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
