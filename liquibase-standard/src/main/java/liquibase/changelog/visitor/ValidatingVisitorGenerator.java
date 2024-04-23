package liquibase.changelog.visitor;

import liquibase.changelog.RanChangeSet;
import liquibase.plugin.Plugin;

import java.util.List;

/**
 * The ValidatingVisitor interface allows implementations to supply their own version of a ValidatingVisitor. By default
 * Liquibase uses the {@link ValidatingVisitor}. To use your own, you must register it with a higher priority
 * in the {@link ValidatingVisitorGeneratorFactory}.
 */
public interface ValidatingVisitorGenerator extends Plugin {

    int getPriority();

    ValidatingVisitor generateValidatingVisitor(List<RanChangeSet> ranChangeSetList);
}
