package org.liquibase.intellij.plugin.change.wizard;

import com.intellij.ide.wizard.AbstractWizard;
import com.intellij.ide.wizard.Step;
import liquibase.database.Database;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.MigrationFailedException;
import org.liquibase.ide.common.change.wizard.RefactorChangeExecutor;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;
import org.liquibase.ide.common.change.action.BaseRefactorAction;
import org.liquibase.ide.common.action.BaseDatabaseAction;
import org.liquibase.intellij.plugin.IntellijFacade;
import org.liquibase.intellij.plugin.LiquibaseProjectComponent;
import org.liquibase.intellij.plugin.change.wizard.page.ChangeMetaDataWizardPage;
import org.liquibase.intellij.plugin.change.wizard.page.IntellijRefactorWizardPage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IntellijRefactorWizard extends AbstractWizard<Step> {
    private ChangeMetaDataWizardPage metaDataPage;
    //    private Database database;
    private Database database;
    private DatabaseObject selectedObject;

    private RefactorWizard refactorWizard;
    private BaseRefactorAction action;
    private RefactorWizardPage[] pages;

    public IntellijRefactorWizard(RefactorWizard refactorWizard, DatabaseObject selectedObject, Database database, BaseRefactorAction action) {
        super(refactorWizard.getTitle(), LiquibaseProjectComponent.getInstance().getProject());
        this.refactorWizard = refactorWizard;
        this.database = database;
        this.selectedObject = selectedObject;
        this.action = action;

        pages = refactorWizard.getPages();
        for (RefactorWizardPage page : pages) {
            addStep(new IntellijRefactorWizardPage(page));
        }

        metaDataPage = new ChangeMetaDataWizardPage(LiquibaseProjectComponent.getInstance().getProject());
        addStep(metaDataPage);

        getFinishButton().addActionListener(new FinishListener());
        init();
    }

//    public IntellijRefactorWizard(String title, Project project, Database database, Connection connection, Object selectedObject, BaseRefactorWizardPage... additionalPages) {
//        super(title, project);
//        this.database = database;
//        this.connection = connection;
//        this.selectedObject = selectedObject;
//        System.out.println("Database is: " + database);
//
//        for (BaseRefactorWizardPage page : createPages()) {
//            addStep(page);
//        }
//
//        metaDataPage = new ChangeMetaDataWizardPage(project);
//        addStep(metaDataPage);
//
//        getFinishButton().addActionListener(new FinishListener());
//        init();
//    }

    public Database getDatabase() {
        return database;
    }

    protected String getHelpID() {
        return null;
    }

    private class FinishListener implements ActionListener {

        public void actionPerformed(ActionEvent actionEvent) {
            try {
                new RefactorChangeExecutor().executeChangeSet(new IntellijFacade(), database, metaDataPage, action.createChanges(selectedObject, pages));
            } catch (MigrationFailedException e) {
                e.printStackTrace();
            }
        }

        protected void refresh() {
//        if (selectedObject instanceof AbstractDBObject) {
//            ((AbstractDBObject) selectedObject).refresh();
//        }
        }
    }
}
