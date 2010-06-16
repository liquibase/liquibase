package liquibase.diff;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.*;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.structure.*;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.parser.core.xml.LiquibaseEntityResolver;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.core.RawSqlStatement;
import liquibase.util.ISODateFormat;
import liquibase.util.StringUtils;
import liquibase.util.csv.CSVWriter;
import liquibase.util.xml.DefaultXmlWriter;
import liquibase.util.xml.XmlWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class DiffResult {

	private Long idRoot = new Date().getTime();
	private int changeNumber = 1;

	private DatabaseSnapshot referenceSnapshot;
	private DatabaseSnapshot targetSnapshot;

	private DiffComparison productName;
	private DiffComparison productVersion;

	private SortedSet<Table> missingTables = new TreeSet<Table>();
	private SortedSet<Table> unexpectedTables = new TreeSet<Table>();

	private SortedSet<View> missingViews = new TreeSet<View>();
	private SortedSet<View> unexpectedViews = new TreeSet<View>();
	private SortedSet<View> changedViews = new TreeSet<View>();

	private SortedSet<Column> missingColumns = new TreeSet<Column>();
	private SortedSet<Column> unexpectedColumns = new TreeSet<Column>();
	private SortedSet<Column> changedColumns = new TreeSet<Column>();

	private SortedSet<ForeignKey> missingForeignKeys = new TreeSet<ForeignKey>();
	private SortedSet<ForeignKey> unexpectedForeignKeys = new TreeSet<ForeignKey>();

	private SortedSet<Index> missingIndexes = new TreeSet<Index>();
	private SortedSet<Index> unexpectedIndexes = new TreeSet<Index>();

	private SortedSet<PrimaryKey> missingPrimaryKeys = new TreeSet<PrimaryKey>();
	private SortedSet<PrimaryKey> unexpectedPrimaryKeys = new TreeSet<PrimaryKey>();

	private SortedSet<UniqueConstraint> missingUniqueConstraints = new TreeSet<UniqueConstraint>();
	private SortedSet<UniqueConstraint> unexpectedUniqueConstraints = new TreeSet<UniqueConstraint>();

	private SortedSet<Sequence> missingSequences = new TreeSet<Sequence>();
	private SortedSet<Sequence> unexpectedSequences = new TreeSet<Sequence>();

	private boolean diffData = false;
	private String dataDir = null;
	private String changeSetContext;
	private String changeSetAuthor;

	public DiffResult(DatabaseSnapshot referenceDatabaseSnapshot,
			DatabaseSnapshot targetDatabaseSnapshot) {
		this.referenceSnapshot = referenceDatabaseSnapshot;

		if (targetDatabaseSnapshot == null) {
			targetDatabaseSnapshot = new DatabaseSnapshot(
					referenceDatabaseSnapshot.getDatabase(), null);
		}
		this.targetSnapshot = targetDatabaseSnapshot;
	}

	public DiffComparison getProductName() {
		return productName;
	}

	public void setProductName(DiffComparison productName) {
		this.productName = productName;
	}

	public DiffComparison getProductVersion() {
		return productVersion;
	}

	public void setProductVersion(DiffComparison product) {
		this.productVersion = product;
	}

	public void addMissingTable(Table table) {
		missingTables.add(table);
	}

	public SortedSet<Table> getMissingTables() {
		return missingTables;
	}

	public void addUnexpectedTable(Table table) {
		unexpectedTables.add(table);
	}

	public SortedSet<Table> getUnexpectedTables() {
		return unexpectedTables;
	}

	public void addMissingView(View viewName) {
		missingViews.add(viewName);
	}

	public SortedSet<View> getMissingViews() {
		return missingViews;
	}

	public void addUnexpectedView(View viewName) {
		unexpectedViews.add(viewName);
	}

	public SortedSet<View> getUnexpectedViews() {
		return unexpectedViews;
	}

	public void addChangedView(View viewName) {
		changedViews.add(viewName);
	}

	public SortedSet<View> getChangedViews() {
		return changedViews;
	}

	public void addMissingColumn(Column columnName) {
		missingColumns.add(columnName);
	}

	public SortedSet<Column> getMissingColumns() {
		return missingColumns;
	}

	public void addUnexpectedColumn(Column columnName) {
		unexpectedColumns.add(columnName);
	}

	public SortedSet<Column> getUnexpectedColumns() {
		return unexpectedColumns;
	}

	public void addChangedColumn(Column columnName) {
		changedColumns.add(columnName);
	}

	public SortedSet<Column> getChangedColumns() {
		return changedColumns;
	}

	public void addMissingForeignKey(ForeignKey fkName) {
		missingForeignKeys.add(fkName);
	}

	public SortedSet<ForeignKey> getMissingForeignKeys() {
		return missingForeignKeys;
	}

	public void addUnexpectedForeignKey(ForeignKey fkName) {
		unexpectedForeignKeys.add(fkName);
	}

	public SortedSet<ForeignKey> getUnexpectedForeignKeys() {
		return unexpectedForeignKeys;
	}

	public void addMissingIndex(Index fkName) {
		missingIndexes.add(fkName);
	}

	public SortedSet<Index> getMissingIndexes() {
		return missingIndexes;
	}

	public void addUnexpectedIndex(Index fkName) {
		unexpectedIndexes.add(fkName);
	}

	public SortedSet<Index> getUnexpectedIndexes() {
		return unexpectedIndexes;
	}

	public void addMissingPrimaryKey(PrimaryKey primaryKey) {
		missingPrimaryKeys.add(primaryKey);
	}

	public SortedSet<PrimaryKey> getMissingPrimaryKeys() {
		return missingPrimaryKeys;
	}

	public void addUnexpectedPrimaryKey(PrimaryKey primaryKey) {
		unexpectedPrimaryKeys.add(primaryKey);
	}

	public SortedSet<PrimaryKey> getUnexpectedPrimaryKeys() {
		return unexpectedPrimaryKeys;
	}

	public void addMissingSequence(Sequence sequence) {
		missingSequences.add(sequence);
	}

	public SortedSet<Sequence> getMissingSequences() {
		return missingSequences;
	}

	public void addUnexpectedSequence(Sequence sequence) {
		unexpectedSequences.add(sequence);
	}

	public SortedSet<Sequence> getUnexpectedSequences() {
		return unexpectedSequences;
	}

	public void addMissingUniqueConstraint(UniqueConstraint uniqueConstraint) {
		missingUniqueConstraints.add(uniqueConstraint);
	}

	public SortedSet<UniqueConstraint> getMissingUniqueConstraints() {
		return this.missingUniqueConstraints;
	}

	public void addUnexpectedUniqueConstraint(UniqueConstraint uniqueConstraint) {
		unexpectedUniqueConstraints.add(uniqueConstraint);
	}

	public SortedSet<UniqueConstraint> getUnexpectedUniqueConstraints() {
		return unexpectedUniqueConstraints;
	}

	public boolean shouldDiffData() {
		return diffData;
	}

	public void setDiffData(boolean diffData) {
		this.diffData = diffData;
	}

	public String getDataDir() {
		return dataDir;
	}

	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}

	public String getChangeSetContext() {
		return changeSetContext;
	}

	public void setChangeSetContext(String changeSetContext) {
		this.changeSetContext = changeSetContext;
	}

        public boolean differencesFound() throws DatabaseException,IOException{
            boolean differencesInData=false;
            if(shouldDiffData()) {
                List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
                addInsertDataChanges(changeSets, dataDir);
                differencesInData=!changeSets.isEmpty();
            }

            return getMissingColumns().size()>0 ||
                    getMissingForeignKeys().size()>0 ||
                    getMissingIndexes().size()>0 ||
                    getMissingPrimaryKeys().size()>0 ||
                    getMissingSequences().size()>0 ||
                    getMissingTables().size()>0 ||
                    getMissingUniqueConstraints().size()>0 ||
                    getMissingViews().size()>0 ||
                    getUnexpectedColumns().size()>0 ||
                    getUnexpectedForeignKeys().size()>0 ||
                    getUnexpectedIndexes().size()>0 ||
                    getUnexpectedPrimaryKeys().size()>0 ||
                    getUnexpectedSequences().size()>0 ||
                    getUnexpectedTables().size()>0 ||
                    getUnexpectedUniqueConstraints().size()>0 ||
                    getUnexpectedViews().size()>0 ||
                    differencesInData;
        }


	public void printResult(PrintStream out) throws DatabaseException {
		out.println("Reference Database: " + referenceSnapshot.getDatabase());
		out.println("Target Database: " + targetSnapshot.getDatabase());

		printComparision("Product Name", productName, out);
		printComparision("Product Version", productVersion, out);
		printSetComparison("Missing Tables", getMissingTables(), out);
		printSetComparison("Unexpected Tables", getUnexpectedTables(), out);
		printSetComparison("Missing Views", getMissingViews(), out);
		printSetComparison("Unexpected Views", getUnexpectedViews(), out);
		printSetComparison("Changed Views", getChangedViews(), out);
		printSetComparison("Missing Columns", getMissingColumns(), out);
		printSetComparison("Unexpected Columns", getUnexpectedColumns(), out);
		printColumnComparison(getChangedColumns(), out);
		printSetComparison("Missing Foreign Keys", getMissingForeignKeys(), out);
		printSetComparison("Unexpected Foreign Keys",
				getUnexpectedForeignKeys(), out);
		printSetComparison("Missing Primary Keys", getMissingPrimaryKeys(), out);
		printSetComparison("Unexpected Primary Keys",
				getUnexpectedPrimaryKeys(), out);
		printSetComparison("Missing Unique Constraints",
				getMissingUniqueConstraints(), out);
		printSetComparison("Unexpected Unique Constraints",
				getUnexpectedUniqueConstraints(), out);
		printSetComparison("Missing Indexes", getMissingIndexes(), out);
		printSetComparison("Unexpected Indexes", getUnexpectedIndexes(), out);
		printSetComparison("Missing Sequences", getMissingSequences(), out);
		printSetComparison("Unexpected Sequences", getUnexpectedSequences(),
				out);
	}

	private void printSetComparison(String title, SortedSet<?> objects,
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

	private void printColumnComparison(SortedSet<Column> changedColumns,
			PrintStream out) {
		out.print("Changed Columns: ");
		if (changedColumns.size() == 0) {
			out.println("NONE");
		} else {
			out.println();
			for (Column column : changedColumns) {
				out.println("     " + column);
				Column baseColumn = referenceSnapshot.getColumn(column
						.getTable().getName(), column.getName());
				if (baseColumn != null) {
					if (baseColumn.isDataTypeDifferent(column)) {
						out.println("           from "
								+ baseColumn
										.getDataTypeString(referenceSnapshot
												.getDatabase())
								+ " to "
								+ targetSnapshot.getColumn(
										column.getTable().getName(),
										column.getName()).getDataTypeString(
										targetSnapshot.getDatabase()));
					}
					if (baseColumn.isNullabilityDifferent(column)) {
						Boolean nowNullable = targetSnapshot.getColumn(
								column.getTable().getName(), column.getName())
								.isNullable();
						if (nowNullable == null) {
							nowNullable = Boolean.TRUE;
						}
						if (nowNullable) {
							out.println("           now nullable");
						} else {
							out.println("           now not null");
						}
					}
				}
			}
		}
	}

	private void printComparision(String title, DiffComparison comparison,
			PrintStream out) {
		out.print(title + ":");
		if (comparison.areTheSame()) {
			out.println(" EQUAL");
		} else {
			out.println();
			out.println("     Reference:   '"
					+ comparison.getReferenceVersion() + "'");
			out.println("     Target: '" + comparison.getTargetVersion() + "'");
		}

	}

	public void printChangeLog(String changeLogFile, Database targetDatabase)
			throws ParserConfigurationException, IOException, DatabaseException {
		this.printChangeLog(changeLogFile, targetDatabase,
				new DefaultXmlWriter());
	}

	public void printChangeLog(PrintStream out, Database targetDatabase)
			throws ParserConfigurationException, IOException, DatabaseException {
		this.printChangeLog(out, targetDatabase, new DefaultXmlWriter());
	}

	public void printChangeLog(String changeLogFile, Database targetDatabase,
			XmlWriter xmlWriter) throws ParserConfigurationException,
			IOException, DatabaseException {
		File file = new File(changeLogFile);
		if (!file.exists()) {
			LogFactory.getLogger().info(file + " does not exist, creating");
			FileOutputStream stream = new FileOutputStream(file);
			printChangeLog(new PrintStream(stream), targetDatabase, xmlWriter);
			stream.close();
		} else {
			LogFactory.getLogger().info(file + " exists, appending");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			printChangeLog(new PrintStream(out), targetDatabase, xmlWriter);

			String xml = new String(out.toByteArray());
			xml = xml.replaceFirst("(?ms).*<databaseChangeLog[^>]*>", "");
			xml = xml.replaceFirst("</databaseChangeLog>", "");
			xml = xml.trim();

			String lineSeparator = System.getProperty("line.separator");
			BufferedReader fileReader = new BufferedReader(new FileReader(file));
			String line;
			long offset = 0;
			while ((line = fileReader.readLine()) != null) {
				int index = line.indexOf("</databaseChangeLog>");
				if (index >= 0) {
					offset += index;
				} else {
					offset += line.getBytes().length;
					offset += lineSeparator.getBytes().length;
				}
			}
			fileReader.close();

			fileReader = new BufferedReader(new FileReader(file));
			fileReader.skip(offset);

			fileReader.close();

			// System.out.println("resulting XML: " + xml.trim());

			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
			randomAccessFile.seek(offset);
			randomAccessFile.writeBytes("    " + xml + lineSeparator);
			randomAccessFile.writeBytes("</databaseChangeLog>" + lineSeparator);
			randomAccessFile.close();

			// BufferedWriter fileWriter = new BufferedWriter(new
			// FileWriter(file));
			// fileWriter.append(xml);
			// fileWriter.close();
		}
	}

	/**
	 * Prints changeLog that would bring the target database to be the same as
	 * the reference database
	 */
	public void printChangeLog(PrintStream out, Database targetDatabase,
			XmlWriter xmlWriter) throws ParserConfigurationException,
			IOException, DatabaseException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		documentBuilder.setEntityResolver(new LiquibaseEntityResolver());

		Document doc = documentBuilder.newDocument();

		Element changeLogElement = doc.createElement("databaseChangeLog");
		changeLogElement.setAttribute("xmlns",
				"http://www.liquibase.org/xml/ns/dbchangelog");
		changeLogElement.setAttribute("xmlns:xsi",
				"http://www.w3.org/2001/XMLSchema-instance");
		changeLogElement
				.setAttribute(
						"xsi:schemaLocation",
						"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-"
								+ XMLChangeLogSAXParser.getSchemaVersion()
								+ ".xsd");

		doc.appendChild(changeLogElement);

		List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
		addMissingTableChanges(changeSets, targetDatabase);
		addMissingColumnChanges(changeSets, targetDatabase);
		addChangedColumnChanges(changeSets);
		addMissingPrimaryKeyChanges(changeSets);
		addUnexpectedPrimaryKeyChanges(changeSets);
		addMissingUniqueConstraintChanges(changeSets);
		addUnexpectedUniqueConstraintChanges(changeSets);

		if (diffData) {
			addInsertDataChanges(changeSets, dataDir);
		}

		addMissingForeignKeyChanges(changeSets);
		addUnexpectedForeignKeyChanges(changeSets);
		addMissingIndexChanges(changeSets);
		addUnexpectedIndexChanges(changeSets);
		addUnexpectedColumnChanges(changeSets);
		addMissingSequenceChanges(changeSets);
		addUnexpectedSequenceChanges(changeSets);
		addMissingViewChanges(changeSets);
		addUnexpectedViewChanges(changeSets);
		addChangedViewChanges(changeSets);
		addUnexpectedTableChanges(changeSets);

		XMLChangeLogSerializer changeLogSerializer = new XMLChangeLogSerializer();
		changeLogSerializer.setCurrentChangeLogFileDOM(doc);
		for (ChangeSet changeSet : changeSets) {
			doc.getDocumentElement().appendChild(
					changeLogSerializer.createNode(changeSet));
		}

		xmlWriter.write(doc, out);

		out.flush();
	}

	private ChangeSet generateChangeSet(Change change) {
		ChangeSet changeSet = generateChangeSet();
		changeSet.addChange(change);

		return changeSet;
	}

	private ChangeSet generateChangeSet() {
		return new ChangeSet(generateId(), getChangeSetAuthor(), false, false,
				null, null, getChangeSetContext(), null);
	}

	private String getChangeSetAuthor() {
		if (changeSetAuthor != null) {
			return changeSetAuthor;
		}
		String author = System.getProperty("user.name");
		if (StringUtils.trimToNull(author) == null) {
			return "diff-generated";
		} else {
			return author + " (generated)";
		}
	}

	public void setChangeSetAuthor(String changeSetAuthor) {
		this.changeSetAuthor = changeSetAuthor;
	}

	private String generateId() {
		return idRoot.toString() + "-" + changeNumber++;
	}

	private void addUnexpectedIndexChanges(List<ChangeSet> changes) {
		for (Index index : getUnexpectedIndexes()) {

			DropIndexChange change = new DropIndexChange();
			change.setTableName(index.getTable().getName());
			change.setSchemaName(index.getTable().getSchema());
			change.setIndexName(index.getName());
            change.setAssociatedWith(index.getAssociatedWithAsString());

			changes.add(generateChangeSet(change));
		}
	}

	private void addMissingIndexChanges(List<ChangeSet> changes) {
		for (Index index : getMissingIndexes()) {

			CreateIndexChange change = new CreateIndexChange();
			change.setTableName(index.getTable().getName());
			change.setTablespace(index.getTablespace());
			change.setSchemaName(index.getTable().getSchema());
			change.setIndexName(index.getName());
			change.setUnique(index.isUnique());
			change.setAssociatedWith(index.getAssociatedWithAsString());

			for (String columnName : index.getColumns()) {
				ColumnConfig column = new ColumnConfig();
				column.setName(columnName);
				change.addColumn(column);
			}
			changes.add(generateChangeSet(change));
		}
	}

	private void addUnexpectedPrimaryKeyChanges(List<ChangeSet> changes) {
		for (PrimaryKey pk : getUnexpectedPrimaryKeys()) {

			if (!getUnexpectedTables().contains(pk.getTable())) {
				DropPrimaryKeyChange change = new DropPrimaryKeyChange();
				change.setTableName(pk.getTable().getName());
				change.setSchemaName(pk.getTable().getSchema());
				change.setConstraintName(pk.getName());
				change.setTableName(pk.getTablespace());

				changes.add(generateChangeSet(change));
			}
		}
	}

	private void addMissingPrimaryKeyChanges(List<ChangeSet> changes) {
		for (PrimaryKey pk : getMissingPrimaryKeys()) {

			AddPrimaryKeyChange change = new AddPrimaryKeyChange();
			change.setTableName(pk.getTable().getName());
			change.setSchemaName(pk.getTable().getSchema());
			change.setConstraintName(pk.getName());
			change.setColumnNames(pk.getColumnNames());
			change.setTablespace(pk.getTablespace());

			changes.add(generateChangeSet(change));
		}
	}

	private void addUnexpectedUniqueConstraintChanges(List<ChangeSet> changes) {
		for (UniqueConstraint uc : getUnexpectedUniqueConstraints()) {
			// Need check for nulls here due to NullPointerException using Postgres
			if (null != uc) {
				if (null != uc.getTable()) {
					if (!getUnexpectedTables().contains(uc.getTable())) {
						DropUniqueConstraintChange change = new DropUniqueConstraintChange();
						change.setTableName(uc.getTable().getName());
						change.setSchemaName(uc.getTable().getSchema());
						change.setConstraintName(uc.getName());

						changes.add(generateChangeSet(change));
					}
				}
			}
		}
	}

	private void addMissingUniqueConstraintChanges(List<ChangeSet> changes) {
		for (UniqueConstraint uc : getMissingUniqueConstraints()) {
			// Need check for nulls here due to NullPointerException using Postgres
			if (null != uc)
				if (null != uc.getTable()) {
					AddUniqueConstraintChange change = new AddUniqueConstraintChange();
					change.setTableName(uc.getTable().getName());
					change.setTablespace(uc.getTablespace());
					change.setSchemaName(uc.getTable().getSchema());
					change.setConstraintName(uc.getName());
					change.setColumnNames(uc.getColumnNames());
					change.setDeferrable(uc.isDeferrable());
					change.setInitiallyDeferred(uc.isInitiallyDeferred());
					change.setDisabled(uc.isDisabled());
					changes.add(generateChangeSet(change));
				}
		}
	}

	private void addUnexpectedForeignKeyChanges(List<ChangeSet> changes) {
		for (ForeignKey fk : getUnexpectedForeignKeys()) {

			DropForeignKeyConstraintChange change = new DropForeignKeyConstraintChange();
			change.setConstraintName(fk.getName());
			change.setBaseTableName(fk.getForeignKeyTable().getName());
			change.setBaseTableSchemaName(fk.getForeignKeyTable().getSchema());

			changes.add(generateChangeSet(change));
		}
	}

	private void addMissingForeignKeyChanges(List<ChangeSet> changes) {
		for (ForeignKey fk : getMissingForeignKeys()) {

			AddForeignKeyConstraintChange change = new AddForeignKeyConstraintChange();
			change.setConstraintName(fk.getName());

			change.setReferencedTableName(fk.getPrimaryKeyTable().getName());
			change.setReferencedTableSchemaName(fk.getPrimaryKeyTable()
					.getSchema());
			change.setReferencedColumnNames(fk.getPrimaryKeyColumns());

			change.setBaseTableName(fk.getForeignKeyTable().getName());
			change.setBaseTableSchemaName(fk.getForeignKeyTable().getSchema());
			change.setBaseColumnNames(fk.getForeignKeyColumns());

			change.setDeferrable(fk.isDeferrable());
			change.setInitiallyDeferred(fk.isInitiallyDeferred());
			change.setOnUpdate(fk.getUpdateRule());
			change.setOnDelete(fk.getDeleteRule());

			change.setReferencesUniqueColumn(fk.getReferencesUniqueColumn());

			changes.add(generateChangeSet(change));
		}
	}

	private void addUnexpectedSequenceChanges(List<ChangeSet> changes) {
		for (Sequence sequence : getUnexpectedSequences()) {

			DropSequenceChange change = new DropSequenceChange();
			change.setSequenceName(sequence.getName());
			change.setSchemaName(sequence.getSchema());

			changes.add(generateChangeSet(change));
		}
	}

	private void addMissingSequenceChanges(List<ChangeSet> changes) {
		for (Sequence sequence : getMissingSequences()) {

			CreateSequenceChange change = new CreateSequenceChange();
			change.setSequenceName(sequence.getName());
			change.setSchemaName(sequence.getSchema());

			changes.add(generateChangeSet(change));
		}
	}

	private void addUnexpectedColumnChanges(List<ChangeSet> changes) {
		for (Column column : getUnexpectedColumns()) {
			if (!shouldModifyColumn(column)) {
				continue;
			}

			DropColumnChange change = new DropColumnChange();
			change.setTableName(column.getTable().getName());
			change.setSchemaName(column.getTable().getSchema());
			change.setColumnName(column.getName());

			changes.add(generateChangeSet(change));
		}
	}

	private void addMissingViewChanges(List<ChangeSet> changes) {
		for (View view : getMissingViews()) {

			CreateViewChange change = new CreateViewChange();
			change.setViewName(view.getName());
			change.setSchemaName(view.getSchema());
			String selectQuery = view.getDefinition();
			if (selectQuery == null) {
				selectQuery = "COULD NOT DETERMINE VIEW QUERY";
			}
			change.setSelectQuery(selectQuery);

			changes.add(generateChangeSet(change));
		}
	}

	private void addChangedViewChanges(List<ChangeSet> changes) {
		for (View view : getChangedViews()) {

			CreateViewChange change = new CreateViewChange();
			change.setViewName(view.getName());
			change.setSchemaName(view.getSchema());
			String selectQuery = view.getDefinition();
			if (selectQuery == null) {
				selectQuery = "COULD NOT DETERMINE VIEW QUERY";
			}
			change.setSelectQuery(selectQuery);
			change.setReplaceIfExists(true);

			changes.add(generateChangeSet(change));
		}
	}

	private void addChangedColumnChanges(List<ChangeSet> changes) {
		for (Column column : getChangedColumns()) {
			if (!shouldModifyColumn(column)) {
				continue;
			}

			boolean foundDifference = false;
			Column referenceColumn = referenceSnapshot.getColumn(column
					.getTable().getName(), column.getName());
			if (column.isDataTypeDifferent(referenceColumn)) {
				ModifyDataTypeChange change = new ModifyDataTypeChange();
				change.setTableName(column.getTable().getName());
				change.setSchemaName(column.getTable().getSchema());
				change.setColumnName(column.getName());
				change.setNewDataType(referenceColumn
						.getDataTypeString(targetSnapshot.getDatabase()));
				changes.add(generateChangeSet(change));
				foundDifference = true;
			}
			if (column.isNullabilityDifferent(referenceColumn)) {
				if (referenceColumn.isNullable() == null
						|| referenceColumn.isNullable()) {
					DropNotNullConstraintChange change = new DropNotNullConstraintChange();
					change.setTableName(column.getTable().getName());
					change.setSchemaName(column.getTable().getSchema());
					change.setColumnName(column.getName());
					change.setColumnDataType(referenceColumn
							.getDataTypeString(targetSnapshot.getDatabase()));

					changes.add(generateChangeSet(change));
					foundDifference = true;
				} else {
					AddNotNullConstraintChange change = new AddNotNullConstraintChange();
					change.setTableName(column.getTable().getName());
					change.setSchemaName(column.getTable().getSchema());
					change.setColumnName(column.getName());
					change.setColumnDataType(referenceColumn
							.getDataTypeString(targetSnapshot.getDatabase()));

					changes.add(generateChangeSet(change));
					foundDifference = true;
				}

			}
			if (!foundDifference) {
				throw new RuntimeException("Unknown difference");
			}
		}
	}

	private boolean shouldModifyColumn(Column column) {
		return column.getView() == null
				&& !referenceSnapshot.getDatabase().isLiquibaseTable(
						column.getTable().getName());

	}

	private void addUnexpectedViewChanges(List<ChangeSet> changes) {
		for (View view : getUnexpectedViews()) {

			DropViewChange change = new DropViewChange();
			change.setViewName(view.getName());
			change.setSchemaName(view.getSchema());

			changes.add(generateChangeSet(change));
		}
	}

	private void addMissingColumnChanges(List<ChangeSet> changes,
			Database database) {
		for (Column column : getMissingColumns()) {
			if (!shouldModifyColumn(column)) {
				continue;
			}

			AddColumnChange change = new AddColumnChange();
			change.setTableName(column.getTable().getName());
			change.setSchemaName(column.getTable().getSchema());

			ColumnConfig columnConfig = new ColumnConfig();
			columnConfig.setName(column.getName());

			String dataType = column.getDataTypeString(database);

			columnConfig.setType(dataType);

			Object defaultValue = column.getDefaultValue();
			if (defaultValue != null) {
				String defaultValueString = TypeConverterFactory.getInstance()
						.findTypeConverter(database).getDataType(defaultValue)
						.convertObjectToString(defaultValue, database);
				if (defaultValueString != null) {
					defaultValueString = defaultValueString.replaceFirst("'",
							"").replaceAll("'$", "");
				}
				columnConfig.setDefaultValue(defaultValueString);
			}

			if (column.getRemarks() != null) {
				columnConfig.setRemarks(column.getRemarks());
			}
			if (column.isNullable() != null && !column.isNullable()) {
				ConstraintsConfig constraintsConfig = columnConfig
						.getConstraints();
				if (constraintsConfig == null) {
					constraintsConfig = new ConstraintsConfig();
					columnConfig.setConstraints(constraintsConfig);
				}
				constraintsConfig.setNullable(false);
			}

			change.addColumn(columnConfig);

			changes.add(generateChangeSet(change));
		}
	}

	private void addMissingTableChanges(List<ChangeSet> changes,
			Database database) {
		for (Table missingTable : getMissingTables()) {
			if (referenceSnapshot.getDatabase().isLiquibaseTable(
					missingTable.getName())) {
				continue;
			}

			CreateTableChange change = new CreateTableChange();
			change.setTableName(missingTable.getName());
			change.setSchemaName(missingTable.getSchema());
			if (missingTable.getRemarks() != null) {
				change.setRemarks(missingTable.getRemarks());
			}

			for (Column column : missingTable.getColumns()) {
				ColumnConfig columnConfig = new ColumnConfig();
				columnConfig.setName(column.getName());
				columnConfig.setType(column.getDataTypeString(database));

				ConstraintsConfig constraintsConfig = null;
				if (column.isPrimaryKey()) {
					PrimaryKey primaryKey = null;
					for (PrimaryKey pk : getMissingPrimaryKeys()) {
						if (pk.getTable().getName().equalsIgnoreCase(missingTable.getName())) {
							primaryKey = pk;
						}
					}

					if (primaryKey == null || primaryKey.getColumnNamesAsList().size() == 1) {
						constraintsConfig = new ConstraintsConfig();
						constraintsConfig.setPrimaryKey(true);
						constraintsConfig.setPrimaryKeyTablespace(column.getTablespace());

						if (primaryKey != null) {
							constraintsConfig.setPrimaryKeyName(primaryKey.getName());
							getMissingPrimaryKeys().remove(primaryKey);
						}
					}
				}

				if (column.isAutoIncrement()) {
					columnConfig.setAutoIncrement(true);
				}

				if (column.isNullable() != null && !column.isNullable()) {
					if (constraintsConfig == null) {
						constraintsConfig = new ConstraintsConfig();
					}

					constraintsConfig.setNullable(false);
				}
				if (constraintsConfig != null) {
					columnConfig.setConstraints(constraintsConfig);
				}

				Object defaultValue = column.getDefaultValue();
				if (defaultValue == null) {
					// do nothing
				} else if (column.isAutoIncrement()) {
					// do nothing
				} else if (defaultValue instanceof Date) {
					columnConfig.setDefaultValueDate((Date) defaultValue);
				} else if (defaultValue instanceof Boolean) {
					columnConfig.setDefaultValueBoolean(((Boolean) defaultValue));
				} else if (defaultValue instanceof Number) {
					columnConfig.setDefaultValueNumeric(((Number) defaultValue));
				} else if (defaultValue instanceof DatabaseFunction) {
				    columnConfig.setDefaultValueComputed((DatabaseFunction) defaultValue);
				} else {
					columnConfig.setDefaultValue(defaultValue.toString());
				}

				if (column.getRemarks() != null) {
					columnConfig.setRemarks(column.getRemarks());
				}

				change.addColumn(columnConfig);
			}

			changes.add(generateChangeSet(change));
		}
	}

	private void addUnexpectedTableChanges(List<ChangeSet> changes) {
		for (Table unexpectedTable : getUnexpectedTables()) {
			DropTableChange change = new DropTableChange();
			change.setTableName(unexpectedTable.getName());
			change.setSchemaName(unexpectedTable.getSchema());

			changes.add(generateChangeSet(change));
		}
	}

	private void addInsertDataChanges(List<ChangeSet> changeSets, String dataDir)
			throws DatabaseException, IOException {
		try {
			String schema = referenceSnapshot.getSchema();
			for (Table table : referenceSnapshot.getTables()) {
				List<Change> changes = new ArrayList<Change>();
				List<Map> rs = ExecutorService.getInstance().getExecutor(
						referenceSnapshot.getDatabase()).queryForList(
						new RawSqlStatement("SELECT * FROM "
								+ referenceSnapshot.getDatabase()
										.escapeTableName(schema,
												table.getName())));

				if (rs.size() == 0) {
					continue;
				}

                List<String> columnNames = new ArrayList<String>();
                for (Column column : table.getColumns()) {
                    columnNames.add(column.getName());
                }

				// if dataDir is not null, print out a csv file and use loadData
				// tag
				if (dataDir != null) {
					String fileName = table.getName().toLowerCase() + ".csv";
					if (dataDir != null) {
						fileName = dataDir + "/" + fileName;
					}

					File parentDir = new File(dataDir);
					if (!parentDir.exists()) {
						parentDir.mkdirs();
					}
					if (!parentDir.isDirectory()) {
						throw new RuntimeException(parentDir
								+ " is not a directory");
					}

					CSVWriter outputFile = new CSVWriter(new FileWriter(
							fileName));
					String[] dataTypes = new String[columnNames.size()];
					String[] line = new String[columnNames.size()];
					for (int i = 0; i < columnNames.size(); i++) {
						line[i] = columnNames.get(i);
					}
					outputFile.writeNext(line);

					for (Map row : rs) {
						line = new String[columnNames.size()];

						for (int i = 0; i < columnNames.size(); i++) {
							Object value = row.get(columnNames.get(i).toUpperCase());
							if (dataTypes[i] == null && value != null) {
								if (value instanceof Number) {
									dataTypes[i] = "NUMERIC";
								} else if (value instanceof Boolean) {
									dataTypes[i] = "BOOLEAN";
								} else if (value instanceof Date) {
									dataTypes[i] = "DATE";
								} else {
									dataTypes[i] = "STRING";
								}
							}
							if (value == null) {
								line[i] = "NULL";
							} else {
                                if (value instanceof Date) {
                                    line[i] = new ISODateFormat().format(((Date) value));
                                } else {
                                    line[i] = value.toString();
                                }
                            }
						}
						outputFile.writeNext(line);
					}
					outputFile.flush();
					outputFile.close();

					LoadDataChange change = new LoadDataChange();
					change.setFile(fileName);
					change.setEncoding("UTF-8");
					change.setSchemaName(schema);
					change.setTableName(table.getName());

					for (int i = 0; i < columnNames.size(); i++) {
						String colName = columnNames.get(i);
						LoadDataColumnConfig columnConfig = new LoadDataColumnConfig();
						columnConfig.setHeader(colName);
						columnConfig.setName(colName);
						columnConfig.setType(dataTypes[i]);

						change.addColumn(columnConfig);
					}

					changes.add(change);
				} else { // if dataDir is null, build and use insert tags
					for (Map row : rs) {
						InsertDataChange change = new InsertDataChange();
						change.setSchemaName(schema);
						change.setTableName(table.getName());

						// loop over all columns for this row
						for (int i = 0; i < columnNames.size(); i++) {
							ColumnConfig column = new ColumnConfig();
							column.setName(columnNames.get(i));

							Object value = row.get(columnNames.get(i).toUpperCase());
							if (value == null) {
								column.setValue(null);
							} else if (value instanceof Number) {
								column.setValueNumeric((Number) value);
							} else if (value instanceof Boolean) {
								column.setValueBoolean((Boolean) value);
							} else if (value instanceof Date) {
								column.setValueDate((Date) value);
							} else { // string
								column.setValue(value.toString());
							}

							change.addColumn(column);

						}

						// for each row, add a new change
						// (there will be one group per table)
						changes.add(change);
					}

				}
				if (changes.size() > 0) {
					ChangeSet changeSet = generateChangeSet();
					for (Change change : changes) {
						changeSet.addChange(change);
					}
					changeSets.add(changeSet);
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
