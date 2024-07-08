package liquibase.precondition;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.parser.ChangeLogParserConfiguration;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Marker interface for precondition logic tags (and,or, not)
 */
public abstract class PreconditionLogic extends AbstractPrecondition {
    private final List<Precondition> nestedPreconditions = new ArrayList<>();

    public List<Precondition> getNestedPreconditions() {
        return this.nestedPreconditions;
    }

    public void addNestedPrecondition(Precondition precondition) {
        if (precondition != null) {
            nestedPreconditions.add(precondition);
        }
    }

    @Override
    public ValidationErrors validate(Database database) {
        final ValidationErrors validationErrors = new ValidationErrors();
        for (Precondition precondition : getNestedPreconditions()) {
            validationErrors.addAll(precondition.validate(database));
        }

        return validationErrors;
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        super.load(parsedNode, resourceAccessor);

        for (ParsedNode child : parsedNode.getChildren()) {
            addNestedPrecondition(toPrecondition(child, resourceAccessor));
        }
    }

    protected Precondition toPrecondition(ParsedNode node, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        Precondition precondition = PreconditionFactory.getInstance().create(node.getName());
        if (precondition == null) {
            if (node.getChildren() != null && node.getChildren().size() > 0 && ChangeLogParserConfiguration.CHANGELOG_PARSE_MODE.getCurrentValue().equals(ChangeLogParserConfiguration.ChangelogParseMode.STRICT)) {
                throw new ParsedNodeException("Unknown precondition '" + node.getName() + "'. Check for spelling or capitalization errors and missing extensions such as liquibase-commercial.");
            }

            return null;
        }

        precondition.load(node, resourceAccessor);
        return precondition;
    }
}
