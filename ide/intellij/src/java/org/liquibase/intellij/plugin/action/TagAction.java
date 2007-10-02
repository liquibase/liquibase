package org.liquibase.intellij.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import org.liquibase.intellij.plugin.LiquibaseProjectComponent;

public class TagAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Project application = ProjectManager.getInstance().getDefaultProject();
        LiquibaseProjectComponent liquibaseComponent = application.getComponent(LiquibaseProjectComponent.class);

        String tag = Messages.showInputDialog(((Project) e.getDataContext().getData(DataConstants.PROJECT)),
                "Tag: ",
                "Enter Database Tag",
                Messages.getQuestionIcon(),
                "",
                new InputValidator() {

                    public boolean checkInput(String string) {
                        return string.trim().length() > 0;
                    }

                    public boolean canClose(String string) {
                        return true;
                    }
                });

        liquibaseComponent.tag(tag, null);
    }
}
