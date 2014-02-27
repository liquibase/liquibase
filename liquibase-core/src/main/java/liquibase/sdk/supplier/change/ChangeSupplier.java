package liquibase.sdk.supplier.change;

import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;

public class ChangeSupplier {

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
}
