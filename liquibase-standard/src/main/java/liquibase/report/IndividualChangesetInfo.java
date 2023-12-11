package liquibase.report;

import liquibase.util.CollectionUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class IndividualChangesetInfo {
    private int index;
    private String changesetAuthor;
    private String changesetId;
    private String changelogFile;
    private String comment;
    private Boolean success;
    private String changesetOutcome;
    private String errorMsg;
    private String labels;
    private String contexts;
    private List<String> attributes;
    private List<String> generatedSql;

    /**
     * Used in the report template. Do not remove.
     *
     * @return true if there are any attributes
     */
    public boolean hasAttributes() {
        return !CollectionUtil.createIfNull(attributes).isEmpty();
    }
}
