package org.liquibase.intellij.plugin;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import dbhelp.db.*;
import dbhelp.db.ui.DBTree;
import dbhelp.plugin.action.portable.ActionGroup;
import dbhelp.plugin.action.portable.PopupMenuManager;
import dbhelp.plugin.idea.ProjectMain;
import dbhelp.plugin.idea.utils.IDEAUtils;
import liquibase.util.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.liquibase.ide.common.IdeFacade;
import org.liquibase.ide.common.action.*;
import org.liquibase.ide.common.change.action.*;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class LiquibaseProjectComponent implements ProjectComponent, JDOMExternalizable {

    private static Map<Project, LiquibaseProjectComponent> _context = new HashMap<Project, LiquibaseProjectComponent>();

    private Project project;
    public String outputChangeLogFile; //externalized
    public String rootChangeLogFile; //externalized
    public Boolean promptForChangeLog; //externalized
//    private ProjectMain projectMain;

    private DBTree dbTree;

    private IdeFacade ideFacade;

    public LiquibaseProjectComponent(Project project) {
        this.project = project;
        this.ideFacade = new IntellijFacade();

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

        PopupMenuManager.getInstance().addAction(createLiquibaseMenu(liquibase.database.Database.class), DBTree.class);

        PopupMenuManager.getInstance().addAction(createRefactorMenu(liquibase.database.Database.class), Catalog.class);
        PopupMenuManager.getInstance().addAction(createRefactorMenu(liquibase.database.Database.class), Schema.class);
        PopupMenuManager.getInstance().addAction(createRefactorMenu(liquibase.database.structure.Table.class), Table.class);
        PopupMenuManager.getInstance().addAction(createRefactorMenu(liquibase.database.structure.Column.class), Column.class);
        PopupMenuManager.getInstance().addAction(createRefactorMenu(liquibase.database.structure.ForeignKey.class), ForeignKey.class);
//        PopupMenuManager.getInstance().getActions(DBTree.class)

        try {
            //this is ugly, but I don't see any way to access the DBTree nicely
            this.dbTree = (DBTree) ((JScrollPane) ((JSplitPane) ((JTabbedPane) ProjectMain.getInstance().getBrowser().getComponent(0)).getComponentAt(0)).getComponent(1)).getViewport().getComponent(0);
        } catch (Exception e) {
            System.out.println("Unable to find DBTree");
            e.printStackTrace();
        }
//        Component[] components = ProjectMain.getInstance().getBrowser().getComponents();
//        for (Component c : components) {
//            System.out.println("Component: "+c.getClass().getName());
//        }
    }

    private ActionGroup createLiquibaseMenu(Class dbObjectType) {
        ActionGroup actionGroup = new ActionGroup("LiquiBase");
        //database actions
        actionGroup.addAction(new IntellijActionWrapper(new MigrateAction(), dbObjectType));
        actionGroup.addAction(new IntellijActionWrapper(new MigrateSqlAction(), dbObjectType));

        actionGroup.addAction(new IntellijActionWrapper(new TagDatabaseAction(), dbObjectType));
        actionGroup.addAction(new IntellijActionWrapper(new RollbackCountAction(), dbObjectType));
        actionGroup.addAction(new IntellijActionWrapper(new RollbackCountSqlAction(), dbObjectType));
        actionGroup.addAction(new IntellijActionWrapper(new RollbackFutureSqlAction(), dbObjectType));
        actionGroup.addAction(new IntellijActionWrapper(new RollbackToDateAction(), dbObjectType));
        actionGroup.addAction(new IntellijActionWrapper(new RollbackToDateSqlAction(), dbObjectType));
        actionGroup.addAction(new IntellijActionWrapper(new RollbackToTagAction(), dbObjectType));
        actionGroup.addAction(new IntellijActionWrapper(new RollbackToTagSqlAction(), dbObjectType));

        actionGroup.addAction(new IntellijActionWrapper(new GenerateChangelogAction(), dbObjectType));

        actionGroup.addAction(new IntellijActionWrapper(new GenerateDbDocAction(), dbObjectType));

        actionGroup.addAction(new IntellijActionWrapper(new StatusAction(), dbObjectType));
        actionGroup.addAction(new IntellijActionWrapper(new ValidateAction(), dbObjectType));
        actionGroup.addAction(new IntellijActionWrapper(new ClearChecksumsAction(), dbObjectType));
        actionGroup.addAction(new IntellijActionWrapper(new ListLocksAction(), dbObjectType));
        actionGroup.addAction(new IntellijActionWrapper(new ReleaseLocksAction(), dbObjectType));
        actionGroup.addAction(new IntellijActionWrapper(new DropAllAction(), dbObjectType));

        actionGroup.addAction(new IntellijActionWrapper(new CreateEmptyChangeLogAction(), dbObjectType));

        return actionGroup;
    }

    private ActionGroup createRefactorMenu(Class dbObjectType) {
        ActionGroup refactorActionGroup = new ActionGroup("Refactor");
        //database actions
        refactorActionGroup.addAction(new IntellijActionWrapper(new CreateTableAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new CreateViewAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new CreateProcedureAction(), dbObjectType));

        //table actions
        refactorActionGroup.addAction(new IntellijActionWrapper(new AddColumnAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new DropTableAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new RenameTableAction(), dbObjectType));        

        //column actions
        refactorActionGroup.addAction(new IntellijActionWrapper(new AddAutoIncrementAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new AddDefaultValueAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new AddForeignKeyConstraintAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new AddLookupTableAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new AddNotNullConstraintAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new AddPrimaryKeyAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new AddUniqueConstraintAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new CreateIndexAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new DropColumnAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new DropDefaultValueAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new DropNotNullConstraintAction(), dbObjectType));
        refactorActionGroup.addAction(new IntellijActionWrapper(new RenameColumnAction(), dbObjectType));

        //fk actions
        refactorActionGroup.addAction(new IntellijActionWrapper(new DropForeignKeyConstraintAction(), dbObjectType));

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

//        ProjectMain.getInstance().getPopupMenuManager().addAction(new CreateTableAction(), DBTree.class);
        addActions();
    }

    public void projectClosed() {
        // called when project is being closed
    }
    
    public String getRootChangeLogFile() {
        if (rootChangeLogFile != null) {
            return rootChangeLogFile;
        }
        if (outputChangeLogFile != null) {
            return outputChangeLogFile;
        }
        return null;
    }

    public void setRootChangeLogFile(String rootChangeLogFile) {
        this.rootChangeLogFile = rootChangeLogFile;
    }

    public String getOutputChangeLogFile() {
        return outputChangeLogFile;
    }

    public void setOutputChangeLogFile(String outputChangeLogFile) {
        this.outputChangeLogFile = StringUtils.trimToNull(outputChangeLogFile);
    }

    public VirtualFile getChangeLogVirtualFile() {
        VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
        return virtualFileManager.refreshAndFindFileByUrl(VirtualFileManager.constructUrl(LocalFileSystem.PROTOCOL, getOutputChangeLogFile()));
    }


    public void readExternal(Element element) throws InvalidDataException {
        DefaultJDOMExternalizer.readExternal(this, element);
    }

    public void writeExternal(Element element) throws WriteExternalException {
        DefaultJDOMExternalizer.writeExternal(this, element);
    }

    public IdeFacade getIdeFacade() {
        return ideFacade;
    }


    public boolean getPromptForChangeLog() {
        return promptForChangeLog == null || promptForChangeLog;
    }

    public void setPromptForChangeLog(boolean shouldPrompt) {
        this.promptForChangeLog = shouldPrompt;
    }

    public DBTree getDbTree() {
        return dbTree;
    }
}
