package liquibase.command.core;

import liquibase.Scope;
import liquibase.command.*;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.configuration.ConfigurationValueConverter;
import liquibase.configuration.ConfiguredValue;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.UnexpectedLiquibaseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.lang.module.ModuleDescriptor;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This command step is responsible for running the Liquibase Package Manager (LPM) command.
 */
public class LpmCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"lpm"};
    
    public static final CommandArgumentDefinition<Boolean> DOWNLOAD_ARG;
    public static final CommandArgumentDefinition<String> LPM_HOME_ARG;
    
    // Configuration definition for LPM_HOME
    public static final ConfigurationDefinition<String> LPM_HOME;
    
    // Constants
    private static final String LPM_BINARY_NAME = "lpm";
    private static final String DOCS_URL = "http://docs.liquibase.com/LPM";
    private static final String LPM_ERROR_PREFIX = "ERROR: Liquibase Package Manager (LPM)";

    private static final String LPM_DOWNLOAD_PAGE_URL = "https://api.github.com/repos/liquibase/liquibase-package-manager/releases/latest";
    
    static {
        // Define LPM_HOME configuration property
        ConfigurationDefinition.Builder configBuilder = new ConfigurationDefinition.Builder("liquibase.lpm");
        LPM_HOME = configBuilder.define("home", String.class)
                .setDescription("Directory where LPM (Liquibase Package Manager) is installed. Defaults to LIQUIBASE_HOME.")
                .setValueHandler(value -> {
                    if (value == null || value.toString().trim().isEmpty()) {
                        // Fall back to liquibase.home
                        ConfiguredValue<String> liquibaseHome = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class)
                                .getCurrentConfiguredValue(ConfigurationValueConverter.STRING, null, "liquibase.home");
                        return liquibaseHome.getValue();
                    }
                    return value.toString();
                })
                .build();
        
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        DOWNLOAD_ARG = builder.argument("download", Boolean.class)
                .description("Download and install LPM binary")
                .defaultValue(false)
                .build();
        
        LPM_HOME_ARG = builder.argument("lpmHome", String.class)
                .description("Directory where LPM is installed")
                .build();
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return new ArrayList<>();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Initialize and update Liquibase Package Manager (LPM)");
        commandDefinition.setLongDescription("Download, install, and manage Liquibase packages using the Liquibase Package Manager (LPM)");
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        // Resolve and validate LPM_HOME path
        String lpmHome = validateAndResolveLpmHome(resultsBuilder);
        
        final String lpmExecutable = buildLpmExecutablePath(lpmHome);
        
        boolean download = resultsBuilder.getCommandScope().getArgumentValue(DOWNLOAD_ARG);
        // If --download is specified, check for updates and install/upgrade LPM
        if (download) {
            this.downloadOrUpgradeLpm(lpmHome, lpmExecutable);
            return;
        }
        
        // Ensure LPM is installed before running commands
        this.checkForLpmInstallation(lpmHome, lpmExecutable);
        
        // Build LPM command based on arguments
        List<String> lpmCommand = new ArrayList<>();
        String lpmArgsStr = Scope.getCurrentScope().get(Scope.Attr.lpmArgs, String.class);
        if (lpmArgsStr != null && !lpmArgsStr.trim().isEmpty()) {
            String[] lpmArgs = lpmArgsStr.trim().split("\\s+");
            Collections.addAll(lpmCommand, lpmArgs);
        } else {
            // Default behavior - show help
            lpmCommand.add("--help");
        }

        this.runLpmWithCommands(lpmExecutable, lpmCommand);
    }

    private void runLpmWithCommands(String lpmExecutable, List<String> commands) {
        List<String> lpmExecArgs = new ArrayList<>();
        lpmExecArgs.add(lpmExecutable);
        lpmExecArgs.addAll(commands);
        
        try {
            Scope.getCurrentScope().getLog(getClass()).fine("Executing LPM command: " + String.join(" ", lpmExecArgs));
            final Process process = new ProcessBuilder(lpmExecArgs).redirectErrorStream(true).start();
            try (InputStream stdOut = process.getInputStream()) {
                List<String> lines = IOUtils.readLines(stdOut, StandardCharsets.UTF_8);
                for (String line : lines) {
                    Scope.getCurrentScope().getUI().sendMessage(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new UnexpectedLiquibaseException("LPM command failed with exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new UnexpectedLiquibaseException("Failed to execute LPM command: " + e.getMessage(), e);
        }
    }

    /**
     * Downloads LPM or upgrades to the latest version if a newer one is available.
     */
    private void downloadOrUpgradeLpm(String lpmHome, String lpmExecutable) {
        validateLpmHomeNotEmpty(lpmHome);
        
        String latestVersion = getLatestLpmVersion();
        File lpmExecutableFile = new File(lpmExecutable);
        
        if (lpmExecutableFile.exists()) {
            // LPM exists, check if we need to upgrade
            String currentVersion = getCurrentLpmVersion(lpmExecutable);
            if (currentVersion != null && isNewerVersion(latestVersion, currentVersion)) {
                Scope.getCurrentScope().getUI().sendMessage("Upgrading LPM from version " + currentVersion + " to " + latestVersion);
                this.installLpm(lpmHome, latestVersion);
                Scope.getCurrentScope().getUI().sendMessage("LPM has been upgraded to version " + latestVersion + " successfully.");
            } else {
                Scope.getCurrentScope().getUI().sendMessage("LPM is already up to date (version " + (currentVersion != null ? currentVersion : "unknown") + ").");
            }
        } else {
            // LPM doesn't exist, install it
            Scope.getCurrentScope().getUI().sendMessage("Installing LPM version " + latestVersion + "...");
            
            // Ensure lpmHome directory exists
            ensureDirectoryExists(lpmHome);
            
            this.installLpm(lpmHome, latestVersion);
            Scope.getCurrentScope().getUI().sendMessage("LPM has been installed successfully (version " + latestVersion + ").");
        }
    }
    
    /**
     * Gets the current version of the installed LPM by executing --version command.
     */
    private String getCurrentLpmVersion(String lpmExecutable) {
        try {
            Process process = new ProcessBuilder(lpmExecutable, "--version").start();
            try (InputStream inputStream = process.getInputStream()) {
                String output = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                if (output != null && !output.trim().isEmpty()) {
                    // Extract version number from output like "lpm version 0.2.10" or "0.2.10"
                    Pattern versionPattern = Pattern.compile("(?:version\\s+)?v?(\\d+\\.\\d+\\.\\d+(?:\\.\\d+)?)");
                    Matcher matcher = versionPattern.matcher(output);
                    if (matcher.find()) {
                        return matcher.group(1);
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).fine("Could not get current LPM version: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Compares two version strings to determine if the first is newer than the second.
     */
    boolean isNewerVersion(String version1, String version2) {
        if (version1 == null || version2 == null) {
            return version1 != null;
        }
        
        try {
            // Clean versions first to handle prefixes like "v" or suffixes like "-alpha"
            String cleanVersion1 = cleanVersionString(version1);
            String cleanVersion2 = cleanVersionString(version2);
            var v1 = ModuleDescriptor.Version.parse(cleanVersion1);
            var v2 = ModuleDescriptor.Version.parse(cleanVersion2);
            return v1.compareTo(v2) > 0;
        } catch (IllegalArgumentException e) {
            Scope.getCurrentScope().getLog(getClass()).fine("Failed to parse versions: " + e.getMessage());
            return false; // Assume not newer if parsing fails
        }
    }
    
    /**
     * Cleans version string by extracting only the numeric part with dots.
     * Handles formats like "v1.2.3-alpha" -> "1.2.3"
     */
    private String cleanVersionString(String version) {
        if (version == null) return "0";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)*)");
        java.util.regex.Matcher matcher = pattern.matcher(version);
        return matcher.find() ? matcher.group(1) : "0";
    }

    /**
     * Resolves and validates the LPM_HOME directory path from various configuration sources.
     * 
     * @param resultsBuilder The command results builder containing CLI arguments
     * @return The validated and resolved LPM_HOME path
     * @throws UnexpectedLiquibaseException if the path is invalid or inaccessible
     */
    private String validateAndResolveLpmHome(CommandResultsBuilder resultsBuilder) {
        LpmHomeResolution resolution = resolveLpmHomeFromSources(resultsBuilder);
        return validateLpmHomePath(resolution.path, resolution.configuredValue);
    }
    
    /**
     * Resolves the LPM_HOME path from various configuration sources in priority order.
     */
    private LpmHomeResolution resolveLpmHomeFromSources(CommandResultsBuilder resultsBuilder) {
        // Get LPM_HOME from CLI argument first (highest priority)
        String lpmHome = resultsBuilder.getCommandScope().getArgumentValue(LPM_HOME_ARG);
        ConfiguredValue<String> configuredValue = null;
        
        if (lpmHome == null || lpmHome.trim().isEmpty()) {
            // Try to get from configuration (env vars, properties, etc.)
            configuredValue = LPM_HOME.getCurrentConfiguredValue();
            lpmHome = configuredValue.getValue();
            
            // If still null, fall back to liquibase.home
            if (lpmHome == null || lpmHome.trim().isEmpty()) {
                configuredValue = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class)
                        .getCurrentConfiguredValue(ConfigurationValueConverter.STRING, null, "liquibase.home");
                lpmHome = configuredValue.getValue();
            }
        }
        
        if (lpmHome == null || lpmHome.trim().isEmpty()) {
            throw new UnexpectedLiquibaseException("LPM home directory is not configured. Cannot determine where to install LPM.");
        }
        
        return new LpmHomeResolution(lpmHome, configuredValue);
    }
    
    /**
     * Validates that the resolved LPM_HOME path is accessible and usable.
     */
    private String validateLpmHomePath(String lpmHome, ConfiguredValue<String> configuredValue) {
        Path lpmHomePath = Paths.get(lpmHome).toAbsolutePath();
        File lpmHomeDir = lpmHomePath.toFile();
        
        if (lpmHomeDir.exists()) {
            validateExistingDirectory(lpmHomePath, lpmHomeDir, configuredValue);
        }
        // Directory doesn't exist yet, but that's OK - we'll create it when needed
        
        return lpmHomePath.toString();
    }
    
    /**
     * Validates that an existing directory is accessible and writable.
     */
    private void validateExistingDirectory(Path lpmHomePath, File lpmHomeDir, ConfiguredValue<String> configuredValue) {
        if (!lpmHomeDir.isDirectory()) {
            String source = getConfigurationSource(configuredValue);
            throw new UnexpectedLiquibaseException(
                formatLpmErrorMessage("path '%s' set by %s is not a directory", lpmHomePath, source)
            );
        }
        if (!lpmHomeDir.canRead() || !lpmHomeDir.canWrite()) {
            String source = getConfigurationSource(configuredValue);
            throw new UnexpectedLiquibaseException(
                formatLpmErrorMessage("was not installed at path of '%s' set by %s", lpmHomePath, source)
            );
        }
    }
    
    /**
     * Simple data holder for LPM home resolution result.
     */
    private static class LpmHomeResolution {
        final String path;
        final ConfiguredValue<String> configuredValue;
        
        LpmHomeResolution(String path, ConfiguredValue<String> configuredValue) {
            this.path = path;
            this.configuredValue = configuredValue;
        }
    }
    
    /**
     * Gets a human-readable description of where a configuration value came from.
     */
    private String getConfigurationSource(ConfiguredValue<String> configuredValue) {
        if (configuredValue == null || configuredValue.getProvidedValue() == null) {
            return "default configuration";
        }
        
        String provider = configuredValue.getProvidedValue().getProvider().getClass().getSimpleName();
        
        // Map provider class names to user-friendly descriptions
        if (provider.contains("Environment")) {
            return "LIQUIBASE_LPM_HOME environment variable";
        } else if (provider.contains("SystemProperty")) {
            return "liquibase.lpm.home system property";
        } else if (provider.contains("CommandLine")) {
            return "--lpm-home command line argument";
        } else if (provider.contains("PropertiesFile")) {
            return "liquibase.lpm.home in properties file";
        } else if (provider.contains("Default")) {
            return "default configuration";
        }
        
        return "configuration";
    }

    private void checkForLpmInstallation(String lpmHome, String lpmExecutable) {
        validateLpmHomeNotEmpty(lpmHome);
        
        Scope.getCurrentScope().getLog(getClass()).fine("Checking for LPM at " + lpmExecutable);
        File lpmExecutableFile = new File(lpmExecutable);
        
        if (!lpmExecutableFile.exists() || !lpmExecutableFile.isFile()) {
            Scope.getCurrentScope().getUI().sendMessage("LPM not found at '" + lpmExecutable + "'. Installing LPM automatically...");
            
            // Ensure lpmHome directory exists
            ensureDirectoryExists(lpmHome);
            
            this.installLpm(lpmHome);
        } else {
            Scope.getCurrentScope().getLog(getClass()).info("LPM found at " + lpmExecutable);
        }
    }

    private void installLpm(String lpmHome) {
        ConfiguredValue<String> lpmVersionProperty = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class)
                .getCurrentConfiguredValue(ConfigurationValueConverter.STRING, null, "liquibase.lpmVersion");
        final String version = lpmVersionProperty != null && lpmVersionProperty.getValue() != null 
            ? lpmVersionProperty.getValue() 
            : getLatestLpmVersion();
        installLpm(lpmHome, version);
    }
    
    private void installLpm(String lpmHome, String version) {
        
        // Determine platform for download URL
        String platform = determinePlatform();
        final String lpmUrl = String.format("https://github.com/liquibase/liquibase-package-manager/releases/download/v%s/lpm-%s-%s.zip", version, version, platform);
        
        Scope.getCurrentScope().getUI().sendMessage("Downloading LPM version " + version + " for " + platform + " from " + lpmUrl);
        
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("lpm-download-", ".zip");
            FileUtils.copyURLToFile(new URL(lpmUrl), tempFile.toFile());
            this.unzipLpm(tempFile, lpmHome);
            Scope.getCurrentScope().getUI().sendMessage("LPM installation completed successfully.");
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException("Failed to download or install LPM: " + e.getMessage(), e);
        } finally {
            // Clean up temporary file
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    Scope.getCurrentScope().getLog(getClass()).warning("Could not delete temporary LPM installer: " + tempFile);
                }
            }
        }
    }
    
    /**
     * Fetches the latest LPM version from GitHub releases API.
     * Falls back to hardcoded version if API call fails.
     */
    private String getLatestLpmVersion() {
        final String fallbackVersion = "0.2.10";
        
        try {
            Scope.getCurrentScope().getLog(getClass()).fine("Fetching latest LPM version from GitHub API");
            
            String response = IOUtils.toString(new URI(LPM_DOWNLOAD_PAGE_URL), StandardCharsets.UTF_8);
            String version = parseVersionFromGitHubResponse(response);
            if (version != null) {
                Scope.getCurrentScope().getLog(getClass()).info("Using latest LPM version: " + version);
                return version;
            }
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).warning("Failed to fetch latest LPM version from GitHub: " + e.getMessage() + ", using fallback version");
        }
        
        Scope.getCurrentScope().getLog(getClass()).info("Using fallback LPM version: " + fallbackVersion);
        return fallbackVersion;
    }
    
    /**
     * Parses the version from GitHub API JSON response using simple regex.
     * Looks for "tag_name": "v1.2.3" pattern and extracts the version number.
     */
    private String parseVersionFromGitHubResponse(String jsonResponse) {
        // Simple regex to extract tag_name value from JSON
        // Matches "tag_name": "v1.2.3" or "tag_name":"v1.2.3" (with or without spaces)
        Pattern pattern = Pattern.compile("\"tag_name\"\\s*:\\s*\"v?([^\"]+)\"");
        Matcher matcher = pattern.matcher(jsonResponse);
        
        if (matcher.find()) {
            String version = matcher.group(1);
            if (version.startsWith("v")) {
                version = version.substring(1);
            }
            return version;
        }
        
        return null;
    }

    private String determinePlatform() {
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();
        
        if (osName.contains("win")) {
            return "windows";
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            if (osArch.contains("aarch64") || osArch.contains("arm")) {
                return "darwin-arm64";
            } else {
                return "darwin";
            }
        } else if (osName.contains("linux")) {
            if (osArch.contains("s390") || osArch.contains("zarch")) {
                return "s390x";
            } else if (osArch.contains("aarch64") || osArch.contains("arm64")) {
                return "linux-arm64";
            } else {
                return "linux";
            }
        } else {
            // Fallback to Linux for unknown Unix-like systems
            Scope.getCurrentScope().getLog(getClass()).warning("Unknown OS: " + osName + ", defaulting to linux platform");
            return "linux";
        }
    }

    private void unzipLpm(Path lpmZip, String lpmHome) {
        Scope.getCurrentScope().getLog(getClass()).info("Unzipping LPM to " + lpmHome);
        
        if (!Files.exists(lpmZip)) {
            throw new UnexpectedLiquibaseException("LPM zip file not found: " + lpmZip);
        }
        
        try (FileInputStream fis = new FileInputStream(lpmZip.toFile());
             ZipInputStream zis = new ZipInputStream(fis)) {
            
            ZipEntry ze;
            boolean foundExecutable = false;
            
            while ((ze = zis.getNextEntry()) != null) {
                String fileName = ze.getName();
                
                // Skip directories
                if (ze.isDirectory()) {
                    continue;
                }
                
                // Validate file path to prevent directory traversal
                File newFile = new File(lpmHome, fileName);
                String canonicalDestPath = new File(lpmHome).getCanonicalPath();
                String canonicalFilePath = newFile.getCanonicalPath();
                
                if (!canonicalFilePath.startsWith(canonicalDestPath + File.separator)) {
                    throw new UnexpectedLiquibaseException("Entry is outside of the target dir: " + fileName);
                }
                
                Scope.getCurrentScope().getLog(getClass()).fine("Extracting: " + fileName);
                
                // Create parent directories
                File parentDir = newFile.getParentFile();
                if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                    throw new UnexpectedLiquibaseException("Could not create directory: " + parentDir.getAbsolutePath());
                }

                try (var fos = new FileOutputStream(newFile)) {
                    zis.transferTo(fos);
                }

                if (fileName.toLowerCase().contains("lpm") && !fileName.contains(".") || fileName.endsWith(".exe")) {
                    newFile.setExecutable(true);
                    foundExecutable = true;
                    Scope.getCurrentScope().getLog(getClass()).info("Made executable: " + newFile.getAbsolutePath());
                }
                
                zis.closeEntry();
            }
            
            if (!foundExecutable) {
                Scope.getCurrentScope().getLog(getClass()).warning("No LPM executable found in the extracted files");
            }
            
        } catch (IOException e) {
            throw new UnexpectedLiquibaseException("Failed to extract LPM archive: " + e.getMessage(), e);
        }
    }
    
    /**
     * Builds the full path to the LPM executable based on the home directory.
     */
    private String buildLpmExecutablePath(String lpmHome) {
        String extension = System.getProperty("os.name").toLowerCase().contains("win") ? ".exe" : "";
        return lpmHome + File.separator + LPM_BINARY_NAME + extension;
    }
    
    /**
     * Validates that LPM home is not null or empty.
     */
    private void validateLpmHomeNotEmpty(String lpmHome) {
        if (lpmHome == null || lpmHome.trim().isEmpty()) {
            throw new UnexpectedLiquibaseException("LPM home directory is not configured. Cannot determine where to install LPM.");
        }
    }
    
    /**
     * Ensures that a directory exists and is writable, creating it if necessary.
     */
    private void ensureDirectoryExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new UnexpectedLiquibaseException("Cannot create LPM home directory: " + directoryPath);
        } else if (directory.exists() && !directory.canWrite()) {
            throw new UnexpectedLiquibaseException("Unable to write to LPM home directory: " + directoryPath);
        }
    }
    
    /**
     * Formats standardized LPM error messages.
     */
    private String formatLpmErrorMessage(String messageTemplate, Object... args) {
        String message = String.format(messageTemplate, args);
        return String.format("%s %s. Please check this path for access, or edit your property value. Learn more at %s",
                LPM_ERROR_PREFIX, message, DOCS_URL);
    }
}
