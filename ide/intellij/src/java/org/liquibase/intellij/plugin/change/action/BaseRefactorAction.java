package org.liquibase.intellij.plugin.change.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.liquibase.intellij.plugin.action.BaseDatabaseAction;
import org.liquibase.intellij.plugin.change.wizard.BaseIntellijRefactorWizard;

import java.awt.event.ActionEvent;

public abstract class BaseRefactorAction extends BaseDatabaseAction {

    protected BaseRefactorAction(String name) {
        super(name);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        Project project = ProjectManager.getInstance().getDefaultProject();
        BaseIntellijRefactorWizard wizard = createWizard(project);

        wizard.pack();
        wizard.show();
    }

    protected abstract BaseIntellijRefactorWizard createWizard(Project project);
}
