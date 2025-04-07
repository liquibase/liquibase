package liquibase.logging.mdc.customobjects;

import liquibase.logging.mdc.CustomMdcObject;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This class is the representation of the summary available in the update command.
 */
@Getter
@Setter
public class UpdateSummary implements CustomMdcObject {
    private String value;
    private int run;
    private int runPreviously;
    private SortedMap<String, Integer> skipped;
    private int totalChangesets;
    private final SortedMap<String, Set<String>> matchingProblems = new TreeMap<>();

    /**
     * Constructor for service locator.
     */
    public UpdateSummary() {
    }

    public UpdateSummary(String value, int run, int runPreviously, SortedMap<String, Integer> skipped, int totalChangesets) {
        this.value = value;
        this.run = run;
        this.runPreviously = runPreviously;
        this.skipped = skipped;
        this.totalChangesets = totalChangesets;
    }

}
