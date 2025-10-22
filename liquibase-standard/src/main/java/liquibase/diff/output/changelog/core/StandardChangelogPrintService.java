package liquibase.diff.output.changelog.core;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.changelog.ChangeLogChild;
import liquibase.changelog.ChangeSet;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.DatabaseException;
import liquibase.resource.OpenOptions;
import liquibase.resource.Resource;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.util.StreamUtil;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Community implementation of the ChangelogPrintService, which supports print
 * operations that occurring GenerateChangeLog and DiffChangelog operations
 *
 */
public class StandardChangelogPrintService implements ChangelogPrintService {
    protected DiffToChangeLog diffToChangeLog;
    public StandardChangelogPrintService() {
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public ChangelogPrintService setDiffChangelog(DiffToChangeLog diffToChangeLog) {
        this.diffToChangeLog = diffToChangeLog;
        return this;
    }

    /**
     *
     * Print change sets to a new changelog file during generateChangelog and diffChangelog
     *
     * @param   changeLogSerializer                 The ChangeLogSerializer used to write the change sets
     * @param   file                                The changelog file to write
     * @throws  ParserConfigurationException        Thrown
     * @throws  IOException                         Thrown
     * @throws  DatabaseException                   Thrown
     *
     */
    @Override
    public void printNew(ChangeLogSerializer changeLogSerializer, Resource file) throws ParserConfigurationException, IOException, DatabaseException {
        List<ChangeSet> changeSets = diffToChangeLog.generateChangeSets();
        List<ChangeLogChild> changeLogChildren = new ArrayList<>(changeSets);

        printChangeSets(changeLogSerializer, file, changeLogChildren, new OpenOptions());
    }

    /**
     *
     * Append new change sets to an existing changelog
     *
     * @param   changeLogSerializer                 The ChangeLogSerializer used to write the change sets
     * @param   file                                The changelog file to write
     * @param   overwriteOutputFile                 Overwrite the existing file if true
     * @throws  ParserConfigurationException        Thrown
     * @throws  IOException                         Thrown
     * @throws  DatabaseException                   Thrown
     *
     */
    @Override
    public void printToExisting(ChangeLogSerializer changeLogSerializer, Resource file, boolean overwriteOutputFile) throws ParserConfigurationException, IOException, DatabaseException {
        StringBuilder fileContents = new StringBuilder();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        print(file, new PrintStream(out, true, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()), changeLogSerializer);

        String xml = out.toString(GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue());
        if (overwriteOutputFile) {
            // write xml contents to file
            Scope.getCurrentScope().getLog(getClass()).info(file.getUri() + " exists, overwriting");
            fileContents.append(xml);
        } else {
            // read existing file
            Scope.getCurrentScope().getLog(getClass()).info(file.getUri() + " exists, appending");
            fileContents = new StringBuilder(StreamUtil.readStreamAsString(file.openInputStream()));

            String innerXml = xml.replaceFirst("(?ms).*<databaseChangeLog[^>]*>", "");

            innerXml = innerXml.replaceFirst(DiffToChangeLog.DATABASE_CHANGE_LOG_CLOSING_XML_TAG, "");
            innerXml = innerXml.trim();
            if (innerXml.isEmpty()) {
                Scope.getCurrentScope().getLog(getClass()).info("No changes found, nothing to do");
                return;
            }

            // insert new XML
            int endTagIndex = fileContents.indexOf(DiffToChangeLog.DATABASE_CHANGE_LOG_CLOSING_XML_TAG);
            if (endTagIndex == -1) {
                fileContents.append(xml);
            } else {
                String lineSeparator = GlobalConfiguration.OUTPUT_LINE_SEPARATOR.getCurrentValue();
                String toInsert = "    " + innerXml + lineSeparator;
                fileContents.insert(endTagIndex, toInsert);
            }
        }

        try (OutputStream outputStream = file.openOutputStream(new OpenOptions())) {
            outputStream.write(fileContents.toString().getBytes());
        }
    }

    /**
     *
     * Write passed ChangeLogChild objects to the changelog.  The ChangeLogChild type allows the list fo
     * hold both ChangeSet and Include object types
     *
     * @param   changeLogSerializer                 The ChangeLogSerializer used to write the change sets
     * @param   file                                The changelog file to write
     * @param   changelogChildren                   The List of ChangelogChild to print
     * @param   openOptions                         OpenOptions to use when opening the file
     * @throws  IOException                         Thrown
     *
     */
    @Override
    public void printChangeSets(ChangeLogSerializer changeLogSerializer, Resource file, List<ChangeLogChild> changelogChildren, OpenOptions openOptions) throws IOException {
        Scope.getCurrentScope().getLog(getClass()).info("changeSets count: " + changelogChildren.size());
        if (changelogChildren.isEmpty()) {
            Scope.getCurrentScope().getLog(getClass()).info("No changesets to add to the changelog output.");
        } else {
            Scope.getCurrentScope().getLog(getClass()).info(file + " does not exist, creating and adding " + changelogChildren.size() + " changesets.");
            try (OutputStream stream = file.openOutputStream(openOptions);
                 PrintStream out = new PrintStream(stream, true, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue())) {
                changeLogSerializer.write(changelogChildren, out);
            }
        }
    }

    /**
     *
     * @param   ignored                                        Not used
     * @param   out                                            The stream to write to
     * @param   changeLogSerializer                            The ChangeLogSerializer used to write the change sets
     * @throws  ParserConfigurationException                   Thrown
     * @throws  IOException                                    Thrown
     * @throws  DatabaseException                              Thrown
     *
     */
    @Override
    public void print(Resource ignored, PrintStream out, ChangeLogSerializer changeLogSerializer) throws ParserConfigurationException, IOException, DatabaseException {
        diffToChangeLog.print(new PrintStream(out, true, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()), changeLogSerializer);
    }
}