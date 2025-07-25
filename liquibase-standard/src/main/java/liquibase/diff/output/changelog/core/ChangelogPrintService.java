package liquibase.diff.output.changelog.core;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.changelog.ChangeLogChild;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.DatabaseException;
import liquibase.plugin.Plugin;
import liquibase.resource.OpenOptions;
import liquibase.resource.Resource;
import liquibase.serializer.ChangeLogSerializer;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 *
 * Interface that supports printing change sets during generateChangelog and diffChangelog operations
 *
 */
public interface ChangelogPrintService extends Plugin {
    ChangelogPrintService setDiffChangelog(DiffToChangeLog diffToChangeLog);
    void printNew(ChangeLogSerializer changeLogSerializer, Resource file) throws ParserConfigurationException, IOException, DatabaseException;
    void printToExisting(ChangeLogSerializer changeLogSerializer, Resource file, boolean overwriteOutputFile) throws ParserConfigurationException, IOException, DatabaseException;
    void printChangeSets(ChangeLogSerializer changeLogSerializer, Resource file, List<ChangeLogChild> changeSets, OpenOptions openOptions) throws IOException;
    void print(Resource file, final PrintStream out, final ChangeLogSerializer changeLogSerializer) throws ParserConfigurationException, IOException, DatabaseException;
    int getPriority();
}