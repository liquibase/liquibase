package liquibase;

import liquibase.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Labels {

    private Set<String> labels = new LinkedHashSet<>();

    public Labels() {
    }

    public Labels(String... labels) {
        if (labels.length == 1) {
            parseLabelString(labels[0]);
        } else {
            for (String label : labels) {
                this.labels.add(label.toLowerCase());
            }
        }
    }

    public Labels(String labels) {
        parseLabelString(labels);
    }

    private void parseLabelString(String labels) {
        labels = StringUtils.trimToNull(labels);

        if (labels == null) {
            return;
        }
        for (String label : StringUtils.splitAndTrim(labels, ",")) {
            this.labels.add(label.toLowerCase());
        }

    }

    public Labels(Collection<String> labels) {
        if (labels != null) {
            for (String label : labels) {
                this.labels.add(label.toLowerCase());
            }

        }
    }

    public boolean add(String label) {
        return this.labels.add(label.toLowerCase());
    }

    public boolean remove(String label) {
      return this.labels.remove(label.toLowerCase());
    }

    @Override
    public String toString() {
        return StringUtils.join(new LinkedHashSet<>(this.labels),",");
    }

    public boolean isEmpty() {
        return (this.labels == null) || this.labels.isEmpty();
    }

    public Set<String> getLabels() {
        return Collections.unmodifiableSet(labels);
    }

}
