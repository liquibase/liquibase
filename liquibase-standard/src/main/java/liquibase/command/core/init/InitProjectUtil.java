package liquibase.command.core.init;

import liquibase.Scope;
import liquibase.command.CommandResultsBuilder;
import liquibase.configuration.ConfiguredValue;
import liquibase.exception.CommandExecutionException;
import liquibase.resource.OpenOptions;
import liquibase.resource.PathHandlerFactory;
import liquibase.resource.Resource;
import liquibase.util.FileUtil;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtil;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class InitProjectUtil {
    public static final String SQL = "sql";
    public static final String JSON = "json";
    public static final String YML = "yml";
    public static final String YAML = "yaml";
    public static final String XML = "xml";
    public static final String MODIFIED_DEFAULTS_FILE_CONTENTS = "modifiedDefaultsFileContents";

    /**
     * Create the project directory, including any non-existent segments
     *
     * @param projectDirFile Directory to create
     * @throws CommandExecutionException
     */
    public static void createProjectDirectory(File projectDirFile) throws CommandExecutionException {
        if (!projectDirFile.exists()) {
            boolean b = projectDirFile.mkdirs();
            if (!(b || projectDirFile.exists())) {
                String message = "Unable to create specified project directory '" + projectDirFile.getAbsolutePath() + "'.\n" +
                        "Please check permission and try 'liquibase init project' again";
                throw new CommandExecutionException(message);
            }
        }
    }

    /**
     * Make sure the changelog path does not have any path elements
     *
     * @param changelogFilePath Path to changelog
     */
    public static void validateChangelogFilePath(String changelogFilePath) {
        new FilenameGetter().validate(changelogFilePath);
    }

    /**
     * Make sure the input project directory is not a file
     *
     * @param projectDirFile The project directory File object
     * @throws CommandExecutionException
     */
    public static void validateProjectDirectory(File projectDirFile) throws CommandExecutionException {
        if (projectDirFile.exists() && projectDirFile.isFile()) {
            String message =
                    System.lineSeparator() + System.lineSeparator() + "The specified project directory '" + projectDirFile.getAbsolutePath() + "' cannot be a file" + System.lineSeparator() + System.lineSeparator();
            throw new CommandExecutionException(message);
        }
    }

    /**
     * Update the defaults file with any argument values
     *
     * @param defaultsFile                The defaults file we are updating
     * @param newDefaultsFile             True if this is a new file False if not
     * @param format                      The current format value
     * @param changelogConfig             The ConfiguredValue for changelogfile
     * @param urlConfig                   The ConfiguredValue for url
     * @param usernameConfig              The ConfiguredValue for username
     * @param passwordConfig              The ConfiguredValue for password
     * @param changelogFileCreationResult boolean indicating whether the changelogfile was copied into the project directory. If
     *                                    the file was not copied, then an empty changelogfile property is used in the defaults
     *                                    file.
     * @param shouldBackupDefaultsFile    If false, the defaults file will not be backed up and the caller will be responsible
     *                                    for both backing up the existing defaults file AND writing the new contents of the
     *                                    modified defaults file. The contents of the modified will be returned in the
     *                                    results builder.
     * @param resultsBuilder              The results builder
     * @throws IOException
     */
    public static void updateDefaultsFile(File defaultsFile, boolean newDefaultsFile, String format, ConfiguredValue<String> changelogConfig, ConfiguredValue<String> urlConfig, ConfiguredValue<String> usernameConfig, ConfiguredValue<String> passwordConfig, FileCreationResultEnum changelogFileCreationResult, Boolean shouldBackupDefaultsFile, CommandResultsBuilder resultsBuilder) throws IOException {
        String contents = FileUtil.getContents(defaultsFile);
        if (contents == null) {
            return;
        }

        String newContents = contents;
        if (changelogFileCreationResult == FileCreationResultEnum.already_existed || changelogFileCreationResult == FileCreationResultEnum.created) {
            if (!changelogConfig.wasDefaultValueUsed()) {
                String changelogPath = changelogConfig.getValue();
                if (!changelogPath.contains("." + format)) {
                    changelogPath += "." + format;
                }
                newContents = replaceProperty("changeLogFile", changelogPath, newContents, newDefaultsFile);
            } else {
                if (format.equalsIgnoreCase(YML)) {
                    newContents = replaceProperty("changeLogFile", "example-changelog.yml", newContents, newDefaultsFile);
                }
            }
        } else { // skipped changelog file creation
            newContents = replaceProperty("changeLogFile", "", newContents, true, true, false, newDefaultsFile, true);
        }
        if (!urlConfig.wasDefaultValueUsed()) {
            newContents = replaceProperty("url", urlConfig, newContents, newDefaultsFile);
        }
        if (!usernameConfig.wasDefaultValueUsed()) {
            newContents = replaceProperty("username", usernameConfig, newContents, newDefaultsFile);
        }
        if (!passwordConfig.wasDefaultValueUsed()) {
            newContents = replaceProperty("password", passwordConfig, newContents, newDefaultsFile);
        }

        //
        // If new file or no changes then just write the file and go back
        //
        if (newDefaultsFile || contents.equals(newContents)) {
            FileUtil.write(newContents, defaultsFile, false);
            return;
        }

        //
        // Make a backup and then write the new file
        //
        if (Boolean.FALSE.equals(shouldBackupDefaultsFile)) {
            // If the caller explicitly told us not to backup the defaults file, then we add the modified contents to
            // the results object.
            resultsBuilder.addResult(MODIFIED_DEFAULTS_FILE_CONTENTS, newContents);
        } else {
            File backupFile = calcBackupFile(defaultsFile);
            Resource resource = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class).getResource(defaultsFile.getAbsolutePath());
            Resource backupResource = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class).getResource(backupFile.getAbsolutePath());
            makeBackup(resource, backupResource, contents, null, false);
            FileUtil.write(newContents, defaultsFile, false);
            outputBackedUpDefaultsFileMessage(defaultsFile.getAbsolutePath());
        }
    }


    /**
     * Make a backup copy of the checks or flow file
     *
     * @param resource       The resource to back up
     * @param backupResource The resource to back up to
     * @param contents       The contents of the file
     * @param extraMessage   Extra message text to include
     * @param printMessage   Should the warning message be printed to the console? If an exception occurs, the
     *                       exception message will be printed regardless of the value of printMessage.
     */
    public static String makeBackup(Resource resource, Resource backupResource, String contents, String extraMessage, boolean printMessage) {
        //
        // Make a backup copy of the file
        //
        try {
            try (OutputStream outputStream = backupResource.openOutputStream(new OpenOptions())) {
                outputStream.write(contents.getBytes());
            }
            String message =
                    "The file '" + stripFileUriPrefix(resource.getUri()) +
                            "' has been updated so it can be used by your current version of Liquibase, and to simplify resolving merge conflicts in Source Control. No action is required from you. Your original file was backed up as '" +
                            stripFileUriPrefix(backupResource.getUri()) + "'." +
                            (extraMessage != null ? extraMessage : "");
            if (printMessage) {
                Scope.getCurrentScope().getLog(InitProjectUtil.class).warning(message);
                Scope.getCurrentScope().getUI().sendMessage("WARNING: " + message);
            }
            return message;
        } catch (IOException ioe) {
            String message = "Error creating backup file '" + resource.getUri() + "' " + ioe.getMessage();
            Scope.getCurrentScope().getLog(InitProjectUtil.class).warning(message, ioe);
            Scope.getCurrentScope().getUI().sendMessage(message);
            return null;
        }
    }

    /**
     * Print out a message to the console and logs indicating that the defaults file was backed up.
     *
     * @param absolutePath the path of the defaults file that was backed up (not the path of the backup file)
     */
    public static void outputBackedUpDefaultsFileMessage(String absolutePath) {
        String message =
                String.format("%sThe defaults file '%s' was backed up and then updated with your supplied values.%s",
                        System.lineSeparator(),
                        removeDotsFromPath(absolutePath),
                        System.lineSeparator());
        Scope.getCurrentScope().getUI().sendMessage(message);
        Scope.getCurrentScope().getLog(InitProjectUtil.class).info(message);
    }

    /**
     * Replace the contents where "key=<some value>" with the value from the ConfiguredValue
     *
     * @param key             The property key to replace
     * @param config          The ConfiguredValue of the property
     * @param contents        The current defaults file contents
     * @param newDefaultsFile Specify whether the defaults file was newly created (true), or it already existed (false)
     * @return String
     */
    public static String replaceProperty(String key, ConfiguredValue<String> config, String contents, boolean newDefaultsFile) {
        return replaceProperty(key, config.getValue(), contents, newDefaultsFile);
    }

    /**
     * Replace the contents where "key=<some value>" with the value from the ConfiguredValue
     *
     * @param key             The property key to replace
     * @param configValue     The property value
     * @param contents        The current defaults file contents
     * @param newDefaultsFile Specify whether the defaults file was newly created (true), or it already existed (false)
     * @return String
     */
    public static String replaceProperty(String key, String configValue, String contents, boolean newDefaultsFile) {
        return replaceProperty(key, configValue, contents, false, true, true, newDefaultsFile, false);
    }

    /**
     * Replace the contents where "key=<some value>" with the value from the ConfiguredValue
     *
     * @param key                      The property key to replace
     * @param configValue              The property value
     * @param contents                 The current defaults file contents
     * @param commented                If true, put in the new value with the entire line commented out with a # symbol
     * @param includeKeyPrefix         If true, the key will be prefixed with liquibase.command. unless it is found exactly
     *                                 as written in the existing contents.
     * @param addPropertyIfMissing     If true, the key specified will be added to the file contents if it does not already
     *                                 exist. If false, and the key does not already exist, it will not be added.
     * @param newDefaultsFile          Specify whether the defaults file was newly created (true), or it already existed (false)
     * @param useExistingValueIfExists If true, and the defaults file already existed and the existing value in the defaults
     *                                 file is not empty, it will remain there, untouched.
     * @return String
     */
    public static String replaceProperty(String key, String configValue, String contents, boolean commented, boolean includeKeyPrefix, boolean addPropertyIfMissing, boolean newDefaultsFile, boolean useExistingValueIfExists) {
        String newContents = contents;
        String regex = "^[\\s]*liquibase\\.command\\." + key + "[\\s]*[=:][\\s]*(.*?)$";
        String keyWithPrefix = (includeKeyPrefix ? "liquibase.command." : "") + key;
        Pattern p = Pattern.compile(regex, Pattern.DOTALL | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(contents.toLowerCase());
        if (m.find()) {
            String obfuscatedValue = obfuscateValue(configValue, key);
            String existingValue = m.group(1).trim();
            Scope.getCurrentScope().getLog(InitProjectUtil.class).info("Replacing value for 'liquibase.command." + key + "' with '" + obfuscatedValue + "'");
            if (useExistingValueIfExists && !newDefaultsFile && StringUtil.isNotEmpty(existingValue)) {
                return newContents;
            }
            newContents = newContents.replaceAll("liquibase\\.command\\.(?i)" + key + "[\\s]*([=:])[\\s]*(.*)",
                    (shouldCommentProperty(commented, newDefaultsFile, StringUtil.isEmpty(existingValue)) ? "#" : "") + keyWithPrefix + "$1" + (configValue == null ? configValue : Matcher.quoteReplacement(configValue)));
            newContents = newContents.replace(keyWithPrefix + ":" + configValue, "liquibase.command." + key + ": " + configValue);
        } else {
            regex = "^[\\s]*(?i)" + key + "[\\s]*[=:][\\s]*(.*?)$";
            p = Pattern.compile(regex, Pattern.DOTALL | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
            m = p.matcher(contents.toLowerCase());
            if (m.find()) {
                String obfuscatedValue = obfuscateValue(configValue, key);
                String existingValue = m.group(1).trim();
                if (useExistingValueIfExists && !newDefaultsFile && StringUtil.isNotEmpty(existingValue)) {
                    return newContents;
                }
                newContents = newContents.replaceAll("(?i)" + key + "[\\s]*([=:])[\\s]*(.*)",
                        (shouldCommentProperty(commented, newDefaultsFile, StringUtil.isEmpty(existingValue)) ? "#" : "") + key + "$1" + (configValue == null ? configValue : Matcher.quoteReplacement(configValue)));
                Scope.getCurrentScope().getLog(InitProjectUtil.class).info("Replacing value for '" + key + "' with '" + obfuscatedValue + "'");
                newContents = newContents.replace(key + ":" + configValue, key + ": " + configValue);
            } else {
                if (addPropertyIfMissing) {
                    Scope.getCurrentScope().getLog(InitProjectUtil.class).info("Adding property 'liquibase.command." + key + "' with value '" + configValue + "'");
                    newContents += "\n" + keyWithPrefix + "=" + configValue;
                }
            }
        }
        return newContents;
    }

    /**
     * Obfuscate the value if the corresponding key indicates some kind of secure item.
     *
     * @param configValue the value to obfuscate
     * @param key         the key which the value corresponds to
     * @return the obfuscated value
     */
    private static String obfuscateValue(String configValue, String key) {
        String obfuscatedValue = configValue;
        if (key.toLowerCase().contains("username") || key.toLowerCase().contains("password")) {
            obfuscatedValue = "*****";
        }
        if (obfuscatedValue != null) {
            obfuscatedValue = obfuscatedValue.trim();
        }
        return obfuscatedValue;
    }

    /**
     * Determine if the property should be commented out in the defaults file.
     *
     * @param commented                     If true, put in the new value with the entire line commented out with a # symbol
     * @param newDefaultsFile               Specify whether the defaults file was newly created (true), or it already existed (false)
     * @param isChangelogPropertyValueEmpty true if the existing value is null or empty
     * @return true if the value should be commented
     */
    private static boolean shouldCommentProperty(boolean commented, boolean newDefaultsFile, boolean isChangelogPropertyValueEmpty) {
        return commented && (newDefaultsFile || isChangelogPropertyValueEmpty);
    }

    /**
     * Copy the example properties file to the project directory
     * Return the File object representing the properties file
     *
     * @param format                 The format of the associated changelog
     * @param projectDirFile         The project directory File object
     * @param targetDefaultsFilename The name we will give to the copied file
     * @return File                           The new properties File object
     * @throws CommandExecutionException
     */
    public static File copyExampleProperties(String format, String projectDirFile, String targetDefaultsFilename) throws CommandExecutionException {
        try {
            String examplePropertiesPath;
            if (format == null || format.equalsIgnoreCase(SQL)) {
                examplePropertiesPath = "liquibase/examples/sql/liquibase.properties";
            } else if (format.equalsIgnoreCase(XML)) {
                examplePropertiesPath = "liquibase/examples/xml/liquibase.properties";
            } else if (format.equalsIgnoreCase(JSON)) {
                examplePropertiesPath = "liquibase/examples/json/liquibase.properties";
            } else if (format.equalsIgnoreCase(YAML) || format.equalsIgnoreCase(YML)) {
                examplePropertiesPath = "liquibase/examples/yaml/liquibase.properties";
            } else {
                throw new CommandExecutionException("Unknown format '" + format + "'");
            }
            String contents;
            try (InputStream resourceAsStream = InitProjectUtil.class.getClassLoader().getResourceAsStream(examplePropertiesPath)) {
                contents = StreamUtil.readStreamAsString(resourceAsStream);
            }
            if (StringUtil.isEmpty(contents)) {
                throw new CommandExecutionException("Unable to read the example changelog file resource.");
            }

            File targetFile = new File(projectDirFile, targetDefaultsFilename);
            FileUtil.write(contents, targetFile, false);
            String displayPath = removeDotsFromPath(targetFile.getAbsolutePath());
            String message = "Created example defaults file '" + displayPath + "'";
            Scope.getCurrentScope().getLog(InitProjectUtil.class).info(message);
            Scope.getCurrentScope().getUI().sendMessage(message);
            return targetFile;
        } catch (IOException ioe) {
            throw new CommandExecutionException("Unable to create the example properties file", ioe);
        }
    }

    /**
     * Copy the example changelog file of the format specified to the project directory if necessary
     * Return the File object representing the properties file
     *
     * @param format              The format of the changelog
     * @param projectDirFile      The project directory File object
     * @param changelogFilePath   The path to the changelog
     * @param changelogFileConfig Changelog file argument config
     * @return boolean                        True if copied and false if not
     * @throws CommandExecutionException
     */
    public static FileCreationResultEnum copyExampleChangelog(String format, File projectDirFile, String changelogFilePath, ConfiguredValue<String> changelogFileConfig)
            throws CommandExecutionException {
        try {
            //
            // If we have the file in the project directory then we do not need to copy the sample
            //
            File[] listOfChangelogs = findChangeLogsInProjectDir(projectDirFile);
            if (changelogFilePath != null && listOfChangelogs != null && listOfChangelogs.length > 0) {
                final File changelogFile = new File(projectDirFile, changelogFilePath);
                boolean foundMatch =
                        Arrays.stream(listOfChangelogs)
                                .anyMatch(f -> f.getAbsolutePath().equalsIgnoreCase(changelogFile.getAbsolutePath()));
                if (foundMatch) {
                    return FileCreationResultEnum.already_existed;
                }
            }

            //
            // Determine the path to the example resource
            //
            String exampleChangelogPath;
            if (format.equalsIgnoreCase(SQL)) {
                exampleChangelogPath = "liquibase/examples/sql/example-changelog.sql";
            } else if (format.equalsIgnoreCase(XML)) {
                exampleChangelogPath = "liquibase/examples/xml/example-changelog.xml";
            } else if (format.equalsIgnoreCase(JSON)) {
                exampleChangelogPath = "liquibase/examples/json/example-changelog.json";
            } else if (format.equalsIgnoreCase(YAML) || format.equalsIgnoreCase(YML)) {
                exampleChangelogPath = "liquibase/examples/yaml/example-changelog.yaml";
            } else {
                throw new CommandExecutionException("Unknown format '" + format + "'");
            }
            String contents;
            try (InputStream resourceAsStream = InitProjectUtil.class.getClassLoader().getResourceAsStream(exampleChangelogPath)) {
                contents = StreamUtil.readStreamAsString(resourceAsStream);
            }
            if (StringUtil.isEmpty(contents)) {
                throw new CommandExecutionException("Unable to read the example changelog file resource.");
            }

            //
            // Determine the name of the file we will write
            //
            File targetFile;
            if (changelogFilePath != null && !changelogFileConfig.wasDefaultValueUsed()) {
                if (changelogFilePath.contains("." + format)) {
                    targetFile = new File(projectDirFile, changelogFilePath);
                } else {
                    targetFile = new File(projectDirFile, changelogFilePath + "." + format);
                }
            } else {
                targetFile = new File(projectDirFile, "example-changelog" + "." + format);
            }

            //
            // Write the contents of the sample
            //
            FileUtil.write(contents, targetFile, false);
            String displayPath = removeDotsFromPath(targetFile.getAbsolutePath());
            String message = System.lineSeparator() + "Created example changelog file '" + displayPath + "'";
            Scope.getCurrentScope().getLog(InitProjectUtil.class).info(message);
            Scope.getCurrentScope().getUI().sendMessage(message);
        } catch (IOException ioe) {
            throw new CommandExecutionException("Unable to create the example changelog", ioe);
        }
        return FileCreationResultEnum.created;
    }

    /**
     * Copy the example flow files of the format specified to the project directory if they do not exist
     *
     * @param format         The format of the changelog
     * @param projectDirFile The project directory File object
     * @throws CommandExecutionException if we do not recognize the format, or we cannot create the files
     */
    public static void copyExampleFlowFiles(String format, File projectDirFile)
            throws CommandExecutionException {
        try {
            String exampleFormat = format == null ? SQL : format; //if no format was selected (the user skipped creating an example changelog) use SQL to grab the example flow files
            Map<String, String> exampleFlowFilePaths = buildFlowFilePaths(exampleFormat);
            for (Map.Entry<String, String> entry : exampleFlowFilePaths.entrySet()) {
                String fileName = entry.getKey();
                String pathToExample = entry.getValue();
                String contents;
                if (Files.exists(Paths.get(projectDirFile.getPath() + "/" + fileName))) {
                    String message = "Flow file '" + fileName + "' already exists! Skipping example generation.";
                    Scope.getCurrentScope().getLog(InitProjectUtil.class).info(message);
                    continue;
                }

                try (InputStream resourceAsStream = InitProjectUtil.class.getClassLoader().getResourceAsStream(pathToExample)) {
                    contents = StreamUtil.readStreamAsString(resourceAsStream);
                }

                if (StringUtil.isEmpty(contents)) {
                    throw new CommandExecutionException("Unable to read the example flow file resource.");
                }

                File targetFile = new File(projectDirFile, fileName);
                FileUtil.write(contents, targetFile, false);
                String displayPath = removeDotsFromPath(targetFile.getAbsolutePath());
                String message = "Created example flow file '" + displayPath + "'";
                Scope.getCurrentScope().getLog(InitProjectUtil.class).info(message);
                Scope.getCurrentScope().getUI().sendMessage(message);
            }
        } catch (IOException ioe) {
            throw new CommandExecutionException("Unable to create the example flow files", ioe);
        }
    }

    /**
     * Copy the example flow files of the format specified to the project directory if they do not exist
     *
     * @param format         The format of the changelog
     * @param projectDirFile The project directory File object
     * @throws CommandExecutionException if we do not recognize the format, or we cannot create the files
     */
    public static void copyChecksPackageFile(String format, File projectDirFile)
            throws CommandExecutionException {
        try {
            format = format == null ? SQL : format; //if no format was selected (the user skipped creating an example changelog) use SQL to grab the example flow files
            String fileName = "liquibase.checks-package.yaml";
            String contents;
            if (Files.exists(Paths.get(projectDirFile.getPath() + "/" + fileName))) {
                String message = "File '" + fileName + "' already exists! Skipping example generation.";
                Scope.getCurrentScope().getLog(InitProjectUtil.class).info(message);
                return;
            }

            try (InputStream resourceAsStream = InitProjectUtil.class.getClassLoader().getResourceAsStream(buildChecksPackagePath(format))) {
                contents = StreamUtil.readStreamAsString(resourceAsStream);
            }

            if (StringUtil.isEmpty(contents)) {
                throw new CommandExecutionException("Unable to read the example checks package resource.");
            }

            File targetFile = new File(projectDirFile, fileName);
            FileUtil.write(contents, targetFile, false);
            String displayPath = removeDotsFromPath(targetFile.getAbsolutePath());
            String message = "Created example checks package '" + displayPath + "'";
            Scope.getCurrentScope().getLog(InitProjectUtil.class).info(message);
            Scope.getCurrentScope().getUI().sendMessage(message);
        } catch (IOException ioe) {
            throw new CommandExecutionException("Unable to create the example checks package", ioe);
        }
    }

    /**
     * Builds map of filename and location of example flow files
     *
     * @param format the example file format
     * @return a map of fileNames and the string filepath to the location of the example flow file
     * @throws CommandExecutionException when the format is not recognized
     */
    private static Map<String, String> buildFlowFilePaths(String format) throws CommandExecutionException {
        List<String> flowFiles = Arrays.asList("liquibase.flowfile.yaml", "liquibase.advanced.flowfile.yaml",
                "liquibase.endstage.flow", "liquibase.flowvariables.yaml");
        String filePathPlaceholder = "liquibase/examples/%s/%s";
        String actualFormat;
        if (Arrays.asList(SQL, JSON, YAML, YML, XML).contains(format.toLowerCase())) {
            actualFormat = format.equalsIgnoreCase(YML) ? YAML : format; //Handle converting yml to yaml to match example directory (yaml)
            return flowFiles.stream()
                    .collect(Collectors.toMap(fileName -> fileName,
                            fileName -> String.format(filePathPlaceholder, actualFormat, fileName)));
        } else {
            throw new CommandExecutionException("Unknown format '" + format + "'");
        }
    }

    private static String buildChecksPackagePath(String format) throws CommandExecutionException {
        String checksPackageFilename = "liquibase.checks-package.yaml";
        String filePathPlaceholder = "liquibase/examples/%s/%s";
        String actualFormat;
        if (Arrays.asList(SQL, JSON, YAML, YML, XML).contains(format.toLowerCase())) {
            actualFormat = format.equalsIgnoreCase(YML) ? YAML : format; //Handle converting yml to yaml to match example directory (yaml)
            return String.format(filePathPlaceholder, actualFormat, checksPackageFilename);
        } else {
            throw new CommandExecutionException("Unknown format '" + format + "'");
        }
    }

    /**
     * Determine if H2 was used.
     */
    public static boolean wasH2Used(ConfiguredValue<String> urlConfig, ConfiguredValue<String> usernameConfig, ConfiguredValue<String> passwordConfig) {
        // Either the user actually used the default values (like by entering no CLI params or whatever)
        return ((urlConfig.wasDefaultValueUsed() && usernameConfig.wasDefaultValueUsed() && passwordConfig.wasDefaultValueUsed()) ||
                // Or they entered values that are identical to the default values, effectively using default values.
                (urlConfig.getValue().equals(InitProjectCommandStep.URL_ARG.getDefaultValue()) && usernameConfig.getValue().equals(InitProjectCommandStep.USERNAME_ARG.getDefaultValue()) && passwordConfig.getValue().equals(InitProjectCommandStep.PASSWORD_ARG.getDefaultValue())));
    }

    /**
     * Enum class which indicates the outcome of the attempt to create a file.
     */
    public enum FileCreationResultEnum {
        /**
         * A new file was created.
         */
        created,
        /**
         * The file already existed.
         */
        already_existed,
        /**
         * The user skipped the creation of the changelog file, and thus the file was not created.
         */
        skipped_changelog_step
    }

    public static String removeDotsFromPath(String path) {
        // replace ./ in path, only if not preceded by another .
        // this is so that directory traversal .. is not replaced
        return path.replaceAll("(?<!\\.)\\./", "").replaceAll("(?<!\\.)\\.\\\\", "");
    }

    /**
     * Given a path to a changelog file, determine the format
     * based on the extension.  If the format is unrecognized
     * then return null. If there is no extension then return the default format.
     *
     * @param changelogFilePath The path to the changelog file
     * @param defaultFormat     The default format
     * @return String
     */
    public static String determineFormatType(String changelogFilePath, String defaultFormat) {
        String extension = getExtension(changelogFilePath);
        if (extension == null) {
            if (defaultFormat != null) {
                return defaultFormat;
            }
            return null;
        }
        switch (extension) {
            case SQL:
            case JSON:
            case YML:
            case YAML:
            case XML:
                return extension;
            default:
                return null;
        }
    }

    /**
     * Return any extension, i.e. the part of the path after
     * the last '.' character
     *
     * @param changelogFilePath The path to the changelog file to check
     * @return String
     */
    public static String getExtension(String changelogFilePath) {
        if (changelogFilePath.isEmpty()) {
            return null;
        }
        String[] parts = changelogFilePath.split("\\.");
        if (parts.length == 1) {
            return null;
        }
        return parts[parts.length - 1].toLowerCase();
    }

    /**
     * Return true if a file with a name matching *changelog* is found in the project directory
     *
     * @param projectDirFile The File object for the project directory
     * @return File[]                      The list of changelog files
     */
    public static File[] findChangeLogsInProjectDir(File projectDirFile) {
        FileFilter fileFilter = file -> !file.isDirectory() && file.getName().toLowerCase().contains("changelog") && hasRecognizedExtension(file);
        return projectDirFile.listFiles(fileFilter);
    }

    /**
     * Check a file against the recognized extensions
     *
     * @param file The File object to check
     * @return boolean        True if recognized extension false if not
     */
    public static boolean hasRecognizedExtension(File file) {
        return determineFormatType(file.getName(), null) != null;
    }

    //
    // Calculate the name of the new backup file
    //
    public static File calcBackupFile(File defaultsFile) {
        int version = 1;
        File backupFile = new File(defaultsFile.getParentFile(), defaultsFile.getName() + ".backup." + newVersion(version));
        boolean looking = true;
        while (looking) {
            looking = backupFile.exists();
            if (!looking) {
                looking = false;
            } else {
                version++;
                backupFile = new File(defaultsFile.getParentFile(), defaultsFile.getName() + ".backup." + newVersion(version));
            }
        }
        return backupFile;
    }

    //
    // Given an integer representing a version then return a string of 2 characters,
    // i.e. padded with a '0' if necessary
    //
    private static String newVersion(int version) {
        if (version < 10) {
            return "0" + Integer.toString(version);
        }
        return Integer.toString(version);
    }

    /**
     * Remove the "file://" prefix from a URI if it exists.
     *
     * @param uri the URI to modify
     * @return the string of the URI with the "file://" removed or an empty string if URI is null
     */
    private static String stripFileUriPrefix(URI uri) {
        if (uri == null) {
            return "";
        }
        return uri.toString().replace("file://", "");
    }
}
