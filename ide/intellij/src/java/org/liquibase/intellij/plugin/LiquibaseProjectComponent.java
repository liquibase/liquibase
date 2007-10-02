package org.liquibase.intellij.plugin;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import dbhelp.db.Table;
import dbhelp.db.ui.DBTree;
import dbhelp.plugin.action.portable.ActionGroup;
import dbhelp.plugin.action.portable.PopupMenuManager;
import dbhelp.plugin.idea.ProjectMain;
import liquibase.exception.JDBCException;
import liquibase.exception.LiquibaseException;
import liquibase.migrator.Migrator;
import org.jetbrains.annotations.NotNull;
import org.liquibase.intellij.plugin.change.action.AddTableAction;

import java.sql.Connection;

public class LiquibaseProjectComponent implements ProjectComponent {

    private Project project;
//    private ProjectMain projectMain;

    public LiquibaseProjectComponent(Project project) {
        this.project = project;

    }

    private void addActions() {
        //        Project application = ProjectManager.getInstance().getDefaultProject();

//        BaseComponent dbHelperComponent = application.getComponent(ProjectMain.class);

//        ProjectMain dbHelperProjectMain = ((ProjectMain) dbHelperComponent);

        ActionGroup refactorActionGroup = new ActionGroup("Refactor");
        refactorActionGroup.addAction(new AddTableAction());

        PopupMenuManager.getInstance().addAction(refactorActionGroup, DBTree.class, Table.class);

//        PopupMenuManager.getInstance().getActions(DBTree.class)
    }

    public void initComponent() {

    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return "LiquibaseProjectComponent";
    }

    public void projectOpened() {
//        ProjectMain.getInstance().getPopupMenuManager().addAction(new AddTableAction(), DBTree.class);
        addActions();        
    }

    public void projectClosed() {
        // called when project is being closed
    }

    public void migrate(Connection conn) {
        try {
            getMigrator(conn).migrate();
        } catch (LiquibaseException e) {
            displayError(e);
        }
    }

    public void tag(String tag, Connection conn) {
        try {
            getMigrator(conn).tag(tag);
        } catch (LiquibaseException e) {
            displayError(e);
        }
    }

    public void displayError(Exception e) {
        // Show dialog with message
        Messages.showErrorDialog(this.project,
                e.getMessage(),
                "LiquiBase Error");
    }

    public Migrator getMigrator(Connection connection) {
        Migrator migrator = new Migrator("changelog.xml", new IntellijFileOpener());
        try {
            migrator.init(connection);
        } catch (JDBCException e) {
            throw new RuntimeException(e);
        }
        return migrator;
    }

    public String getCurrentChangeLog() {
        return "changelog.xml";
    }
}
