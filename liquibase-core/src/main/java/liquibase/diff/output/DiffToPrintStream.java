package liquibase.diff.output;

import java.io.PrintStream;
import java.util.SortedSet;

import liquibase.database.structure.DatabaseObject;
import liquibase.diff.DiffResult;
import liquibase.diff.StringDiff;
import liquibase.exception.DatabaseException;

public class DiffToPrintStream {

    private DiffResult diffResult;
    private PrintStream out;

    public DiffToPrintStream(DiffResult diffResult, PrintStream out) {
        this.diffResult = diffResult;
        this.out = out;
    }

    public void print() throws DatabaseException {
        out.println("Reference Database: " + diffResult.getReferenceSnapshot().getDatabase());
        out.println("Comparison Database: " + diffResult.getComparisonSnapshot().getDatabase());

        printComparison("Product Name", diffResult.getProductName(), out);
        printComparison("Product Version", diffResult.getProductVersion(), out);
        
        for (Class<? extends DatabaseObject> type : diffResult.getDiffControl().getTypesToCompare()) {
            printSetComparison("Missing " + getTypeName(type), diffResult.getObjectDiff(type).getMissing(), out);
            printSetComparison("Unexpected "+getTypeName(type), diffResult.getObjectDiff(type).getUnexpected(), out);
            printSetComparison("Changed "+getTypeName(type), diffResult.getObjectDiff(type).getChanged(), out);

        }
        
//        printColumnComparison(diffResult.getColumns().getChanged(), out);
    }

    protected String getTypeName(Class<? extends DatabaseObject> type) {
        return type.getSimpleName().replaceAll("([A-Z])", " $1").trim() + "(s)";
    }

    protected void printSetComparison(String title, SortedSet<?> objects,
                                    PrintStream out) {
        out.print(title + ": ");
        if (objects.size() == 0) {
            out.println("NONE");
        } else {
            out.println();
            for (Object object : objects) {
                out.println("     " + object);
            }
        }
    }

//    private void printColumnComparison(SortedSet<Column> changedColumns,
//                                       PrintStream out) {
//        out.print("Changed Columns: ");
//        if (changedColumns.size() == 0) {
//            out.println("NONE");
//        } else {
//            out.println();
//            for (Column column : changedColumns) {
//                out.println("     " + column);
//                Column baseColumn = diffResult.getReferenceSnapshot().getColumn(column.getRelation().getSchema(),
//                        column.getRelation().getName(), column.getName());
//                if (baseColumn != null) {
//                    if (baseColumn.isDataTypeDifferent(column)) {
//                        out.println("           from "
//                                + baseColumn.getType()
//                                + " to "
//                                + diffResult.getComparisonSnapshot().getColumn(column.getRelation().getSchema(), column.getRelation().getName(), column.getName()).getType());
//                    }
//                    if (baseColumn.isNullabilityDifferent(column)) {
//                        Boolean nowNullable = diffResult.getComparisonSnapshot().getColumn(column.getRelation().getSchema(),
//                                column.getRelation().getName(), column.getName())
//                                .isNullable();
//                        if (nowNullable == null) {
//                            nowNullable = Boolean.TRUE;
//                        }
//                        if (nowNullable) {
//                            out.println("           now nullable");
//                        } else {
//                            out.println("           now not null");
//                        }
//                    }
//                }
//            }
//        }
//    }

    protected void printComparison(String title, StringDiff string, PrintStream out) {
        out.print(title + ":");

        if (string == null) {
            out.print("NULL");
            return;
        }

        if (string.areEqual()) {
            out.println(" EQUAL");
        } else {
            out.println();
            out.println("     Reference:   '"
                    + string.getReferenceVersion() + "'");
            out.println("     Target: '" + string.getTargetVersion() + "'");
        }

    }

}
