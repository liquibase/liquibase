package liquibase.changelog;

import liquibase.Scope;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.util.ObjectUtil;
import lombok.Getter;

/**
 * Container class to handle the modifyChangeSets tag.
 * Other attributes may be added later
 */
@Getter
public class ModifyChangeSets {
    private final String runWith;
    private final String runWithSpool;
    private Boolean stripComments;

    /**
     *
     * @param node         The ParsedNode to use
     *
     */
    public ModifyChangeSets(ParsedNode node) throws ParsedNodeException  {
        this.runWith = (String) node.getChildValue(null, "runWith");
        this.runWithSpool = (String) node.getChildValue(null, "runWithSpoolFile");
        Object stripCommentsValue = node.getChildValue(null, "stripComments");
        if (stripCommentsValue != null) {
            this.stripComments = ObjectUtil.convert(stripCommentsValue, Boolean.class);
        }
    }

    /**
     *
     * @param runWith       The native executor to execute all included change sets with. Can be null
     * @param runWithSpool  The name of the spool file to be created
     *
     */
    public ModifyChangeSets(String runWith, String runWithSpool) {
        this(runWith, runWithSpool, false);
    }

    /**
     *
     * @param runWith       The native executor to execute all included change sets with. Can be null
     * @param runWithSpool  The name of the spool file to be created
     * @param stripComments Boolean flag to strip comments from SQL
     *
     */
    public ModifyChangeSets(String runWith, String runWithSpool, boolean stripComments) {
        this.runWith = runWith;
        this.runWithSpool = runWithSpool;
        this.stripComments = stripComments;
    }

    public String getRunWith() {
        return runWith;
    }

    public String getRunWithSpool() {
        return runWithSpool;
    }

    public Boolean isStripComments() {
        return stripComments;
    }

}
