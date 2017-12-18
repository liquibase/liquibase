package liquibase.integration.cdi;

import liquibase.integration.cdi.annotations.LiquibaseSchema;
import liquibase.integration.cdi.exceptions.CyclicDependencyException;
import liquibase.integration.cdi.exceptions.DependencyNotFoundException;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Nikita Lipatov (https://github.com/islonik), antoermo (https://github.com/dikeert)
 * @since 27/5/17.
 */
@ApplicationScoped
public class SchemesTreeBuilder {
    private static final Logger log = LogService.getLog(SchemesTreeBuilder.class);

    private class SchemaNode {
        private final LiquibaseSchema item;

        private final Collection<SchemaNode> children = new ArrayList<>();

        public SchemaNode(LiquibaseSchema item) {
            this.item = item;
        }

        public LiquibaseSchema getItem() {
            return item;
        }

        public Collection<SchemaNode> getChildren() {
            return Collections.unmodifiableCollection(children);
        }

        public void addChild(LiquibaseSchema child) {
            children.add(new SchemaNode(child));
        }

        public SchemaNode find(String name) {
            SchemaNode result = null;
            if (this.item.name().equals(name)) {
                result = this;
            } else {
                for (SchemaNode child : children) {
                    SchemaNode found = child.find(name);
                    if ((result == null) && (found != null)) {
                        result = child.find(name);
                    } else if ((result != null) && (found != null)) {
                        throw new IllegalStateException(String.format(
                                "Duplicate schema names [%s] detected!",
                                result.getItem().name()));
                    }
                }
            }

            return result;
        }

        public List<LiquibaseSchema> toList() {
            List<LiquibaseSchema> list = new ArrayList<>(children.size() + 1);
            list.add(item);
            for (SchemaNode child : children) {
                list.addAll(child.toList());
            }
            return list;
        }
    }

    /**
     * Builds a collection of schemes sorted according dependencies
     *
     * @param schemes All found Liquibase Schema annotations in 'war' or 'ear' type file.
     * @return sorted collection of schemes
     */
    public List<LiquibaseSchema> build(final String id, Collection<LiquibaseSchema> schemes) {
        log.debug(LogType.LOG, String.format("[id = %s] build(%s)", id, schemes));

        log.info(LogType.LOG, String.format("[id = %s] Sorting schemes according dependencies...", id));

        if (schemes.isEmpty()) {
            return Collections.emptyList();
        }

        SchemaNode root = null;

        // first, copy schemes to no modify source collection
        schemes = new ArrayList<>(schemes);
        Collection<LiquibaseSchema> availableSchemes = new ArrayList<>(schemes);
        // then find not dependent schemes - this will the roots of hierarchy.

        List<LiquibaseSchema> notDependent = new ArrayList<>();
        for (LiquibaseSchema liquibaseSchema : schemes) {
            String depends = liquibaseSchema.depends();
            if (depends.trim().isEmpty()) {
                notDependent.add(liquibaseSchema);
            }
        }

        log.info(LogType.LOG, String.format("[id = %s] Found [%s] not dependent schemes.", id, notDependent.size()));

        if (notDependent.isEmpty()) { // if there is no not-dependent schema, then there is a cyclic dependency.
            throw new CyclicDependencyException(String.format("[id = %s] Not independent schemes, possible cyclic dependencies discovered.", id));
        } else {
            // take first of not-dependent and use it as root of hierarchy.
            root = new SchemaNode(notDependent.get(0));
            log.debug(LogType.LOG, String.format("[id = %s] Selected dependencies tree root [%s]", id, root.getItem()));
            availableSchemes.removeAll(notDependent); // we won't to check not-dependent schemes.
            notDependent.remove(root.getItem());  // remove root from not-dependent schemes
            schemes.retainAll(availableSchemes); // remove not-dependent from all schemes

            // now make all not-dependent schemes children to selected root.
            for (LiquibaseSchema liquibaseSchema : notDependent) {
                root.addChild(liquibaseSchema);
            }

            log.debug(LogType.LOG, String.format("[id = %s] Made other non-dependent schemes children of root. [%s] dependent schemes to resolve. Resolving...",
                    id,
                    availableSchemes.size()
            ));

            int cycles = 0;
            long start = System.currentTimeMillis();
            // until we resolve all dependencies
            while (!availableSchemes.isEmpty()) {
                cycles++;
                log.debug(LogType.LOG, String.format("[id = %s] Resolution cycle [%s] started.", id, cycles));
                int additions = 0; //we will count dependencies resolution for each resolution cycle.
                for (LiquibaseSchema liquibaseSchema : schemes) {
                    log.debug(LogType.LOG, String.format(
                            "[id = %s] LiquibaseSchema [name=%s] depends on liquibaseSchema [name=%s].",
                            id, liquibaseSchema.name(), liquibaseSchema.depends()
                    ));
                    SchemaNode parent = root.find(liquibaseSchema.depends());

                    // we make the dependent liquibaseSchema as a child for it's dependency if found. If not, we just continue.
                    if (parent == null) {
                        log.debug(LogType.LOG, String.format(
                                "[id = %s] Dependency not found in resolved dependencies tree, skipping liquibaseSchema [name=%s] for a while.",
                                id, liquibaseSchema.name()
                        ));

                        boolean isDependencyMissed = true;
                        for (LiquibaseSchema tmpLiquibaseSchema : availableSchemes) {
                            if (tmpLiquibaseSchema.name().equalsIgnoreCase(liquibaseSchema.depends())) {
                                isDependencyMissed = false;
                                break;
                            }
                        }
                        if (isDependencyMissed) {
                            throw new DependencyNotFoundException(String.format(
                                    "[id = %s][name=%s] depends on [name=%s], but it is not found!",
                                    id, liquibaseSchema.name(), liquibaseSchema.depends()
                            ));
                        }
                    } else {
                        log.debug(LogType.LOG, String.format(
                                "[id = %s] Dependency found for liquibaseSchema [name=%s], moving it to resolved dependencies tree.",
                                id, liquibaseSchema.name()
                        ));
                        parent.addChild(liquibaseSchema);
                        availableSchemes.remove(liquibaseSchema);
                        additions++;
                    }
                }
                log.debug(LogType.LOG, String.format("[id = %s] Resolution cycle [%s] completed", id, cycles));

                //if not resolutions happened through resolution cycle, definitely there is a cyclic dependency.
                if (additions == 0) {
                    throw new CyclicDependencyException(String.format("[id = %s] Cyclic dependencies discovered!", id));
                }

                schemes.retainAll(availableSchemes);
            }

            log.info(LogType.LOG, String.format("[id = %s] Dependencies resolved in [cycles=%s, millis=%s]", id, cycles, System.currentTimeMillis() - start));
        }

        return root.toList();
    }
}
