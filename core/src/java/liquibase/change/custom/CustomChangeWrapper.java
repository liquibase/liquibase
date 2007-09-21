package liquibase.change.custom;

import liquibase.change.AbstractChange;
import liquibase.database.Database;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.CustomChangeException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.util.ObjectUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Adapts CustomChange implementations to the standard change system used by LiquiBase.
 * Custom change implementations should implement CustomSqlChange or CustomTaskChange
 *
 * @see liquibase.change.custom.CustomSqlChange
 * @see liquibase.change.custom.CustomTaskChange
 */
public class CustomChangeWrapper extends AbstractChange {

    private CustomChange customChange;
    private String className;
    private SortedSet<String> params = new TreeSet<String>();
    private Map<String, String> paramValues = new HashMap<String, String>();


    public CustomChangeWrapper() {
        super("customChange", "Custom Change");
    }

    public CustomChange getCustomChange() {
        return customChange;
    }

    public void setClass(String className) throws CustomChangeException {
        this.className = className;
        try {
            customChange = (CustomChange) Class.forName(className).newInstance();
        } catch (Exception e) {
            throw new CustomChangeException(e);
        }
    }

    public void setParam(String name, String value) {
        this.params.add(name);
        this.paramValues.put(name, value);
    }


    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        SqlStatement[] statements = null;
        try {
            configureCustomChange();
            if (customChange instanceof CustomSqlChange) {
                statements = ((CustomSqlChange) customChange).generateStatements(database);
            } else if (customChange instanceof CustomTaskChange) {
                ((CustomTaskChange) customChange).execute(database);
            } else {
                throw new UnsupportedChangeException(customChange.getClass().getName() + " does not implement " + CustomSqlChange.class.getName() + " or " + CustomTaskChange.class.getName());
            }
        } catch (CustomChangeException e) {
            throw new UnsupportedChangeException(e);
        }

        if (statements == null) {
            statements = new SqlStatement[0];
        }
        return statements;
    }


    public SqlStatement[] generateRollbackStatements(Database database) throws UnsupportedChangeException, RollbackImpossibleException {
        SqlStatement[] statements = null;
        try {
            configureCustomChange();
            if (customChange instanceof CustomSqlRollback) {
                statements = ((CustomSqlRollback) customChange).generateRollbackStatements(database);
            } else if (customChange instanceof CustomTaskRollback) {
                ((CustomTaskRollback) customChange).rollback(database);
            } else {
                throw new UnsupportedChangeException("Unknown rollback type: "+customChange.getClass().getName());
            }
        } catch (CustomChangeException e) {
            throw new UnsupportedChangeException(e);
        }

        if (statements == null) {
            statements = new SqlStatement[0];
        }
        return statements;

    }


    public boolean canRollBack() {
        return customChange instanceof CustomSqlRollback || customChange instanceof CustomTaskRollback;
    }

    private void configureCustomChange() throws CustomChangeException {
        try {
            for (String param : params) {
                ObjectUtil.setProperty(customChange, param, paramValues.get(param));
            }
        } catch (Exception e) {
            throw new CustomChangeException(e);
        }
    }

    public String getConfirmationMessage() {
        return customChange.getConfirmationMessage();
    }

    public Element createNode(Document currentChangeLogDOM) {
        Element customElement = currentChangeLogDOM.createElement("custom");
        customElement.setAttribute("class", className);

        for (String param : params) {
            Element paramElement = currentChangeLogDOM.createElement("param");
            paramElement.setAttribute("name", param);
            paramElement.setAttribute("value", paramValues.get(param));

            customElement.appendChild(paramElement);
        }

        return customElement;
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        return customChange.getAffectedDatabaseObjects();
    }
}
