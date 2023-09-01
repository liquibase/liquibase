package liquibase;

import liquibase.util.StringUtil;

import java.util.*;

/**
 * Wrapper for list of contexts.
 *
 * <p>
 * Contexts in Liquibase are expressions you can add to changesets to control which will be executed in any particular
 * migration run. Any string can be used for the context name and they are checked case-insensitively.
 * </p>
 *
 * @see <a href="https://docs.liquibase.com/concepts/changelogs/attributes/contexts.html" target="_top">contexts</a> in documentation
 */
public class Contexts {

    private final HashSet<String> contextStore = new HashSet<>();

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
        if (contexts != null) {
            contexts = contexts.replace("\\", "");
        }
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
        contexts = StringUtil.trimToNull(contexts);

        if (contexts == null) {
            return;
        }
        for (String context : StringUtil.splitAndTrim(contexts, ",")) {
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
        return StringUtil.join(new TreeSet<>(this.contextStore), ",");
    }


    public boolean isEmpty() {
        return this.contextStore.isEmpty();
    }

    public Set<String> getContexts() {
        return Collections.unmodifiableSet(contextStore);
    }
}
