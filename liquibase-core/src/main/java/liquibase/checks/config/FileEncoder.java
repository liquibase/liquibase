package liquibase.checks.config;

import liquibase.Scope;

import java.util.Base64;

/**
 * This class handles encoding and decoding the quality checks settings files. The QC settings file is encoded as a
 * Base64 string with an unencoded header that explains the purpose of the file.
 */
@Deprecated
public class FileEncoder {

    /**
     * The QC settings file version
     */
    public static final String QUALITY_CHECKS_PREFIX = "Quality Checks Version:";
    public static final String QUALITY_CHECKS_VERSION = "1.0";
    /**
     * DO NOT CHANGE THE SEPARATOR.
     * This separator should not be changed because future versions of Liquibase should be able to decode previous versions
     * config files, and changing the separator will break the logic which strips the header text.
     */
    private static final String SEPARATOR = "-----------------------------------------------------------------------------------------------";

    public static final String FILE_HEADER_LINUX =
            QUALITY_CHECKS_PREFIX + " " + QUALITY_CHECKS_VERSION + "\n" +
            "IMPORTANT: DO NOT MODIFY THIS FILE DIRECTLY. UNRECOVERABLE and UNSUPPORTED ERRORS ARE LIKELY IF YOU EDIT THIS FILE DIRECTLY.\n" +
            "This file is created and modified by running commands in the Liquibase CLI.\n" +
            "For help using quality checks, visit the documentation at https://docs.liquibase.com/quality-checks\n\n"+
            SEPARATOR + "\n";

    /**
     * Decode the provided contents.
     * @param contents the previously encoded contents to be decoded
     * @return the decoded contents
     */
    public static FileEncoderDTO decode(String contents) {
        FileEncoderDTO dto = new FileEncoderDTO();
        String loadedContents;
        if (!isVersioned(contents)) {
            Scope.getCurrentScope().getLog(FileEncoder.class).info("The contents of this settings file are not versioned");
        }
        try {
            loadedContents = new String(Base64.getDecoder().decode(stripHeaderFromString(contents)));
            dto.encoded = true;
        } catch (Exception e) {
            loadedContents = stripHeaderFromString(contents);
            dto.encoded = false;
        }
        dto.contents = loadedContents;
        return dto;
    }

    /**
     * Return only the actual config part of the file, stripping away the header text, the separator dashes, and the
     * line ending after the separator dashes.
     */
    private static String stripHeaderFromString(String contents) {
        String lineWithoutDashes = contents.substring(contents.indexOf(SEPARATOR) + SEPARATOR.length());
        if (lineWithoutDashes.startsWith("\n")) {
            return lineWithoutDashes.replaceFirst("\n", "");
        } else if (lineWithoutDashes.startsWith("\r\n")) {
            return lineWithoutDashes.replaceFirst("\r\n", "");
        } else {
            return lineWithoutDashes;
        }
    } 

    /**
     *
     * Check the contents to see if there is a version string
     *
     * @param    contents     String to check
     * @return   boolean      True if there is a version string False if not
     *
     */
    public static boolean isVersioned(String contents) {
        return contents.contains(QUALITY_CHECKS_PREFIX);
    }

    /**
     * Object used to communicate information back from the loading of the file contents
     */
    public static class FileEncoderDTO {
        /**
         * The original contents of the file
         */
        public String contents;
        /**
         * File contents are to be encoded on write
         */
        public boolean encoded;
    }
}