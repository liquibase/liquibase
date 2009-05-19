package org.liquibase.intellij.plugin.change.wizard;

import com.intellij.ide.wizard.AbstractWizard;
import com.intellij.ide.wizard.Step;
import liquibase.database.Database;
import liquibase.database.structure.DatabaseObject;
import org.liquibase.ide.common.IdeFacade;
import org.liquibase.ide.common.WizardPage;
import org.liquibase.ide.common.change.action.BaseRefactorAction;
import org.liquibase.ide.common.change.wizard.RefactorChangeExecutor;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;
import org.liquibase.intellij.plugin.LiquibaseProjectComponent;
import org.liquibase.intellij.plugin.change.wizard.page.ChangeMetaDataWizardPage;
import org.liquibase.intellij.plugin.change.wizard.page.IntellijRefactorWizardPage;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IntellijRefactorWizard extends AbstractWizard<Step> {
    private ChangeMetaDataWizardPage metaDataPage;
    //    private Database database;
    private Database database;
    private DatabaseObject selectedObject;

    private BaseRefactorAction action;
    private RefactorWizardPage[] pages;

    public IntellijRefactorWizard(RefactorWizard refactorWizard, DatabaseObject selectedObject, Database database, BaseRefactorAction action) {
        super(refactorWizard.getTitle(), LiquibaseProjectComponent.getInstance().getProject());
        this.database = database;
        this.selectedObject = selectedObject;
        this.action = action;

        pages = refactorWizard.getPages();

        metaDataPage = new ChangeMetaDataWizardPage(LiquibaseProjectComponent.getInstance().getProject());
        addValidationListeners(metaDataPage);

        getFinishButton().addActionListener(new FinishListener());
    }

    public void setup() {
        for (final RefactorWizardPage page : pages) {
            addStep(new IntellijRefactorWizardPage(page));
            addValidationListeners(page);
        }

        addStep(metaDataPage);

        validateWizardPages();

        init();
    }

    private void addValidationListeners(WizardPage page) {
        JComponent[] validationComponents = page.getValidationComponents();
        if (validationComponents != null) {
            for (JComponent component : validationComponents) {
                if (component == null) {
                    System.out.println("Cannot add validation to null component");
                } else if (component instanceof JTextComponent) {
                    ((JTextComponent) component).getDocument().addDocumentListener(new DocumentListener() {
                        public void insertUpdate(DocumentEvent e) {
                            validateWizardPages();
                        }

                        public void removeUpdate(DocumentEvent e) {
                            validateWizardPages();
                        }

                        public void changedUpdate(DocumentEvent e) {
                            validateWizardPages();
                        }
                    });
                } else if (component instanceof JButton) {
                    ((JButton) component).addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            validateWizardPages();
                        }
                    });
                } else {
                    throw new RuntimeException("Unknown component type to watch for validation: "+component.getClass().getName());
                }
            }
        }
    }

    public Database getDatabase() {
        return database;
    }

    @Override
    protected String getHelpID() {
        return "liquibase.refactoring";
    }

    private void validateWizardPages() {
        boolean isValid = true;
        for (RefactorWizardPage page : pages) {
            if (!page.isValid()) {
                isValid = false;
            }
        }
        if (!metaDataPage.isValid()) {
            isValid = false;
        }

        getFinishButton().setEnabled(isValid);
    }

    private class FinishListener implements ActionListener {

        public void actionPerformed(ActionEvent actionEvent) {
            IdeFacade facade = LiquibaseProjectComponent.getInstance().getIdeFacade();
            try {
                new RefactorChangeExecutor().executeChangeSet(facade, database, metaDataPage, action.createChanges(selectedObject, pages));
            } catch (Exception e) {
                facade.showError("Error Executing Change", e);
            }
        }

        protected void refresh() {
//        if (selectedObject instanceof AbstractDBObject) {
//            ((AbstractDBObject) selectedObject).refresh();
//        }
        }
    }
}
