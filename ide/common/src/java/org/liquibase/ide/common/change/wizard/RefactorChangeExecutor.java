package org.liquibase.ide.common.change.wizard;

import liquibase.ChangeSet;
import liquibase.DatabaseChangeLog;
import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.database.sql.SqlStatement;
import liquibase.database.template.JdbcTemplate;
import liquibase.exception.MigrationFailedException;
import liquibase.migrator.Migrator;
import liquibase.util.StringUtils;
import org.liquibase.ide.common.ChangeLogWriter;
import org.liquibase.ide.common.IdeFacade;
import org.liquibase.ide.common.ProgressMonitor;
import org.liquibase.ide.common.change.wizard.page.ChangeMetaDataWizardPage;

public class RefactorChangeExecutor {
    public void executeChangeSet(IdeFacade ide, Database database, ChangeMetaDataWizardPage metaDataPage, Change... changes) throws MigrationFailedException {

        ProgressMonitor monitor = ide.getProgressMonitor();
        Migrator migrator = ide.getMigrator(database);
        ChangeLogWriter changeLogWriter = ide.getChangeLogWriter();
        DatabaseChangeLog changeLog = ide.getRootChangeLog();

        monitor.beginTask("Refactoring Database", 100);

        ChangeSet changeSet = null;
        try {
            monitor.subTask("Checking Control Tables");
            migrator.getDatabase().checkDatabaseChangeLogTable(
                    migrator);
            migrator.getDatabase().checkDatabaseChangeLogLockTable(
                    migrator);
            monitor.worked(25);


            monitor.subTask("Executing Change");

            changeSet = new ChangeSet(metaDataPage.getId(),
                    metaDataPage.getAuthor(),
                    metaDataPage.isAlwaysRun(),
                    metaDataPage.isRunOnChange(),
                    changeLog,
                    StringUtils.trimToNull(metaDataPage.getContext()), StringUtils.trimToNull(metaDataPage.getDbms()));
            changeSet.setComments(metaDataPage.getComments());

            for (Change change : changes) {
                changeSet.addChange(change);
            }

            liquibase.database.Database liquibaseDatabase = migrator.getDatabase();
            for (Change change : changeSet.getChanges()) {
                for (SqlStatement sql : change.generateStatements(liquibaseDatabase)) {
                    new JdbcTemplate(liquibaseDatabase).execute(sql);
                }
            }
            monitor.worked(25);

            monitor.subTask("Marking Change Set As Ran");
            migrator.markChangeSetAsRan(changeSet);
            monitor.worked(25);

            monitor.subTask("Writing to Change Log");
            changeLogWriter.appendChangeSet(changeSet);

            monitor.done();
        } catch (Exception e) {
            throw new MigrationFailedException(changeSet, e);
        }

    }
}
