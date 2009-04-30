package liquibase.change.custom;

import liquibase.change.AbstractChange;
import liquibase.database.Database;
import liquibase.database.statement.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.util.ObjectUtil;

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
    private ClassLoader classLoader;

    public CustomChangeWrapper() {
        super("customChange", "Custom Change");
    }

    public CustomChange getCustomChange() {
        return customChange;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setClass(String className) throws CustomChangeException {
        this.className = className;
        try {
//            System.out.println(classLoader.toString());
            try {
                customChange = (CustomChange) Class.forName(className, true, classLoader).newInstance();
            } catch (ClassCastException e) { //fails in Ant in particular
                customChange = (CustomChange) Class.forName(className).newInstance();
            }
        } catch (Exception e) {
            throw new CustomChangeException(e);
        }
    }

    public String getClassName() {
        return className;
    }

    public void setParam(String name, String value) {
        this.params.add(name);
        this.paramValues.put(name, value);
    }

    public SortedSet<String> getParams() {
        return params;
    }

    public Map<String, String> getParamValues() {
        return paramValues;
    }

    public void validate(Database database) throws InvalidChangeDefinitionException {
        customChange.validate(database);
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


    public boolean supportsRollback() {
        return customChange instanceof CustomSqlRollback || customChange instanceof CustomTaskRollback;
    }

    private void configureCustomChange() throws CustomChangeException {
        try {
            for (String param : params) {
                ObjectUtil.setProperty(customChange, param, paramValues.get(param));
            }
            customChange.setFileOpener(getFileOpener());
            customChange.setUp();
        } catch (Exception e) {
            throw new CustomChangeException(e);
        }
    }

    public String getConfirmationMessage() {
        return customChange.getConfirmationMessage();
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects() {
        return null;
    }
}
