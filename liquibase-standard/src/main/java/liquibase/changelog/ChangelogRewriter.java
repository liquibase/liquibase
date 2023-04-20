package liquibase.changelog;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.OpenOptions;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangelogRewriter {

    public static final String XSD_FILE_REGEX = "([dbchangelog|liquibase-pro])-3.[0-9]?[0-9]?.xsd";
    public static final Pattern XSD_FILE_PATTERN = Pattern.compile(XSD_FILE_REGEX);
    private static final String CHANGELOG_TAG_REGEX = "(?ms).*<databaseChangeLog[^>]*>";
    private static final Pattern CHANGELOG_TAG_PATTERN = Pattern.compile(CHANGELOG_TAG_REGEX);

    /**
     *
     * Remove the changelog ID from the changelog file
     *
     * @param   changeLogFile            The changelog file we are updating
     * @param   changeLogId              The changelog ID we are removing
     * @param   databaseChangeLog        The DatabaseChangeLog object to reset the ID in
     * @return  ChangeLogRewriterResult  A result object with a message and a success flag
     *
     */
    public static ChangeLogRewriterResult removeChangeLogId(String changeLogFile, String changeLogId, DatabaseChangeLog databaseChangeLog) {
        //
        // Make changes to the changelog file
        //
        final ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();

        try {
            Resource resource = resourceAccessor.get(changeLogFile);
            String changeLogString;
            String encoding = GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue();

            try (InputStream is = resource.openInputStream()) {
                changeLogString = StreamUtil.readStreamAsString(is, encoding);
                if (changeLogFile.toLowerCase().endsWith(".xml")) {
                    //
                    // Remove the changelog ID
                    //
                    final String changeLogIdString = " changeLogId=\"" + changeLogId + "\"";
                    String editedString = changeLogString.replaceFirst(changeLogIdString, "");
                    if (editedString.equals(changeLogString)) {
                        return new ChangeLogRewriterResult("Unable to update changeLogId in changelog file '" + changeLogFile + "'", false);
                    }
                    changeLogString = editedString;
                } else if (changeLogFile.toLowerCase().endsWith(".sql")) {
                    //
                    // Formatted SQL changelog
                    //
                    String newChangeLogString = changeLogString.replaceFirst("--(\\s*)liquibase formatted sql changeLogId:(\\s*)" + changeLogId,
                            "-- liquibase formatted sql");
                    if (newChangeLogString.equals(changeLogString)) {
                        return new ChangeLogRewriterResult("Unable to update changeLogId in changelog file '" + changeLogFile + "'", false);
                    }
                    changeLogString = newChangeLogString;

                } else if (changeLogFile.toLowerCase().endsWith(".json")) {
                    //
                    // JSON changelog
                    //
                    changeLogString = changeLogString.replaceFirst("\"changeLogId\"" + ":" + "\"" + changeLogId + "\",", "\n");
                } else if (changeLogFile.toLowerCase().endsWith(".yml") || changeLogFile.toLowerCase().endsWith(".yaml")) {
                    //
                    // YAML changelog
                    //
                    changeLogString = changeLogString.replaceFirst("- changeLogId: " + changeLogId, "");
                } else {
                    return new ChangeLogRewriterResult("Changelog file '" + changeLogFile + "' is not a supported format", false);
                }
            }

            try (OutputStream outputStream = resource.openOutputStream(new OpenOptions())) {
                outputStream.write(changeLogString.getBytes(encoding));
            }

            //
            // Update the current DatabaseChangeLog with its id
            //
            if (databaseChangeLog != null) {
                databaseChangeLog.setChangeLogId(null);
            }
            String message = "The changeLogId has been removed from changelog '" + changeLogFile + "'.";
            Scope.getCurrentScope().getLog(ChangelogRewriter.class).info(message);
            return new ChangeLogRewriterResult(message, true);
        }
        catch (IOException ioe) {
            String errorMessage = "Changelog file '" + changeLogFile +
                "' with changelog ID '" + changeLogId + "' was not deactivated due to an error: " + ioe.getMessage();
            Scope.getCurrentScope().getLog(ChangelogRewriter.class).warning(errorMessage);
            return new ChangeLogRewriterResult(errorMessage, false);
        }
    }

    /**
     *
     * Add the changelog ID from the changelog file and update the XSD version
     *
     * @param   changeLogFile            The changelog file we are updating
     * @param   changeLogId              The changelog ID we are adding
     * @param   databaseChangeLog        The DatabaseChangeLog object to set the ID in
     * @return  ChangeLogRewriterResult  A result object with a message and a success flag
     *
     */
    public static ChangeLogRewriterResult addChangeLogId(String changeLogFile, String changeLogId, DatabaseChangeLog databaseChangeLog) {
        //
        // Make changes to the changelog file
        //
        final ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        try {
            Resource resource = resourceAccessor.get(changeLogFile);
            InputStream is = resource.openInputStream();
            String encoding = GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue();
            String changeLogString = StreamUtil.readStreamAsString(is, encoding);
            if (changeLogFile.toLowerCase().endsWith(".xml")) {
                Matcher matcher = CHANGELOG_TAG_PATTERN.matcher(changeLogString);
                if (matcher.find()) {
                    //
                    // Update the XSD versions
                    //
                    String header = changeLogString.substring(matcher.start(), matcher.end() - 1);
                    Matcher xsdMatcher = XSD_FILE_PATTERN.matcher(header);
                    String editedString = xsdMatcher.replaceAll("$1-" + XMLChangeLogSAXParser.getSchemaVersion() + ".xsd");

                    //
                    // Add the changeLogId attribute
                    //
                    final String outputChangeLogString = " changeLogId=\"" + changeLogId + "\"";
                    if (changeLogString.trim().endsWith("/>")) {
                        changeLogString = changeLogString.replaceFirst("/>", outputChangeLogString + "/>");
                    } else {
                        String outputHeader = editedString + outputChangeLogString + ">";
                        changeLogString = changeLogString.replaceFirst(CHANGELOG_TAG_REGEX, outputHeader);
                    }
                }
            } else if (changeLogFile.toLowerCase().endsWith(".sql")) {
                //
                // Formatted SQL changelog
                //
                String newChangeLogString = changeLogString.replaceFirst("--(\\s*)liquibase formatted sql",
                    "-- liquibase formatted sql changeLogId:" + changeLogId);
                if (newChangeLogString.equals(changeLogString)) {
                    return new ChangeLogRewriterResult("Unable to update changeLogId in changelog file '" + changeLogFile + "'", false);
                }
                changeLogString = newChangeLogString;

            } else if (changeLogFile.toLowerCase().endsWith(".json")) {
                //
                // JSON changelog
                //
                changeLogString = changeLogString.replaceFirst("\\[", "\\[\n" +
                    "\"changeLogId\"" + ":" + "\"" + changeLogId + "\",\n");
            } else if (changeLogFile.toLowerCase().endsWith(".yml") || changeLogFile.toLowerCase().endsWith(".yaml")) {
                //
                // YAML changelog
                //
                changeLogString = changeLogString.replaceFirst("^databaseChangeLog:(\\s*)\n", "databaseChangeLog:$1\n" +
                    "- changeLogId: " + changeLogId + "$1\n");
            } else {
                return new ChangeLogRewriterResult("Changelog file '" + changeLogFile + "' is not a supported format", false);
            }

            //
            // Write out the file again
            //
            try (OutputStream outputStream = resource.openOutputStream(new OpenOptions())) {
                outputStream.write(changeLogString.getBytes(encoding));
            }

            //
            // Update the current DatabaseChangeLog with its id
            //
            if (databaseChangeLog != null) {
                databaseChangeLog.setChangeLogId(changeLogId);
            }
        }
        catch (IOException ioe) {
            return new ChangeLogRewriterResult("* Changelog file '" + changeLogFile +
                "' with changelog ID '" + changeLogId + "' was not registered due to an error: " + ioe.getMessage(), false);
        }
        return new ChangeLogRewriterResult("* Changelog file '" + changeLogFile +
            "' has been updated with changelog ID '" + changeLogId + "'.", true);
    }

    public static class ChangeLogRewriterResult {
        public ChangeLogRewriterResult(String message, boolean success) {
            this.message = message;
            this.success = success;
        }

        public String message;
        public boolean success;
    }
}
