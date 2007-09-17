package liquibase.change;

import liquibase.database.sql.SqlStatement;
import liquibase.database.Database;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.UnsupportedChangeException;
import liquibase.exception.CustomChangeException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.util.ObjectUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import java.util.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

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
        try {
            configureCustomChange();
        } catch (CustomChangeException e) {
            throw new UnsupportedChangeException(e);
        }
        return customChange.generateStatements(database);
    }


    public SqlStatement[] generateRollbackStatements(Database database) throws UnsupportedChangeException, RollbackImpossibleException {
        try {
            configureCustomChange();
        } catch (CustomChangeException e) {
            throw new UnsupportedChangeException(e);
        }
        return customChange.generateRollbackStatements(database);
    }


    public boolean canRollBack() {
        return customChange.canRollBack();
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
