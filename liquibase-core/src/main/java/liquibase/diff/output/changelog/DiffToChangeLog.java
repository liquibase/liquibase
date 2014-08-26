package liquibase.diff.output.changelog;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.ChangeLogSerializerFactory;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;
import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectComparator;
import liquibase.util.DependencyGraph;
import liquibase.util.StringUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class DiffToChangeLog {

    private String idRoot = String.valueOf(new Date().getTime());
    private int changeNumber = 1;

    private String changeSetContext;
    private String changeSetAuthor;
    private String changeSetPath;
    private DiffResult diffResult;
    private DiffOutputControl diffOutputControl;


    public DiffToChangeLog(DiffResult diffResult, DiffOutputControl diffOutputControl) {
        this.diffResult = diffResult;
        this.diffOutputControl = diffOutputControl;
    }

    public DiffToChangeLog(DiffOutputControl diffOutputControl) {
        this.diffOutputControl = diffOutputControl;
    }

    public void setDiffResult(DiffResult diffResult) {
        this.diffResult = diffResult;
    }

    public void setChangeSetContext(String changeSetContext) {
        this.changeSetContext = changeSetContext;
    }

    public void print(String changeLogFile) throws ParserConfigurationException, IOException, DatabaseException {
        ChangeLogSerializer changeLogSerializer = ChangeLogSerializerFactory.getInstance().getSerializer(changeLogFile);
        this.print(changeLogFile, changeLogSerializer);
    }

    public void print(PrintStream out) throws ParserConfigurationException, IOException, DatabaseException {
        this.print(out, new XMLChangeLogSerializer());
    }

    public void print(String changeLogFile, ChangeLogSerializer changeLogSerializer) throws ParserConfigurationException, IOException, DatabaseException {
        File file = new File(changeLogFile);
        if (!file.exists()) {
            LogFactory.getLogger().info(file + " does not exist, creating");
            FileOutputStream stream = new FileOutputStream(file);
            print(new PrintStream(stream), changeLogSerializer);
            stream.close();
        } else {
            LogFactory.getLogger().info(file + " exists, appending");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            print(new PrintStream(out), changeLogSerializer);

            String xml = new String(out.toByteArray());
            xml = xml.replaceFirst("(?ms).*<databaseChangeLog[^>]*>", "");
            xml = xml.replaceFirst("</databaseChangeLog>", "");
            xml = xml.trim();
            if ("".equals(xml)) {
                LogFactory.getLogger().info("No changes found, nothing to do");
                return;
            }

            String lineSeparator = LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputLineSeparator();
            BufferedReader fileReader = new BufferedReader(new FileReader(file));
            String line;
            long offset = 0;
            while ((line = fileReader.readLine()) != null) {
                int index = line.indexOf("</databaseChangeLog>");
                if (index >= 0) {
                    offset += index;
                } else {
                    offset += line.getBytes().length;
                    offset += lineSeparator.getBytes().length;
                }
            }
            fileReader.close();

            // System.out.println("resulting XML: " + xml.trim());

            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(offset);
            randomAccessFile.writeBytes("    ");
            randomAccessFile.write(xml.getBytes());
            randomAccessFile.writeBytes(lineSeparator);
            randomAccessFile.writeBytes("</databaseChangeLog>" + lineSeparator);
            randomAccessFile.close();

            // BufferedWriter fileWriter = new BufferedWriter(new
            // FileWriter(file));
            // fileWriter.append(xml);
            // fileWriter.close();
        }
    }

    /**
     * Prints changeLog that would bring the target database to be the same as
     * the reference database
     */
    public void print(PrintStream out, ChangeLogSerializer changeLogSerializer) throws ParserConfigurationException, IOException, DatabaseException {

        List<ChangeSet> changeSets = generateChangeSets();

        changeLogSerializer.write(changeSets, out);

        out.flush();
    }

    public List<ChangeSet> generateChangeSets() {
        final ChangeGeneratorFactory changeGeneratorFactory = ChangeGeneratorFactory.getInstance();
        DatabaseObjectComparator comparator = new DatabaseObjectComparator();

        List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
        Set<Class<? extends DatabaseObject>> typesToInclude = diffResult.getReferenceSnapshot().getSnapshotControl().getTypesToInclude();
        List<Class<? extends DatabaseObject>> types = new DependencyGraph(MissingObjectChangeGenerator.class, diffResult.getReferenceSnapshot().getDatabase()).getOrderedOutputTypes(typesToInclude);
        for (Class<? extends DatabaseObject> type : types) {
            ObjectQuotingStrategy quotingStrategy = ObjectQuotingStrategy.QUOTE_ALL_OBJECTS;
            for (DatabaseObject object : diffResult.getMissingObjects(type, comparator)) {
                if (object == null) {
                    continue;
                }
                if (!diffResult.getReferenceSnapshot().getDatabase().isLiquibaseObject(object) && !diffResult.getReferenceSnapshot().getDatabase().isSystemObject(object)) {
                    Change[] changes = changeGeneratorFactory.fixMissing(object, diffOutputControl, diffResult.getReferenceSnapshot().getDatabase(), diffResult.getComparisonSnapshot().getDatabase());
                    addToChangeSets(changes, changeSets, quotingStrategy);
                }
            }
        }

        types = new DependencyGraph(UnexpectedObjectChangeGenerator.class, diffResult.getReferenceSnapshot().getDatabase()).getOrderedOutputTypes(types);
        for (Class<? extends DatabaseObject> type : types) {
            ObjectQuotingStrategy quotingStrategy = ObjectQuotingStrategy.QUOTE_ALL_OBJECTS;
            for (DatabaseObject object : diffResult.getUnexpectedObjects(type, comparator)) {
                if (!diffResult.getComparisonSnapshot().getDatabase().isLiquibaseObject(object) && !diffResult.getComparisonSnapshot().getDatabase().isSystemObject(object)) {
                    Change[] changes = changeGeneratorFactory.fixUnexpected(object, diffOutputControl, diffResult.getReferenceSnapshot().getDatabase(), diffResult.getComparisonSnapshot().getDatabase());
                    addToChangeSets(changes, changeSets, quotingStrategy);
                }
            }
        }

        types = new DependencyGraph(ChangedObjectChangeGenerator.class, diffResult.getReferenceSnapshot().getDatabase()).getOrderedOutputTypes(typesToInclude);
        for (Class<? extends DatabaseObject> type : types) {
            ObjectQuotingStrategy quotingStrategy = ObjectQuotingStrategy.QUOTE_ALL_OBJECTS;
            for (Map.Entry<? extends DatabaseObject, ObjectDifferences> entry : diffResult.getChangedObjects(type, comparator).entrySet()) {
                if (!diffResult.getReferenceSnapshot().getDatabase().isLiquibaseObject(entry.getKey()) && !diffResult.getReferenceSnapshot().getDatabase().isSystemObject(entry.getKey())) {
                    Change[] changes = changeGeneratorFactory.fixChanged(entry.getKey(), entry.getValue(), diffOutputControl, diffResult.getReferenceSnapshot().getDatabase(), diffResult.getComparisonSnapshot().getDatabase());
                    addToChangeSets(changes, changeSets, quotingStrategy);
                }
            }
        }
        return changeSets;
    }


    private void addToChangeSets(Change[] changes, List<ChangeSet> changeSets, ObjectQuotingStrategy quotingStrategy) {
        if (changes != null) {
            ChangeSet changeSet = new ChangeSet(generateId(), getChangeSetAuthor(), false, false, null, changeSetContext,
                    null, quotingStrategy, null);
            for (Change change : changes) {
                changeSet.addChange(change);
            }
            changeSets.add(changeSet);
        }
    }

    protected String getChangeSetAuthor() {
        if (changeSetAuthor != null) {
            return changeSetAuthor;
        }
        String author = System.getProperty("user.name");
        if (StringUtils.trimToNull(author) == null) {
            return "diff-generated";
        } else {
            return author + " (generated)";
        }
    }

    public void setChangeSetAuthor(String changeSetAuthor) {
        this.changeSetAuthor = changeSetAuthor;
    }

    public String getChangeSetPath() {
        return changeSetPath;
    }

    public void setChangeSetPath(String changeSetPath) {
        this.changeSetPath = changeSetPath;
    }

    public void setIdRoot(String idRoot) {
        this.idRoot = idRoot;
    }

    protected String generateId() {
        return idRoot + "-" + changeNumber++;
    }

}
