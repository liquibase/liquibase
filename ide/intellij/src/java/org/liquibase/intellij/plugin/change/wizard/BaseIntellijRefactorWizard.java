package org.liquibase.intellij.plugin.change.wizard;

import com.intellij.ide.wizard.AbstractWizard;
import com.intellij.ide.wizard.Step;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import dbhelp.db.Database;
import liquibase.ChangeSet;
import liquibase.DatabaseChangeLog;
import liquibase.change.Change;
import liquibase.database.DatabaseFactory;
import liquibase.database.sql.SqlStatement;
import liquibase.database.template.JdbcTemplate;
import liquibase.migrator.Migrator;
import liquibase.util.StringUtils;
import org.liquibase.intellij.plugin.LiquibaseProjectComponent;
import org.liquibase.intellij.plugin.change.wizard.page.ChangeMetaDataWizardPage;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.sql.Connection;

public abstract class BaseIntellijRefactorWizard extends AbstractWizard<Step> {
    private ChangeMetaDataWizardPage metaDataPage = new ChangeMetaDataWizardPage();
    private Database database;
    private Connection connection;

    public BaseIntellijRefactorWizard(String title, Project project, Database database, Connection connection) {
        super(title, project);
        this.database = database;
        this.connection = connection;
        System.out.println("Database is: " + database);

        for (Step page : createPages()) {
            addStep(page);
        }

        addStep(metaDataPage);

        getFinishButton().addActionListener(new FinishListener());
        init();
    }

    protected abstract Step[] createPages();

    protected abstract Change[] createChanges();

    public Database getDatabase() {
        return database;
    }

    public Connection getConnection() {
        return connection;
    }

    protected String getHelpID() {
        return null;
    }

    private class FinishListener implements ActionListener {

        public void actionPerformed(ActionEvent actionEvent) {
            try {
//            ProgressMonitorDialog pd = new ProgressMonitorDialog(this
//                    .getShell());
//            pd.run(false, false, new IRunnableWithProgress() {
//
//                public void run(IProgressMonitor monitor)
//                        throws InvocationTargetException, InterruptedException {
//                    monitor.beginTask("Refactoring Database", 100);
//
//                    try {
//                        Migrator migrator = new Migrator(LiquibasePreferences
//                                .getCurrentChangeLog(), new EclipseFileOpener());
//                        migrator.init(getConnection());
//
//                        monitor.subTask("Checking Control Tables");
//                        migrator.getDatabase().checkDatabaseChangeLogTable(
//                                migrator);
//                        migrator.getDatabase().checkDatabaseChangeLogLockTable(
//                                migrator);
//                        monitor.worked(25);
//
//
//                        monitor.subTask("Executing Change");
                DatabaseChangeLog changeLog = new DatabaseChangeLog(getMigrator(), getCurrentChangeLog());

                ChangeSet changeSet = new ChangeSet(StringUtils.trimToEmpty(metaDataPage.getId()),
                        StringUtils.trimToEmpty(metaDataPage.getAuthor()),
                        metaDataPage.isAlwaysRun(),
                        metaDataPage.isRunOnChange(),
                        changeLog,
                        StringUtils.trimToNull(metaDataPage.getContext()),
                        StringUtils.trimToNull(metaDataPage.getDbms()));
                changeSet.setComments(metaDataPage.getComments());

                for (Change change : createChanges()) {
                    changeSet.addChange(change);
                }

//
                liquibase.database.Database liquibaseDatabase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
                for (Change change : changeSet.getChanges()) {
                    for (SqlStatement sql : change.generateStatements(liquibaseDatabase)) {
                        new JdbcTemplate(liquibaseDatabase).execute(sql);
                    }
                }
//                        monitor.worked(25);
//
//                        monitor.subTask("Marking Change Set As Ran");
                getMigrator().markChangeSetAsRan(changeSet);
//                        monitor.worked(25);
//
//                        monitor.subTask("Writing to Change Log");
//                        DocumentBuilderFactory factory = DocumentBuilderFactory
//                                .newInstance();
//                        DocumentBuilder documentBuilder = factory
//                                .newDocumentBuilder();
//                        documentBuilder
//                                .setEntityResolver(new MigratorSchemaResolver());
//
//                        File file = new File(LiquibasePreferences
//                                .getCurrentChangeLogFileName());
//                        Document doc;
//                        if (!file.exists() || file.length() == 0) {
//                            doc = documentBuilder.newDocument();
//
//                            Element changeLogElement = doc
//                                    .createElement("databaseChangeLog");
//                            changeLogElement
//                                    .setAttribute("xmlns",
//                                            "http://www.liquibase.org/xml/ns/dbchangelog/1.2");
//                            changeLogElement
//                                    .setAttribute("xmlns:xsi",
//                                            "http://www.w3.org/2001/XMLSchema-instance");
//                            changeLogElement
//                                    .setAttribute(
//                                            "xsi:schemaLocation",
//                                            "http://www.liquibase.org/xml/ns/dbchangelog/1.2 http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-1.2.xsd");
//
//                            doc.appendChild(changeLogElement);
//                        } else {
//                            doc = documentBuilder.parse(file);
//                        }
//
//                        doc.getDocumentElement().appendChild(
//                                changeSet.createNode(doc));
//
//                        FileOutputStream out = new FileOutputStream(file);
//                        OutputFormat format = new OutputFormat(doc);
//                        format.setIndenting(true);
//                        XMLSerializer serializer = new XMLSerializer(out,
//                                format);
//                        serializer.asDOMSerializer();
//                        serializer.serialize(doc);
//
//                        monitor.done();
//                    } catch (Exception e) {
//                        throw new InvocationTargetException(e);
//                    }
//
//                }
//            });
//            refresh();
            } catch (Throwable e) {
                e.printStackTrace();
//            IStatus status = new OperationStatus(IStatus.ERROR,
//                    LiquibasePreferences.PLUGIN_ID, 1,
//                    "Error Creating Change: " + e.getMessage(), e);
//            ErrorDialog.openError(this.getShell(), "Error",
//                    "Database Change Error", status);

//                return true;
            }

//            return true;
        }
    }

    private String getCurrentChangeLog() {
        Project project = ProjectManager.getInstance().getDefaultProject();
        LiquibaseProjectComponent liquibaseComponent = project.getComponent(LiquibaseProjectComponent.class);
        return liquibaseComponent.getCurrentChangeLog();
    }

    private Migrator getMigrator() {
        Project project = ProjectManager.getInstance().getDefaultProject();
        LiquibaseProjectComponent liquibaseComponent = project.getComponent(LiquibaseProjectComponent.class);
        Migrator migrator = liquibaseComponent.getMigrator(getConnection());
        return migrator;

    }

    protected abstract void refresh();

}
