package liquibase.diff;

import liquibase.database.Database;
import liquibase.database.structure.*;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import liquibase.util.StringUtils;

import java.util.*;

public class Diff {

	private Database referenceDatabase;
	private Database targetDatabase;

	private DatabaseSnapshot referenceSnapshot;
	private DatabaseSnapshot targetSnapshot;

	private Set<DiffStatusListener> statusListeners = new HashSet<DiffStatusListener>();

	private boolean diffTables = true;
	private boolean diffColumns = true;
	private boolean diffViews = true;
	private boolean diffPrimaryKeys = true;
	private boolean diffUniqueConstraints = true;
	private boolean diffIndexes = true;
	private boolean diffForeignKeys = true;
	private boolean diffSequences = true;
	private boolean diffData = false;

	public Diff(Database referenceDatabase, Database targetDatabase) {
		this.referenceDatabase = referenceDatabase;

		this.targetDatabase = targetDatabase;
	}

	public Diff(Database originalDatabase, String schema)
			throws DatabaseException {
		targetDatabase = null;

		referenceDatabase = originalDatabase;
		referenceDatabase.setDefaultSchemaName(schema);
	}

	public Diff(DatabaseSnapshot referenceSnapshot,
			DatabaseSnapshot targetDatabaseSnapshot) {
		this.referenceSnapshot = referenceSnapshot;

		this.targetSnapshot = targetDatabaseSnapshot;
	}

	public void addStatusListener(DiffStatusListener listener) {
		statusListeners.add(listener);
	}

	public void removeStatusListener(DiffStatusListener listener) {
		statusListeners.remove(listener);
	}

	public DiffResult compare() throws DatabaseException {
		if (referenceSnapshot == null) {
			referenceSnapshot = DatabaseSnapshotGeneratorFactory.getInstance()
					.createSnapshot(referenceDatabase, null, statusListeners);
		}

		if (targetSnapshot == null) {
			if (targetDatabase == null) {
				targetSnapshot = new DatabaseSnapshot(referenceDatabase, null);
			} else {
				targetSnapshot = DatabaseSnapshotGeneratorFactory.getInstance()
						.createSnapshot(targetDatabase, null, statusListeners);
			}
		}

		DiffResult diffResult = new DiffResult(referenceSnapshot,
				targetSnapshot);
		checkVersionInfo(diffResult);
		if (shouldDiffTables()) {
			checkTables(diffResult);
		}
		if (shouldDiffViews()) {
			checkViews(diffResult);
		}
		if (shouldDiffColumns()) {
			checkColumns(diffResult);
		}
		if (shouldDiffForeignKeys()) {
			checkForeignKeys(diffResult);
		}
		if (shouldDiffPrimaryKeys()) {
			checkPrimaryKeys(diffResult);
		}
		if (shouldDiffUniqueConstraints()) {
			checkUniqueConstraints(diffResult);
		}
		if (shouldDiffIndexes()) {
			checkIndexes(diffResult);
		}
		if (shouldDiffSequences()) {
			checkSequences(diffResult);
		}
		diffResult.setDiffData(shouldDiffData());

        // Hack:  Sometimes Indexes or Unique Constraints with multiple columns get added twice (1 for each column),
		// so we're combining them back to a single Index or Unique Constraint here.
		removeDuplicateIndexes( diffResult.getMissingIndexes() );
		removeDuplicateIndexes( diffResult.getUnexpectedIndexes() );
		removeDuplicateUniqueConstraints( diffResult.getMissingUniqueConstraints() );
		removeDuplicateUniqueConstraints( diffResult.getUnexpectedUniqueConstraints() );
        
		return diffResult;
	}

	public void setDiffTypes(String diffTypes) {
		if (StringUtils.trimToNull(diffTypes) != null) {
			Set<String> types = new HashSet<String>(Arrays.asList(diffTypes.toLowerCase().split("\\s*,\\s*")));
            
			diffTables = types.contains("tables");
			diffColumns = types.contains("columns");
			diffViews = types.contains("views");
			diffPrimaryKeys = types.contains("primaryKeys".toLowerCase());
			diffUniqueConstraints = types.contains("uniqueConstraints".toLowerCase());
			diffIndexes = types.contains("indexes");
			diffForeignKeys = types.contains("foreignKeys".toLowerCase());
			diffSequences = types.contains("sequences");
			diffData = types.contains("data");
		}
	}

	public boolean shouldDiffTables() {
		return diffTables;
	}

	public void setDiffTables(boolean diffTables) {
		this.diffTables = diffTables;
	}

	public boolean shouldDiffColumns() {
		return diffColumns;
	}

	public void setDiffColumns(boolean diffColumns) {
		this.diffColumns = diffColumns;
	}

	public boolean shouldDiffViews() {
		return diffViews;
	}

	public void setDiffViews(boolean diffViews) {
		this.diffViews = diffViews;
	}

	public boolean shouldDiffPrimaryKeys() {
		return diffPrimaryKeys;
	}

	public void setDiffPrimaryKeys(boolean diffPrimaryKeys) {
		this.diffPrimaryKeys = diffPrimaryKeys;
	}

	public boolean shouldDiffIndexes() {
		return diffIndexes;
	}

	public void setDiffIndexes(boolean diffIndexes) {
		this.diffIndexes = diffIndexes;
	}

	public boolean shouldDiffForeignKeys() {
		return diffForeignKeys;
	}

	public void setDiffForeignKeys(boolean diffForeignKeys) {
		this.diffForeignKeys = diffForeignKeys;
	}

	public boolean shouldDiffSequences() {
		return diffSequences;
	}

	public void setDiffSequences(boolean diffSequences) {
		this.diffSequences = diffSequences;
	}

	public boolean shouldDiffData() {
		return diffData;
	}

	public void setDiffData(boolean diffData) {
		this.diffData = diffData;
	}

	public boolean shouldDiffUniqueConstraints() {
		return this.diffUniqueConstraints;
	}

	public void setDiffUniqueConstraints(boolean diffUniqueConstraints) {
		this.diffUniqueConstraints = diffUniqueConstraints;
	}

	private void checkVersionInfo(DiffResult diffResult)
			throws DatabaseException {

		if (targetDatabase != null) {
			diffResult.setProductName(new DiffComparison(referenceDatabase
					.getDatabaseProductName(), targetDatabase
					.getDatabaseProductName()));
			diffResult.setProductVersion(new DiffComparison(referenceDatabase
					.getDatabaseProductVersion(), targetDatabase
					.getDatabaseProductVersion()));
		}

	}

	private void checkTables(DiffResult diffResult) {
		for (Table baseTable : referenceSnapshot.getTables()) {
			if (!targetSnapshot.getTables().contains(baseTable)) {
				diffResult.addMissingTable(baseTable);
			}
		}

		for (Table targetTable : targetSnapshot.getTables()) {
			if (!referenceSnapshot.getTables().contains(targetTable)) {
				diffResult.addUnexpectedTable(targetTable);
			}
		}
	}

	private void checkViews(DiffResult diffResult) {
		for (View baseView : referenceSnapshot.getViews()) {
			if (!targetSnapshot.getViews().contains(baseView)) {
				diffResult.addMissingView(baseView);
			}
		}

		for (View targetView : targetSnapshot.getViews()) {
			if (!referenceSnapshot.getViews().contains(targetView)) {
				diffResult.addUnexpectedView(targetView);
			} else {
				for (View referenceView : referenceSnapshot.getViews()) {
					if (referenceView.getName().equals(targetView.getName())) {
						if (!referenceView.getDefinition().equals(targetView.getDefinition())) {
							diffResult.addChangedView(referenceView);
						}
					}
				}
			}
		}
	}

	private void checkColumns(DiffResult diffResult) {
		for (Column baseColumn : referenceSnapshot.getColumns()) {
			if (!targetSnapshot.getColumns().contains(baseColumn)
					&& (baseColumn.getTable() == null || !diffResult
							.getMissingTables().contains(baseColumn.getTable()))
					&& (baseColumn.getView() == null || !diffResult
							.getMissingViews().contains(baseColumn.getView()))) {
				diffResult.addMissingColumn(baseColumn);
			}
		}

		for (Column targetColumn : targetSnapshot.getColumns()) {
			if (!referenceSnapshot.getColumns().contains(targetColumn)
					&& (targetColumn.getTable() == null || !diffResult
							.getUnexpectedTables().contains(
									targetColumn.getTable()))
					&& (targetColumn.getView() == null || !diffResult
							.getUnexpectedViews().contains(
									targetColumn.getView()))) {
				diffResult.addUnexpectedColumn(targetColumn);
			} else if (targetColumn.getTable() != null
					&& !diffResult.getUnexpectedTables().contains(
							targetColumn.getTable())) {
				Column baseColumn = referenceSnapshot.getColumn(targetColumn
						.getTable().getName(), targetColumn.getName());

				if (baseColumn == null || targetColumn.isDifferent(baseColumn)) {
					diffResult.addChangedColumn(targetColumn);
				}
			}
		}
	}

	private void checkForeignKeys(DiffResult diffResult) {
		for (ForeignKey baseFK : referenceSnapshot.getForeignKeys()) {
			if (!targetSnapshot.getForeignKeys().contains(baseFK)) {
				diffResult.addMissingForeignKey(baseFK);
			}
		}

		for (ForeignKey targetFK : targetSnapshot.getForeignKeys()) {
			if (!referenceSnapshot.getForeignKeys().contains(targetFK)) {
				diffResult.addUnexpectedForeignKey(targetFK);
			}
		}
	}

	private void checkUniqueConstraints(DiffResult diffResult) {
		for (UniqueConstraint baseIndex : referenceSnapshot
				.getUniqueConstraints()) {
			if (!targetSnapshot.getUniqueConstraints().contains(baseIndex)) {
				diffResult.addMissingUniqueConstraint(baseIndex);
			}
		}

		for (UniqueConstraint targetIndex : targetSnapshot
				.getUniqueConstraints()) {
			if (!referenceSnapshot.getUniqueConstraints().contains(targetIndex)) {
				diffResult.addUnexpectedUniqueConstraint(targetIndex);
			}
		}
	}

	private void checkIndexes(DiffResult diffResult) {
		for (Index baseIndex : referenceSnapshot.getIndexes()) {
			if (!targetSnapshot.getIndexes().contains(baseIndex)) {
				diffResult.addMissingIndex(baseIndex);
			}
		}

		for (Index targetIndex : targetSnapshot.getIndexes()) {
			if (!referenceSnapshot.getIndexes().contains(targetIndex)) {
				diffResult.addUnexpectedIndex(targetIndex);
			}
		}
	}

	private void checkPrimaryKeys(DiffResult diffResult) {
		for (PrimaryKey basePrimaryKey : referenceSnapshot.getPrimaryKeys()) {
			if (!targetSnapshot.getPrimaryKeys().contains(basePrimaryKey)) {
				diffResult.addMissingPrimaryKey(basePrimaryKey);
			}
		}

		for (PrimaryKey targetPrimaryKey : targetSnapshot.getPrimaryKeys()) {
			if (!referenceSnapshot.getPrimaryKeys().contains(targetPrimaryKey)) {
				diffResult.addUnexpectedPrimaryKey(targetPrimaryKey);
			}
		}
	}

	private void checkSequences(DiffResult diffResult) {
		for (Sequence baseSequence : referenceSnapshot.getSequences()) {
			if (!targetSnapshot.getSequences().contains(baseSequence)) {
				diffResult.addMissingSequence(baseSequence);
			}
		}

		for (Sequence targetSequence : targetSnapshot.getSequences()) {
			if (!referenceSnapshot.getSequences().contains(targetSequence)) {
				diffResult.addUnexpectedSequence(targetSequence);
			}
		}
	}

    /**
	 * Removes duplicate Indexes from the DiffResult object.
	 *
	 * @param indexes [IN/OUT] - A set of Indexes to be updated.
	 */
	private void removeDuplicateIndexes( SortedSet<Index> indexes )
	{
		SortedSet<Index> combinedIndexes = new TreeSet<Index>();
		SortedSet<Index> indexesToRemove = new TreeSet<Index>();

		// Find Indexes with the same name, copy their columns into the first one,
		// then remove the duplicate Indexes.
		for ( Index idx1 : indexes )
		{
			if ( !combinedIndexes.contains( idx1 ) )
			{
				for ( Index idx2 : indexes.tailSet( idx1 ) )
				{
					if ( idx1 == idx2 ) {
						continue;
					}

					if ( idx1.getName().equalsIgnoreCase( idx2.getName() )
							&& idx1.getTable().getName().equalsIgnoreCase( idx2.getTable().getName() ) )
					{
						for ( String column : idx2.getColumns() )
						{
							if ( !idx1.getColumns().contains( column ) ) {
								idx1.getColumns().add( column );
							}
						}

						indexesToRemove.add( idx2 );
					}
				}

				combinedIndexes.add( idx1 );
			}
		}

		// Sort the columns.
		Set<String> sortedColumns = new TreeSet<String>();
		for ( Index idx : combinedIndexes )
		{
			List<String> columns = idx.getColumns();
			sortedColumns.clear();
			sortedColumns.addAll( columns );
			columns.clear();
			columns.addAll( sortedColumns );
		}

		indexes.removeAll( indexesToRemove );
	}

	/**
	 * Removes duplicate Unique Constraints from the DiffResult object.
	 *
	 * @param uniqueConstraints [IN/OUT] - A set of Unique Constraints to be updated.
	 */
	private void removeDuplicateUniqueConstraints( SortedSet<UniqueConstraint> uniqueConstraints ) {
		SortedSet<UniqueConstraint> combinedConstraints = new TreeSet<UniqueConstraint>();
		SortedSet<UniqueConstraint> constraintsToRemove = new TreeSet<UniqueConstraint>();

		// Find UniqueConstraints with the same name, copy their columns into the first one,
		// then remove the duplicate UniqueConstraints.
		for ( UniqueConstraint uc1 : uniqueConstraints )
		{
			if ( !combinedConstraints.contains( uc1 ) )
			{
				for ( UniqueConstraint uc2 : uniqueConstraints.tailSet( uc1 ) )
				{
					if ( uc1 == uc2 ) {
						continue;
					}

					if ( uc1.getName().equalsIgnoreCase( uc2.getName() )
							&& uc1.getTable().getName().equalsIgnoreCase( uc2.getTable().getName() ) )
					{
						for ( String column : uc2.getColumns() )
						{
							if ( !uc1.getColumns().contains( column ) ) {
								uc1.getColumns().add( column );
							}
						}

						constraintsToRemove.add( uc2 );
					}
				}

				combinedConstraints.add( uc1 );
			}
		}

		// Sort the columns.
		Set<String> sortedColumns = new TreeSet<String>();
		for ( UniqueConstraint uc : combinedConstraints )
		{
			List<String> columns = uc.getColumns();
			sortedColumns.clear();
			sortedColumns.addAll( columns );
			columns.clear();
			columns.addAll( sortedColumns );
		}

		uniqueConstraints.removeAll( constraintsToRemove );
	}
}
