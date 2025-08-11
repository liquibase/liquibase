package liquibase.command.core;

import liquibase.Scope;
import liquibase.command.*;
import liquibase.configuration.ConfigurationValueConverter;
import liquibase.configuration.ConfiguredValue;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.DownloadUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public static final String[] COMMAND_NAME = {"init", "lpm"};
    
    public static final CommandArgumentDefinition<Boolean> DOWNLOAD_ARG;
    
    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        DOWNLOAD_ARG = builder.argument("download", Boolean.class)
                .description("Download and install LPM binary")
                .defaultValue(false)
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
        final String lpmHome = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class)
                .getCurrentConfiguredValue(ConfigurationValueConverter.STRING, null, "liquibase.home").getValue();
        final String lpmExecutable = lpmHome + File.separator + "lpm" + (System.getProperty("os.name").toLowerCase().contains("win") ? ".exe" : "");
        
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
            InputStream stdOut = process.getInputStream();
            try (BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(stdOut))) {
                String line;
                while ((line = stdOutReader.readLine()) != null) {
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
        if (lpmHome == null || lpmHome.trim().isEmpty()) {
            throw new UnexpectedLiquibaseException("liquibase.home is not configured. Cannot determine where to install LPM.");
        }
        
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
            File lpmHomeDir = new File(lpmHome);
            if (!lpmHomeDir.exists()) {
                throw new UnexpectedLiquibaseException("Cannot find liquibase home directory: " + lpmHome);
            } else if (!lpmHomeDir.canWrite()) {
                throw new UnexpectedLiquibaseException("Unable to write to liquibase home directory: " + lpmHome);
            }
            
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
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String output = reader.readLine();
                if (output != null) {
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
     * Assumes semantic versioning (e.g., "1.2.3").
     */
    boolean isNewerVersion(String version1, String version2) {
        if (version1 == null || version2 == null) {
            return version1 != null;
        }
        
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");
        int maxLength = Math.max(parts1.length, parts2.length);
        
        for (int i = 0; i < maxLength; i++) {
            int v1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int v2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            
            if (v1 > v2) return true;
            if (v1 < v2) return false;
        }
        
        return false; // versions are equal
    }

    private void checkForLpmInstallation(String lpmHome, String lpmExecutable) {
        if (lpmHome == null || lpmHome.trim().isEmpty()) {
            throw new UnexpectedLiquibaseException("liquibase.home is not configured. Cannot determine where to install LPM.");
        }
        
        Scope.getCurrentScope().getLog(getClass()).fine("Checking for LPM at " + lpmExecutable);
        File lpmExecutableFile = new File(lpmExecutable);
        
        if (!lpmExecutableFile.exists() || !lpmExecutableFile.isFile()) {
            Scope.getCurrentScope().getUI().sendMessage("LPM not found at '" + lpmExecutable + "'. Installing LPM automatically...");
            
            // Ensure lpmHome directory exists
            File lpmHomeDir = new File(lpmHome);
            if (!lpmHomeDir.exists() && !lpmHomeDir.mkdirs()) {
                throw new UnexpectedLiquibaseException("Cannot create liquibase home directory: " + lpmHome);
            }
            
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
        final String lpmZip = String.format("%s%slpm.zip", lpmHome, File.separator);
        
        Scope.getCurrentScope().getUI().sendMessage("Downloading LPM version " + version + " for " + platform + " from " + lpmUrl);
        
        try {
            Path downloadedFilePath = DownloadUtil.downloadToFile(lpmUrl, new File(lpmZip));
            this.unzipLpm(downloadedFilePath, lpmHome);
            try {
                Files.delete(downloadedFilePath);
            } catch (IOException e) {
                Scope.getCurrentScope().getLog(getClass()).warning("Could not delete lpm installer " + downloadedFilePath);
            }
            Scope.getCurrentScope().getUI().sendMessage("LPM installation completed successfully.");
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException("Failed to download or install LPM: " + e.getMessage(), e);
        }
    }
    
    /**
     * Fetches the latest LPM version from GitHub releases API.
     * Falls back to hardcoded version if API call fails.
     */
    String getLatestLpmVersion() {
        final String fallbackVersion = "0.2.4";
        final String apiUrl = "https://api.github.com/repos/liquibase/liquibase-package-manager/releases/latest";
        
        try {
            Scope.getCurrentScope().getLog(getClass()).fine("Fetching latest LPM version from GitHub API");
            
            String response = DownloadUtil.fetchAsString(apiUrl);
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
    String parseVersionFromGitHubResponse(String jsonResponse) {
        // Simple regex to extract tag_name value from JSON
        // Matches "tag_name": "v1.2.3" or "tag_name":"v1.2.3" (with or without spaces)
        Pattern pattern = Pattern.compile("\"tag_name\"\\s*:\\s*\"v?([^\"]+)\"");
        Matcher matcher = pattern.matcher(jsonResponse);
        
        if (matcher.find()) {
            String version = matcher.group(1);
            // Remove 'v' prefix if present
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
            if (osArch.contains("aarch64") || osArch.contains("arm64")) {
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
            byte[] buffer = new byte[1024];
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
                
                // Extract file
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
                
                // Make executable if it's the LPM binary
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
}
