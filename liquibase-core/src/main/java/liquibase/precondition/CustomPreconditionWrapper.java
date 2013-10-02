package liquibase.precondition;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.*;
import liquibase.precondition.core.ErrorPrecondition;
import liquibase.precondition.core.FailedPrecondition;
import liquibase.util.ObjectUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class CustomPreconditionWrapper implements Precondition {

    private String className;
    private ClassLoader classLoader;

    private SortedSet<String> params = new TreeSet<String>();
    private Map<String, String> paramValues = new HashMap<String, String>();

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setParam(String name, String value) {
        this.params.add(name);
        this.paramValues.put(name, value);
    }

    public Warnings warn(Database database) {
        return new Warnings();
    }

    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
    
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        CustomPrecondition customPrecondition;
        try {
//            System.out.println(classLoader.toString());
            try {
                customPrecondition = (CustomPrecondition) Class.forName(className, true, classLoader).newInstance();
            } catch (ClassCastException e) { //fails in Ant in particular
                customPrecondition = (CustomPrecondition) Class.forName(className).newInstance();
            }
        } catch (Exception e) {
            throw new PreconditionFailedException("Could not open custom precondition class "+className, changeLog, this);
        }

        for (String param : params) {
            try {
                ObjectUtil.setProperty(customPrecondition, param, paramValues.get(param));
            } catch (Exception e) {
                throw new PreconditionFailedException("Error setting parameter "+param+" on custom precondition "+className, changeLog, this);
            }
        }

        try {
            customPrecondition.check(database);
        } catch (CustomPreconditionFailedException e) {
            throw new PreconditionFailedException(new FailedPrecondition("Custom Precondition Failed: "+e.getMessage(), changeLog, this));
        } catch (CustomPreconditionErrorException e) {
            throw new PreconditionErrorException(new ErrorPrecondition(e, changeLog, this));
        }
    }

    public String getName() {
        return "customPrecondition";
    }
}
