package liquibase.checks.config;

/**
 *
 * Class used to return information from the FileAccessor
 * contents   - The original contents of the file
 * versioned  - Flag to indicate whether a version string existed
 * encode     - File contents are to be encoded on write
 *
 */
public class FileAccessorDTO {
    public String contents;
    public boolean versioned;
    public boolean encoded;
    public String warningMessage;
}