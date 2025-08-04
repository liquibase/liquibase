package liquibase.integration.commandline.util;

import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.resource.DirectoryPathHandler;
import liquibase.resource.Resource;
import liquibase.util.LiquibaseLauncherSettings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static liquibase.util.LiquibaseLauncherSettings.LiquibaseLauncherSetting.LIQUIBASE_DEFAULTS_FILE;
import static liquibase.util.LiquibaseLauncherSettings.getSetting;

/**
 * Utility class for reading parameters from system properties, command line arguments and properties file.
 */
public class ParameterUtil {

    private ParameterUtil() {
        // prevent instantiation
    }

    /**
     * Get parameter from system properties, command line arguments and properties file.
     * @param param parameter to get
     * @param cmd parameter aliases for cmdline/file
     * @param args command line arguments
     * @param verifyDefaultsFile whether to look for it in properties file or not
     * @return parameter value
     * @throws IOException if there is an error reading the properties file
     */
    public static String getParameter(LiquibaseLauncherSettings.LiquibaseLauncherSetting param, String cmd, String[] args, boolean verifyDefaultsFile) throws IOException {
        // read parameter from system properties
        String parameter = getSetting(param);
        if (parameter != null) {
            return parameter;
        }

        //read it from command line args
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.matches("--.*" + cmd + "=.*")) {
                String[] cp = arg.split("=");
                if (cp.length == 2) {
                    return cp[1];
                }
            } else if (arg.matches("--.*" + cmd) && (i + 1 < args.length) && !args[i + 1].startsWith("--")) {
            return args[i + 1];
            }
        }

        if (verifyDefaultsFile) {
            //read it from properties file!
            return getParameterFromPropertiesFile(cmd, args);
        }

        //give up
        return null;
    }

    private static String getParameterFromPropertiesFile(String cmd, String[] args) throws IOException {
        String propertiesFile = getParameter(LIQUIBASE_DEFAULTS_FILE, "defaults.*[fF]ile", args, false);
        if (propertiesFile == null) {
            propertiesFile = LiquibaseCommandLineConfiguration.DEFAULTS_FILE.getDefaultValue();
        }
        Resource resource = new DirectoryPathHandler().getResource(propertiesFile);
        if (resource.exists()) {
            try (InputStream defaultsStream = resource.openInputStream()) {
                return getPropertyFromInputStream(cmd, defaultsStream);
            }
        } else {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFile);
            return getPropertyFromInputStream(cmd, inputStream);
        }
    }

    private static String getPropertyFromInputStream(String cmd, InputStream defaultsStream) throws IOException {
        if (defaultsStream != null) {
            Properties properties = new Properties();
            properties.load(defaultsStream);
            Optional<Map.Entry<Object, Object>> property = properties.entrySet().stream()
                    .filter(entry -> entry.getKey().toString().matches(cmd))
                    .findFirst();
            if (property.isPresent()) {
                return property.get().getValue().toString();
            }
        }
        return null;
    }
}
