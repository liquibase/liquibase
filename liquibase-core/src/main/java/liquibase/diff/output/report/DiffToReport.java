package liquibase.diff.output.report;

import liquibase.database.Database;
import liquibase.diff.DiffResult;
import liquibase.diff.Difference;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.StringDiff;
import liquibase.diff.compare.CompareControl;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectComparator;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtils;

import java.io.PrintStream;
import java.util.*;

public class DiffToReport {

    private DiffResult diffResult;
    private PrintStream out;

    public DiffToReport(DiffResult diffResult, PrintStream out) {
        this.diffResult = diffResult;
        this.out = out;
    }

    public void print() throws DatabaseException {
        final DatabaseObjectComparator comparator = new DatabaseObjectComparator();
        out.println("Reference Database: " + diffResult.getReferenceSnapshot().getDatabase());
        out.println("Comparison Database: " + diffResult.getComparisonSnapshot().getDatabase());

        CompareControl.SchemaComparison[] schemas = diffResult.getCompareControl().getSchemaComparisons();
        if ((schemas != null) && (schemas.length > 0)) {
            out.println("Compared Schemas: " + StringUtils.join(Arrays.asList(schemas), ", ", new StringUtils.StringUtilsFormatter<CompareControl.SchemaComparison>() {
                @Override
                public String toString(CompareControl.SchemaComparison obj) {
                    String referenceName;
                    String comparisonName;

                    Database referenceDatabase = diffResult.getReferenceSnapshot().getDatabase();
                    Database comparisonDatabase = diffResult.getComparisonSnapshot().getDatabase();

                    if (referenceDatabase.supportsSchemas()) {
                        referenceName = obj.getReferenceSchema().getSchemaName();
                        if (referenceName == null) {
                            referenceName = referenceDatabase.getDefaultSchemaName();
                        }
                    } else if (referenceDatabase.supportsCatalogs()) {
                        referenceName = obj.getReferenceSchema().getCatalogName();
                        if (referenceName == null) {
                            referenceName = referenceDatabase.getDefaultCatalogName();
                        }
                    } else {
                        return "";
                    }

                    if (comparisonDatabase.supportsSchemas()) {
                        comparisonName = obj.getComparisonSchema().getSchemaName();
                        if (comparisonName == null) {
                            comparisonName = comparisonDatabase.getDefaultSchemaName();
                        }
                    } else if (comparisonDatabase.supportsCatalogs()) {
                        comparisonName = obj.getComparisonSchema().getCatalogName();
                        if (comparisonName == null) {
                            comparisonName = comparisonDatabase.getDefaultCatalogName();
                        }
                    } else {
                        return "";
                    }

                    if (referenceName == null) {
                        referenceName = StringUtils.trimToEmpty(referenceDatabase.getDefaultSchemaName());
                    }

                    if (comparisonName == null) {
                        comparisonName = StringUtils.trimToEmpty(comparisonDatabase.getDefaultSchemaName());
                    }

                    if (referenceName.equalsIgnoreCase(comparisonName)) {
                        return referenceName;
                    } else {
                        return referenceName + " -> " + comparisonName;
                    }
                }
            }, true));
        }

        printComparison("Product Name", diffResult.getProductNameDiff(), out);
        printComparison("Product Version", diffResult.getProductVersionDiff(), out);


        TreeSet<Class<? extends DatabaseObject>> types = new TreeSet<>(new Comparator<Class<? extends DatabaseObject>>() {
            @Override
            public int compare(Class<? extends DatabaseObject> o1, Class<? extends DatabaseObject> o2) {
                return o1.getSimpleName().compareTo(o2.getSimpleName());
            }
        });
        types.addAll(diffResult.getCompareControl().getComparedTypes());
        for (Class<? extends DatabaseObject> type : types) {
            if (type.equals(Schema.class) && !diffResult.getComparisonSnapshot().getDatabase().supportsSchemas()) {
                continue;
            }
            printSetComparison("Missing " + getTypeName(type), diffResult.getMissingObjects(type, comparator), out);
            printSetComparison("Unexpected " + getTypeName(type), diffResult.getUnexpectedObjects(type, comparator), out);

            printChangedComparison("Changed " + getTypeName(type), diffResult.getChangedObjects(type, comparator), out);

        }

//        printColumnComparison(diffResult.getColumns().getChanged(), out);
    }

    protected String getTypeName(Class<? extends DatabaseObject> type) {
        return type.getSimpleName().replaceAll("([A-Z])", " $1").trim() + "(s)";
    }

    protected boolean getIncludeSchema() {
        return diffResult.getCompareControl().getSchemaComparisons().length > 1;
    }

    protected void printChangedComparison(String title, Map<? extends DatabaseObject, ObjectDifferences> objects, PrintStream out) {
        out.print(title + ": ");
        if (objects.isEmpty()) {
            out.println("NONE");
        } else {
            out.println();
            for (Map.Entry<? extends DatabaseObject, ObjectDifferences> object : objects.entrySet()) {
                if (object.getValue().hasDifferences()) {
                    out.println("     " + object.getKey());
                    for (Difference difference : object.getValue().getDifferences()) {
                        out.println("          " + difference.toString());
                    }
                }
            }
        }
    }

    protected void printSetComparison(String title, Set<? extends DatabaseObject> objects, PrintStream out) {
        out.print(title + ": ");
        Schema lastSchema = null;
        if (objects.isEmpty()) {
            out.println("NONE");
        } else {
            out.println();
            for (DatabaseObject object : objects) {
                if (getIncludeSchema() && (object.getSchema() != null) && ((lastSchema == null) || !lastSchema.equals
                    (object.getSchema()))) {
                    lastSchema = object.getSchema();
                    String schemaName = object.getSchema().getName();
                    if (schemaName == null) {
                        schemaName = object.getSchema().getCatalogName();
                    }
                    schemaName = includeSchemaComparison(schemaName);

                    out.println("  SCHEMA: " + schemaName);
                }
                out.println("     " + object);
            }
        }
    }

    protected String includeSchemaComparison(String schemaName) {
        String convertedSchemaName = CompareControl.SchemaComparison.convertSchema(schemaName, diffResult.getCompareControl().getSchemaComparisons());

        if ((convertedSchemaName != null) && !convertedSchemaName.equals(schemaName)) {
            schemaName = schemaName + " -> " + convertedSchemaName;
        }
        return schemaName;
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
            String referenceVersion = string.getReferenceVersion();
            if (referenceVersion == null) {
                referenceVersion = "NULL";
            } else {
                referenceVersion = "'" + referenceVersion + "'";
            }

            String targetVersion = string.getTargetVersion();
            if (targetVersion == null) {
                targetVersion = "NULL";
            } else {
                targetVersion = "'" + targetVersion + "'";
            }


            out.println();
            out.println("     Reference:   "
                    + referenceVersion);
            out.println("     Target: " + targetVersion);
        }

    }

}
