package liquibase.changelog.definition;

import java.util.ArrayList;
import java.util.List;

public class ChangeLogTableDefinition {

    private static final int DESCRIPTON_SIZE = 255;
    private static final int TAG_SIZE = 255;
    private static final int COMMENTS_SIZE = 255;
    private static final int LIQUIBASE_SIZE = 20;
    private static final int MD5SUM_SIZE = 35;
    private static final int EXECTYPE_SIZE = 10;
    private static final int ORDEREXECUTED_DEFAULT_VALUE = -1;
    private static final int DEPLOYMENT_COLUMN_SIZE = 10;

    private static final int LABELS_SIZE = 255;
    private static final int CONTEXTS_SIZE = 255;

    private List<ChangeLogColumnDefinition> columnDefinitions = new ArrayList<>();

    public ChangeLogTableDefinition() {
        columnDefinitions.add(new DeploymentColumnDefinition("DEPLOYMENT_ID", DEPLOYMENT_COLUMN_SIZE));
        columnDefinitions.add(new ShortTextColumnDefinition("LIQUIBASE", LIQUIBASE_SIZE));
        columnDefinitions.add(new ShortTextColumnDefinition("MD5SUM", MD5SUM_SIZE));
        columnDefinitions.add(new SimpleTextColumnDefinition("DESCRIPTION", DESCRIPTON_SIZE));
        columnDefinitions.add(new SimpleTextColumnDefinition("TAG", TAG_SIZE));
        columnDefinitions.add(new SimpleTextColumnDefinition("COMMENTS", COMMENTS_SIZE));
        columnDefinitions.add(new ColumnWithDefaultValueDefinition("ORDEREXECUTED", ORDEREXECUTED_DEFAULT_VALUE, "int"));
        columnDefinitions.add(new TextColumnWithDefaultValueDefinition("EXECTYPE", "EXECUTED", EXECTYPE_SIZE));
        columnDefinitions.add(new SimpleTextColumnIgnoreDifferentTypeDefinition("CONTEXTS", CONTEXTS_SIZE));
        columnDefinitions.add(new SimpleTextColumnIgnoreDifferentTypeDefinition("LABELS", LABELS_SIZE));
    }

    public List<ChangeLogColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
    }
}
