package org.liquibase.intellij.plugin.change.action;

import com.intellij.openapi.project.Project;

import java.awt.event.ActionEvent;

import org.liquibase.intellij.plugin.LiquibaseProjectComponent;
import org.liquibase.intellij.plugin.change.wizard.AddTableWizard;
import org.liquibase.intellij.plugin.change.wizard.AddAutoIncrementWizard;

public class AddAutoIncrementAction extends BaseRefactorAction {


    public AddAutoIncrementAction() {
        super("Add Auto-Increment");
    }

    protected AddAutoIncrementWizard createWizard(Project project) {
        return new AddAutoIncrementWizard(project, getSelectedColumn(), getSelectedConnection(), getSelectedObject());
    }
}