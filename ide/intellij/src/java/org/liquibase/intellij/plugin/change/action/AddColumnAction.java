package org.liquibase.intellij.plugin.change.action;

import com.intellij.openapi.project.Project;
import org.liquibase.intellij.plugin.change.wizard.AddColumnWizard;

public class AddColumnAction extends BaseRefactorAction {


    public AddColumnAction() {
        super("Add Column");
    }

    protected AddColumnWizard createWizard(Project project) {
        return new AddColumnWizard(project, getSelectedTable(), getSelectedConnection(), getSelectedObject());
    }
}