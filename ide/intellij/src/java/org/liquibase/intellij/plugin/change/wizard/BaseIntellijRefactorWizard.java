package org.liquibase.intellij.plugin.change.wizard;

import com.intellij.ide.wizard.AbstractWizard;
import com.intellij.ide.wizard.Step;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import dbhelp.db.Database;
import liquibase.ChangeSet;
import liquibase.DatabaseChangeLog;
import liquibase.change.Change;
import liquibase.database.DatabaseFactory;
import liquibase.database.sql.SqlStatement;
import liquibase.database.template.JdbcTemplate;
import liquibase.migrator.Migrator;
import liquibase.parser.MigratorSchemaResolver;
import liquibase.util.StringUtils;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.liquibase.intellij.plugin.LiquibaseProjectComponent;
import org.liquibase.intellij.plugin.change.wizard.page.ChangeMetaDataWizardPage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;

public abstract class BaseIntellijRefactorWizard extends AbstractWizard<Step> {
    private ChangeMetaDataWizardPage metaDataPage;
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

        metaDataPage = new ChangeMetaDataWizardPage(project);
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
//                        Migrator migrator = LiquibaseProjectComponent.getInstance().getMigrator(getConnection());
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

                final ChangeSet changeSet = new ChangeSet(StringUtils.trimToEmpty(metaDataPage.getId()),
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
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        try {
                            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
                            documentBuilder.setEntityResolver(new MigratorSchemaResolver());

                            VirtualFile file = LiquibaseProjectComponent.getInstance().getChangeLogFile().getVirtualFile();
                            Document doc;
                            if (!file.isValid() || file.getLength() == 0) {
                                doc = documentBuilder.newDocument();

                                Element changeLogElement = doc.createElement("databaseChangeLog");
                                changeLogElement.setAttribute("xmlns", "http://www.liquibase.org/xml/ns/dbchangelog/1.3");
                                changeLogElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
                                changeLogElement.setAttribute("xsi:schemaLocation", "http://www.liquibase.org/xml/ns/dbchangelog/1.3 http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-1.3.xsd");

                                doc.appendChild(changeLogElement);
                            } else {
                                doc = documentBuilder.parse(file.getInputStream());
                            }

                            doc.getDocumentElement().appendChild(changeSet.createNode(doc));

                            OutputFormat format = new OutputFormat(doc);
                            format.setIndenting(true);
                            XMLSerializer serializer = new XMLSerializer(file.getOutputStream(null), format);
                            serializer.asDOMSerializer();
                            serializer.serialize(doc);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
//
//                        monitor.done();
//                    } catch (Exception e) {
//                        throw new InvocationTargetException(e);
//                    }
//
//                }
//            });
                refresh();
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
        LiquibaseProjectComponent liquibaseComponent = LiquibaseProjectComponent.getInstance();
        return liquibaseComponent.getCurrentChangeLog();
    }

    private Migrator getMigrator() {
        LiquibaseProjectComponent liquibaseComponent = LiquibaseProjectComponent.getInstance();
        return liquibaseComponent.getMigrator(getConnection());

    }

    protected abstract void refresh();

}
