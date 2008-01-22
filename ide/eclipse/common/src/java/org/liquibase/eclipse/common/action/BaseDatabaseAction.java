package org.liquibase.eclipse.common.action;

import liquibase.migrator.Migrator;
import liquibase.database.DatabaseFactory;
import liquibase.exception.JDBCException;
import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.modelbase.sql.constraints.ForeignKey;
import org.eclipse.datatools.modelbase.sql.constraints.Index;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.schema.Schema;
import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.datatools.modelbase.sql.tables.Table;
import org.eclipse.datatools.modelbase.sql.tables.ViewTable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.liquibase.eclipse.common.migrator.EclipseFileOpener;

import java.sql.Connection;
import java.util.List;

public abstract class BaseDatabaseAction implements IObjectActionDelegate, IWorkbenchWindowActionDelegate {
	
	private IWorkbenchWindow window;

	private ISelection selection = null;

	public void dispose() {
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.window = targetPart.getSite().getWorkbenchWindow();
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
		// this.window.getSelectionService().addSelectionListener("org.eclipse.datatools.connectivity.DataSourceExplorerNavigator",
		// new ISelectionListener() {
		//
		// public void selectionChanged(IWorkbenchPart part, ISelection
		// selection) {
		// List selectedObjects = ((TreeSelection)selection).toList();
		// for (Object object : selectedObjects) {
		// if (object instanceof ICatalogObject) {
		// database = ((ICatalogObject)object).getCatalogDatabase();
		// connection = ((ICatalogObject)object).getConnection();
		// }
		// if (object instanceof Schema) {
		// schema = (Schema) object;
		// }
		// }
		// }
		//			
		// });
	}

	
	public IWorkbenchWindow getWindow() {
		return window;
	}

	public ISelection getSelection() {
		return selection;
	}

	protected Database getSelectedDatabase(ISelection selection) {
		List<?> selectedObjects = ((TreeSelection) selection).toList();
		for (Object object : selectedObjects) {
			if (object instanceof ICatalogObject) {
				return ((ICatalogObject) object).getCatalogDatabase();
			}
		}
		return null;
	}

	protected Connection getSelectedConnection(ISelection selection) {
		List<?> selectedObjects = ((TreeSelection) selection).toList();
		for (Object object : selectedObjects) {
			if (object instanceof ICatalogObject) {
				return ((ICatalogObject) object).getConnection();
			}
		}
		return null;
	}

	protected Schema getSelectedSchema(ISelection selection) {
		List<?> selectedObjects = ((TreeSelection) selection).toList();
		
		for (Object object : selectedObjects) {
			System.out.println(object.getClass().getName());
		}

		for (Object object : selectedObjects) {
			if (object instanceof Schema) {
				return (Schema) object;
			}
		}
		return null;
	}

	protected Table getSelectedTable(ISelection selection) {
		List<?> selectedObjects = ((TreeSelection) selection).toList();
		for (Object object : selectedObjects) {
			if (object instanceof Table) {
				return (Table) object;
			}
		}
		return null;
	}

	protected Column getSelectedColumn(ISelection selection) {
		List<?> selectedObjects = ((TreeSelection) selection).toList();

		for (Object object : selectedObjects) {
			System.out.println(object.getClass().getName());
		}

		for (Object object : selectedObjects) {
			if (object instanceof Column) {
				return (Column) object;
			}
		}
		return null;
	}

	protected ForeignKey getSelectedForeignKey(ISelection selection) {
		List<?> selectedObjects = ((TreeSelection) selection).toList();
		for (Object object : selectedObjects) {
			if (object instanceof ForeignKey) {
				return (ForeignKey) object;
			}
		}
		return null;
	}

	protected Index getSelectedIndex(ISelection selection) {
		List<?> selectedObjects = ((TreeSelection) selection).toList();
		for (Object object : selectedObjects) {
			if (object instanceof Index) {
				return (Index) object;
			}
		}
		return null;
	}

	protected ViewTable getSelectedView(ISelection selection) {
		List<?> selectedObjects = ((TreeSelection) selection).toList();
		for (Object object : selectedObjects) {
			if (object instanceof ViewTable) {
				return (ViewTable) object;
			}
		}
		return null;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
	
	protected Migrator getMigrator(String changeLogFile, Connection conn) throws JDBCException {
		return new Migrator(changeLogFile, new EclipseFileOpener(), DatabaseFactory.getInstance().findCorrectDatabaseImplementation(conn));
	}
}
