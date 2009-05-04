package liquibase.changelog.filter;

import liquibase.ChangeSet;
import liquibase.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ContextChangeSetFilter implements ChangeSetFilter {
    private Set<String> contexts;

    public ContextChangeSetFilter(String... contexts) {
        this.contexts = new HashSet<String>();
        if (contexts != null) {
            for (int i=0; i<contexts.length; i++) {
                if (contexts[i] != null) {
                    contexts[i] = contexts[i].toLowerCase();
                }
            }

            if (contexts.length == 1) {
                if (contexts[0] == null) {
                    //do nothing
                } else if (contexts[0].indexOf(",") >= 0) {
                    this.contexts.addAll(StringUtils.splitAndTrim(contexts[0], ","));
                } else {
                    this.contexts.add(contexts[0]);
                }
            } else {
                this.contexts.addAll(Arrays.asList(contexts));
            }
        }
    }

    public boolean accepts(ChangeSet changeSet) {
        if (contexts == null || contexts.size() == 0) {
            return true;
        }

        if (changeSet.getContexts() == null) {
            return true;
        }
        
        for (String context : changeSet.getContexts()) {
            if (contexts.contains(context.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}
