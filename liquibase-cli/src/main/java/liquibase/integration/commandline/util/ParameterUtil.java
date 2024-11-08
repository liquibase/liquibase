package liquibase.integration.commandline.util;

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

        if (parameter == null) {
            //read it from command line args
            for (String arg : args) {
                if (arg.matches("--.*" + cmd + "=.*")) {
                    String[] cp = arg.split("=");
                    if (cp.length == 2) {
                        parameter = cp[1];
                        break;
                    }
                }
            }
        }
        if (parameter == null && verifyDefaultsFile) {
            //read it from properties file!
            parameter = getParameterFromPropertiesFile(cmd, args, parameter);
        }
        return parameter;
    }

    private static String getParameterFromPropertiesFile(String cmd, String[] args, String parameter) throws IOException {
        String propertiesFile = getParameter(LIQUIBASE_DEFAULTS_FILE, "defaults.*[fF]ile", args, false);
        Resource resource = new DirectoryPathHandler().getResource(propertiesFile);
        if (resource.exists()) {
            try (InputStream defaultsStream = resource.openInputStream()) {
                parameter = getPropertyFromInputStream(cmd, parameter, defaultsStream);
            }
        } else {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesFile);
            parameter = getPropertyFromInputStream(cmd, parameter, inputStream);
        }
        return parameter;
    }

    private static String getPropertyFromInputStream(String cmd, String parameter, InputStream defaultsStream) throws IOException {
        if (defaultsStream != null) {
            Properties properties = new Properties();
            properties.load(defaultsStream);
            Optional<Map.Entry<Object, Object>> property = properties.entrySet().stream()
                    .filter(entry -> entry.getKey().toString().matches(cmd))
                    .findFirst();
            if (property.isPresent()) {
                parameter = property.get().getValue().toString();
            }
        }
        return parameter;
    }
}
