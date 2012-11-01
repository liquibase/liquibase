package liquibase.diff.core;

import liquibase.database.Database;
import liquibase.diff.StringDiff;
import liquibase.diff.DiffControl;
import liquibase.diff.DiffGenerator;
import liquibase.diff.DiffResult;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.DatabaseObject;

public class StandardDiffGenerator implements DiffGenerator {

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(Database referenceDatabase, Database comparisonDatabase) {
        return true;
    }

    public DiffResult compare(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, DiffControl diffControl) throws DatabaseException {

        if (comparisonSnapshot == null) {
            comparisonSnapshot = new DatabaseSnapshot(referenceSnapshot.getDatabase()); //, diffControl.toSnapshotControl(DiffControl.DatabaseRole.REFERENCE));
        }

        DiffResult diffResult = new DiffResult(referenceSnapshot, comparisonSnapshot, diffControl);
        checkVersionInfo(referenceSnapshot, comparisonSnapshot, diffResult);

        for (Class<? extends DatabaseObject> typeToCompare : diffResult.getComparedTypes()) {
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
            diffResult.setProductName(new StringDiff(referenceSnapshot.getDatabase().getDatabaseProductName(), comparisonSnapshot.getDatabase().getDatabaseProductName()));
            diffResult.setProductVersion(new StringDiff(referenceSnapshot.getDatabase().getDatabaseProductVersion(), comparisonSnapshot.getDatabase().getDatabaseProductVersion()));
        }

    }

    protected <T extends DatabaseObject> void compareObjectType(Class<T> type, DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, DiffResult diffResult) {

//todo        for (DiffControl.SchemaComparison schemaComparison : diffResult.getDiffControl().getSchemaComparisons()) {
//            Schema referenceSchema = referenceSnapshot.getDatabase().correctSchema(schemaComparison.getReferenceSchema());
//            Schema comparisonSchema = null;
//            if (comparisonSnapshot.getDatabase() != null) {
//                comparisonSchema = comparisonSnapshot.getDatabase().correctSchema(schemaComparison.getComparisonSchema());
//            }
//            for (T referenceObject : referenceSnapshot.getDatabaseObjects(referenceSchema, type)) {
//                if (referenceObject instanceof Table && referenceSnapshot.getDatabase().isLiquibaseTable(referenceSchema, referenceObject.getName())) {
//                    continue;
//                }
//                if (comparisonSnapshot.contains(comparisonSchema, referenceObject)) {
//                    if (!comparisonSnapshot.matches(comparisonSchema, referenceObject)) {
//                        diffResult.getObjectDiff(type).addChanged(referenceObject);
//                    }
//                } else {
//                    diffResult.getObjectDiff(type).addMissing(referenceObject);
//                }
//            }
//
//            for (T targetObject : comparisonSnapshot.getDatabaseObjects(comparisonSchema, type)) {
//                if (targetObject instanceof Table && comparisonSnapshot.getDatabase().isLiquibaseTable(comparisonSchema, targetObject.getName())) {
//                    continue;
//                }
//                if (!referenceSnapshot.contains(referenceSchema, targetObject)) {
//                    diffResult.getObjectDiff(type).addUnexpected(targetObject);
//                }
//            }
//        }

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
