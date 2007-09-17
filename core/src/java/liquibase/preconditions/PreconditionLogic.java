package liquibase.preconditions;

import java.util.List;
import java.util.ArrayList;

/**
 * Marker interface for precondition logic tags (and,or, not)
 */
public abstract class PreconditionLogic implements Precondition {
    private List<Precondition> nestedPreconditions = new ArrayList<Precondition>();

    public List<Precondition> getNestedPreconditions() {
        return this.nestedPreconditions;
    }

    public void addNestedPrecondition(Precondition precondition) {
        nestedPreconditions.add(precondition);
    }
    
}
