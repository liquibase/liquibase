package org.liquibase.intellij.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.liquibase.intellij.plugin.LiquibaseProjectComponent;

public class MigrateAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Project application = ProjectManager.getInstance().getDefaultProject();
        LiquibaseProjectComponent liquibaseComponent = application.getComponent( LiquibaseProjectComponent.class);
        liquibaseComponent.migrate(null);
    }
}
