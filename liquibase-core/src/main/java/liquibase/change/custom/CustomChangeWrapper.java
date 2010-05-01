package liquibase.change.custom;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeProperty;
import liquibase.database.Database;
import liquibase.exception.*;
import liquibase.statement.SqlStatement;
import liquibase.util.ObjectUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Adapts CustomChange implementations to the standard change system used by LiquiBase.
 * Custom change implementations should implement CustomSqlChange or CustomTaskChange
 *
 * @see liquibase.change.custom.CustomSqlChange
 * @see liquibase.change.custom.CustomTaskChange
 */
public class CustomChangeWrapper extends AbstractChange {

    @ChangeProperty(includeInSerialization = false)
    private CustomChange customChange;
    
    private String className;

    @ChangeProperty(includeInSerialization = false)
    private SortedSet<String> params = new TreeSet<String>();

    private Map<String, String> paramValues = new HashMap<String, String>();

    @ChangeProperty(includeInSerialization = false)
    private ClassLoader classLoader;

    public CustomChangeWrapper() {
        super("customChange", "Custom Change", ChangeMetaData.PRIORITY_DEFAULT);
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
                try {
                    customChange = (CustomChange) Class.forName(className, true, classLoader).newInstance();
                } catch (ClassCastException e) { //fails in Ant in particular
                    try {
                        customChange = (CustomChange) Thread.currentThread().getContextClassLoader().loadClass(className).newInstance();
                    } catch (ClassNotFoundException e1) {
                        customChange = (CustomChange) Class.forName(className).newInstance();
                    }
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

    @Override
    public ValidationErrors validate(Database database) {
        try {
            return customChange.validate(database);
        } catch (AbstractMethodError e) {
            return new ValidationErrors(); //old interface, //todo: be smarter about handling upgrade            
        }
    }

    public SqlStatement[] generateStatements(Database database) {
        SqlStatement[] statements = null;
        try {
            configureCustomChange();
            if (customChange instanceof CustomSqlChange) {
                statements = ((CustomSqlChange) customChange).generateStatements(database);
            } else if (customChange instanceof CustomTaskChange) {
                ((CustomTaskChange) customChange).execute(database);
            } else {
                throw new UnexpectedLiquibaseException(customChange.getClass().getName() + " does not implement " + CustomSqlChange.class.getName() + " or " + CustomTaskChange.class.getName());
            }
        } catch (CustomChangeException e) {
            throw new UnexpectedLiquibaseException(e);
        }

        if (statements == null) {
            statements = new SqlStatement[0];
        }
        return statements;
    }

    @Override
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


    @Override
    public boolean supportsRollback(Database database) {
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
}
