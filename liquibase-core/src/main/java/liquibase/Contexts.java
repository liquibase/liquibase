package liquibase;

import liquibase.util.StringUtils;

import java.util.*;

/**
 * List of contexts Liquibase is running under.
 */
public class Contexts {

    private HashSet<String> contexts = new HashSet<String>();

    public Contexts() {
    }

    public Contexts(String... contexts) {
        if (contexts.length == 1) {
            parseContextString(contexts[0]);
        } else {
            for (String context : contexts) {
                this.contexts.add(context.toLowerCase());
            }
        }
    }

    public Contexts(String contexts) {
        parseContextString(contexts);
    }

    private void parseContextString(String contexts) {
        contexts = StringUtils.trimToNull(contexts);

        if (contexts == null) {
            return;
        }
        for (String context : StringUtils.splitAndTrim(contexts, ",")) {
            this.contexts.add(context.toLowerCase());
        }

    }

    public Contexts(Collection<String> contexts) {
        if (contexts != null) {
            for (String context : contexts) {
                this.contexts.add(context.toLowerCase());
            }

        }
    }

    public boolean add(String context) {
        return this.contexts.add(context.toLowerCase());
    }

    @Override
    public String toString() {
        return StringUtils.join(new TreeSet(this.contexts),",");
    }


    public boolean isEmpty() {
        return this.contexts == null || this.contexts.isEmpty();
    }

    public Set<String> getContexts() {
        return Collections.unmodifiableSet(contexts);
    }
}
