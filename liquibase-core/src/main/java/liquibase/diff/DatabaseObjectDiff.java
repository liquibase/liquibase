package liquibase.diff;

import java.util.SortedSet;
import java.util.TreeSet;

public class DatabaseObjectDiff<Type> {
    private SortedSet<Type> missing = new TreeSet<Type>();
    private SortedSet<Type> unexpected = new TreeSet<Type>();
    private SortedSet<Type> changed = new TreeSet<Type>();

    public SortedSet<Type> getMissing() {
        return missing;
    }
    
    public void addMissing(Type obj) {
        missing.add(obj);
    }

    public SortedSet<Type> getUnexpected() {
        return unexpected;
    }
    
    public void addUnexpected(Type obj) {
        unexpected.add(obj);
    }

    public SortedSet<Type> getChanged() {
        return changed;
    }
    
    public void addChanged(Type obj) {
        changed.add(obj);
    }

    public boolean areEqual() {
        return missing.size() == 0 && unexpected.size() == 0 && changed.size() == 0;
    }
}
