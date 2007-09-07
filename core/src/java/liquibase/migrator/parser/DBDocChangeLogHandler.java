package liquibase.migrator.parser;

import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.Table;
import liquibase.migrator.*;
import liquibase.migrator.change.Change;
import liquibase.migrator.dbdoc.*;
import liquibase.migrator.exception.DatabaseHistoryException;
import liquibase.migrator.exception.JDBCException;
import liquibase.migrator.exception.LockException;
import liquibase.migrator.exception.MigrationFailedException;
import liquibase.util.StreamUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DBDocChangeLogHandler extends BaseChangeLogHandler {

    private SortedSet<DatabaseChangeLog> changeLogs;
    private Map<DatabaseObject, List<Change>> changesByObject;
    private Map<String, List<Change>> changesByAuthor;
    private List<Change> changesToRun;

    private File rootOutputDir;
    private HTMLWriter tableWriter;
    private HTMLWriter columnWriter;
    private HTMLWriter authorWriter;
    private ChangeLogWriter changeLogWriter;


    public DBDocChangeLogHandler(Migrator migrator, String physicalChangeLogLocation, FileOpener fileOpener) {
        super(migrator, physicalChangeLogLocation, fileOpener);

        changesByObject = new HashMap<DatabaseObject, List<Change>>();
        changesByAuthor = new HashMap<String, List<Change>>();
        changeLogs = new TreeSet<DatabaseChangeLog>();
        changesToRun = new ArrayList<Change>();

        rootOutputDir = new File("/tmp/dbdoc");
        if (!rootOutputDir.exists()) {
            rootOutputDir.mkdir();
        }

        changeLogWriter = new ChangeLogWriter(migrator.getFileOpener(), rootOutputDir);
        authorWriter = new AuthorWriter(rootOutputDir);
        tableWriter = new TableWriter(rootOutputDir);
        columnWriter = new ColumnWriter(rootOutputDir);

        this.migrator = migrator;
    }

    protected void handleChangeSet(ChangeSet changeSet) throws JDBCException, DatabaseHistoryException, MigrationFailedException, IOException {
        if (!changesByAuthor.containsKey(changeSet.getAuthor())) {
            changesByAuthor.put(changeSet.getAuthor(), new ArrayList<Change>());
        }
        for (Change change : changeSet.getChanges()) {
            changesByAuthor.get(changeSet.getAuthor()).add(change);
        }
        if (!changeLogs.contains(changeSet.getDatabaseChangeLog())) {
            changeLogs.add(changeSet.getDatabaseChangeLog());
        }

        ChangeSet.RunStatus runStatus = migrator.getRunStatus(changeSet);

        for (Change change : changeSet.getChanges()) {
            if (runStatus.equals(ChangeSet.RunStatus.NOT_RAN) || runStatus.equals(ChangeSet.RunStatus.RUN_AGAIN)) {
                changesToRun.add(change);
            }

            Set<DatabaseObject> affectedDatabaseObjects = change.getAffectedDatabaseObjects();
            if (affectedDatabaseObjects != null) {
                for (DatabaseObject dbObject : affectedDatabaseObjects) {
                    if (!changesByObject.containsKey(dbObject)) {
                        changesByObject.put(dbObject, new ArrayList<Change>());
                    }
                    changesByObject.get(dbObject).add(change);
                }
            }
        }
    }

    protected void handleIncludedChangeLog(String fileName) throws MigrationFailedException, IOException, JDBCException, LockException, LockException {
        new IncludeMigrator(fileName, migrator).validate();
    }

    public void writeHTML(Migrator migrator) throws IOException, JDBCException, DatabaseHistoryException {
        copyFile("liquibase/migrator/dbdoc/stylesheet.css");
        copyFile("liquibase/migrator/dbdoc/index.html");
        copyFile("liquibase/migrator/dbdoc/globalnav.html");
        copyFile("liquibase/migrator/dbdoc/overview-summary.html");

        DatabaseSnapshot snapshot = new DatabaseSnapshot(migrator.getDatabase());

        new ChangeLogListWriter(rootOutputDir).writeHTML(changeLogs);
        new TableListWriter(rootOutputDir).writeHTML(new TreeSet<Object>(snapshot.getTables()));
        new AuthorListWriter(rootOutputDir).writeHTML(new TreeSet<Object>(changesByAuthor.keySet()));

        for (String author : changesByAuthor.keySet()) {
            authorWriter.writeHTML(author, changesByAuthor.get(author), migrator);
        }

        for (Table table : snapshot.getTables()) {
            tableWriter.writeHTML(table, changesByObject.get(table), migrator);
        }

        for (Column column : snapshot.getColumns()) {
            columnWriter.writeHTML(column, changesByObject.get(column), migrator);
        }

        for (DatabaseChangeLog changeLog : changeLogs) {
            changeLogWriter.writeChangeLog(changeLog);
        }

    }

    private void copyFile(String fileToCopy) throws IOException {
        InputStream stylesheet = getClass().getClassLoader().getResourceAsStream(fileToCopy);
        if (stylesheet == null) {
            throw new IOException("Can not find " + fileToCopy);
        }
        FileOutputStream stylesheetOutputStream = new FileOutputStream(new File(rootOutputDir, fileToCopy.replaceFirst(".*\\/", "")), false);
        StreamUtil.copy(stylesheet, stylesheetOutputStream);
        stylesheetOutputStream.close();
    }
}
