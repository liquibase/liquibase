package liquibase.changelog;

import liquibase.Scope;
import liquibase.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.InputStreamList;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangelogRewriter {
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
        InputStreamList list = null;
        try {
            list = resourceAccessor.openStreams("", changeLogFile);
            List<URI> uris = list.getURIs();
            InputStream is = list.iterator().next();
            String encoding = GlobalConfiguration.OUTPUT_ENCODING.getCurrentValue();
            String changeLogString = StreamUtil.readStreamAsString(is, encoding);
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

            //
            // Write out the file again.  We reset the length before writing
            // because the length will not shorten otherwise
            //
            File f = new File(uris.get(0).getPath());
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(f, "rw")) {
                randomAccessFile.setLength(0);
                randomAccessFile.write(changeLogString.getBytes(encoding));
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
        finally {
            try {
                if (list != null) {
                    list.close();
                }
            }
            catch (IOException ioe) {
                // consume
            }
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
        InputStreamList list = null;
        try {
            list = resourceAccessor.openStreams("", changeLogFile);
            List<URI> uris = list.getURIs();
            InputStream is = list.iterator().next();
            String encoding = GlobalConfiguration.OUTPUT_ENCODING.getCurrentValue();
            String changeLogString = StreamUtil.readStreamAsString(is, encoding);
            if (changeLogFile.toLowerCase().endsWith(".xml")) {
                String patternString = "(?ms).*<databaseChangeLog[^>]*>";
                Pattern pattern = Pattern.compile(patternString);
                Matcher matcher = pattern.matcher(changeLogString);
                if (matcher.find()) {
                    //
                    // Update the XSD versions
                    //
                    String header = changeLogString.substring(matcher.start(), matcher.end() - 1);
                    String xsdPatternString = "([dbchangelog|liquibase-pro])-3.[0-9]?[0-9]?.xsd";
                    Pattern xsdPattern = Pattern.compile(xsdPatternString);
                    Matcher xsdMatcher = xsdPattern.matcher(header);
                    String editedString = xsdMatcher.replaceAll("$1-" + XMLChangeLogSAXParser.getSchemaVersion() + ".xsd");

                    //
                    // Add the changeLogId attribute
                    //
                    final String outputChangeLogString = " changeLogId=\"" + changeLogId + "\"";
                    if (changeLogString.trim().endsWith("/>")) {
                        changeLogString = changeLogString.replaceFirst("/>", outputChangeLogString + "/>");
                    } else {
                        String outputHeader = editedString + outputChangeLogString + ">";
                        changeLogString = changeLogString.replaceFirst(patternString, outputHeader);
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
            File f = new File(uris.get(0).getPath());
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(f, "rw")) {
                randomAccessFile.write(changeLogString.getBytes(encoding));
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
        finally {
            try {
                if (list != null) {
                    list.close();
                }
            }
            catch (IOException ioe) {
                // consume
            }
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
