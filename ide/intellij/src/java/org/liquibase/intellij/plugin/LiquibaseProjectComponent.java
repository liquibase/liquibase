package org.liquibase.intellij.plugin;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import dbhelp.db.Column;
import dbhelp.db.Table;
import dbhelp.db.ui.DBTree;
import dbhelp.plugin.action.portable.ActionGroup;
import dbhelp.plugin.action.portable.PopupMenuManager;
import dbhelp.plugin.idea.utils.IDEAUtils;
import liquibase.database.Database;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.LiquibaseException;
import liquibase.migrator.Migrator;
import liquibase.util.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.liquibase.ide.common.change.action.AddAutoIncrementAction;
import org.liquibase.ide.common.change.action.AddColumnAction;
import org.liquibase.ide.common.change.action.AddTableAction;

import java.util.HashMap;
import java.util.Map;

public class LiquibaseProjectComponent implements ProjectComponent, JDOMExternalizable {

    private static Map<Project, LiquibaseProjectComponent> _context = new HashMap<Project, LiquibaseProjectComponent>();

    private Project project;
    public String changeLogFile; //externalized
//    private ProjectMain projectMain;

    public LiquibaseProjectComponent(Project project) {
        this.project = project;

    }

    public static LiquibaseProjectComponent getInstance() {
        return _context.get(IDEAUtils.getProject());
    }


    public Project getProject() {
        return project;
    }

    private void addActions() {
        //        Project application = ProjectManager.getInstance().getDefaultProject();

//        BaseComponent dbHelperComponent = application.getComponent(ProjectMain.class);

//        ProjectMain dbHelperProjectMain = ((ProjectMain) dbHelperComponent);

        PopupMenuManager.getInstance().addAction(createRefactorMenu(liquibase.database.Database.class), DBTree.class);
        PopupMenuManager.getInstance().addAction(createRefactorMenu(liquibase.database.structure.Table.class), Table.class);
        PopupMenuManager.getInstance().addAction(createRefactorMenu(liquibase.database.structure.Column.class), Column.class);

//        PopupMenuManager.getInstance().getActions(DBTree.class)
    }

    private ActionGroup createRefactorMenu(Class dbObjectType) {
        ActionGroup refactorActionGroup = new ActionGroup("Refactor");
        refactorActionGroup.addAction(new IntellijActionWrapper(new AddTableAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new AddColumnAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new AddAutoIncrementAction(), dbObjectType));
        return refactorActionGroup;
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
        _context.put(project, this);

//        ProjectMain.getInstance().getPopupMenuManager().addAction(new AddTableAction(), DBTree.class);
        addActions();        
    }

    public void projectClosed() {
        // called when project is being closed
    }

    public void migrate(Database database) {
        try {
            getMigrator(database).migrate();
        } catch (LiquibaseException e) {
            displayError(e);
        }
    }

    public void tag(String tag, Database database) {
        try {
            getMigrator(database).tag(tag);
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

    public Migrator getMigrator(Database database) {
        Migrator migrator = new Migrator("changelog.xml", new IntellijFileOpener());
        if (database == null) {
            return migrator;
        }
        try {
            migrator.init(database);
            migrator.checkDatabaseChangeLogTable();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return migrator;
    }

    public String getCurrentChangeLog() {
        return "changelog.xml";
    }


    public String getChangeLogFile() {
        return changeLogFile;
    }

    public void setChangeLogFile(String changeLogFile) {
        this.changeLogFile = StringUtils.trimToNull(changeLogFile);
    }

    public VirtualFile getChangeLogVirtualFile() {
        return null;
    }


    public void readExternal(Element element) throws InvalidDataException {
        DefaultJDOMExternalizer.readExternal(this, element);
    }

    public void writeExternal(Element element) throws WriteExternalException {
        DefaultJDOMExternalizer.writeExternal(this, element);
    }
}
