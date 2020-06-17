package liquibase;

import liquibase.util.StringUtils;

import java.util.*;

/**
 * List of contexts Liquibase is running under.
 */
public class Contexts {

    private HashSet<String> contextStore = new HashSet<>();

    public Contexts() {
    }

    public Contexts(String... contexts) {
        if (contexts.length == 1) {
            parseContextString(contexts[0]);
        } else {
            for (String context : contexts) {
                this.contextStore.add(context.toLowerCase());
            }
        }
    }

    public Contexts(String contexts) {
        parseContextString(contexts);
    }

    public Contexts(Collection<String> contexts) {
        if (contexts != null) {
            for (String context : contexts) {
                this.contextStore.add(context.toLowerCase());
            }

        }
    }

    private void parseContextString(String contexts) {
        contexts = StringUtils.trimToNull(contexts);

        if (contexts == null) {
            return;
        }
        for (String context : StringUtils.splitAndTrim(contexts, ",")) {
            this.contextStore.add(context.toLowerCase());
        }

    }

    public boolean add(String context) {
        return this.contextStore.add(context.toLowerCase());
    }
    
    public boolean remove(String context) {
        return this.contextStore.remove(context.toLowerCase());
    }

    @Override
    public String toString() {
        return StringUtils.join(new TreeSet<String>(this.contextStore), ",");
    }


    public boolean isEmpty() {
        return (this.contextStore == null) || this.contextStore.isEmpty();
    }

    public Set<String> getContexts() {
        return Collections.unmodifiableSet(contextStore);
    }
}
