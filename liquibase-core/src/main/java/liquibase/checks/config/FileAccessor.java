package liquibase.checks.config;

import liquibase.exception.CommandExecutionException;

import java.io.IOException;

/**
 * This abstraction is written for the CheckSettingsConfigHelper, so that it does not have to deal with
 * reading and writing the files, but instead only with processing the yaml document.
 */
public interface FileAccessor {

    /**
     * Load the contents of the file located in the current working directory.
     * @param filename the name of the file
     * @return the contents of the file
     * @throws IOException if the file cannot be found, a FileNotFoundException is thrown. Other file access exceptions
     * result in IOExceptions
     */
    FileAccessorDTO loadFileContents(String filename) throws IOException;

    /**
     * Write the contents to the file specified. Any existing file with the same name is overwritten and the contents
     * replaced. If the file cannot be found, it is created.
     * @param filename the name of the file
     * @param contents the contents of the file
     */
    void writeFileContents(String filename, String contents) throws IOException, CommandExecutionException;
}
