package liquibase.precondition;

import liquibase.exception.SetupException;
import liquibase.parser.core.ParsedNode;
import liquibase.resource.ResourceAccessor;
import org.apache.velocity.runtime.directive.Parse;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Marker interface for precondition logic tags (and,or, not)
 */
public abstract class PreconditionLogic extends AbstractPrecondition {
    private List<Precondition> nestedPreconditions = new ArrayList<Precondition>();

    public List<Precondition> getNestedPreconditions() {
        return this.nestedPreconditions;
    }

    public void addNestedPrecondition(Precondition precondition) {
        if (precondition != null) {
            nestedPreconditions.add(precondition);
        }
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParseException, SetupException {
        super.load(parsedNode, resourceAccessor);

        Object value = parsedNode.getValue();
        if (value != null) {
            if (value instanceof ParsedNode) {
                addNestedPrecondition(toPrecondition(((ParsedNode) value), resourceAccessor));
            } else if (value instanceof Collection) {
                for (Object childValue : ((Collection) value)) {
                    if (childValue instanceof ParsedNode) {
                        addNestedPrecondition(toPrecondition(((ParsedNode) childValue), resourceAccessor));
                    }
                }
            }
        }
        for (ParsedNode child : parsedNode.getChildren()) {
            addNestedPrecondition(toPrecondition(child, resourceAccessor));
        }
    }

    protected Precondition toPrecondition(ParsedNode node, ResourceAccessor resourceAccessor) throws ParseException, SetupException {
        Precondition precondition = PreconditionFactory.getInstance().create(node.getNodeName());
        if (precondition == null) {
            return null;
        }
        precondition.load(node, resourceAccessor);
        return precondition;
    }
}
