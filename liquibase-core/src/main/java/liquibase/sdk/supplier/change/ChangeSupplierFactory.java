package liquibase.sdk.supplier.change;

import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.sdk.exception.UnexpectedLiquibaseSdkException;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;

public class ChangeSupplierFactory {

    public Set<Class<? extends Change>> getExtensionClasses() {
        Set<Class<? extends Change>> classes = new HashSet<Class<? extends Change>>(Arrays.asList(ServiceLocator.getInstance().findClasses(Change.class)));
        return classes;
    }

    public Set<Change> getExtensionChanges() {
        Set<Change> returnSet = new HashSet<Change>();
        for (String change : ChangeFactory.getInstance().getDefinedChanges()) {
            returnSet.add(ChangeFactory.getInstance().create(change));
        }
        return returnSet;
    }

    public Set<String> getExtensionChangeNames() {
        Set<String> returnSet = new HashSet<String>();
        for (Change change : getExtensionChanges()) {
            returnSet.add(ChangeFactory.getInstance().getChangeMetaData(change).getName());
        }
        return returnSet;
    }

    public void prepareDatabase(Change change, Database database) {
        ChangeSupplier supplier = getSupplier(change);

        try {
            Change[] changes = supplier.prepareDatabase(change);
            if (changes != null) {
                for (Change prepareChange : changes) {
                    ExecutorService.getInstance().getExecutor(database).execute(prepareChange);
                }
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseSdkException("Error executing change supplier prepareDatabase" + supplier.getClass().getName() + ": " + e.getMessage(), e);
        }
    }

    public void revertDatabase(Change change, Database database) {
        ChangeSupplier supplier = getSupplier(change);

        try {
            Change[] changes = supplier.revertDatabase(change);
            if (changes != null) {
                for (Change revertChange : changes) {
                    ExecutorService.getInstance().getExecutor(database).execute(revertChange);
                }
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseSdkException("Error executing change supplier prepareDatabase" + supplier.getClass().getName() + ": " + e.getMessage(), e);
        }
    }

    protected ChangeSupplier getSupplier(Change change) {
        String supplierClassName = change.getClass().getName().replaceFirst("(.*)\\.(\\w+)", "$1\\.supplier\\.$2Supplier");
        try {
            Class supplierClass = Class.forName(supplierClassName);
            return (ChangeSupplier) supplierClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new UnexpectedLiquibaseSdkException("No change supplier class " + supplierClassName);
        } catch (InstantiationException e) {
            throw new UnexpectedLiquibaseSdkException("Error instantiating supplier class " + supplierClassName);
        } catch (IllegalAccessException e) {
            throw new UnexpectedLiquibaseSdkException("Error instantiating supplier class " + supplierClassName);
        }
    }

    public ChangeSupplier getSupplier(Class<? extends Change> change) {
        try {
            return getSupplier(change.newInstance());
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public boolean isValid(Change change, Database database) {
        return getSupplier(change).isValid(change, database);
    }
}
