package liquibase.changelog.visitor;

import liquibase.changelog.RanChangeSet;
import liquibase.plugin.Plugin;

import java.util.List;

/**
 * An interface for generating validating visitors, which are used to validate changesets.
 */
public interface ValidatingVisitorGenerator extends Plugin {

    int getPriority();

    /**
     * Generates a validating visitor for the provided list of ran change sets.
     *
     * @param ranChangeSetList The list of ran change sets to validate.
     * @return A validating visitor for the provided list of ran change sets.
     */
    ValidatingVisitor generateValidatingVisitor(List<RanChangeSet> ranChangeSetList);
}
