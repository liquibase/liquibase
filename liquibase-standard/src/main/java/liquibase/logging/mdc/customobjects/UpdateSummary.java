package liquibase.logging.mdc.customobjects;

import liquibase.logging.mdc.CustomMdcObject;
import lombok.Getter;

import java.util.SortedMap;

/**
 * This class is the representation of the summary available in the update command.
 */
@Getter
public class UpdateSummary implements CustomMdcObject {
    private String value;
    private int run;
    private int runPreviously;
    private SortedMap<String, Integer> skipped;
    private int totalChangesets;

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

    public void setValue(String value) {
        this.value = value;
    }

    public void setRun(int run) {
        this.run = run;
    }

    public void setRunPreviously(int runPreviously) {
        this.runPreviously = runPreviously;
    }

    public void setSkipped(SortedMap<String, Integer> skipped) {
        this.skipped = skipped;
    }

    public void setTotalChangesets(int totalChangesets) {
        this.totalChangesets = totalChangesets;
    }

}
