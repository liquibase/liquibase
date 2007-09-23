package org.liquibase.intellij.plugin;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class LiquibaseProjectComponent implements ProjectComponent {
    public LiquibaseProjectComponent(Project project) {
    }

    public void initComponent() {
        // TODO: insert component initialization logic here
    }

    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    public String getComponentName() {
        return "LiquibaseProjectComponent";
    }

    public void projectOpened() {
        // called when project is opened
    }

    public void projectClosed() {
        // called when project is being closed
    }

    public void sayHello() {
        // Show dialog with message
        Messages.showMessageDialog(
                "Hello World!",
                "Sample",
                Messages.getInformationIcon()
        );
    }
}
