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

    protected AddTableWizard createWizard(Project project) {
        return new AddTableWizard(project, getSelectedDatabase(), getSelectedConnection(), getSelectedObject());
    }
}