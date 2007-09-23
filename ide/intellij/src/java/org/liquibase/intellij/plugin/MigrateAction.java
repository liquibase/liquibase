package org.liquibase.intellij.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

public class SayHelloAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Project application =
                ProjectManager.getInstance().getDefaultProject();
        LiquibaseProjectComponent helloWorldComponent =
                application.getComponent(
                        LiquibaseProjectComponent.class);
        helloWorldComponent.sayHello();
    }
}
