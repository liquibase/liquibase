package liquibase.changelog;

import liquibase.*;
import liquibase.change.Change;
import liquibase.change.CheckSum;
import liquibase.change.core.TagDatabaseChange;
import liquibase.changelog.ChangeSet.ExecType;
import liquibase.database.Database;
import liquibase.database.OfflineConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.servicelocator.LiquibaseService;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import liquibase.statement.core.MarkChangeSetRanStatement;
import liquibase.statement.core.RemoveChangeSetRanStatusStatement;
import liquibase.statement.core.UpdateChangeSetChecksumStatement;
import liquibase.util.ISODateFormat;
import liquibase.util.LiquibaseUtil;
import liquibase.util.csv.CSVReader;
import liquibase.util.csv.CSVWriter;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@LiquibaseService(skip = true)
@SuppressWarnings("java:S899")
public class OfflineChangeLogHistoryService extends AbstractChangeLogHistoryService {

    private final File changeLogFile;
    private boolean executeDmlAgainstDatabase = true;
    /**
     * Output CREATE TABLE LIQUIBASECHANGELOG or not
     */
    private boolean executeDdlAgainstDatabase = true;

    private Integer lastChangeSetSequenceValue;
    private enum Columns {
        ID,
        AUTHOR,
        FILENAME,
        DATEEXECUTED,
        ORDEREXECUTED,
        EXECTYPE,
        MD5SUM,
        DESCRIPTION,
        COMMENTS,
        TAG,
        LIQUIBASE,
        CONTEXTS,
        LABELS,
        DEPLOYMENT_ID,
    }

    public OfflineChangeLogHistoryService(Database database, File changeLogFile, boolean executeDmlAgainstDatabase, boolean executeDdlAgainstDatabase) {
        setDatabase(database);
        this.executeDmlAgainstDatabase = executeDmlAgainstDatabase;
        this.executeDdlAgainstDatabase = executeDdlAgainstDatabase;

        changeLogFile = changeLogFile.getAbsoluteFile();
        this.changeLogFile = changeLogFile;
    }

    @Override
    public int getPriority() {
        return 500;
    }

    @Override
    public boolean supports(Database database) {
        return (database.getConnection() != null) && (database.getConnection() instanceof OfflineConnection);
    }

    public boolean isExecuteDmlAgainstDatabase() {
        return executeDmlAgainstDatabase;
    }

    public void setExecuteDmlAgainstDatabase(boolean executeDmlAgainstDatabase) {
        this.executeDmlAgainstDatabase = executeDmlAgainstDatabase;
    }

    public boolean isExecuteDdlAgainstDatabase() {
        return executeDdlAgainstDatabase;
    }

    public void setExecuteDdlAgainstDatabase(boolean executeDdlAgainstDatabase) {
        this.executeDdlAgainstDatabase = executeDdlAgainstDatabase;
    }


    @Override
    public void reset() {
        // nothing to do.
    }

    @Override
    public void init() throws DatabaseException {
        if (!changeLogFile.exists()) {
            changeLogFile.getParentFile().mkdirs();
            try {
                changeLogFile.createNewFile();
                writeHeader(changeLogFile);

                if (isExecuteDdlAgainstDatabase()) {
                    Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase()).execute(new CreateDatabaseChangeLogTableStatement());
                }


            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }

    }

    protected void writeHeader(File file) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(file.toPath());
             Writer writer = new OutputStreamWriter(outputStream,
                     GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue())
        ) {
            CSVWriter csvWriter = new CSVWriter(writer);
            String[] columns = new String[Columns.values().length];
            int i = 0;
            for (Columns column : Columns.values()) {
                columns[i++] = column.toString();
            }
            csvWriter.writeNext(columns);
        }
    }

    @Override
    public void replaceChecksum(final ChangeSet changeSet) throws DatabaseException {
        if (isExecuteDmlAgainstDatabase()) {
            Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase()).execute(new UpdateChangeSetChecksumStatement(changeSet));
        }
        replaceChangeSet(changeSet, line -> {
            line[Columns.MD5SUM.ordinal()] = changeSet.generateCheckSum(ChecksumVersion.latest()).toString();
            return line;
        });
    }

    @Override
    public List<RanChangeSet> getRanChangeSets() throws DatabaseException {
        try (
                Reader reader = new InputStreamReader(Files.newInputStream(this.changeLogFile.toPath()), GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue())
        )
        {
            CSVReader csvReader = new CSVReader(reader);
            String[] line = csvReader.readNext();

            if (line == null) { //empty file
                writeHeader(this.changeLogFile);
                return new ArrayList<>();
            }
            if (!"ID".equals(line[Columns.ID.ordinal()])) {
                throw new DatabaseException("Missing header in file "+this.changeLogFile.getAbsolutePath());
            }

            List<RanChangeSet> returnList = new ArrayList<>();
            while ((line = csvReader.readNext()) != null) {
                ContextExpression contexts = new ContextExpression();
                if (line.length > Columns.CONTEXTS.ordinal()) {
                    contexts = new ContextExpression(line[Columns.CONTEXTS.ordinal()]);
                }
                Labels labels = new Labels();
                if (line.length > Columns.LABELS.ordinal()) {
                    labels = new Labels(line[Columns.LABELS.ordinal()]);
                }

                String deploymentId = null;
                if (line.length > Columns.DEPLOYMENT_ID.ordinal()) {
                    deploymentId = line[Columns.DEPLOYMENT_ID.ordinal()];
                }

                returnList.add(new RanChangeSet(
                        line[Columns.FILENAME.ordinal()],
                        line[Columns.ID.ordinal()],
                        line[Columns.AUTHOR.ordinal()],
                        CheckSum.parse(line[Columns.MD5SUM.ordinal()]),
                        new ISODateFormat().parse(line[Columns.DATEEXECUTED.ordinal()]),
                        line[Columns.TAG.ordinal()],
                        ChangeSet.ExecType.valueOf(line[Columns.EXECTYPE.ordinal()]),
                        line[Columns.DESCRIPTION.ordinal()],
                        line[Columns.COMMENTS.ordinal()],
                        contexts,
                        labels,
                        deploymentId));
            }

            return returnList;
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    protected void replaceChangeSet(ChangeSet changeSet, ReplaceChangeSetLogic replaceLogic) throws DatabaseException {
        File oldFile = this.changeLogFile;
        File newFile = new File(oldFile.getParentFile(), oldFile.getName()+".new");

        try (
                Reader reader = new InputStreamReader(Files.newInputStream(oldFile.toPath()), GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue());
                Writer writer = new OutputStreamWriter(Files.newOutputStream(newFile.toPath()), GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue());
                CSVReader csvReader = new CSVReader(reader);
                CSVWriter csvWriter = new CSVWriter(writer)
        )
        {
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if ((changeSet == null) || (line[Columns.ID.ordinal()].equals(changeSet.getId()) && line[Columns.AUTHOR.ordinal()].equals
                    (changeSet.getAuthor()) && line[Columns.FILENAME.ordinal()].equals(changeSet.getFilePath()))) {
                    line = replaceLogic.execute(line);
                }
                if (line != null) {
                    csvWriter.writeNext(line);
                }
            }
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
        oldFile.delete();
        newFile.renameTo(oldFile);
    }

    protected void appendChangeSet(ChangeSet changeSet, ChangeSet.ExecType execType) throws DatabaseException {
        File oldFile = this.changeLogFile;
        File newFile = new File(oldFile.getParentFile(), oldFile.getName()+".new");

        try (
                Reader reader = new InputStreamReader(Files.newInputStream(oldFile.toPath()), GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue());
                Writer writer = new OutputStreamWriter(Files.newOutputStream(newFile.toPath()), GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue());
                CSVReader csvReader = new CSVReader(reader);
                CSVWriter csvWriter = new CSVWriter(writer)
        )
        {
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                csvWriter.writeNext(line);
            }

            String tag = "";
            for (Change change : changeSet.getChanges()) {
                if (change instanceof TagDatabaseChange) {
                    TagDatabaseChange tagChange = (TagDatabaseChange) change;
                    tag = tagChange.getTag();
                }
            }

            String[] newLine = new String[Columns.values().length];
            newLine[Columns.ID.ordinal()] = changeSet.getId();
            newLine[Columns.AUTHOR.ordinal()] = changeSet.getAuthor();
            newLine[Columns.FILENAME.ordinal()] =  changeSet.getFilePath();
            newLine[Columns.DATEEXECUTED.ordinal()] = new ISODateFormat().format(new java.sql.Timestamp(new Date().getTime()));
            newLine[Columns.ORDEREXECUTED.ordinal()] = String.valueOf(getNextSequenceValue());
            newLine[Columns.EXECTYPE.ordinal()] = execType.value;
            newLine[Columns.MD5SUM.ordinal()] = changeSet.generateCheckSum(ChecksumVersion.latest()).toString();
            newLine[Columns.DESCRIPTION.ordinal()] = changeSet.getDescription();
            newLine[Columns.COMMENTS.ordinal()] = changeSet.getComments();
            newLine[Columns.TAG.ordinal()] = tag;
            newLine[Columns.LIQUIBASE.ordinal()] = LiquibaseUtil.getBuildVersion();

            newLine[Columns.CONTEXTS.ordinal()] = (changeSet.getContextFilter() == null) ? null : changeSet.getContextFilter().toString();
            newLine[Columns.LABELS.ordinal()] = (changeSet.getLabels() == null) ? null : changeSet.getLabels().toString();

            newLine[Columns.DEPLOYMENT_ID.ordinal()] = getDeploymentId();

            csvWriter.writeNext(newLine);

        } catch (Exception e) {
            throw new DatabaseException(e);
        }

        oldFile.delete();
        newFile.renameTo(oldFile);
    }

    @Override
    public void setExecType(final ChangeSet changeSet, final ChangeSet.ExecType execType) throws DatabaseException {
        if (isExecuteDmlAgainstDatabase()) {
            Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase()).execute(new MarkChangeSetRanStatement(changeSet, execType));
            getDatabase().commit();
        }

        if (execType.equals(ChangeSet.ExecType.FAILED) || execType.equals(ChangeSet.ExecType.SKIPPED)) {
            return; //do nothing
        } else  if (execType.ranBefore) {
            replaceChangeSet(changeSet, line -> {
                line[Columns.DATEEXECUTED.ordinal()] = new ISODateFormat().format(new java.sql.Timestamp(new Date().getTime()));
                line[Columns.MD5SUM.ordinal()] = changeSet.generateCheckSum(
                        ChecksumVersion.enumFromChecksumVersion(changeSet.getStoredCheckSum().getVersion())).toString();
                line[Columns.EXECTYPE.ordinal()] = execType.value;
                return line;
            });
        } else {
            appendChangeSet(changeSet, execType);
        }
    }

    @Override
    public void removeFromHistory(ChangeSet changeSet) throws DatabaseException {
        if (isExecuteDmlAgainstDatabase()) {
            Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase()).execute(new RemoveChangeSetRanStatusStatement(changeSet));
            getDatabase().commit();
        }

        replaceChangeSet(changeSet, line -> null);
    }

    @Override
    public int getNextSequenceValue() throws LiquibaseException {
        if (lastChangeSetSequenceValue == null) {
            lastChangeSetSequenceValue = 0;

            try (
                    Reader reader = new InputStreamReader(Files.newInputStream(this.changeLogFile.toPath()), GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue())
            )
            {
                
                CSVReader csvReader = new CSVReader(reader);
                String[] line = csvReader.readNext(); //skip header line

                while ((line = csvReader.readNext()) != null) {
                    try {
                        lastChangeSetSequenceValue = Integer.valueOf(line[Columns.ORDEREXECUTED.ordinal()]);
                    } catch (NumberFormatException ignore) {
                        // ignore.
                    }
                }
            } catch (Exception ignore) {
                // ignore
            }
        }

        return ++lastChangeSetSequenceValue;
    }

    @Override
    public void tag(final String tagString) throws DatabaseException {
        RanChangeSet last = null;
        List<RanChangeSet> ranChangeSets = getRanChangeSets();
        if (ranChangeSets.isEmpty()) {
            ChangeSet emptyChangeSet = new ChangeSet(String.valueOf(new Date().getTime()), "liquibase", false, false, "liquibase-internal", null, null, getDatabase().getObjectQuotingStrategy(), null);
            appendChangeSet(emptyChangeSet, ExecType.EXECUTED);
            last = new RanChangeSet(emptyChangeSet);
        } else {
            last = ranChangeSets.get(ranChangeSets.size() - 1);
        }

        ChangeSet lastChangeSet = new ChangeSet(last.getId(), last.getAuthor(), false, false, last.getChangeLog(), null, null, true, null, null);
        replaceChangeSet(lastChangeSet, line -> {
            line[Columns.TAG.ordinal()] = tagString;
            return line;
        });
    }

    @Override
    public boolean tagExists(String tag) throws DatabaseException {
        List<RanChangeSet> ranChangeSets = getRanChangeSets();
        for (RanChangeSet changeset : ranChangeSets) {
            if (tag.equals(changeset.getTag())) {
                return true;
            }
        }
        return false;
    }

    private interface ReplaceChangeSetLogic {
        String[] execute(String[] line);
    }

    @Override
    public void clearAllCheckSums() throws LiquibaseException {
        replaceChangeSet(null, line -> {
            line[Columns.MD5SUM.ordinal()] = null;
            return line;
        });

    }

    @Override
    public void destroy() throws DatabaseException {
        if (changeLogFile.exists() && !changeLogFile.delete()) {
            throw new DatabaseException("Could not delete changelog history file "+changeLogFile.getAbsolutePath());
        }
    }

    @Override
    public boolean isDatabaseChecksumsCompatible() {
        return true;
    }
}
