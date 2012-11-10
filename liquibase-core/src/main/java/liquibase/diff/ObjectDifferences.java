package liquibase.diff;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ObjectDifferences {
    private Set<Difference> differences = new HashSet<Difference>();

    public Set<Difference> getDifferences() {
        return Collections.unmodifiableSet(differences);
    }

    public ObjectDifferences addDifference(String changedField, Object referenceValue, Object compareToValue) {
        this.differences.add(new Difference(changedField, referenceValue, compareToValue));

        return this;
    }

    public ObjectDifferences addDifference(String message, String changedField, Object referenceValue, Object compareToValue) {
        this.differences.add(new Difference(message, changedField, referenceValue, compareToValue));

        return this;
    }

    public boolean hasDifferences() {
        return differences.size() > 0;
    }
}
