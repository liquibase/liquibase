package org.liquibase.intellij.plugin.change.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import dbhelp.plugin.action.portable.PortableAction;
import dbhelp.db.ui.DBTree;
import dbhelp.db.*;
import dbhelp.db.model.AbstractDBObject;

import java.awt.event.ActionEvent;

import org.liquibase.intellij.plugin.change.wizard.AddTableWizard;
import org.liquibase.intellij.plugin.LiquibaseProjectComponent;

import javax.swing.tree.TreePath;

public class AddTableAction extends BaseRefactorAction {


    public AddTableAction() {
        super("Create Table");
    }

    public void actionPerformed(ActionEvent actionEvent) {
        Project project = LiquibaseProjectComponent.getInstance().getProject();
//        LiquibaseProjectComponent liquibaseComponent = project.getComponent( LiquibaseProjectComponent.class);
//        liquibaseComponent.sayHello();
        AddTableWizard wizard = createWizard(project);

        wizard.pack();
        wizard.show();
    }

    protected AddTableWizard createWizard(Project project) {
        return new AddTableWizard(project, getSelectedDatabase(), getSelectedConnection(), getSelectedObject());
    }

//    public void actionPerformed(AnActionEvent e) {
//        Project application = ProjectManager.getInstance().getDefaultProject();
//        LiquibaseProjectComponent liquibaseComponent = application.getComponent( LiquibaseProjectComponent.class);
//        liquibaseComponent.sayHello();
//    }

}