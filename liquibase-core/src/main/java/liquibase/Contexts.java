package liquibase;

import liquibase.util.StringUtils;

import java.util.*;

public class Contexts extends HashSet<String> {

    public Contexts() {
    }

    public Contexts(String... contexts) {
        if (contexts.length == 1) {
            parseContextString(contexts[0]);
        } else {
            addAll(Arrays.asList(contexts));
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
        addAll(StringUtils.splitAndTrim(contexts, ","));
    }

    public Contexts(Collection<String> contexts) {
        addAll(contexts);
    }

    @Override
    public boolean add(String s) {
        return super.add(s.toLowerCase());
    }

    @Override
    public String toString() {
        return StringUtils.join(new TreeSet(this),",");
    }
}
