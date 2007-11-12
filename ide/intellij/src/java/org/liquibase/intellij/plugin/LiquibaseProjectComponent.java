package org.liquibase.intellij.plugin;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import dbhelp.db.Catalog;
import dbhelp.db.Column;
import dbhelp.db.Schema;
import dbhelp.db.Table;
import dbhelp.plugin.action.portable.ActionGroup;
import dbhelp.plugin.action.portable.PopupMenuManager;
import dbhelp.plugin.idea.utils.IDEAUtils;
import liquibase.CompositeFileOpener;
import liquibase.FileSystemFileOpener;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.migrator.Migrator;
import liquibase.util.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.liquibase.ide.common.action.MigrateDatabaseAction;
import org.liquibase.ide.common.change.action.*;

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

        PopupMenuManager.getInstance().addAction(createLiquibaseMenu(liquibase.database.Database.class), Catalog.class);

        PopupMenuManager.getInstance().addAction(createRefactorMenu(liquibase.database.Database.class), Catalog.class);
        PopupMenuManager.getInstance().addAction(createRefactorMenu(liquibase.database.Database.class), Schema.class);
        PopupMenuManager.getInstance().addAction(createRefactorMenu(liquibase.database.structure.Table.class), Table.class);
        PopupMenuManager.getInstance().addAction(createRefactorMenu(liquibase.database.structure.Column.class), Column.class);

//        PopupMenuManager.getInstance().getActions(DBTree.class)
    }

    private ActionGroup createLiquibaseMenu(Class dbObjectType) {
        ActionGroup actionGroup = new ActionGroup("LiquiBase");
        //database actions
        actionGroup.addAction(new IntellijActionWrapper(new MigrateDatabaseAction(), dbObjectType));


        return actionGroup;
    }

    private ActionGroup createRefactorMenu(Class dbObjectType) {
        ActionGroup refactorActionGroup = new ActionGroup("Refactor");
        //database actions
        refactorActionGroup.addAction(new IntellijActionWrapper(new AddTableAction(), dbObjectType));

        //table actions
        refactorActionGroup.addAction(new IntellijActionWrapper(new AddColumnAction(), dbObjectType));

        //column actions
        refactorActionGroup.addAction(new IntellijActionWrapper(new AddAutoIncrementAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new AddDefaultValueAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new AddForeignKeyConstraintAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new AddLookupTableAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new AddNotNullConstraintAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new AddPrimaryKeyAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new AddUniqueConstraintAction(), dbObjectType));

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
        Migrator migrator = new Migrator(getChangeLogFile(), new CompositeFileOpener(new IntellijFileOpener(), new FileSystemFileOpener()));
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
        VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
        return virtualFileManager.refreshAndFindFileByUrl(VirtualFileManager.constructUrl(LocalFileSystem.PROTOCOL, getChangeLogFile()));
    }


    public void readExternal(Element element) throws InvalidDataException {
        DefaultJDOMExternalizer.readExternal(this, element);
    }

    public void writeExternal(Element element) throws WriteExternalException {
        DefaultJDOMExternalizer.writeExternal(this, element);
    }
}
