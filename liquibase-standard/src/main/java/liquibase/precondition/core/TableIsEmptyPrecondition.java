package liquibase.precondition.core;

public class TableIsEmptyPrecondition extends RowCountPrecondition {

    public TableIsEmptyPrecondition() {
        this.setExpectedRows(0L);
    }

    @Override
    protected String getFailureMessage(long result, long expectedRows) {
        return "Table "+getTableName()+" is not empty. Contains "+result+" rows";
    }

    @Override
    public String getName() {
        return "tableIsEmpty";
    }

}
