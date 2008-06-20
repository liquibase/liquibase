package liquibase.preconditions;

import liquibase.exception.MigrationFailedException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.CustomPreconditionFailedException;
import liquibase.database.Database;
import liquibase.DatabaseChangeLog;

public class CustomPreconditionWrapper implements Precondition {

    private String className;
    private ClassLoader classLoader;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) throws Exception{
        this.className = className;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void check(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException {
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

        try {
            customPrecondition.check(database);
        } catch (CustomPreconditionFailedException e) {
            throw new PreconditionFailedException(new FailedPrecondition("Custom Precondition Failed: "+e.getMessage(), changeLog, this));
        }
    }

    public String getTagName() {
        return "customPrecondition";
    }
}
