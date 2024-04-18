package liquibase.io;

import liquibase.command.CommandScope;
import liquibase.plugin.Plugin;

import java.io.IOException;

/**
 * The OutputFileHandler interface defines methods for handling log output files in Liquibase.
 */
public interface OutputFileHandler extends Plugin {

    /**
     * Returns the priority of the output file handler based on the specified output file.
     * @return the priority of the output file handler
     */
    int getPriority();

    /**
     * Creates a new output file with the specified name and sets the output stream in the command scope.
     * @param outputFile the path or name of the output file to create
     * @param commandScope the scope of the command that triggered the creation of the output file
     * @throws IOException if an I/O error occurs while creating the output file
     */
    void create(String outputFile, CommandScope commandScope) throws IOException;

    /**
     * Closes any resources associated with the output file handler.
     * @throws IOException if an I/O error occurs while closing the output stream
     */
    void close() throws IOException;
}

