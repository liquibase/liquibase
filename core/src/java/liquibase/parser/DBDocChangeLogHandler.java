package liquibase.parser;

import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.Table;
import liquibase.migrator.*;
import liquibase.change.Change;
import liquibase.dbdoc.*;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.JDBCException;
import liquibase.exception.LockException;
import liquibase.exception.MigrationFailedException;
import liquibase.util.StreamUtil;
import liquibase.ChangeSet;
import liquibase.FileOpener;
import liquibase.DatabaseChangeLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DBDocChangeLogHandler extends BaseChangeLogHandler {

    private static boolean staticsInitialized = false;

    private static SortedSet<DatabaseChangeLog> changeLogs;
    private static Map<DatabaseObject, List<Change>> changesByObject;
    static private Map<String, List<Change>> changesByAuthor;

    private static Map<DatabaseObject, List<Change>> changesToRunByObject;
    private static Map<String, List<Change>> changesToRunByAuthor;
    private static List<Change> changesToRun;

    private static File rootOutputDir;
    private static HTMLWriter tableWriter;
    private static HTMLWriter columnWriter;
    private static HTMLWriter authorWriter;
    private static HTMLWriter pendingChangesWriter;
    private static HTMLWriter pendingSQLWriter;
    private static ChangeLogWriter changeLogWriter;
    private static String rootChangeLog;


    public DBDocChangeLogHandler(String outputDirectory, Migrator migrator, String physicalChangeLogLocation, FileOpener fileOpener) {
        super(migrator, physicalChangeLogLocation, fileOpener);

        if (!staticsInitialized) {
            staticsInitialized = true;

            changesByObject = new HashMap<DatabaseObject, List<Change>>();
            changesByAuthor = new HashMap<String, List<Change>>();
            changeLogs = new TreeSet<DatabaseChangeLog>();

            changesToRunByObject = new HashMap<DatabaseObject, List<Change>>();
            changesToRunByAuthor = new HashMap<String, List<Change>>();
            changesToRun = new ArrayList<Change>();

            rootOutputDir = new File(outputDirectory);
            if (!rootOutputDir.exists()) {
                rootOutputDir.mkdirs();
            }

            changeLogWriter = new ChangeLogWriter(migrator.getFileOpener(), rootOutputDir);
            authorWriter = new AuthorWriter(rootOutputDir);
            tableWriter = new TableWriter(rootOutputDir);
            columnWriter = new ColumnWriter(rootOutputDir);
            pendingChangesWriter = new PendingChangesWriter(rootOutputDir);
            pendingSQLWriter = new PendingSQLWriter(rootOutputDir);

            this.migrator = migrator;

            rootChangeLog = physicalChangeLogLocation;
        }
    }

    protected void handleChangeSet(ChangeSet changeSet) throws JDBCException, DatabaseHistoryException, MigrationFailedException, IOException {
        ChangeSet.RunStatus runStatus = migrator.getRunStatus(changeSet);

        if (!changesByAuthor.containsKey(changeSet.getAuthor())) {
            changesByAuthor.put(changeSet.getAuthor(), new ArrayList<Change>());
        }
        if (!changesToRunByAuthor.containsKey(changeSet.getAuthor())) {
            changesToRunByAuthor.put(changeSet.getAuthor(), new ArrayList<Change>());
        }

        boolean toRun = runStatus.equals(ChangeSet.RunStatus.NOT_RAN) || runStatus.equals(ChangeSet.RunStatus.RUN_AGAIN);
        for (Change change : changeSet.getChanges()) {
            if (toRun) {
                changesToRunByAuthor.get(changeSet.getAuthor()).add(change);
                changesToRun.add(change);
            } else {
                changesByAuthor.get(changeSet.getAuthor()).add(change);
            }
        }


        if (!changeLogs.contains(changeSet.getDatabaseChangeLog())) {
            changeLogs.add(changeSet.getDatabaseChangeLog());
        }

        for (Change change : changeSet.getChanges()) {
            Set<DatabaseObject> affectedDatabaseObjects = change.getAffectedDatabaseObjects();
            if (affectedDatabaseObjects != null) {
                for (DatabaseObject dbObject : affectedDatabaseObjects) {
                    if (toRun) {
                        if (!changesToRunByObject.containsKey(dbObject)) {
                            changesToRunByObject.put(dbObject, new ArrayList<Change>());
                        }
                        changesToRunByObject.get(dbObject).add(change);
                    }

                    if (!changesByObject.containsKey(dbObject)) {
                        changesByObject.put(dbObject, new ArrayList<Change>());
                    }
                    changesByObject.get(dbObject).add(change);
                }
            }
        }
    }

    protected void handleIncludedChangeLog(String fileName) throws MigrationFailedException, IOException, JDBCException, LockException {
        try {
            new IncludeMigrator(fileName, migrator).generateDocumentation(rootOutputDir.getCanonicalPath());
        } catch (DatabaseHistoryException e) {
            throw new MigrationFailedException(null, e);
        }
    }

    public void writeHTML(Migrator migrator) throws IOException, JDBCException, DatabaseHistoryException {
        copyFile("liquibase/dbdoc/stylesheet.css");
        copyFile("liquibase/dbdoc/index.html");
        copyFile("liquibase/dbdoc/globalnav.html");
        copyFile("liquibase/dbdoc/overview-summary.html");

        DatabaseSnapshot snapshot = new DatabaseSnapshot(migrator.getDatabase());

        new ChangeLogListWriter(rootOutputDir).writeHTML(changeLogs);
        new TableListWriter(rootOutputDir).writeHTML(new TreeSet<Object>(snapshot.getTables()));
        new AuthorListWriter(rootOutputDir).writeHTML(new TreeSet<Object>(changesByAuthor.keySet()));

        for (String author : changesByAuthor.keySet()) {
            authorWriter.writeHTML(author, changesByAuthor.get(author), changesToRunByAuthor.get(author), migrator, rootChangeLog);
        }

        for (Table table : snapshot.getTables()) {
            tableWriter.writeHTML(table, changesByObject.get(table), changesToRunByObject.get(table), migrator, rootChangeLog);
        }

        for (Column column : snapshot.getColumns()) {
            columnWriter.writeHTML(column, changesByObject.get(column), changesToRunByObject.get(column), migrator, rootChangeLog);
        }

        for (DatabaseChangeLog changeLog : changeLogs) {
            changeLogWriter.writeChangeLog(changeLog);
        }

        pendingChangesWriter.writeHTML("index", null, changesToRun, migrator, rootChangeLog);
        pendingSQLWriter.writeHTML("sql", null, changesToRun, migrator, rootChangeLog);

    }

    private void copyFile(String fileToCopy) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileToCopy);
        FileOutputStream outputStream = null;
        try {
            if (inputStream == null) {
                throw new IOException("Can not find " + fileToCopy);
            }
            outputStream = new FileOutputStream(new File(rootOutputDir, fileToCopy.replaceFirst(".*\\/", "")), false);
            StreamUtil.copy(inputStream, outputStream);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
}
