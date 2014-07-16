package liquibase.diff.core;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.diff.*;
import liquibase.diff.compare.CompareControl;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.EmptyDatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.JdbcDatabaseSnapshot;
import liquibase.structure.DatabaseObject;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;

import java.util.Set;

public class StandardDiffGenerator implements DiffGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean supports(Database referenceDatabase, Database comparisonDatabase) {
        return true;
    }

    @Override
    public DiffResult compare(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, CompareControl compareControl) throws DatabaseException {

        if (comparisonSnapshot == null) {
            try {
                comparisonSnapshot = new EmptyDatabaseSnapshot(referenceSnapshot.getDatabase()); //, compareControl.toSnapshotControl(CompareControl.DatabaseRole.REFERENCE));
            } catch (InvalidExampleException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }

        DiffResult diffResult = new DiffResult(referenceSnapshot, comparisonSnapshot, compareControl);
        checkVersionInfo(referenceSnapshot, comparisonSnapshot, diffResult);

        Set<Class<? extends DatabaseObject>> typesToCompare = compareControl.getComparedTypes();
        typesToCompare.retainAll(referenceSnapshot.getSnapshotControl().getTypesToInclude());
        typesToCompare.retainAll(comparisonSnapshot.getSnapshotControl().getTypesToInclude());

        for (Class<? extends DatabaseObject> typeToCompare : typesToCompare) {
            compareObjectType(typeToCompare, referenceSnapshot, comparisonSnapshot, diffResult);
        }

//        // Hack:  Sometimes Indexes or Unique Constraints with multiple columns get added twice (1 for each column),
//        // so we're combining them back to a single Index or Unique Constraint here.
//        removeDuplicateIndexes( diffResult.getMissingIndexes() );
//        removeDuplicateIndexes( diffResult.getUnexpectedIndexes() );
//        removeDuplicateUniqueConstraints( diffResult.getMissingUniqueConstraints() );
//        removeDuplicateUniqueConstraints( diffResult.getUnexpectedUniqueConstraints() );

        return diffResult;
    }

    protected void checkVersionInfo(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, DiffResult diffResult) throws DatabaseException {

        if (comparisonSnapshot != null && comparisonSnapshot.getDatabase() != null) {
            diffResult.setProductNameDiff(new StringDiff(referenceSnapshot.getDatabase().getDatabaseProductName(), comparisonSnapshot.getDatabase().getDatabaseProductName()));
            diffResult.setProductVersionDiff(new StringDiff(referenceSnapshot.getDatabase().getDatabaseProductVersion(), comparisonSnapshot.getDatabase().getDatabaseProductVersion()));
        }

    }

    protected <T extends DatabaseObject> void compareObjectType(Class<T> type, DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, DiffResult diffResult) {

        CompareControl.SchemaComparison[] schemaComparisons = diffResult.getCompareControl().getSchemaComparisons();
        if (schemaComparisons != null) {
            for (CompareControl.SchemaComparison schemaComparison : schemaComparisons) {
                for (T referenceObject : referenceSnapshot.get(type)) {
                    //                if (referenceObject instanceof Table && referenceSnapshot.getDatabase().isLiquibaseTable(referenceSchema, referenceObject.getName())) {
                    //                    continue;
                    //                }
                    T comparisonObject = comparisonSnapshot.get(referenceObject);
                    if (comparisonObject == null) {
                        diffResult.addMissingObject(referenceObject);
                    } else {
                        ObjectDifferences differences = DatabaseObjectComparatorFactory.getInstance().findDifferences(referenceObject, comparisonObject, comparisonSnapshot.getDatabase(), diffResult.getCompareControl());
                        if (differences.hasDifferences()) {
                            diffResult.addChangedObject(referenceObject, differences);
                        }
                    }
                }
                //
                for (T comparisonObject : comparisonSnapshot.get(type)) {
                    //                if (targetObject instanceof Table && comparisonSnapshot.getDatabase().isLiquibaseTable(comparisonSchema, targetObject.getName())) {
                    //                    continue;
                    //                }
                    if (referenceSnapshot.get(comparisonObject) == null) {
                        diffResult.addUnexpectedObject(comparisonObject);
                    }
                    //            }
                }
            }

            //todo: add logic for when container is missing or unexpected also
        }

//    /**
//     * Removes duplicate Indexes from the DiffResult object.
//     *
//     * @param indexes [IN/OUT] - A set of Indexes to be updated.
//     */
//    private void removeDuplicateIndexes( SortedSet<Index> indexes )
//    {
//        SortedSet<Index> combinedIndexes = new TreeSet<Index>();
//        SortedSet<Index> indexesToRemove = new TreeSet<Index>();
//
//        // Find Indexes with the same name, copy their columns into the first one,
//        // then remove the duplicate Indexes.
//        for ( Index idx1 : indexes )
//        {
//            if ( !combinedIndexes.contains( idx1 ) )
//            {
//                for ( Index idx2 : indexes.tailSet( idx1 ) )
//                {
//                    if ( idx1 == idx2 ) {
//                        continue;
//                    }
//
//                    String index1Name = StringUtils.trimToEmpty(idx1.getName());
//                    String index2Name = StringUtils.trimToEmpty(idx2.getName());
//                    if ( index1Name.equalsIgnoreCase(index2Name)
//                            && idx1.getTable().getName().equalsIgnoreCase( idx2.getTable().getName() ) )
//                    {
//                        for ( String column : idx2.getColumns() )
//                        {
//                            if ( !idx1.getColumns().contains( column ) ) {
//                                idx1.getColumns().add( column );
//                            }
//                        }
//
//                        indexesToRemove.add( idx2 );
//                    }
//                }
//
//                combinedIndexes.add( idx1 );
//            }
//        }
//
//        indexes.removeAll( indexesToRemove );
//    }
//
//    /**
//     * Removes duplicate Unique Constraints from the DiffResult object.
//     *
//     * @param uniqueConstraints [IN/OUT] - A set of Unique Constraints to be updated.
//     */
//    private void removeDuplicateUniqueConstraints( SortedSet<UniqueConstraint> uniqueConstraints ) {
//        SortedSet<UniqueConstraint> combinedConstraints = new TreeSet<UniqueConstraint>();
//        SortedSet<UniqueConstraint> constraintsToRemove = new TreeSet<UniqueConstraint>();
//
//        // Find UniqueConstraints with the same name, copy their columns into the first one,
//        // then remove the duplicate UniqueConstraints.
//        for ( UniqueConstraint uc1 : uniqueConstraints )
//        {
//            if ( !combinedConstraints.contains( uc1 ) )
//            {
//                for ( UniqueConstraint uc2 : uniqueConstraints.tailSet( uc1 ) )
//                {
//                    if ( uc1 == uc2 ) {
//                        continue;
//                    }
//
//                    if ( uc1.getName().equalsIgnoreCase( uc2.getName() )
//                            && uc1.getTable().getName().equalsIgnoreCase( uc2.getTable().getName() ) )
//                    {
//                        for ( String column : uc2.getColumns() )
//                        {
//                            if ( !uc1.getColumns().contains( column ) ) {
//                                uc1.getColumns().add( column );
//                            }
//                        }
//
//                        constraintsToRemove.add( uc2 );
//                    }
//                }
//
//                combinedConstraints.add( uc1 );
//            }
//        }
//
//        uniqueConstraints.removeAll( constraintsToRemove );
//    }
    }
}
